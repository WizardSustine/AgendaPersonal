package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.HistorialPostergacion;
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

public class HistorialPostergacionRepository {

    private static final String TABLA = "historial_postergaciones";
    private static final String TIPO_ENTIDAD = "historial_postergaciones";

    private final DatabaseHelper dbHelper;
    private final String deviceId;

    public HistorialPostergacionRepository(Context context, String deviceId) {
        this.dbHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    // ---------------------------------------------------------
    // INSERTAR
    // ---------------------------------------------------------

    public boolean insertar(HistorialPostergacion historial) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            String id = historial.getId();

            if (id == null || id.trim().isEmpty()) {
                id = UuidGenerator.generate();
                historial.setId(id);
            }

            String fechaActual = obtenerFechaActual();

            historial.setCreatedAt(fechaActual);
            historial.setUpdatedAt(fechaActual);
            historial.setRevision(1);
            historial.setDeletedAt(null);

            if (historial.getFechaPostergacion() == null
                    || historial.getFechaPostergacion().trim().isEmpty()) {
                historial.setFechaPostergacion(fechaActual);
            }

            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("owner_id", historial.getOwnerId());
            values.put("tipo_entidad", historial.getTipoEntidad());
            values.put("entidad_id", historial.getEntidadId());
            values.put("fecha_original", historial.getFechaOriginal());
            values.put("nueva_fecha", historial.getNuevaFecha());
            values.put("fecha_postergacion", historial.getFechaPostergacion());
            values.put("created_at", fechaActual);
            values.put("updated_at", fechaActual);
            values.put("revision", 1);
            values.putNull("deleted_at");

            long resultado = db.insert(TABLA, null, values);

            if (resultado == -1) {
                return false;
            }

            registrarCambio(
                    db,
                    id,
                    historial.getOwnerId(),
                    0,
                    1,
                    "UPSERT"
            );

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    // ---------------------------------------------------------
    // ACTUALIZAR
    // ---------------------------------------------------------

    public boolean actualizar(HistorialPostergacion historial) {

        if (historial.getId() == null
                || historial.getId().trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionAnterior = obtenerRevision(db, historial.getId());

            if (revisionAnterior < 0) {
                return false;
            }

            int nuevaRevision = revisionAnterior + 1;
            String fechaActual = obtenerFechaActual();

            ContentValues values = new ContentValues();
            values.put("tipo_entidad", historial.getTipoEntidad());
            values.put("entidad_id", historial.getEntidadId());
            values.put("fecha_original", historial.getFechaOriginal());
            values.put("nueva_fecha", historial.getNuevaFecha());
            values.put("fecha_postergacion", historial.getFechaPostergacion());
            values.put("updated_at", fechaActual);
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    TABLA,
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{historial.getId()}
            );

            if (filas == 0) {
                return false;
            }

            historial.setUpdatedAt(fechaActual);
            historial.setRevision(nuevaRevision);

            String ownerId = obtenerOwnerId(db, historial.getId());

            registrarCambio(
                    db,
                    historial.getId(),
                    ownerId,
                    revisionAnterior,
                    nuevaRevision,
                    "UPSERT"
            );

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    // ---------------------------------------------------------
    // ELIMINAR (BORRADO LÓGICO)
    // ---------------------------------------------------------

    public boolean eliminar(String id) {

        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionAnterior = obtenerRevision(db, id);

            if (revisionAnterior < 0) {
                return false;
            }

            int nuevaRevision = revisionAnterior + 1;
            String fechaActual = obtenerFechaActual();

            ContentValues values = new ContentValues();
            values.put("deleted_at", fechaActual);
            values.put("updated_at", fechaActual);
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    TABLA,
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{id}
            );

            if (filas == 0) {
                return false;
            }

            String ownerId = obtenerOwnerId(db, id);

            registrarCambio(
                    db,
                    id,
                    ownerId,
                    revisionAnterior,
                    nuevaRevision,
                    "DELETE"
            );

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    // ---------------------------------------------------------
    // OBTENER POR ID
    // ---------------------------------------------------------

    public HistorialPostergacion obtenerPorId(String id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
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

            return null;
        }
    }

    // ---------------------------------------------------------
    // OBTENER TODOS
    // ---------------------------------------------------------

    public List<HistorialPostergacion> obtenerTodos() {

        List<HistorialPostergacion> historiales = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha_postergacion DESC"
        )) {
            while (cursor.moveToNext()) {
                historiales.add(convertirCursor(cursor));
            }
        }

        return historiales;
    }

    // ---------------------------------------------------------
    // OBTENER POR ENTIDAD
    // ---------------------------------------------------------

    public List<HistorialPostergacion> obtenerPorEntidad(
            String tipoEntidad,
            String entidadId
    ) {

        List<HistorialPostergacion> historiales = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "tipo_entidad = ? AND entidad_id = ? AND deleted_at IS NULL",
                new String[]{tipoEntidad, entidadId},
                null,
                null,
                "fecha_postergacion DESC"
        )) {
            while (cursor.moveToNext()) {
                historiales.add(convertirCursor(cursor));
            }
        }

        return historiales;
    }

    // ---------------------------------------------------------
    // CONVERTIR CURSOR A MODELO
    // ---------------------------------------------------------

    private HistorialPostergacion convertirCursor(Cursor cursor) {

        HistorialPostergacion historial = new HistorialPostergacion();

        historial.setId(cursor.getString(
                cursor.getColumnIndexOrThrow("id")
        ));

        historial.setOwnerId(cursor.getString(
                cursor.getColumnIndexOrThrow("owner_id")
        ));

        historial.setTipoEntidad(cursor.getString(
                cursor.getColumnIndexOrThrow("tipo_entidad")
        ));

        historial.setEntidadId(cursor.getString(
                cursor.getColumnIndexOrThrow("entidad_id")
        ));

        historial.setFechaOriginal(cursor.getString(
                cursor.getColumnIndexOrThrow("fecha_original")
        ));

        int nuevaFechaIndex = cursor.getColumnIndexOrThrow("nueva_fecha");

        historial.setNuevaFecha(
                cursor.isNull(nuevaFechaIndex)
                        ? null
                        : cursor.getString(nuevaFechaIndex)
        );

        historial.setFechaPostergacion(cursor.getString(
                cursor.getColumnIndexOrThrow("fecha_postergacion")
        ));

        historial.setCreatedAt(cursor.getString(
                cursor.getColumnIndexOrThrow("created_at")
        ));

        historial.setUpdatedAt(cursor.getString(
                cursor.getColumnIndexOrThrow("updated_at")
        ));

        historial.setRevision(cursor.getInt(
                cursor.getColumnIndexOrThrow("revision")
        ));

        int deletedAtIndex = cursor.getColumnIndexOrThrow("deleted_at");

        historial.setDeletedAt(
                cursor.isNull(deletedAtIndex)
                        ? null
                        : cursor.getString(deletedAtIndex)
        );

        return historial;
    }

    // ---------------------------------------------------------
    // REGISTRAR CAMBIO PARA SINCRONIZACIÓN
    // ---------------------------------------------------------

    private void registrarCambio(
            SQLiteDatabase db,
            String entityId,
            String ownerId,
            int baseRevision,
            int entityRevision,
            String operation
    ) {

        try {
            JSONObject payload;

            try (Cursor cursor = db.query(
                    TABLA,
                    null,
                    "id = ?",
                    new String[]{entityId},
                    null,
                    null,
                    null
            )) {
                if (!cursor.moveToFirst()) {
                    throw new IllegalStateException(
                            "No se encontró el historial para generar el payload."
                    );
                }

                payload = JsonCursorHelper.toJsonObject(cursor);
            }

            ContentValues values = new ContentValues();

            values.put("change_id", UuidGenerator.generate());
            values.put("owner_id", ownerId);
            values.put("device_id", deviceId);
            values.put("entity_type", TIPO_ENTIDAD);
            values.put("entity_id", entityId);
            values.put("base_revision", baseRevision);
            values.put("entity_revision", entityRevision);
            values.put("operation", operation);
            values.put("payload", payload.toString());
            values.put("created_at", obtenerFechaActual());

            long resultado = db.insert("sync_changes", null, values);

            if (resultado == -1) {
                throw new IllegalStateException(
                        "No se pudo registrar el cambio de sincronización."
                );
            }

        } catch (JSONException e) {
            throw new IllegalStateException(
                    "Error al generar el JSON del cambio de sincronización.",
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // OBTENER REVISIÓN
    // ---------------------------------------------------------

    private int obtenerRevision(SQLiteDatabase db, String id) {

        try (Cursor cursor = db.query(
                TABLA,
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

            return -1;
        }
    }

    // ---------------------------------------------------------
    // OBTENER OWNER ID
    // ---------------------------------------------------------

    private String obtenerOwnerId(SQLiteDatabase db, String id) {

        try (Cursor cursor = db.query(
                TABLA,
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

            return null;
        }
    }

    // ---------------------------------------------------------
    // FECHA ACTUAL UTC
    // ---------------------------------------------------------

    private String obtenerFechaActual() {

        SimpleDateFormat formato = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.US
        );

        formato.setTimeZone(TimeZone.getTimeZone("UTC"));

        return formato.format(new Date());
    }

    // ---------------------------------------------------------
    // CERRAR
    // ---------------------------------------------------------

    public void cerrar() {
        dbHelper.close();
    }
}