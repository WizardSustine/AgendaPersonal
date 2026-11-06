
package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Pago;
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

public class PagoRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public PagoRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Pago pago) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (pago.getId() == null || pago.getId().isEmpty()) {
                pago.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", pago.getId());
            values.put("owner_id", pago.getOwnerId());
            values.put("fecha", pago.getFecha());
            values.put("descripcion", pago.getDescripcion());
            values.put("clasificacion_id", pago.getClasificacionId());
            values.put("importe", pago.getImporte());
            values.put("periodicidad", pago.getPeriodicidad());
            values.put("estado_especial", pago.getEstadoEspecial());
            values.put("revision", revision);

            db.insertOrThrow("pagos", null, values);

            registrarCambio(
                    db,
                    pago.getOwnerId(),
                    pago.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            pago.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(Pago pago) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, pago.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("fecha", pago.getFecha());
            values.put("descripcion", pago.getDescripcion());
            values.put("clasificacion_id", pago.getClasificacionId());
            values.put("importe", pago.getImporte());
            values.put("periodicidad", pago.getPeriodicidad());
            values.put("estado_especial", pago.getEstadoEspecial());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "pagos",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{pago.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, pago.getId()),
                    pago.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            pago.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public Pago obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "pagos",
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

    public List<Pago> obtenerTodos() {
        List<Pago> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "pagos",
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

    public List<Pago> obtenerPorClasificacion(String clasificacionId) {
        List<Pago> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "pagos",
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

    public List<Pago> obtenerPorEstadoEspecial(String estadoEspecial) {
        List<Pago> registros = new ArrayList<>();
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
                "pagos",
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
                    "pagos",
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

    private Pago convertirCursor(Cursor cursor) {
        Pago pago = new Pago();

        pago.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        pago.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        pago.setFecha(cursor.getString(cursor.getColumnIndexOrThrow("fecha")));
        pago.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow("descripcion")));
        pago.setClasificacionId(cursor.getString(cursor.getColumnIndexOrThrow("clasificacion_id")));
        pago.setImporte(cursor.getDouble(cursor.getColumnIndexOrThrow("importe")));
        pago.setPeriodicidad(cursor.getString(cursor.getColumnIndexOrThrow("periodicidad")));
        pago.setEstadoEspecial(cursor.getString(cursor.getColumnIndexOrThrow("estado_especial")));
        pago.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        pago.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        pago.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        pago.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return pago;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "pagos",
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
                "pagos",
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
                    "pagos",
                    null,
                    "id = ?",
                    new String[]{entityId},
                    null,
                    null,
                    null
            )) {
                if (!cursor.moveToFirst())  {
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
            values.put("entity_type", "pagos");
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
