package com.example.agendapersonal;
import com.example.agendapersonal.avisos.HoraAviso;import org.junit.Test;import static org.junit.Assert.*;import java.time.*;
public class HoraAvisoTest {
 @Test public void sinHoraEsInicioDelDia(){assertEquals(Instant.parse("2026-11-05T03:00:00Z").toEpochMilli(),HoraAviso.calcular("2026-11-05","",0,ZoneId.of("America/Argentina/Buenos_Aires")));}
 @Test public void anticipacionCruzaDia(){assertEquals(Instant.parse("2026-11-04T23:45:00Z").toEpochMilli(),HoraAviso.calcular("2026-11-05","00:15",30,ZoneOffset.UTC));}
 @Test public void anticipacionSonMinutosRealesEnCambioDeHora(){assertEquals(Instant.parse("2026-03-08T06:00:00Z").toEpochMilli(),HoraAviso.calcular("2026-03-08","03:00",60,ZoneId.of("America/New_York")));}
 @Test public void mismaHoraEnOtraZonaSeRecalcula(){long a=HoraAviso.calcular("2026-11-05","10:00",0,ZoneOffset.UTC),b=HoraAviso.calcular("2026-11-05","10:00",0,ZoneOffset.ofHours(-3));assertEquals(10800000,b-a);}
 @Test(expected=IllegalArgumentException.class)public void noAceptaAnticipacionNegativa(){HoraAviso.calcular("2026-11-05","10:00",-1,ZoneOffset.UTC);}
}
