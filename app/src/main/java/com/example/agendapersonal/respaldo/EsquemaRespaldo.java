package com.example.agendapersonal.respaldo;

import com.example.agendapersonal.data.DatabaseHelper;
import java.util.*;

/** Esquemas del código instalado. Nunca se ejecuta DDL leído de una copia externa. */
final class EsquemaRespaldo {
    static final String CONTROL="respaldo_control";
    static Map<String,String> tablas(){Map<String,String> m=new LinkedHashMap<>();for(String sql:DatabaseHelper.esquemaTablasRespaldo())agregar(m,sql);
        agregar(m,"CREATE TABLE sync_meta (clave TEXT PRIMARY KEY NOT NULL, valor TEXT NOT NULL)");
        agregar(m,"CREATE TABLE sync_outbox_sent (owner_id TEXT NOT NULL, change_id TEXT NOT NULL, confirmed_at TEXT NOT NULL, PRIMARY KEY(owner_id,change_id))");
        agregar(m,"CREATE TABLE sync_inbox_receipts (owner_id TEXT NOT NULL, device_id TEXT NOT NULL, change_id TEXT NOT NULL, applied_at TEXT NOT NULL, remote_confirmed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(owner_id,device_id,change_id))");
        agregar(m,"CREATE TABLE sync_deferred (owner_id TEXT NOT NULL, change_id TEXT NOT NULL, cambio_json TEXT NOT NULL, estado TEXT NOT NULL DEFAULT 'PENDIENTE', motivo TEXT, PRIMARY KEY(owner_id,change_id))");
        agregar(m,"CREATE TABLE sync_conflicts (owner_id TEXT NOT NULL, change_id TEXT NOT NULL, entity_type TEXT NOT NULL, entity_id TEXT NOT NULL, local_payload TEXT, remote_change TEXT NOT NULL, created_at TEXT NOT NULL, resolved_at TEXT, resolution_change_id TEXT, PRIMARY KEY(owner_id,change_id))");
        agregar(m,"CREATE TABLE historico_fuentes (tabla TEXT NOT NULL, entidad_id TEXT NOT NULL, fuente_sha256 TEXT NOT NULL, original_json TEXT NOT NULL, PRIMARY KEY(tabla,entidad_id,fuente_sha256))");
        agregar(m,"CREATE TABLE historico_archivo (fuente_sha256 TEXT NOT NULL, hoja TEXT NOT NULL, fila INTEGER NOT NULL, original_json TEXT NOT NULL, motivo_revision TEXT, PRIMARY KEY(fuente_sha256,hoja,fila))");
        agregar(m,"CREATE TABLE eventos_ocurrencias (evento_id TEXT PRIMARY KEY NOT NULL, serie_id TEXT NOT NULL, ordinal INTEGER NOT NULL, fecha_origen TEXT NOT NULL)");
        String historial=m.get("historial_postergaciones").replace("CREATE TABLE historial_postergaciones (","CREATE TABLE historial_postergaciones_v1 (").replace(",'INGRESO','PAGO','GASTO'","");agregar(m,historial);return m;
    }
    private static void agregar(Map<String,String> m,String sql){String nombre=sql.substring("CREATE TABLE ".length(),sql.indexOf('(')).trim();m.put(nombre,sql);}
    static String normalizar(String s){return s.replaceAll("(?i)CREATE TABLE IF NOT EXISTS","CREATE TABLE").replace("\"","").replaceAll("\\s+","").toLowerCase(Locale.ROOT);}
    static String nombre(String n){if(!n.matches("[a-z][a-z0-9_]*"))throw new IllegalArgumentException("Nombre de tabla inválido");return "\""+n+"\"";}
}
