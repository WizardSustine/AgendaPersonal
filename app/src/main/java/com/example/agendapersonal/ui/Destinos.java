package com.example.agendapersonal.ui;
import android.app.Activity;import java.util.LinkedHashMap;import java.util.Map;import com.example.agendapersonal.MainActivity;
public final class Destinos {
 private Destinos(){}
 public static Map<String,Class<? extends Activity>> todos(){Map<String,Class<? extends Activity>> m=new LinkedHashMap<>();m.put("Inicio",MainActivity.class);m.put("Agenda",AgendaActivity.class);m.put("Tareas",TareasActivity.class);m.put("Eventos",EventosActivity.class);m.put("Estudios",EstudiosActivity.class);m.put("Sociales",SocialesActivity.class);m.put("Salud",SaludActivity.class);m.put("Trabajo",TrabajoActivity.class);m.put("Freelance",FreelanceActivity.class);m.put("Economía",EconomiaActivity.class);m.put("Ejercicio",EjercicioActivity.class);m.put("Estado emocional",EstadoEmocionalActivity.class);m.put("Investigaciones",InvestigacionesActivity.class);m.put("Personas",PersonasActivity.class);m.put("Frases personales",FrasesActivity.class);m.put("Ajustes",AjustesActivity.class);m.put("Ayuda",AyudaActivity.class);return m;}
 public static Class<? extends Activity> de(String nombre){return todos().get(nombre);}
}
