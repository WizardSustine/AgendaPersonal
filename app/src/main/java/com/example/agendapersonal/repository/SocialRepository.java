
package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Social;
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

public class SocialRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public SocialRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Social social) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (social.getId() == null || social.getId().isEmpty()) {
                social.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", social.getId());
            values.put("owner_id", social.getOwnerId());
            values.put("fecha", social.getFecha());
            values.put("hora", social.getHora());
            values.put("actividad", social.getActividad());
            values.put("persona_id", social.getPersonaId());
            values.put("clasificacion_id", social.getClasificacionId());
            values.put("prioridad", social.getPrioridad());
            values.put("estado_especial", social.getEstadoEspecial());
            values.put("revision", revision);

            db.insertOrThrow("sociales", null, values);

            registrarCambio(
                    db,
                    social.getOwnerId(),
                    social.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            social.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(Social social) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, social.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("fecha", social.getFecha());
            values.put("hora", social.getHora());
            values.put("actividad", social.getActividad());
            values.put("persona_id", social.getPersonaId());
            values.put("clasificacion_id", social.getClasificacionId());
            values.put("prioridad", social.getPrioridad());
            values.put("estado_especial", social.getEstadoEspecial());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "sociales",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{social.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, social.getId()),
                    social.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            social.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public Social obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "sociales",
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

    public List<Social> obtenerTodos() {
        List<Social> sociales = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "sociales",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                sociales.add(convertirCursor(cursor));
            }
        }

        return sociales;
    }

    public List<Social> obtenerPorPersona(String personaId) {
        List<Social> sociales = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "sociales",
                null,
                "persona_id = ? AND deleted_at IS NULL",
                new String[]{personaId},
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                sociales.add(convertirCursor(cursor));
            }
        }

        return sociales;
    }

    public List<Social> obtenerPorClasificacion(String clasificacionId) {
        List<Social> sociales = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "sociales",
                null,
                "clasificacion_id = ? AND deleted_at IS NULL",
                new String[]{clasificacionId},
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                sociales.add(convertirCursor(cursor));
            }
        }

        return sociales;
    }

    public List<Social> obtenerPorEstadoEspecial(String estadoEspecial) {
        List<Social> sociales = new ArrayList<>();
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
                "sociales",
                null,
                seleccion,
                argumentos,
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                sociales.add(convertirCursor(cursor));
            }
        }

        return sociales;
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
                    "sociales",
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

    private Social convertirCursor(Cursor cursor) {
        Social social = new Social();

        social.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        social.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        social.setFecha(cursor.getString(cursor.getColumnIndexOrThrow("fecha")));
        social.setHora(cursor.getString(cursor.getColumnIndexOrThrow("hora")));
        social.setActividad(cursor.getString(cursor.getColumnIndexOrThrow("actividad")));
        social.setPersonaId(cursor.getString(cursor.getColumnIndexOrThrow("persona_id")));
        social.setClasificacionId(cursor.getString(cursor.getColumnIndexOrThrow("clasificacion_id")));
        social.setPrioridad(cursor.getString(cursor.getColumnIndexOrThrow("prioridad")));
        social.setEstadoEspecial(cursor.getString(cursor.getColumnIndexOrThrow("estado_especial")));
        social.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        social.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        social.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        social.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return social;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "sociales",
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
                "sociales",
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
                    "sociales",
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
            values.put("entity_type", "sociales");
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
