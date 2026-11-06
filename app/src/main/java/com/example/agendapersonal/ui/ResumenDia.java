package com.example.agendapersonal.ui;
import java.time.*;
/** Sólo la fecha funcional del día, nunca created_at ni updated_at. */
public final class ResumenDia {
 public int total,finalizadas,pendientes,vencidas;
 public void agregar(String fecha,String hora,String estado,LocalDateTime ahora){
  if(fecha==null||fecha.isEmpty()||"CANCELADO".equals(estado)||"POSTERGADO".equals(estado))return;
  try{if(!LocalDate.parse(fecha.substring(0,10)).equals(ahora.toLocalDate()))return;}catch(RuntimeException e){return;}
  total++;if("FINALIZADO".equals(estado)){finalizadas++;return;}pendientes++;
  if(hora!=null&&!hora.isEmpty())try{if(LocalTime.parse(hora).isBefore(ahora.toLocalTime()))vencidas++;}catch(RuntimeException ignored){}
 }
 public int porcentaje(){return total==0?0:Math.min(100,Math.round(100f*finalizadas/total));}
 public String titulo(){return "Hoy: "+finalizadas+" de "+total+" actividades finalizadas";}
 public String detalle(){return total==0?"Sin actividades para hoy":pendientes==0?"Todo lo de hoy está finalizado":pendientes+" pendientes para hoy"+(vencidas==0?"":" · "+vencidas+" con horario vencido");}
}
