
package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Gasto;
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

public class GastoRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public GastoRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Gasto gasto) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (gasto.getId() == null || gasto.getId().isEmpty()) {
                gasto.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", gasto.getId());
            values.put("owner_id", gasto.getOwnerId());
            values.put("fecha", gasto.getFecha());
            values.put("descripcion", gasto.getDescripcion());
            values.put("clasificacion_id", gasto.getClasificacionId());
            values.put("importe", gasto.getImporte());
            values.put("estado_especial", gasto.getEstadoEspecial());
            values.put("revision", revision);

            db.insertOrThrow("gastos", null, values);

            registrarCambio(
                    db,
                    gasto.getOwnerId(),
                    gasto.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            gasto.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(Gasto gasto) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, gasto.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("fecha", gasto.getFecha());
            values.put("descripcion", gasto.getDescripcion());
            values.put("clasificacion_id", gasto.getClasificacionId());
            values.put("importe", gasto.getImporte());
            values.put("estado_especial", gasto.getEstadoEspecial());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "gastos",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{gasto.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, gasto.getId()),
                    gasto.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            gasto.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public Gasto obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "gastos",
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

    public List<Gasto> obtenerTodos() {
        List<Gasto> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "gastos",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha DESC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Gasto> obtenerPorClasificacion(String clasificacionId) {
        List<Gasto> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "gastos",
                null,
                "clasificacion_id = ? AND deleted_at IS NULL",
                new String[]{clasificacionId},
                null,
                null,
                "fecha DESC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Gasto> obtenerPorEstadoEspecial(String estadoEspecial) {
        List<Gasto> registros = new ArrayList<>();
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
                "gastos",
                null,
                seleccion,
                argumentos,
                null,
                null,
                "fecha DESC"
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
                    "gastos",
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

    private Gasto convertirCursor(Cursor cursor) {
        Gasto gasto = new Gasto();

        gasto.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        gasto.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        gasto.setFecha(cursor.getString(cursor.getColumnIndexOrThrow("fecha")));
        gasto.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow("descripcion")));
        gasto.setClasificacionId(cursor.getString(cursor.getColumnIndexOrThrow("clasificacion_id")));
        gasto.setImporte(cursor.getDouble(cursor.getColumnIndexOrThrow("importe")));
        gasto.setEstadoEspecial(cursor.getString(cursor.getColumnIndexOrThrow("estado_especial")));
        gasto.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        gasto.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        gasto.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        gasto.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return gasto;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "gastos",
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
                "gastos",
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
                    "gastos",
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
            values.put("entity_type", "gastos");
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
