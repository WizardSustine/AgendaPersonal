package com.example.agendapersonal.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.job.*;
import android.content.ComponentName;
import android.os.Handler;
import android.os.Looper;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.io.InterruptedIOException;

/** Un único ejecutor serializa sesión, renovación y ciclos; nunca se bloquea la interfaz. */
public final class CoordinadorSync {
    private static CoordinadorSync instancia;
    private final Context context;
    private final SharedPreferences prefs;
    private final ScheduledThreadPoolExecutor worker=new ScheduledThreadPoolExecutor(1);
    private final Handler main=new Handler(Looper.getMainLooper());
    private final Set<Consumer<String>> observadores=new CopyOnWriteArraySet<>();
    private final List<Runnable> terminados=new ArrayList<>();
    private final AtomicInteger generacion=new AtomicInteger();
    private ScheduledFuture<?> reintento;private boolean ocupado;private int fallos;
    private static final int JOB=40501;
    private CoordinadorSync(Context c){context=c.getApplicationContext();prefs=context.getSharedPreferences("agenda_sync_estado",Context.MODE_PRIVATE);worker.setRemoveOnCancelPolicy(true);worker.execute(()->{actualizarResumen();informar(prefs.getString("mensaje","Supabase todavía no está conectado"));});}
    public static synchronized CoordinadorSync de(Context c){if(instancia==null)instancia=new CoordinadorSync(c);return instancia;}
    public void ejecutar(Runnable trabajo){worker.execute(trabajo);}
    public void observar(Consumer<String> o){observadores.add(o);main.post(()->o.accept(estado()));}
    public void dejarDeObservar(Consumer<String> o){observadores.remove(o);}
    public String estado(){String base=prefs.getString("mensaje","Supabase todavía no está conectado");String resumen=prefs.getString("resumen_confirmaciones","");String movimiento=prefs.getString("ultimo_movimiento","");return base+(resumen.isEmpty()?"":"\n\n"+resumen)+(movimiento.isEmpty()?"":"\n"+movimiento);}
    private void informar(String texto){prefs.edit().putString("mensaje",texto).apply();for(Consumer<String> o:observadores)main.post(()->{if(observadores.contains(o))o.accept(estado());});}
    private void actualizarResumen(){try(AlmacenSync local=new AlmacenSync(context)){String owner=local.cuenta();if(owner==null){prefs.edit().remove("resumen_confirmaciones").apply();return;}
        String texto="Cambios confirmados acumulados: "+local.enviadosConfirmados(owner)+" enviados · "+local.recibidosConfirmados(owner)+" incorporados de otros dispositivos\nAhora: "+local.cantidadSalientes(owner)+" por enviar · "+local.cantidadPendientes(owner)+" por aplicar · "+local.cantidadConflictos(owner)+" conflictos";
        prefs.edit().putString("resumen_confirmaciones",texto).apply();}catch(Exception e){android.util.Log.w("AgendaPersonal","Resumen de sincronización",e);}}
    private String hora(){return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"));}
    public boolean automatico(){return prefs.getBoolean("automatico",false);}
    private String device(){SharedPreferences p=context.getSharedPreferences("agenda_identidad",Context.MODE_PRIVATE);String id=p.getString("device_id",null);if(id==null){id=UUID.randomUUID().toString();p.edit().putString("device_id",id).commit();}return id;}
    public void activar(SesionSupabase s,Consumer<String> respuesta){ejecutar(()->{try{SharedPreferences identidad=context.getSharedPreferences("agenda_identidad",Context.MODE_PRIVATE);try(AlmacenSync a=new AlmacenSync(context)){a.vincular(s.url,s.owner,identidad.getString("owner_id",null));}new SesionSegura(context).guardar(s);identidad.edit().putString("owner_id",s.owner).commit();prefs.edit().putBoolean("automatico",true).putBoolean("requiere_login",false).commit();fallos=0;programar();informar("Cuenta conectada. Los cambios locales permanecen guardados.");main.post(()->respuesta.accept(null));solicitar(false,null);}catch(Exception e){main.post(()->respuesta.accept(mensaje(e)));}});}
    public void automatico(boolean activo){prefs.edit().putBoolean("automatico",activo).commit();if(activo){programar();solicitar(false,null);}else{cancelar();JobScheduler js=context.getSystemService(JobScheduler.class);if(js!=null)js.cancel(JOB);informar("Sincronización automática pausada. Podés usar Sincronizar ahora.");}}
    public void desconectar(){prefs.edit().putBoolean("automatico",false).commit();cancelar();JobScheduler js=context.getSystemService(JobScheduler.class);if(js!=null)js.cancel(JOB);ejecutar(()->{new SesionSegura(context).borrar();informar("Sesión desconectada. La base local y los cambios pendientes se conservan.");});}
    public void cancelar(){generacion.incrementAndGet();synchronized(this){if(reintento!=null)reintento.cancel(false);reintento=null;}}
    public synchronized void solicitar(boolean manual,Runnable terminado){
        if(!new SesionSegura(context).existe()||(!manual&&(!automatico()||prefs.getBoolean("requiere_login",false)))){if(terminado!=null)terminado.run();return;}
        if(terminado!=null)terminados.add(terminado);if(ocupado)return;if(reintento!=null)reintento.cancel(false);reintento=null;ocupado=true;int version=generacion.get();worker.execute(()->ciclo(version));
    }
    private void ciclo(int version){boolean reintentar=false;
        try{informar("Sincronizando con Supabase…");SesionSegura segura=new SesionSegura(context);SesionSupabase s=segura.leer();if(s==null)return;
            try(AlmacenSync local=new AlmacenSync(context)){local.comprobarCuenta(s.url,s.owner);if(version!=generacion.get())throw new InterruptedIOException();MotorSync.Resultado resultado=null;for(int intento=0;intento<2;intento++){if(s.porVencer()||intento>0){s=SupabaseRest.renovar(s);segura.guardar(s);}try{MotorSync motor=new MotorSync(local,new SupabaseRest(s.url,s.clave,s.accessToken),s.owner,device(),()->version!=generacion.get(),avance->{actualizarResumen();informar("Sincronizando… este ciclo: "+avance.enviados+" enviados · "+avance.aplicados+" incorporados");});resultado=motor.ejecutar();break;}catch(SupabaseRest.ApiException e){if(e.estado!=401||intento>0)throw e;}}if(resultado==null)throw new IllegalStateException("No se completó la sincronización");com.example.agendapersonal.avisos.ProgramadorAvisos.actualizar(context);fallos=0;prefs.edit().putLong("ultima_correcta",System.currentTimeMillis()).putBoolean("requiere_login",false).apply();if(resultado.enviados>0||resultado.aplicados>0)prefs.edit().putString("ultimo_movimiento","Última transferencia "+hora()+": "+resultado.enviados+" enviados · "+resultado.aplicados+" incorporados").apply();actualizarResumen();informar("Última revisión "+hora()+": "+(resultado.enviados==0&&resultado.aplicados==0?"sin cambios nuevos":resultado.enviados+" enviados · "+resultado.aplicados+" incorporados"));}
        }catch(InterruptedIOException e){informar("Sincronización pausada. Los cambios no confirmados siguen pendientes.");}
        catch(Exception e){fallos++;boolean login=e instanceof SupabaseRest.ApiException&&(((SupabaseRest.ApiException)e).estado==401);if(login)prefs.edit().putBoolean("requiere_login",true).apply();informar(mensaje(e)+" Los cambios locales se conservan.");reintentar=!login&&automatico();}
        finally{actualizarResumen();informar(prefs.getString("mensaje","Supabase todavía no está conectado"));List<Runnable> callbacks;synchronized(this){ocupado=false;callbacks=new ArrayList<>(terminados);terminados.clear();if(reintentar&&version==generacion.get()){long demora=ReglasSync.demoraReintento(fallos);reintento=worker.schedule(()->solicitar(false,null),demora,TimeUnit.MILLISECONDS);}}for(Runnable r:callbacks)r.run();}
    }
    public void programar(){if(!automatico()||!new SesionSegura(context).existe())return;JobScheduler js=context.getSystemService(JobScheduler.class);if(js==null)return;if(js.getPendingJob(JOB)==null)js.schedule(new JobInfo.Builder(JOB,new ComponentName(context,TrabajoSync.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setPeriodic(15*60*1000L).setBackoffCriteria(30000L,JobInfo.BACKOFF_POLICY_EXPONENTIAL).build());}
    public static String mensaje(Exception e){if(e instanceof SupabaseRest.ApiException)return e.getMessage();if(e instanceof IllegalArgumentException||e instanceof IllegalStateException)return e.getMessage()==null?"No se pudo completar la sincronización.":e.getMessage();if(e instanceof java.net.UnknownHostException||e instanceof java.net.ConnectException||e instanceof java.net.SocketTimeoutException)return "No se pudo conectar. Se reintentará cuando haya conexión.";return "No se pudo completar la sincronización. Revisá la conexión o iniciá sesión nuevamente.";}
}
