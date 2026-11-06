package com.example.agendapersonal.eventos;
import android.content.Context;import android.database.Cursor;
import androidx.test.ext.junit.runners.AndroidJUnit4;import androidx.test.platform.app.InstrumentationRegistry;
import com.example.agendapersonal.ContextoPrueba;import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.ui.registro.Registro;
import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;
import java.time.LocalDate;import java.util.*;

/** Exactamente dos casos funcionales. SQLite real aislado, sin sincronización ni alarmas del dispositivo. */
@RunWith(AndroidJUnit4.class)
public class RecurrenciaDosCasosInstrumentedTest {
 Context c;
 @Before public void crear(){c=new ContextoPrueba(InstrumentationRegistry.getInstrumentation().getTargetContext(),"test_dos_recurrencias_"+UUID.randomUUID()+"_");}
 @After public void cerrar(){c.deleteDatabase("agenda_personal.db");}
 Registro nuevo(String fecha){Registro r=new Registro();r.put("descripcion","Recurrencia de prueba");r.put("categoria","ESTUDIOS");r.put("fecha",fecha);r.put("hora","10:30");r.put("alarmaActivada","false");r.put("anticipacionMinutos",null);return r;}
 void crearSerie(Registro r,String frecuencia,ReglaRecurrencia.Unidad unidad,int cada,String fin){SeriesEvento.guardarDatos(c,r,new Registro(),SeriesEvento.TODAS,frecuencia,unidad,cada,fin);}
 Registro inicial(Registro r){Registro p=SeriesEvento.cargar(c,r.texto("id"));assertNotNull(p);assertFalse(p.texto("serieId").isEmpty());return p;}
 int cantidad(String tabla){try(DatabaseHelper h=new DatabaseHelper(c);Cursor q=h.getReadableDatabase().rawQuery("SELECT count(*) FROM "+tabla,null)){assertTrue(q.moveToFirst());return q.getInt(0);}}
 List<String> fechas(String serie,int n){List<String> r=new ArrayList<>();for(int i=0;i<n;i++){Registro e=SeriesEvento.ocurrencia(c,serie,i);if(e!=null)r.add(e.texto("fecha"));}return r;}
 @Test public void caso1SemanalConExcepcionIndividualSinModificarLasDemas(){
  Registro r=nuevo("2026-11-05");crearSerie(r,"SEMANAL",ReglaRecurrencia.Unidad.SEMANAS,1,null);Registro primero=inicial(r);String serie=primero.texto("serieId");
  assertEquals(Arrays.asList("2026-11-05","2026-11-12","2026-11-19","2026-11-26","2026-12-03"),fechas(serie,5));
  assertEquals(1,cantidad("series_eventos"));assertEquals(1,cantidad("eventos"));assertEquals(2,cantidad("sync_changes"));
  List<Registro> antes=SeriesEvento.virtuales(c,LocalDate.parse("2026-11-05"),LocalDate.parse("2026-12-03"));assertEquals(4,antes.size());
  Registro segunda=SeriesEvento.ocurrencia(c,serie,1),cambio=segunda.copia();String id=segunda.texto("id");assertEquals(id,SeriesEvento.ocurrencia(c,serie,1).texto("id"));
  cambio.put("estadoEspecial","CANCELADO");SeriesEvento.guardarDatos(c,cambio,segunda,SeriesEvento.SOLO,"SEMANAL",ReglaRecurrencia.Unidad.SEMANAS,1,null);
  assertEquals("CANCELADO",SeriesEvento.cargar(c,id).texto("estadoEspecial"));assertEquals("",SeriesEvento.ocurrencia(c,serie,0).texto("estadoEspecial"));assertEquals("",SeriesEvento.ocurrencia(c,serie,2).texto("estadoEspecial"));
  assertEquals(Arrays.asList("2026-11-05","2026-11-12","2026-11-19","2026-11-26","2026-12-03"),fechas(serie,5));
  assertEquals(2,cantidad("eventos"));assertEquals(3,cantidad("sync_changes"));assertEquals(3,SeriesEvento.virtuales(c,LocalDate.parse("2026-11-05"),LocalDate.parse("2026-12-03")).size());
 }
 @Test public void caso2PersonalizadaCadaDosMesesConFinInclusivoYCambioSoloDeUnaFecha(){
  Registro r=nuevo("2026-12-31");crearSerie(r,"PERSONALIZADA",ReglaRecurrencia.Unidad.MESES,2,"2027-08-31");Registro primero=inicial(r);String serie=primero.texto("serieId");
  assertEquals(Arrays.asList("2026-12-31","2027-02-28","2027-04-30","2027-06-30","2027-08-31"),fechas(serie,6));assertNull(SeriesEvento.ocurrencia(c,serie,5));
  assertEquals(4,SeriesEvento.virtuales(c,LocalDate.parse("2026-12-01"),LocalDate.parse("2028-01-01")).size());assertEquals(1,cantidad("eventos"));
  Registro febrero=SeriesEvento.ocurrencia(c,serie,1),postergada=febrero.copia();postergada.put("fecha","2027-03-02");postergada.put("estadoEspecial","POSTERGADO");
  SeriesEvento.guardarDatos(c,postergada,febrero,SeriesEvento.SOLO,"PERSONALIZADA",ReglaRecurrencia.Unidad.MESES,2,"2027-08-31");
  assertEquals(Arrays.asList("2026-12-31","2027-03-02","2027-04-30","2027-06-30","2027-08-31"),fechas(serie,6));assertNull(SeriesEvento.ocurrencia(c,serie,5));
  Registro guardada=SeriesEvento.cargar(c,febrero.texto("id"));assertEquals("POSTERGADO",guardada.texto("estadoEspecial"));assertEquals("2027-02-28",guardada.texto("_fechaOrigen"));
  assertEquals("",SeriesEvento.ocurrencia(c,serie,2).texto("estadoEspecial"));assertEquals(3,SeriesEvento.virtuales(c,LocalDate.parse("2026-12-01"),LocalDate.parse("2028-01-01")).size());assertEquals(2,cantidad("eventos"));
 }
}
