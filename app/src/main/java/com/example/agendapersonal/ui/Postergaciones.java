package com.example.agendapersonal.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.ui.registro.Definicion;
import com.example.agendapersonal.util.JsonCursorHelper;
import org.json.JSONException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Modificación, historial y dos cambios sincronizables se confirman juntos. */
public final class Postergaciones {
    private Postergaciones() {}
    public static void guardar(Context context,String device,String tabla,String id,String fechaCampo,String nueva) {
        guardar(context,device,tabla,id,fechaCampo,nueva,null);}
    public static void guardar(Context context,String device,String tabla,String id,String fechaCampo,String nueva,com.example.agendapersonal.ui.registro.Registro ocurrencia) {
        Map<String,String> tipos=new HashMap<>();
        tipos.put("tareas","TAREA");tipos.put("eventos","EVENTO");tipos.put("estudios","ESTUDIO");
        tipos.put("sociales","SOCIAL");tipos.put("salud","SALUD");tipos.put("trabajo","TRABAJO");tipos.put("freelance","FREELANCE");
        tipos.put("ingresos","INGRESO");tipos.put("pagos","PAGO");tipos.put("gastos","GASTO");
        String tipo=tipos.get(tabla);
        if(tipo==null)throw new IllegalArgumentException("El esquema no admite historial de postergación para esta entidad");
        Definicion d=Definicion.de(tabla);
        if(d.campo(fechaCampo)==null)throw new IllegalArgumentException("Fecha desconocida");
        String columna=fechaCampo.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(java.util.Locale.ROOT);
        try(DatabaseHelper helper=new DatabaseHelper(context)) {
            SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();
            try {
                if(tabla.equals("eventos")&&ocurrencia!=null&&Boolean.parseBoolean(ocurrencia.texto("_virtual")))com.example.agendapersonal.eventos.SeriesEvento.materializar(db,context,device,ocurrencia);String original,owner;int revision;
                try(Cursor c=db.query(tabla,null,"id = ? AND deleted_at IS NULL",new String[]{id},null,null,null)) {
                    if(!c.moveToFirst())throw new IllegalStateException("Registro inexistente");
                    original=c.getString(c.getColumnIndexOrThrow(columna));owner=c.getString(c.getColumnIndexOrThrow("owner_id"));revision=c.getInt(c.getColumnIndexOrThrow("revision"));
                }
                if(original==null||original.isEmpty())throw new IllegalArgumentException("El registro no tiene fecha original");
                String ahora=Instant.now().toString(),historialId=UUID.randomUUID().toString();
                ContentValues historial=new ContentValues();historial.put("id",historialId);historial.put("owner_id",owner);
                historial.put("tipo_entidad",tipo);historial.put("entidad_id",id);historial.put("fecha_original",original);
                historial.put("nueva_fecha",nueva.isEmpty()?null:nueva);historial.put("fecha_postergacion",ahora);
                db.insertOrThrow("historial_postergaciones",null,historial);
                ContentValues cambio=new ContentValues();cambio.put("estado_especial","POSTERGADO");cambio.put("revision",revision+1);cambio.put("updated_at",ahora);
                if(!nueva.isEmpty())cambio.put(columna,nueva);
                if(db.update(tabla,cambio,"id = ? AND revision = ?",new String[]{id,String.valueOf(revision)})!=1)throw new IllegalStateException("El registro cambió durante la edición");
                registrar(db,device,owner,tabla,id,revision,revision+1);
                registrar(db,device,owner,"historial_postergaciones",historialId,0,1);
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        }
        com.example.agendapersonal.eventos.SeriesEvento.cambios(context);
    }
    private static void registrar(SQLiteDatabase db,String device,String owner,String tabla,String id,int base,int revision) {
        ContentValues values=new ContentValues();values.put("change_id",UUID.randomUUID().toString());values.put("owner_id",owner);values.put("device_id",device);values.put("entity_type",tabla);values.put("entity_id",id);values.put("base_revision",base);values.put("entity_revision",revision);values.put("operation","UPSERT");
        try(Cursor c=db.query(tabla,null,"id = ?",new String[]{id},null,null,null)) {
            if(!c.moveToFirst())throw new IllegalStateException();org.json.JSONObject p=JsonCursorHelper.toJsonObject(c);if(tabla.equals("eventos")){org.json.JSONObject indice=com.example.agendapersonal.eventos.SeriesEvento.metadatos(db,id);if(indice!=null)p.put(com.example.agendapersonal.eventos.SeriesEvento.META,indice);}values.put("payload",p.toString());
        } catch(JSONException e) { throw new IllegalStateException(e); }
        db.insertOrThrow("sync_changes",null,values);
    }
}
