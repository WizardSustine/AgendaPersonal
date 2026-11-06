package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Trabajo;
import com.example.agendapersonal.util.UuidGenerator;
import com.example.agendapersonal.util.JsonCursorHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TrabajoRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public TrabajoRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Trabajo trabajo) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (trabajo.getId() == null || trabajo.getId().isEmpty()) {
                trabajo.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", trabajo.getId());
            values.put("owner_id", trabajo.getOwnerId());
            values.put("fecha_entrega", trabajo.getFechaEntrega());
            values.put("hora", trabajo.getHora());
            values.put("actividad", trabajo.getActividad());
            values.put("asunto", trabajo.getAsunto());
            values.put("clasificacion", trabajo.getClasificacion());
            values.put("persona_id", trabajo.getPersonaId());
            values.put("prioridad", trabajo.getPrioridad());
            values.put("estado_especial", trabajo.getEstadoEspecial());
            values.put("revision", revision);

            db.insertOrThrow("trabajo", null, values);

            registrarCambio(
                    db,
                    trabajo.getOwnerId(),
                    trabajo.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            trabajo.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(Trabajo trabajo) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, trabajo.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("fecha_entrega", trabajo.getFechaEntrega());
            values.put("hora", trabajo.getHora());
            values.put("actividad", trabajo.getActividad());
            values.put("asunto", trabajo.getAsunto());
            values.put("clasificacion", trabajo.getClasificacion());
            values.put("persona_id", trabajo.getPersonaId());
            values.put("prioridad", trabajo.getPrioridad());
            values.put("estado_especial", trabajo.getEstadoEspecial());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "trabajo",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{trabajo.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, trabajo.getId()),
                    trabajo.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            trabajo.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public Trabajo obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "trabajo",
                null,
                "id = ? AND deleted_at IS NULL",
                new String[]{id},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return convertirCursor(cursor);
            }
        }

        return null;
    }

    public List<Trabajo> obtenerTodos() {
        List<Trabajo> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "trabajo",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha_entrega ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Trabajo> obtenerPorPersona(String personaId) {
        List<Trabajo> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "trabajo",
                null,
                "persona_id = ? AND deleted_at IS NULL",
                new String[]{personaId},
                null,
                null,
                "fecha_entrega ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Trabajo> obtenerPorEstadoEspecial(String estadoEspecial) {
        List<Trabajo> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String seleccion;
        String[] argumentos;

        if (estadoEspecial == null) {
            seleccion = "estado_especial IS NULL AND deleted_at IS NULL";
            argumentos = null;
        } else {
            seleccion = "estado_especial = ? AND deleted_at IS NULL";
            argumentos = new String[]{estadoEspecial};
        }

        try (Cursor cursor = db.query(
                "trabajo",
                null,
                seleccion,
                argumentos,
                null,
                null,
                "fecha_entrega ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public boolean eliminar(String id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, id);

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;
            String fechaActual = obtenerFechaActual();

            ContentValues values = new ContentValues();
            values.put("deleted_at", fechaActual);
            values.put("updated_at", fechaActual);
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "trabajo",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{id}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, id),
                    id,
                    revisionActual,
                    nuevaRevision,
                    "DELETE"
            );

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    private Trabajo convertirCursor(Cursor cursor) {
        Trabajo trabajo = new Trabajo();

        trabajo.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        trabajo.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        trabajo.setFechaEntrega(cursor.getString(cursor.getColumnIndexOrThrow("fecha_entrega")));
        trabajo.setHora(cursor.getString(cursor.getColumnIndexOrThrow("hora")));
        trabajo.setActividad(cursor.getString(cursor.getColumnIndexOrThrow("actividad")));
        trabajo.setAsunto(cursor.getString(cursor.getColumnIndexOrThrow("asunto")));
        trabajo.setClasificacion(cursor.getString(cursor.getColumnIndexOrThrow("clasificacion")));
        trabajo.setPersonaId(cursor.getString(cursor.getColumnIndexOrThrow("persona_id")));
        trabajo.setPrioridad(cursor.getString(cursor.getColumnIndexOrThrow("prioridad")));
        trabajo.setEstadoEspecial(cursor.getString(cursor.getColumnIndexOrThrow("estado_especial")));
        trabajo.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        trabajo.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        trabajo.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        trabajo.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return trabajo;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "trabajo",
                new String[]{"revision"},
                "id = ? AND deleted_at IS NULL",
                new String[]{id},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return -1;
    }

    private String obtenerOwnerId(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "trabajo",
                new String[]{"owner_id"},
                "id = ?",
                new String[]{id},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }

        return null;
    }

    private void registrarCambio(
            SQLiteDatabase db,
            String ownerId,
            String entityId,
            int baseRevision,
            int entityRevision,
            String operation
    ) {
        try {
            JSONObject payload;

            try (Cursor cursor = db.query(
                    "trabajo",
                    null,
                    "id = ?",
                    new String[]{entityId},
                    null,
                    null,
                    null
            )) {
                if (!cursor.moveToFirst()) {
                    throw new IllegalStateException(
                            "No se encontró el registro para generar el payload."
                    );
                }
                payload = JsonCursorHelper.toJsonObject(cursor);
            }

            ContentValues values = new ContentValues();
            values.put("change_id", UuidGenerator.generate());
            values.put("owner_id", ownerId);
            values.put("device_id", deviceId);
            values.put("entity_type", "trabajo");
            values.put("entity_id", entityId);
            values.put("base_revision", baseRevision);
            values.put("entity_revision", entityRevision);
            values.put("operation", operation);
            values.put("payload", payload.toString());
            values.put("created_at", obtenerFechaActual());

            db.insertOrThrow("sync_changes", null, values);

        } catch (JSONException e) {
            throw new IllegalStateException(
                    "Error al generar el JSON del cambio de sincronización",
                    e
            );
        }
    }

    private String obtenerFechaActual() {
        SimpleDateFormat formato = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.US
        );

        formato.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formato.format(new Date());
    }

    public void cerrar() {
        databaseHelper.close();
    }
}
