package com.example.agendapersonal.sync;
import java.time.Instant;
/** Sobre inmutable. El documento JSON se valida en los adaptadores de red y almacenamiento. */
public final class CambioSync {
 public final String id,fecha,documento;
 public CambioSync(String id,String fecha,String documento){this.id=ReglasSync.uuid(id);Instant.parse(fecha);this.fecha=fecha;if(documento==null||documento.isEmpty())throw new IllegalArgumentException("Cambio sin documento");this.documento=documento;}
}
