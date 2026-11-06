
package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Estudio;
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

public class EstudioRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public EstudioRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Estudio estudio) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (estudio.getId() == null || estudio.getId().isEmpty()) {
                estudio.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", estudio.getId());
            values.put("owner_id", estudio.getOwnerId());
            values.put("fecha_entrega", estudio.getFechaEntrega());
            values.put("hora", estudio.getHora());
            values.put("actividad", estudio.getActividad());
            values.put("materia_id", estudio.getMateriaId());
            values.put("institucion_id", estudio.getInstitucionId());
            values.put("prioridad", estudio.getPrioridad());
            values.put("estado_especial", estudio.getEstadoEspecial());
            values.put("revision", revision);

            db.insertOrThrow("estudios", null, values);

            registrarCambio(
                    db,
                    estudio.getOwnerId(),
                    estudio.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            estudio.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(Estudio estudio) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, estudio.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("fecha_entrega", estudio.getFechaEntrega());
            values.put("hora", estudio.getHora());
            values.put("actividad", estudio.getActividad());
            values.put("materia_id", estudio.getMateriaId());
            values.put("institucion_id", estudio.getInstitucionId());
            values.put("prioridad", estudio.getPrioridad());
            values.put("estado_especial", estudio.getEstadoEspecial());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "estudios",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{estudio.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, estudio.getId()),
                    estudio.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            estudio.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public Estudio obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "estudios",
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

    public List<Estudio> obtenerTodos() {
        List<Estudio> estudios = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "estudios",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha_entrega ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                estudios.add(convertirCursor(cursor));
            }
        }

        return estudios;
    }

    public List<Estudio> obtenerPorMateria(String materiaId) {
        List<Estudio> estudios = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "estudios",
                null,
                "materia_id = ? AND deleted_at IS NULL",
                new String[]{materiaId},
                null,
                null,
                "fecha_entrega ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                estudios.add(convertirCursor(cursor));
            }
        }

        return estudios;
    }

    public List<Estudio> obtenerPorInstitucion(String institucionId) {
        List<Estudio> estudios = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "estudios",
                null,
                "institucion_id = ? AND deleted_at IS NULL",
                new String[]{institucionId},
                null,
                null,
                "fecha_entrega ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                estudios.add(convertirCursor(cursor));
            }
        }

        return estudios;
    }

    public List<Estudio> obtenerPorEstadoEspecial(String estadoEspecial) {
        List<Estudio> estudios = new ArrayList<>();
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
                "estudios",
                null,
                seleccion,
                argumentos,
                null,
                null,
                "fecha_entrega ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                estudios.add(convertirCursor(cursor));
            }
        }

        return estudios;
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
                    "estudios",
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

    private Estudio convertirCursor(Cursor cursor) {
        Estudio estudio = new Estudio();

        estudio.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        estudio.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        estudio.setFechaEntrega(cursor.getString(cursor.getColumnIndexOrThrow("fecha_entrega")));
        estudio.setHora(cursor.getString(cursor.getColumnIndexOrThrow("hora")));
        estudio.setActividad(cursor.getString(cursor.getColumnIndexOrThrow("actividad")));
        estudio.setMateriaId(cursor.getString(cursor.getColumnIndexOrThrow("materia_id")));
        estudio.setInstitucionId(cursor.getString(cursor.getColumnIndexOrThrow("institucion_id")));
        estudio.setPrioridad(cursor.getString(cursor.getColumnIndexOrThrow("prioridad")));
        estudio.setEstadoEspecial(cursor.getString(cursor.getColumnIndexOrThrow("estado_especial")));
        estudio.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        estudio.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        estudio.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        estudio.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return estudio;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "estudios",
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
                "estudios",
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
                    "estudios",
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
            values.put("entity_type", "estudios");
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
