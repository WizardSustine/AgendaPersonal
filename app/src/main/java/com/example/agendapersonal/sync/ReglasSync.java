package com.example.agendapersonal.sync;

import java.net.URI;
import java.util.UUID;

/** Decisiones de revisión independientes de Android y de la red. */
public final class ReglasSync {
    private ReglasSync() {}
    public enum Decision { APLICAR, YA_APLICADO, ESPERAR_ANTERIOR, CONFLICTO }
    public static Decision decidir(Integer actual,int base,int nueva,boolean cambioConocido) {
        if(base<0||nueva<=0||(long)base+1!=nueva)throw new IllegalArgumentException("Secuencia de revisión inválida");
        if(cambioConocido)return Decision.YA_APLICADO;
        if(actual==null)return base==0?Decision.APLICAR:Decision.ESPERAR_ANTERIOR;
        if(actual<=0)throw new IllegalArgumentException("Revisión local inválida");
        return actual==base?Decision.APLICAR:Decision.CONFLICTO;
    }
    public static int revisionResolucion(int local,int remota) {
        int max=Math.max(local,remota);if(max<1||max==Integer.MAX_VALUE)throw new IllegalArgumentException("Revisión fuera de rango");return max+1;
    }
    public static String urlProyecto(String texto) {
        try {
            URI u=new URI(texto.trim());String host=u.getHost();
            if(!"https".equalsIgnoreCase(u.getScheme())||host==null||u.getUserInfo()!=null||u.getQuery()!=null||u.getFragment()!=null||u.getPort()!=-1&&u.getPort()!=443||u.getPath()!=null&&!u.getPath().isEmpty()&&!u.getPath().equals("/"))throw new IllegalArgumentException();
            return "https://"+host.toLowerCase(java.util.Locale.ROOT);
        }catch(Exception e){throw new IllegalArgumentException("Ingresá la URL HTTPS del proyecto, sin rutas ni parámetros");}
    }
    public static String uuid(String valor) {String canonical=UUID.fromString(valor).toString();if(!canonical.equalsIgnoreCase(valor))throw new IllegalArgumentException("UUID inválido");return canonical;}
    public static long demoraReintento(int fallos){int e=Math.max(0,Math.min(6,fallos-1));return Math.min(600000L,10000L*(1L<<e));}
}
