package com.example.agendapersonal.sync;
/** Un snapshot salta revisiones antiguas sólo cuando la copia local sigue igual a su base remota. */
public final class ReglasEstadoSync {
 private ReglasEstadoSync(){}
 public static boolean puedeReemplazar(boolean baseConocida,boolean localSinCambios,int revisionLocal,int revisionRemota){
  if(revisionLocal<1||revisionRemota<1)throw new IllegalArgumentException("Revisión inválida");
  return baseConocida&&localSinCambios&&revisionRemota>revisionLocal;
 }
}
