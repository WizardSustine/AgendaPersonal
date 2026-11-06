package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.RegistroEmocional;
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

public class RegistroEmocionalRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public RegistroEmocionalRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(RegistroEmocional registro) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (registro.getId() == null || registro.getId().isEmpty()) {
                registro.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", registro.getId());
            values.put("owner_id", registro.getOwnerId());
            values.put("fecha_hora", registro.getFechaHora());
            values.put("valoracion", registro.getValoracion());
            values.put("motivo_id", registro.getMotivoId());
            values.put("accion_evento_asociado", registro.getAccionEventoAsociado());
            values.put("observaciones", registro.getObservaciones());
            values.put("revision", revision);

            db.insertOrThrow("registros_emocionales", null, values);

            registrarCambio(
                    db,
                    registro.getOwnerId(),
                    registro.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            registro.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(RegistroEmocional registro) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, registro.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("fecha_hora", registro.getFechaHora());
            values.put("valoracion", registro.getValoracion());
            values.put("motivo_id", registro.getMotivoId());
            values.put("accion_evento_asociado", registro.getAccionEventoAsociado());
            values.put("observaciones", registro.getObservaciones());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "registros_emocionales",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{registro.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, registro.getId()),
                    registro.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            registro.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public RegistroEmocional obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "registros_emocionales",
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

    public List<RegistroEmocional> obtenerTodos() {
        List<RegistroEmocional> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "registros_emocionales",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha_hora DESC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<RegistroEmocional> obtenerPorMotivo(String motivoId) {
        List<RegistroEmocional> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "registros_emocionales",
                null,
                "motivo_id = ? AND deleted_at IS NULL",
                new String[]{motivoId},
                null,
                null,
                "fecha_hora DESC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<RegistroEmocional> obtenerPorValoracion(String valoracion) {
        List<RegistroEmocional> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "registros_emocionales",
                null,
                "valoracion = ? AND deleted_at IS NULL",
                new String[]{valoracion},
                null,
                null,
                "fecha_hora DESC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<RegistroEmocional> obtenerEntreFechas(
            String fechaDesde,
            String fechaHasta
    ) {
        List<RegistroEmocional> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "registros_emocionales",
                null,
                "fecha_hora >= ? AND fecha_hora <= ? AND deleted_at IS NULL",
                new String[]{fechaDesde, fechaHasta},
                null,
                null,
                "fecha_hora ASC"
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
                    "registros_emocionales",
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

    private RegistroEmocional convertirCursor(Cursor cursor) {
        RegistroEmocional registro = new RegistroEmocional();

        registro.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        registro.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        registro.setFechaHora(cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora")));
        registro.setValoracion(cursor.getString(cursor.getColumnIndexOrThrow("valoracion")));
        registro.setMotivoId(cursor.getString(cursor.getColumnIndexOrThrow("motivo_id")));
        registro.setAccionEventoAsociado(cursor.getString(cursor.getColumnIndexOrThrow("accion_evento_asociado")));
        registro.setObservaciones(cursor.getString(cursor.getColumnIndexOrThrow("observaciones")));
        registro.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        registro.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        registro.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        registro.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return registro;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "registros_emocionales",
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
                "registros_emocionales",
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
                    "registros_emocionales",
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
            values.put("entity_type", "registros_emocionales");
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

