
package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Materia;
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

public class MateriaRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public MateriaRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Materia materia) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (materia.getId() == null || materia.getId().isEmpty()) {
                materia.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", materia.getId());
            values.put("owner_id", materia.getOwnerId());
            values.put("institucion_id", materia.getInstitucionId());
            values.put("nombre", materia.getNombre());
            values.put("revision", revision);

            db.insertOrThrow("materias", null, values);

            registrarCambio(
                    db,
                    materia.getOwnerId(),
                    materia.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            materia.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(Materia materia) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, materia.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("institucion_id", materia.getInstitucionId());
            values.put("nombre", materia.getNombre());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "materias",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{materia.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, materia.getId()),
                    materia.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            materia.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public Materia obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "materias",
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

    public List<Materia> obtenerTodos() {
        List<Materia> materias = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "materias",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "nombre COLLATE NOCASE ASC"
        )) {
            while (cursor.moveToNext()) {
                materias.add(convertirCursor(cursor));
            }
        }

        return materias;
    }

    public List<Materia> obtenerPorInstitucion(String institucionId) {
        List<Materia> materias = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "materias",
                null,
                "institucion_id = ? AND deleted_at IS NULL",
                new String[]{institucionId},
                null,
                null,
                "nombre COLLATE NOCASE ASC"
        )) {
            while (cursor.moveToNext()) {
                materias.add(convertirCursor(cursor));
            }
        }

        return materias;
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
                    "materias",
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

    private Materia convertirCursor(Cursor cursor) {
        Materia materia = new Materia();

        materia.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        materia.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        materia.setInstitucionId(cursor.getString(cursor.getColumnIndexOrThrow("institucion_id")));
        materia.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
        materia.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        materia.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        materia.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        materia.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return materia;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "materias",
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
                "materias",
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
                    "materias",
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
            values.put("entity_type", "materias");
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
