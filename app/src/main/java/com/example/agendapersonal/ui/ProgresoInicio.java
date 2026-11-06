package com.example.agendapersonal.ui;
import android.content.Context;import android.content.SharedPreferences;import com.example.agendapersonal.ui.registro.*;import com.example.agendapersonal.util.ReglasAgenda;
import java.time.LocalDate;import java.util.*;
/** Radar azul sobre tareas; rojo sobre actividades finalizadas, sin inferir estados por prioridad. */
public final class ProgresoInicio {
 public final float[]azul=new float[6];public final int[]rojo=new int[6];public int finalizadas,total;
 public final SortedMap<LocalDate,double[]>evolucion=new TreeMap<>();
 private ProgresoInicio(){}
 public static ProgresoInicio cargar(Context c,LocalDate desde,LocalDate hasta){ProgresoInicio resultado=new ProgresoInicio();String device=c.getSharedPreferences("agenda_identidad",Context.MODE_PRIVATE).getString("device_id","");String[]categorias={"ESTUDIOS","SOCIALES","SALUD","TRABAJO","EJERCICIO","FREELANCE"};String[]tablas={"estudios","sociales","salud","trabajo","ejercicios","freelance","eventos","tareas"};int[]totales=new int[6],pendientes=new int[6];
  for(int tabla=0;tabla<tablas.length;tabla++){Definicion d=Definicion.de(tablas[tabla]);try(Fuente f=Fuentes.abrir(c,device,d.tabla)){for(Registro r:f.todos()){String fecha=r.texto(d.fecha());if(fecha.isEmpty())continue;LocalDate dia;try{dia=LocalDate.parse(fecha.substring(0,10));}catch(RuntimeException e){continue;}if(dia.isBefore(desde)||dia.isAfter(hasta))continue;String categoria=tabla<6?categorias[tabla]:r.texto("categoria");int indice=Arrays.asList(categorias).indexOf(categoria);if(indice<0)continue;String estado=ReglasAgenda.estado(fecha,r.get("estadoEspecial"),LocalDate.now());
   if(d.tabla.equals("tareas")){totales[indice]++;if(Arrays.asList("MUCHO","PENDIENTE","URGENTE","CRITICO").contains(estado))pendientes[indice]++;}
   if(d.campo("estadoEspecial")!=null){if(!estado.equals("CANCELADO")&&!estado.equals("POSTERGADO"))resultado.total++;if(estado.equals("FINALIZADO")){resultado.rojo[indice]++;resultado.finalizadas++;resultado.evolucion.computeIfAbsent(dia.withDayOfMonth(1),k->new double[3])[0]++;}}
  }}}
  for(int i=0;i<6;i++)resultado.azul[i]=totales[i]==0?0:(totales[i]-pendientes[i])/(float)totales[i];double acumulado=0;for(double[]valores:resultado.evolucion.values()){acumulado+=valores[0];valores[0]=acumulado;}return resultado;
 }
}
