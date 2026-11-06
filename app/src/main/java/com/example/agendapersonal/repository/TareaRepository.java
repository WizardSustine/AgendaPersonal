package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Tarea;
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

public class TareaRepository {

    private static final String TABLA = "tareas";
    private static final String TIPO_ENTIDAD = "tareas";

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public TareaRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Tarea tarea) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        if (tarea.getId() == null || tarea.getId().trim().isEmpty()) {
            tarea.setId(UuidGenerator.generate());
        }

        String fechaActual = obtenerFechaActual();

        tarea.setCreatedAt(fechaActual);
        tarea.setUpdatedAt(fechaActual);
        tarea.setRevision(1);
        tarea.setDeletedAt(null);

        ContentValues values = obtenerValores(tarea);
        values.put("id", tarea.getId());
        values.put("owner_id", tarea.getOwnerId());
        values.put("created_at", fechaActual);
        values.put("updated_at", fechaActual);
        values.put("revision", 1);
        values.putNull("deleted_at");

        db.beginTransaction();

        try {
            db.insertOrThrow(TABLA, null, values);

            registrarCambio(
                    db,
                    tarea.getOwnerId(),
                    tarea.getId(),
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

    public boolean actualizar(Tarea tarea) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, tarea.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;
            String fechaActual = obtenerFechaActual();

            tarea.setRevision(nuevaRevision);
            tarea.setUpdatedAt(fechaActual);
            tarea.setDeletedAt(null);

            ContentValues values = obtenerValores(tarea);
            values.put("updated_at", fechaActual);
            values.put("revision", nuevaRevision);
            values.putNull("deleted_at");

            int filas = db.update(
                    TABLA,
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{tarea.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, tarea.getId()),
                    tarea.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
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
            String ownerId = obtenerOwnerId(db, id);

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

            registrarCambio(
                    db,
                    ownerId,
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

    public Tarea obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

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
        }

        return null;
    }

    public List<Tarea> obtenerTodas() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Tarea> tareas = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha_vencimiento ASC, hora_vencimiento ASC"
        )) {
            while (cursor.moveToNext()) {
                tareas.add(convertirCursor(cursor));
            }
        }

        return tareas;
    }

    public List<Tarea> obtenerPorCategoria(String categoria) {
        return obtenerPorCampo("categoria", categoria);
    }

    public List<Tarea> obtenerPorPrioridad(String prioridad) {
        return obtenerPorCampo("prioridad", prioridad);
    }

    public List<Tarea> obtenerPorEstadoEspecial(String estado) {
        return obtenerPorCampo("estado_especial", estado);
    }

    public List<Tarea> obtenerPorFechaVencimiento(String fecha) {
        return obtenerPorCampo("fecha_vencimiento", fecha);
    }

    public List<Tarea> obtenerEntreFechasVencimiento(
            String fechaDesde,
            String fechaHasta) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Tarea> tareas = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "deleted_at IS NULL AND fecha_vencimiento >= ? "
                        + "AND fecha_vencimiento <= ?",
                new String[]{fechaDesde, fechaHasta},
                null,
                null,
                "fecha_vencimiento ASC, hora_vencimiento ASC"
        )) {
            while (cursor.moveToNext()) {
                tareas.add(convertirCursor(cursor));
            }
        }

        return tareas;
    }

    public List<Tarea> obtenerPendientes() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Tarea> tareas = new ArrayList<>();

        String seleccion =
                "deleted_at IS NULL AND "
                        + "(estado_especial IS NULL OR "
                        + "estado_especial NOT IN ('FINALIZADO', "
                        + "'CANCELADO', 'POSTERGADO'))";

        try (Cursor cursor = db.query(
                TABLA,
                null,
                seleccion,
                null,
                null,
                null,
                "fecha_vencimiento ASC, hora_vencimiento ASC"
        )) {
            while (cursor.moveToNext()) {
                tareas.add(convertirCursor(cursor));
            }
        }

        return tareas;
    }

    private List<Tarea> obtenerPorCampo(String campo, String valor) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Tarea> tareas = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "deleted_at IS NULL AND " + campo + " = ?",
                new String[]{valor},
                null,
                null,
                "fecha_vencimiento ASC, hora_vencimiento ASC"
        )) {
            while (cursor.moveToNext()) {
                tareas.add(convertirCursor(cursor));
            }
        }

        return tareas;
    }

    private ContentValues obtenerValores(Tarea tarea) {
        ContentValues values = new ContentValues();

        values.put("descripcion", tarea.getDescripcion());
        values.put("fecha_inicio", tarea.getFechaInicio());
        values.put("hora_inicio", tarea.getHoraInicio());
        values.put("fecha_vencimiento", tarea.getFechaVencimiento());
        values.put("hora_vencimiento", tarea.getHoraVencimiento());
        values.put("categoria", tarea.getCategoria());
        values.put("prioridad", tarea.getPrioridad());
        values.put("estado_especial", tarea.getEstadoEspecial());
        values.put("alarma_activada", tarea.isAlarmaActivada() ? 1 : 0);

        if (tarea.getAnticipacionMinutos() != null) {
            values.put(
                    "anticipacion_minutos",
                    tarea.getAnticipacionMinutos()
            );
        } else {
            values.putNull("anticipacion_minutos");
        }

        values.put("observaciones", tarea.getObservaciones());

        return values;
    }

    private Tarea convertirCursor(Cursor cursor) {
        Tarea tarea = new Tarea();

        tarea.setId(cursor.getString(
                cursor.getColumnIndexOrThrow("id")));

        tarea.setOwnerId(cursor.getString(
                cursor.getColumnIndexOrThrow("owner_id")));

        tarea.setDescripcion(cursor.getString(
                cursor.getColumnIndexOrThrow("descripcion")));

        tarea.setFechaInicio(cursor.getString(
                cursor.getColumnIndexOrThrow("fecha_inicio")));

        tarea.setHoraInicio(cursor.getString(
                cursor.getColumnIndexOrThrow("hora_inicio")));

        tarea.setFechaVencimiento(cursor.getString(
                cursor.getColumnIndexOrThrow("fecha_vencimiento")));

        tarea.setHoraVencimiento(cursor.getString(
                cursor.getColumnIndexOrThrow("hora_vencimiento")));

        tarea.setCategoria(cursor.getString(
                cursor.getColumnIndexOrThrow("categoria")));

        tarea.setPrioridad(cursor.getString(
                cursor.getColumnIndexOrThrow("prioridad")));

        tarea.setEstadoEspecial(cursor.getString(
                cursor.getColumnIndexOrThrow("estado_especial")));

        tarea.setAlarmaActivada(
                cursor.getInt(
                        cursor.getColumnIndexOrThrow("alarma_activada")
                ) == 1
        );

        int indiceAnticipacion =
                cursor.getColumnIndexOrThrow("anticipacion_minutos");

        tarea.setAnticipacionMinutos(
                cursor.isNull(indiceAnticipacion)
                        ? null
                        : cursor.getInt(indiceAnticipacion)
        );

        tarea.setObservaciones(cursor.getString(
                cursor.getColumnIndexOrThrow("observaciones")));

        tarea.setCreatedAt(cursor.getString(
                cursor.getColumnIndexOrThrow("created_at")));

        tarea.setUpdatedAt(cursor.getString(
                cursor.getColumnIndexOrThrow("updated_at")));

        tarea.setRevision(cursor.getInt(
                cursor.getColumnIndexOrThrow("revision")));

        tarea.setDeletedAt(cursor.getString(
                cursor.getColumnIndexOrThrow("deleted_at")));

        return tarea;
    }

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
        }

        return -1;
    }

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
        }

        return null;
    }

    private void registrarCambio(
            SQLiteDatabase db,
            String ownerId,
            String entityId,
            int baseRevision,
            int entityRevision,
            String operation) {

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
                            "No se encontró el registro para generar el payload."
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

            db.insertOrThrow("sync_changes", null, values);

        } catch (JSONException e) {
            throw new IllegalStateException(
                    "Error al generar el payload de la tarea.",
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
