package com.example.agendapersonal;
import com.example.agendapersonal.ui.ResumenDia;import org.junit.Test;import static org.junit.Assert.*;import java.time.*;
public class ResumenDiaTest {
 final LocalDateTime ahora=LocalDateTime.of(2026,11,5,12,0);
 @Test public void seiscientasDosHistoricasNoSonElProgresoDeHoy(){ResumenDia r=new ResumenDia();for(int i=0;i<602;i++)r.agregar("2020-01-01","10:00","FINALIZADO",ahora);r.agregar("2026-11-05","13:00",null,ahora);r.agregar("2026-11-05","11:00","FINALIZADO",ahora);assertEquals(2,r.total);assertEquals(1,r.finalizadas);assertEquals(50,r.porcentaje());assertEquals("Hoy: 1 de 2 actividades finalizadas",r.titulo());}
 @Test public void excluyeCanceladasPostergadasYFechasFuturas(){ResumenDia r=new ResumenDia();r.agregar("2026-11-05","10:00","CANCELADO",ahora);r.agregar("2026-11-05","10:00","POSTERGADO",ahora);r.agregar("2026-11-06","10:00",null,ahora);assertEquals(0,r.total);assertEquals("Sin actividades para hoy",r.detalle());}
 @Test public void preocupacionSoloPorHorarioDeHoy(){ResumenDia r=new ResumenDia();r.agregar("2026-11-04","10:00",null,ahora);r.agregar("2026-11-05","10:00",null,ahora);r.agregar("2026-11-05","13:00",null,ahora);r.agregar("2026-11-05",null,null,ahora);assertEquals(3,r.pendientes);assertEquals(1,r.vencidas);}
 @Test public void finalizadaNoEstaVencida(){ResumenDia r=new ResumenDia();r.agregar("2026-11-05","10:00","FINALIZADO",ahora);assertEquals(100,r.porcentaje());assertEquals(0,r.vencidas);assertEquals("Todo lo de hoy está finalizado",r.detalle());}
 @Test public void cambiaAlNuevoDiaSinArrastrarAyer(){ResumenDia r=new ResumenDia();r.agregar("2026-11-05","10:00",null,ahora.plusDays(1));assertEquals(0,r.total);}
 @Test public void fechasInvalidasOSinFechaNoUsanOtraFechaComoReemplazo(){ResumenDia r=new ResumenDia();for(String f:new String[]{null,"","2026","dato inválido"})r.agregar(f,null,null,ahora);assertEquals(0,r.total);assertEquals(0,r.porcentaje());}
}
