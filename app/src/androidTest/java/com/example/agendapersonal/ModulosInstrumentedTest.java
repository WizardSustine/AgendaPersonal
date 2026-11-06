package com.example.agendapersonal;
import android.content.Context;import android.database.Cursor;import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.agendapersonal.data.DatabaseHelper;import com.example.agendapersonal.ui.Postergaciones;import com.example.agendapersonal.ui.registro.*;
import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;
import java.util.*;

/** Cada prueba usa una base con prefijo exclusivo; nunca abre agenda_personal.db del usuario. */
@RunWith(AndroidJUnit4.class)
public class ModulosInstrumentedTest {
 private Context contexto;private final String dispositivo=UUID.randomUUID().toString(),owner=UUID.randomUUID().toString();
 @Before public void preparar(){contexto=new ContextoPrueba(InstrumentationRegistry.getInstrumentation().getTargetContext(),"test_modulos_"+UUID.randomUUID()+"_");}
 @After public void limpiar(){contexto.deleteDatabase("agenda_personal.db");}
 private Registro insertar(String tabla,String...valores){Registro r=new Registro();r.put("ownerId",owner);for(int i=0;i<valores.length;i+=2)r.put(valores[i],valores[i+1]);try(Fuente f=Fuentes.abrir(contexto,dispositivo,tabla)){assertTrue(f.guardar(r,true));assertFalse(r.texto("id").isEmpty());}return r;}
 private int cambios(){try(DatabaseHelper h=new DatabaseHelper(contexto);Cursor c=h.getReadableDatabase().rawQuery("SELECT count(*) FROM sync_changes",null)){c.moveToFirst();return c.getInt(0);}}
 @Test public void materiaEInstitucionConservanSuRelacion(){Registro p=insertar("personas","nombre","Institución de prueba");Registro m=insertar("materias","nombre","Materia de prueba","institucionId",p.texto("id"));Registro e=insertar("estudios","actividad","Preparar entrega","fechaEntrega","2026-12-15","institucionId",p.texto("id"),"materiaId",m.texto("id"),"prioridad","MEDIA");try(Fuente f=Fuentes.abrir(contexto,dispositivo,"estudios")){assertEquals(m.texto("id"),f.todos().get(0).texto("materiaId"));}assertEquals(3,cambios());}
 @Test public void postergacionGuardaFechaOriginalYDosCambios(){Registro r=insertar("tareas","descripcion","Prueba","categoria","OTRA","prioridad","ALTA","fechaVencimiento","2026-11-05","alarmaActivada","false");Postergaciones.guardar(contexto,dispositivo,"tareas",r.texto("id"),"fechaVencimiento","2026-11-12");try(Fuente f=Fuentes.abrir(contexto,dispositivo,"tareas")){assertEquals("2026-11-12",f.todos().get(0).texto("fechaVencimiento"));assertEquals("POSTERGADO",f.todos().get(0).texto("estadoEspecial"));}try(Fuente f=Fuentes.abrir(contexto,dispositivo,"historial_postergaciones")){assertEquals("2026-11-05",f.todos().get(0).texto("fechaOriginal"));}assertEquals(3,cambios());}
 @Test public void falloDeFkNoDejaRegistroNiCambio(){boolean fallo=false;try{insertar("materias","nombre","Inválida","institucionId",UUID.randomUUID().toString());}catch(RuntimeException e){fallo=true;}assertTrue(fallo);assertEquals(0,cambios());try(Fuente f=Fuentes.abrir(contexto,dispositivo,"materias")){assertTrue(f.todos().isEmpty());}}
 @Test public void importesCeroYNulosDeFreelanceNoSeConfunden(){Registro p=insertar("personas","nombre","Cliente de prueba");insertar("freelance","actividad","Sin valor acordado","clienteId",p.texto("id"),"fechaInicio","2026-11-05");insertar("pagos","descripcion","Pago de importe cero","importe","0");try(Fuente f=Fuentes.abrir(contexto,dispositivo,"freelance")){assertEquals("",f.todos().get(0).texto("valor"));}try(Fuente f=Fuentes.abrir(contexto,dispositivo,"pagos")){assertEquals(0,Double.compare(0,Double.parseDouble(f.todos().get(0).texto("importe"))));}}
}
