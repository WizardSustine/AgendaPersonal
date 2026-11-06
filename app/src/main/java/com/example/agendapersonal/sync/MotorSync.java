package com.example.agendapersonal.sync;
import java.util.*;import java.util.function.BooleanSupplier;import java.io.InterruptedIOException;

/** Orquestación verificable sin Android ni conexiones reales. */
public final class MotorSync {
 public static final class Resultado {public int enviados,aplicados,conflictos,pendientes;public String texto(){return enviados+" enviados · "+aplicados+" recibidos · "+conflictos+" conflictos · "+pendientes+" pendientes";}}
 private final LocalSync local;private final TransporteSync remoto;private final String owner,device;private final BooleanSupplier cancelado;private final java.util.function.Consumer<Resultado> progreso;
 public MotorSync(LocalSync l,TransporteSync r,String owner,String device,BooleanSupplier cancelado){this(l,r,owner,device,cancelado,res->{});}
 public MotorSync(LocalSync l,TransporteSync r,String owner,String device,BooleanSupplier cancelado,java.util.function.Consumer<Resultado> progreso){local=l;remoto=r;this.owner=owner;this.device=device;this.cancelado=cancelado;this.progreso=progreso;}
 private void comprobar()throws InterruptedIOException{if(cancelado.getAsBoolean()||Thread.currentThread().isInterrupted())throw new InterruptedIOException("Sincronización pausada");}
 private void recibos()throws Exception{while(true){comprobar();List<String>ids=local.recibosPendientes(owner,device,100);if(ids.isEmpty())break;remoto.recibosVerificados(owner,device,ids);local.confirmarRecibos(owner,device,ids);}}
 private int aplicar()throws Exception{long antes=local.recibidosConfirmados(owner);int total=0;boolean avance;do{avance=false;for(CambioSync c:local.pendientes(owner)){comprobar();if(local.aplicar(c,owner,device)){total++;avance=true;}}}while(avance);return antes<0?total:Math.toIntExact(local.recibidosConfirmados(owner)-antes);}
 public Resultado ejecutar()throws Exception{
  Resultado resultado=new Resultado();comprobar();remoto.dispositivo(owner,device);recibos();
  while(true){comprobar();List<CambioSync>cambios=local.salientes(owner,100);if(cambios.isEmpty())break;remoto.subirVerificados(cambios);local.confirmarEnviados(owner,cambios);resultado.enviados+=cambios.size();progreso.accept(resultado);}
  resultado.aplicados+=aplicar();String fecha=null,id=null;
  while(true){comprobar();List<CambioSync>pagina=remoto.pagina(owner,device,fecha,id,100);if(pagina.isEmpty())break;for(CambioSync c:pagina)local.preparar(c,owner,device);resultado.aplicados+=aplicar();recibos();progreso.accept(resultado);CambioSync ultimo=pagina.get(pagina.size()-1);if(ultimo.fecha.equals(fecha)&&ultimo.id.equals(id))throw new IllegalStateException("La paginación no avanzó");fecha=ultimo.fecha;id=ultimo.id;if(pagina.size()<100)break;}
  resultado.aplicados+=aplicar();recibos();resultado.conflictos=local.cantidadConflictos(owner);resultado.pendientes=local.cantidadPendientes(owner)+local.cantidadSalientes(owner);local.limpiarConfirmados(owner);return resultado;
 }
}
