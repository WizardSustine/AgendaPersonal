package com.example.agendapersonal;
import org.junit.Test;import static org.junit.Assert.*;import java.time.LocalDate;import com.example.agendapersonal.util.ReglasAgenda;
public class ReglasAgendaTest {
 @Test public void calculaLosLimitesAcordados(){LocalDate hoy=LocalDate.of(2026,11,5);int[]dias={-1,0,1,7,8,30,31};String[]estados={"ATRASADO","CRITICO","URGENTE","URGENTE","PENDIENTE","PENDIENTE","MUCHO"};for(int i=0;i<dias.length;i++)assertEquals(estados[i],ReglasAgenda.estado(hoy.plusDays(dias[i]).toString(),null,hoy));}
 @Test public void especialesPrevalecenSobreFecha(){for(String s:new String[]{"FINALIZADO","CANCELADO","POSTERGADO"})assertEquals(s,ReglasAgenda.estado("2000-01-01",s,LocalDate.now()));}
 @Test public void prioridadNoEsEstado(){assertEquals(0,ReglasAgenda.prioridad("ALTISIMA"));assertEquals(1,ReglasAgenda.prioridad("ALTA"));assertEquals(2,ReglasAgenda.prioridad("MEDIA"));assertEquals(4,ReglasAgenda.prioridad("BAJISIMA"));}
 @Test public void validaFechasYNumerosSinAceptarInfinito(){assertNull(ReglasAgenda.validarValor("fecha","2024-02-29"));assertNotNull(ReglasAgenda.validarValor("fecha","2026-02-29"));assertNotNull(ReglasAgenda.validarValor("decimal","1e9999"));assertNotNull(ReglasAgenda.validarValor("entero","-1"));assertNull(ReglasAgenda.validarValor("decimal","0"));}
}
