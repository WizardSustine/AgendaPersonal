package com.example.agendapersonal.respaldo;
import org.junit.Test;import static org.junit.Assert.*;
public class ReglasCuentaRespaldoTest {
 @Test public void instalacionNuevaPuedeRecuperarSuCuenta(){assertTrue(ReglasCuentaRespaldo.compatible(null,null,"cuenta","proyecto","local",null,null));}
 @Test public void mismaCuentaYMismoProyectoSonCompatibles(){assertTrue(ReglasCuentaRespaldo.compatible("cuenta","proyecto","cuenta","proyecto","local",null,null));}
 @Test public void otraCuentaUOtroProyectoSonRechazados(){assertFalse(ReglasCuentaRespaldo.compatible("cuenta","proyecto","otra","proyecto","local",null,null));assertFalse(ReglasCuentaRespaldo.compatible("cuenta","proyecto","cuenta","otro","local",null,null));}
 @Test public void recuperaCopiaAnteriorALaPrimeraVinculacion(){assertTrue(ReglasCuentaRespaldo.compatible("cuenta","proyecto",null,null,"local-conocido","local-conocido",null));}
 @Test public void puedeVolverAlEstadoAnteriorGuardadoAntesDeRestaurar(){assertTrue(ReglasCuentaRespaldo.compatible("cuenta","proyecto",null,null,"origen-anterior","otro-origen","origen-anterior"));}
 @Test public void copiaSinCuentaDeOtroOrigenEsRechazada(){assertFalse(ReglasCuentaRespaldo.compatible("cuenta","proyecto",null,null,"desconocido","local-conocido","anterior"));}
}
