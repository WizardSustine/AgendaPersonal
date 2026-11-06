package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Salud;
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

public class SaludRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public SaludRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Salud salud) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (salud.getId() == null || salud.getId().isEmpty()) {
                salud.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", salud.getId());
            values.put("owner_id", salud.getOwnerId());
            values.put("fecha", salud.getFecha());
            values.put("hora", salud.getHora());
            values.put("actividad", salud.getActividad());
            values.put("especialidad_id", salud.getEspecialidadId());
            values.put("institucion_id", salud.getInstitucionId());
            values.put("prioridad", salud.getPrioridad());
            values.put("estado_especial", salud.getEstadoEspecial());
            values.put("revision", revision);

            db.insertOrThrow("salud", null, values);

            registrarCambio(
                    db,
                    salud.getOwnerId(),
                    salud.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            salud.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(Salud salud) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, salud.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("fecha", salud.getFecha());
            values.put("hora", salud.getHora());
            values.put("actividad", salud.getActividad());
            values.put("especialidad_id", salud.getEspecialidadId());
            values.put("institucion_id", salud.getInstitucionId());
            values.put("prioridad", salud.getPrioridad());
            values.put("estado_especial", salud.getEstadoEspecial());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "salud",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{salud.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, salud.getId()),
                    salud.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            salud.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public Salud obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "salud",
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

    public List<Salud> obtenerTodos() {
        List<Salud> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "salud",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Salud> obtenerPorEspecialidad(String especialidadId) {
        List<Salud> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "salud",
                null,
                "especialidad_id = ? AND deleted_at IS NULL",
                new String[]{especialidadId},
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Salud> obtenerPorInstitucion(String institucionId) {
        List<Salud> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "salud",
                null,
                "institucion_id = ? AND deleted_at IS NULL",
                new String[]{institucionId},
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Salud> obtenerPorEstadoEspecial(String estadoEspecial) {
        List<Salud> registros = new ArrayList<>();
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
                "salud",
                null,
                seleccion,
                argumentos,
                null,
                null,
                "fecha ASC, hora ASC"
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
                    "salud",
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

    private Salud convertirCursor(Cursor cursor) {
        Salud salud = new Salud();

        salud.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        salud.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        salud.setFecha(cursor.getString(cursor.getColumnIndexOrThrow("fecha")));
        salud.setHora(cursor.getString(cursor.getColumnIndexOrThrow("hora")));
        salud.setActividad(cursor.getString(cursor.getColumnIndexOrThrow("actividad")));
        salud.setEspecialidadId(cursor.getString(cursor.getColumnIndexOrThrow("especialidad_id")));
        salud.setInstitucionId(cursor.getString(cursor.getColumnIndexOrThrow("institucion_id")));
        salud.setPrioridad(cursor.getString(cursor.getColumnIndexOrThrow("prioridad")));
        salud.setEstadoEspecial(cursor.getString(cursor.getColumnIndexOrThrow("estado_especial")));
        salud.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        salud.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        salud.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        salud.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return salud;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "salud",
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
                "salud",
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
                    "salud",
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
            values.put("entity_type", "salud");
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
