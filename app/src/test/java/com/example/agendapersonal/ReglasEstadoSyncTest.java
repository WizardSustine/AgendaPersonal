package com.example.agendapersonal;
import org.junit.Test;import static org.junit.Assert.*;import com.example.agendapersonal.sync.ReglasEstadoSync;
public class ReglasEstadoSyncTest {
 @Test public void baseLimpiaPermiteSaltarRevisionesPodadas(){assertTrue(ReglasEstadoSync.puedeReemplazar(true,true,2,50));}
 @Test public void sinBaseNoPisaDatosLegados(){assertFalse(ReglasEstadoSync.puedeReemplazar(false,true,2,50));}
 @Test public void cambioLocalRequiereConflicto(){assertFalse(ReglasEstadoSync.puedeReemplazar(true,false,2,50));}
 @Test public void noRetrocedeNiAceptaRamasIguales(){assertFalse(ReglasEstadoSync.puedeReemplazar(true,true,5,2));assertFalse(ReglasEstadoSync.puedeReemplazar(true,true,5,5));}
 @Test(expected=IllegalArgumentException.class) public void revisionInvalida(){ReglasEstadoSync.puedeReemplazar(true,true,0,3);}
}
