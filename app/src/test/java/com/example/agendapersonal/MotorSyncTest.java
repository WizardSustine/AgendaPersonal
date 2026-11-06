package com.example.agendapersonal;
import org.junit.Test;import static org.junit.Assert.*;import com.example.agendapersonal.sync.*;import java.util.*;import java.io.*;

/** Prueba el motor de producción con red y almacenamiento controlados. No utiliza datos del usuario. */
public class MotorSyncTest {
 static final String OWNER="00000000-0000-4000-8000-000000000001",DEVICE="00000000-0000-4000-8000-000000000002";
 static CambioSync cambio(int n){return new CambioSync(String.format(Locale.ROOT,"00000000-0000-4000-8000-%012d",n),"2026-11-05T10:00:00Z","documento de prueba "+n);}
 static class Local implements LocalSync {
  final LinkedHashMap<String,CambioSync>outbox=new LinkedHashMap<>(),inbox=new LinkedHashMap<>();final Set<String>sent=new HashSet<>(),applied=new HashSet<>(),acked=new HashSet<>();final Map<String,String>requires=new HashMap<>();int efectos;boolean falloLocal;
  public List<CambioSync>salientes(String o,int n){List<CambioSync>r=new ArrayList<>();for(CambioSync c:outbox.values())if(!sent.contains(c.id)&&r.size()<n)r.add(c);return r;}
  public void confirmarEnviados(String o,List<CambioSync>c){for(CambioSync a:c)sent.add(a.id);}
  public void preparar(CambioSync c,String o,String d){if(!applied.contains(c.id))inbox.putIfAbsent(c.id,c);}
  public boolean aplicar(CambioSync c,String o,String d)throws IOException{if(falloLocal)throw new IOException("Cierre antes de aplicar");if(requires.containsKey(c.id)&&!applied.contains(requires.get(c.id)))return false;if(applied.add(c.id))efectos++;inbox.remove(c.id);return true;}
  public List<CambioSync>pendientes(String o){return new ArrayList<>(inbox.values());}
  public List<String>recibosPendientes(String o,String d,int n){List<String>r=new ArrayList<>();for(String id:applied)if(!acked.contains(id)&&r.size()<n)r.add(id);return r;}
  public void confirmarRecibos(String o,String d,List<String>ids){acked.addAll(ids);}
  public int cantidadConflictos(String o){return 0;}public int cantidadPendientes(String o){return inbox.size();}
 }
 static class Red implements TransporteSync {
  final Local local;final LinkedHashMap<String,CambioSync>persistidos=new LinkedHashMap<>();final Set<String>receipts=new HashSet<>();final List<CambioSync>entrantes=new ArrayList<>();boolean perderRespuestaEnvio,perderRespuestaRecibo;int paginas,registroDispositivo;
  Red(Local l){local=l;}
  public void dispositivo(String o,String d){registroDispositivo++;}
  public void subirVerificados(List<CambioSync>c)throws IOException{for(CambioSync a:c)persistidos.putIfAbsent(a.id,a);if(perderRespuestaEnvio)throw new IOException("Persistió, pero se perdió la respuesta");}
  public List<CambioSync>pagina(String o,String d,String fecha,String id,int n){paginas++;int desde=0;if(id!=null){for(int i=0;i<entrantes.size();i++)if(entrantes.get(i).id.equals(id)){desde=i+1;break;}}return new ArrayList<>(entrantes.subList(desde,Math.min(entrantes.size(),desde+n)));}
  public void recibosVerificados(String o,String d,List<String>ids)throws IOException{for(String id:ids){assertTrue("No confirmar antes de aplicar",local.applied.contains(id));receipts.add(id);}if(perderRespuestaRecibo)throw new IOException("Se perdió la confirmación del recibo");}
 }
 MotorSync motor(Local l,Red r){return new MotorSync(l,r,OWNER,DEVICE,()->false);}
 @Test public void envioConfirmadoMarcaLocal()throws Exception{Local l=new Local();l.outbox.put(cambio(1).id,cambio(1));Red r=new Red(l);assertEquals(1,motor(l,r).ejecutar().enviados);assertEquals(1,l.sent.size());assertEquals(1,r.persistidos.size());}
 @Test public void respuestaPerdidaNoMarcaYReintentoNoDuplica()throws Exception{Local l=new Local();l.outbox.put(cambio(1).id,cambio(1));Red r=new Red(l);r.perderRespuestaEnvio=true;try{motor(l,r).ejecutar();fail();}catch(IOException esperado){}assertTrue(l.sent.isEmpty());assertEquals(1,r.persistidos.size());r.perderRespuestaEnvio=false;motor(l,r).ejecutar();assertEquals(1,r.persistidos.size());assertEquals(1,l.sent.size());}
 @Test public void recepcionSeAplicaAntesDeConfirmar()throws Exception{Local l=new Local();Red r=new Red(l);r.entrantes.add(cambio(2));motor(l,r).ejecutar();assertEquals(1,l.efectos);assertTrue(r.receipts.contains(cambio(2).id));assertTrue(l.acked.contains(cambio(2).id));}
 @Test public void corteAntesDeAplicarNoConfirma()throws Exception{Local l=new Local();Red r=new Red(l);r.entrantes.add(cambio(3));l.falloLocal=true;try{motor(l,r).ejecutar();fail();}catch(IOException esperado){}assertTrue(r.receipts.isEmpty());assertTrue(l.applied.isEmpty());l.falloLocal=false;motor(l,r).ejecutar();assertEquals(1,l.efectos);}
 @Test public void corteDespuesDeAplicarNoDuplicaAlReanudar()throws Exception{Local l=new Local();Red r=new Red(l);r.entrantes.add(cambio(4));r.perderRespuestaRecibo=true;try{motor(l,r).ejecutar();fail();}catch(IOException esperado){}assertEquals(1,l.efectos);assertTrue(l.acked.isEmpty());r.perderRespuestaRecibo=false;motor(l,r).ejecutar();assertEquals(1,l.efectos);assertEquals(1,l.acked.size());}
 @Test public void paginaTodosAunqueCoincidanFechas()throws Exception{Local l=new Local();Red r=new Red(l);for(int i=10;i<215;i++)r.entrantes.add(cambio(i));motor(l,r).ejecutar();assertEquals(205,l.efectos);assertEquals(3,r.paginas);assertEquals(205,r.receipts.size());}
 @Test public void dependenciaEnOtraPaginaSeReintenta()throws Exception{Local l=new Local();Red r=new Red(l);for(int i=100;i<201;i++)r.entrantes.add(cambio(i));l.requires.put(cambio(100).id,cambio(200).id);motor(l,r).ejecutar();assertEquals(101,l.efectos);assertTrue(l.inbox.isEmpty());assertEquals(101,r.receipts.size());}
 @Test public void recepcionRepetidaNoDuplica()throws Exception{Local l=new Local();Red r=new Red(l);r.entrantes.add(cambio(5));motor(l,r).ejecutar();motor(l,r).ejecutar();assertEquals(1,l.efectos);}
 @Test public void salidaSeProcesaEnLotes()throws Exception{Local l=new Local();for(int i=1;i<=215;i++)l.outbox.put(cambio(i).id,cambio(i));Red r=new Red(l);MotorSync.Resultado resultado=motor(l,r).ejecutar();assertEquals(215,resultado.enviados);assertEquals(215,l.sent.size());}
 @Test public void cancelacionNoHaceSolicitudes()throws Exception{Local l=new Local();Red r=new Red(l);try{new MotorSync(l,r,OWNER,DEVICE,()->true).ejecutar();fail();}catch(InterruptedIOException esperado){}assertEquals(0,r.registroDispositivo);assertTrue(r.persistidos.isEmpty());}
}
