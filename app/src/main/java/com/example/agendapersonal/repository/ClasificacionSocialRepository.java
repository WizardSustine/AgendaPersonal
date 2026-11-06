
package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.ClasificacionSocial;
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

public class ClasificacionSocialRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public ClasificacionSocialRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(ClasificacionSocial clasificacion) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (clasificacion.getId() == null || clasificacion.getId().isEmpty()) {
                clasificacion.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", clasificacion.getId());
            values.put("owner_id", clasificacion.getOwnerId());
            values.put("nombre", clasificacion.getNombre());
            values.put("revision", revision);

            db.insertOrThrow("clasificaciones_sociales", null, values);

            registrarCambio(
                    db,
                    clasificacion.getOwnerId(),
                    clasificacion.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            clasificacion.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(ClasificacionSocial clasificacion) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, clasificacion.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("nombre", clasificacion.getNombre());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "clasificaciones_sociales",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{clasificacion.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, clasificacion.getId()),
                    clasificacion.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            clasificacion.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public ClasificacionSocial obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "clasificaciones_sociales",
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

    public List<ClasificacionSocial> obtenerTodos() {
        List<ClasificacionSocial> clasificaciones = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "clasificaciones_sociales",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "nombre COLLATE NOCASE ASC"
        )) {
            while (cursor.moveToNext()) {
                clasificaciones.add(convertirCursor(cursor));
            }
        }

        return clasificaciones;
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
                    "clasificaciones_sociales",
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

    private ClasificacionSocial convertirCursor(Cursor cursor) {
        ClasificacionSocial clasificacion = new ClasificacionSocial();

        clasificacion.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        clasificacion.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        clasificacion.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
        clasificacion.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        clasificacion.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        clasificacion.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        clasificacion.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return clasificacion;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "clasificaciones_sociales",
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
                "clasificaciones_sociales",
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
                    "clasificaciones_sociales",
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
            values.put("entity_type", "clasificaciones_sociales");
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
