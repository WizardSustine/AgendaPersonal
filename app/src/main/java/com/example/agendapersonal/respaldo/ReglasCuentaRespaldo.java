package com.example.agendapersonal.respaldo;
import java.util.Objects;
/** Evita mezclar cuentas y permite recuperar el estado local previo a la vinculación. */
public final class ReglasCuentaRespaldo {
 private ReglasCuentaRespaldo(){}
 public static boolean compatible(String actual,String proyectoActual,String fuente,String proyectoFuente,String ownerFuente,String ownerLocalAnterior,String ownerSeguridad){if(actual==null)return true;if(fuente!=null)return actual.equals(fuente)&&Objects.equals(proyectoActual,proyectoFuente);return ownerFuente!=null&&(ownerFuente.equals(actual)||ownerFuente.equals(ownerLocalAnterior)||ownerFuente.equals(ownerSeguridad));}
}
