package com.example.agendapersonal;
import org.junit.Test;import static org.junit.Assert.*;import java.time.*;import com.example.agendapersonal.eventos.ReglaRecurrencia;import com.example.agendapersonal.eventos.ReglaRecurrencia.Unidad;
public class ReglaRecurrenciaTest {
 private ReglaRecurrencia r(String f,Unidad u,int n){LocalDate d=LocalDate.parse(f);return new ReglaRecurrencia(d,u,n,d.getDayOfMonth(),d.getMonthValue());}
 @Test public void mensualRecuperaDia31(){ReglaRecurrencia r=r("2026-01-31",Unidad.MESES,1);assertEquals("2026-02-28",r.fecha(1).toString());assertEquals("2026-03-31",r.fecha(2).toString());}
 @Test public void bisiestoRecupera29(){ReglaRecurrencia r=r("2024-02-29",Unidad.ANIOS,1);assertEquals("2025-02-28",r.fecha(1).toString());assertEquals("2028-02-29",r.fecha(4).toString());}
 @Test public void intervalosPersonalizados(){assertEquals("2026-11-11",r("2026-11-05",Unidad.DIAS,3).fecha(2).toString());assertEquals("2026-12-03",r("2026-11-05",Unidad.SEMANAS,2).fecha(2).toString());assertEquals("2027-05-05",r("2026-11-05",Unidad.MESES,3).fecha(2).toString());assertEquals("2030-11-05",r("2026-11-05",Unidad.ANIOS,2).fecha(2).toString());}
 @Test public void editarSerieConservaOrdinal(){for(Unidad u:Unidad.values()){ReglaRecurrencia r=ReglaRecurrencia.desdeOcurrencia(LocalDate.of(2028,2,29),4,u,2);assertEquals(LocalDate.of(2028,2,29),r.fecha(4));}}
 @Test public void aproximacionNoSaltaLaFecha(){for(Unidad u:Unidad.values()){ReglaRecurrencia r=r("2020-01-31",u,3);LocalDate f=r.fecha(300);long k=r.aproximar(f);assertTrue(k<=300);assertTrue(k>=297);assertFalse(r.fecha(k).isAfter(f));}}
 @Test(expected=IllegalArgumentException.class)public void rechazaCero(){r("2026-01-01",Unidad.DIAS,0);}
 @Test(expected=IllegalArgumentException.class)public void rechazaOrdinalNegativo(){r("2026-01-01",Unidad.DIAS,1).fecha(-1);}
}
