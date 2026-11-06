package com.example.agendapersonal.avisos;
import java.time.*;
/** La referencia de una tarea es su inicio, no su vencimiento. */
public final class HoraAviso {
 private HoraAviso(){}
 public static long calcular(String fecha,String hora,long anticipacion,ZoneId zona){if(anticipacion<0)throw new IllegalArgumentException("Anticipación negativa");return LocalDate.parse(fecha).atTime(hora==null||hora.isEmpty()?LocalTime.MIDNIGHT:LocalTime.parse(hora)).atZone(zona).minusMinutes(anticipacion).toInstant().toEpochMilli();}
}
