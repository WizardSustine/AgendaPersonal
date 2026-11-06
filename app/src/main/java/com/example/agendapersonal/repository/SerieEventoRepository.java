package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.SerieEvento;
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

public class SerieEventoRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public SerieEventoRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(SerieEvento serie) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (serie.getId() == null || serie.getId().isEmpty()) {
                serie.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", serie.getId());
            values.put("owner_id", serie.getOwnerId());
            values.put("frecuencia", serie.getFrecuencia());
            values.put("configuracion_personalizada", serie.getConfiguracionPersonalizada());
            values.put("fecha_fin", serie.getFechaFin());
            values.put("revision", revision);

            db.insertOrThrow("series_eventos", null, values);

            registrarCambio(
                    db,
                    serie.getOwnerId(),
                    serie.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            serie.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(SerieEvento serie) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, serie.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("frecuencia", serie.getFrecuencia());
            values.put("configuracion_personalizada", serie.getConfiguracionPersonalizada());
            values.put("fecha_fin", serie.getFechaFin());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "series_eventos",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{serie.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, serie.getId()),
                    serie.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            serie.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public SerieEvento obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "series_eventos",
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

    public List<SerieEvento> obtenerTodos() {
        List<SerieEvento> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "series_eventos",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "created_at DESC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<SerieEvento> obtenerPorFrecuencia(String frecuencia) {
        List<SerieEvento> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "series_eventos",
                null,
                "frecuencia = ? AND deleted_at IS NULL",
                new String[]{frecuencia},
                null,
                null,
                "created_at DESC"
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
                    "series_eventos",
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

    private SerieEvento convertirCursor(Cursor cursor) {
        SerieEvento serie = new SerieEvento();

        serie.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        serie.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        serie.setFrecuencia(cursor.getString(cursor.getColumnIndexOrThrow("frecuencia")));
        serie.setConfiguracionPersonalizada(
                cursor.getString(cursor.getColumnIndexOrThrow("configuracion_personalizada"))
        );
        serie.setFechaFin(cursor.getString(cursor.getColumnIndexOrThrow("fecha_fin")));
        serie.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        serie.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        serie.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        serie.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return serie;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "series_eventos",
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
                "series_eventos",
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
                    "series_eventos",
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
            values.put("entity_type", "series_eventos");
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
