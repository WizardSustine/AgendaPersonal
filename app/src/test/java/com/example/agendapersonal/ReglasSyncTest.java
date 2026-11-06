package com.example.agendapersonal;
import org.junit.Test;import static org.junit.Assert.*;import com.example.agendapersonal.sync.ReglasSync;
public class ReglasSyncTest {
 @Test public void primeraRevisionSePuedeInsertar(){assertEquals(ReglasSync.Decision.APLICAR,ReglasSync.decidir(null,0,1,false));}
 @Test public void revisionAnteriorAusenteSeEspera(){assertEquals(ReglasSync.Decision.ESPERAR_ANTERIOR,ReglasSync.decidir(null,3,4,false));}
 @Test public void baseCoincidenteSeAplica(){assertEquals(ReglasSync.Decision.APLICAR,ReglasSync.decidir(3,3,4,false));}
 @Test public void ramasDistintasNoSeSobrescriben(){assertEquals(ReglasSync.Decision.CONFLICTO,ReglasSync.decidir(3,1,2,false));}
 @Test public void uuidConocidoNoDuplicaEfecto(){assertEquals(ReglasSync.Decision.YA_APLICADO,ReglasSync.decidir(8,0,1,true));}
 @Test(expected=IllegalArgumentException.class)public void rechazaSaltosDeRevision(){ReglasSync.decidir(1,1,3,false);}
 @Test(expected=IllegalArgumentException.class)public void rechazaBaseNegativa(){ReglasSync.decidir(null,-1,1,false);}
 @Test public void resolucionAvanzaAmbasVersiones(){assertEquals(6,ReglasSync.revisionResolucion(3,5));}
 @Test(expected=IllegalArgumentException.class)public void evitaOverflowDeRevision(){ReglasSync.revisionResolucion(Integer.MAX_VALUE,1);}
 @Test public void reintentosTienenEsperaCrecienteYLimite(){assertEquals(10000L,ReglasSync.demoraReintento(1));assertEquals(20000L,ReglasSync.demoraReintento(2));assertEquals(600000L,ReglasSync.demoraReintento(20));}
 @Test public void urlDebeSerHttpsSinCredenciales(){assertEquals("https://ejemplo.supabase.co",ReglasSync.urlProyecto("https://EJEMPLO.supabase.co/"));for(String u:new String[]{"http://ejemplo.supabase.co","https://user:pass@ejemplo.supabase.co","https://ejemplo.supabase.co/rest/v1","https://ejemplo.supabase.co?x=y","https://ejemplo.supabase.co#token"}){try{ReglasSync.urlProyecto(u);fail(u);}catch(IllegalArgumentException esperado){}}}
 @Test(expected=IllegalArgumentException.class)public void uuidDebeSerCanonico(){ReglasSync.uuid("1-1-1-1-1");}
}
