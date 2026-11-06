package com.example.agendapersonal.ui.registro;
public final class Campo {
 public final String clave, etiqueta, tipo, relacion, padre, destinoPadre;
 public final boolean obligatorio;
 public final String[] opciones;
 public Campo(String c,String e,String t,boolean o,String[] opciones,String r,String p,String dp) { clave=c; etiqueta=e; tipo=t; obligatorio=o; this.opciones=opciones; relacion=r; padre=p; destinoPadre=dp; }
}
