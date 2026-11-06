package com.example.agendapersonal.ui.registro;
import java.util.LinkedHashMap;
public class Registro extends LinkedHashMap<String,String> {
    public String texto(String clave) { String v = get(clave); return v == null ? "" : v; }
    public void poner(String clave, Object valor) { put(clave, valor == null ? null : String.valueOf(valor)); }
    public Registro copia() { Registro r = new Registro(); r.putAll(this); return r; }
}
