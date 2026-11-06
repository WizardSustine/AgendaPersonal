package com.example.agendapersonal.ui;
import android.content.Context;import com.example.agendapersonal.ui.registro.*;import com.example.agendapersonal.eventos.SeriesEvento;import java.time.*;
public final class ProgresoDia {
 private ProgresoDia(){}
 public static ResumenDia cargar(Context c,LocalDateTime ahora){
  ResumenDia resumen=new ResumenDia();String device=c.getSharedPreferences("agenda_identidad",0).getString("device_id","");
  for(String tabla:new String[]{"estudios","sociales","salud","trabajo","freelance","eventos","tareas"}){
   Definicion d=Definicion.de(tabla);try(Fuente f=Fuentes.abrir(c,device,tabla)){for(Registro r:f.todos())agregar(resumen,d,r,ahora);}
  }
  Definicion eventos=Definicion.de("eventos");for(Registro r:SeriesEvento.virtuales(c,ahora.toLocalDate(),ahora.toLocalDate()))agregar(resumen,eventos,r,ahora);
  return resumen;
 }
 private static void agregar(ResumenDia resumen,Definicion d,Registro r,LocalDateTime ahora){
  if(!r.texto("deletedAt").isEmpty())return;
  String hora=d.tabla.equals("tareas")?r.texto("horaVencimiento"):d.tabla.equals("freelance")?r.texto("horaInicio"):r.texto("hora");
  resumen.agregar(r.texto(d.fecha()),hora,r.get("estadoEspecial"),ahora);
 }
}
