package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Freelance;
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

public class FreelanceRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public FreelanceRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Freelance freelance) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (freelance.getId() == null || freelance.getId().isEmpty()) {
                freelance.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", freelance.getId());
            values.put("owner_id", freelance.getOwnerId());
            values.put("valor", freelance.getValor());
            values.put("fecha_inicio", freelance.getFechaInicio());
            values.put("hora_inicio", freelance.getHoraInicio());
            values.put("fecha_finalizacion", freelance.getFechaFinalizacion());
            values.put("hora_finalizacion", freelance.getHoraFinalizacion());
            values.put("actividad", freelance.getActividad());
            values.put("cliente_id", freelance.getClienteId());
            values.put("direccion_id", freelance.getDireccionId());
            values.put("clasificacion", freelance.getClasificacion());
            values.put("estado_especial", freelance.getEstadoEspecial());
            values.put("observacion", freelance.getObservacion());
            values.put("revision", revision);

            db.insertOrThrow("freelance", null, values);

            registrarCambio(
                    db,
                    freelance.getOwnerId(),
                    freelance.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            freelance.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(Freelance freelance) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, freelance.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("valor", freelance.getValor());
            values.put("fecha_inicio", freelance.getFechaInicio());
            values.put("hora_inicio", freelance.getHoraInicio());
            values.put("fecha_finalizacion", freelance.getFechaFinalizacion());
            values.put("hora_finalizacion", freelance.getHoraFinalizacion());
            values.put("actividad", freelance.getActividad());
            values.put("cliente_id", freelance.getClienteId());
            values.put("direccion_id", freelance.getDireccionId());
            values.put("clasificacion", freelance.getClasificacion());
            values.put("estado_especial", freelance.getEstadoEspecial());
            values.put("observacion", freelance.getObservacion());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "freelance",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{freelance.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, freelance.getId()),
                    freelance.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            freelance.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public Freelance obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "freelance",
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

    public List<Freelance> obtenerTodos() {
        List<Freelance> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "freelance",
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha_inicio ASC, hora_inicio ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Freelance> obtenerPorCliente(String clienteId) {
        List<Freelance> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "freelance",
                null,
                "cliente_id = ? AND deleted_at IS NULL",
                new String[]{clienteId},
                null,
                null,
                "fecha_inicio ASC, hora_inicio ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Freelance> obtenerPorDireccion(String direccionId) {
        List<Freelance> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "freelance",
                null,
                "direccion_id = ? AND deleted_at IS NULL",
                new String[]{direccionId},
                null,
                null,
                "fecha_inicio ASC, hora_inicio ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Freelance> obtenerPorEstadoEspecial(String estadoEspecial) {
        List<Freelance> registros = new ArrayList<>();
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
                "freelance",
                null,
                seleccion,
                argumentos,
                null,
                null,
                "fecha_inicio ASC, hora_inicio ASC"
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
                    "freelance",
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

    private Freelance convertirCursor(Cursor cursor) {
        Freelance freelance = new Freelance();

        freelance.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        freelance.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        freelance.setValor(
                cursor.isNull(cursor.getColumnIndexOrThrow("valor"))
                        ? null
                        : cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
        );
        freelance.setFechaInicio(cursor.getString(cursor.getColumnIndexOrThrow("fecha_inicio")));
        freelance.setHoraInicio(cursor.getString(cursor.getColumnIndexOrThrow("hora_inicio")));
        freelance.setFechaFinalizacion(cursor.getString(cursor.getColumnIndexOrThrow("fecha_finalizacion")));
        freelance.setHoraFinalizacion(cursor.getString(cursor.getColumnIndexOrThrow("hora_finalizacion")));
        freelance.setActividad(cursor.getString(cursor.getColumnIndexOrThrow("actividad")));
        freelance.setClienteId(cursor.getString(cursor.getColumnIndexOrThrow("cliente_id")));
        freelance.setDireccionId(cursor.getString(cursor.getColumnIndexOrThrow("direccion_id")));
        freelance.setClasificacion(cursor.getString(cursor.getColumnIndexOrThrow("clasificacion")));
        freelance.setEstadoEspecial(cursor.getString(cursor.getColumnIndexOrThrow("estado_especial")));
        freelance.setObservacion(cursor.getString(cursor.getColumnIndexOrThrow("observacion")));
        freelance.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        freelance.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        freelance.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        freelance.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return freelance;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "freelance",
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
                "freelance",
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
                    "freelance",
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
            values.put("entity_type", "freelance");
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
