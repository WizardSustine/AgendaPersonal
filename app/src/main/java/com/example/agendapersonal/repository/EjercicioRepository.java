package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Ejercicio;
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

public class EjercicioRepository {

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public EjercicioRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Ejercicio ejercicio) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            if (ejercicio.getId() == null || ejercicio.getId().isEmpty()) {
                ejercicio.setId(UuidGenerator.generate());
            }

            int revision = 1;

            ContentValues values = new ContentValues();
            values.put("id", ejercicio.getId());
            values.put("owner_id", ejercicio.getOwnerId());
            values.put("fecha", ejercicio.getFecha());
            values.put("ejercicio", ejercicio.getEjercicio());
            values.put("tipo", ejercicio.getTipo());
            values.put("musculos_trabajados", ejercicio.getMusculosTrabajados());
            values.put("tiempo_segundos", ejercicio.getTiempoSegundos());
            values.put("distancia", ejercicio.getDistancia());
            values.put("cantidad", ejercicio.getCantidad());
            values.put("peso", ejercicio.getPeso());
            values.put("revision", revision);

            db.insertOrThrow("ejercicios", null, values);

            registrarCambio(
                    db,
                    ejercicio.getOwnerId(),
                    ejercicio.getId(),
                    0,
                    revision,
                    "UPSERT"
            );

            ejercicio.setRevision(revision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public boolean actualizar(Ejercicio ejercicio) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, ejercicio.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;

            ContentValues values = new ContentValues();
            values.put("fecha", ejercicio.getFecha());
            values.put("ejercicio", ejercicio.getEjercicio());
            values.put("tipo", ejercicio.getTipo());
            values.put("musculos_trabajados", ejercicio.getMusculosTrabajados());
            values.put("tiempo_segundos", ejercicio.getTiempoSegundos());
            values.put("distancia", ejercicio.getDistancia());
            values.put("cantidad", ejercicio.getCantidad());
            values.put("peso", ejercicio.getPeso());
            values.put("updated_at", obtenerFechaActual());
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    "ejercicios",
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{ejercicio.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, ejercicio.getId()),
                    ejercicio.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            ejercicio.setRevision(nuevaRevision);

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    public Ejercicio obtenerPorId(String id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "ejercicios",
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

    public List<Ejercicio> obtenerTodos() {
        List<Ejercicio> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "ejercicios",
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

    public List<Ejercicio> obtenerPorFecha(String fecha) {
        List<Ejercicio> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "ejercicios",
                null,
                "fecha = ? AND deleted_at IS NULL",
                new String[]{fecha},
                null,
                null,
                "id ASC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    public List<Ejercicio> obtenerPorTipo(String tipo) {
        List<Ejercicio> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "ejercicios",
                null,
                "tipo = ? AND deleted_at IS NULL",
                new String[]{tipo},
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

    public List<Ejercicio> obtenerPorMusculo(String musculo) {
        List<Ejercicio> registros = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "ejercicios",
                null,
                "musculos_trabajados LIKE ? AND deleted_at IS NULL",
                new String[]{"%" + musculo + "%"},
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
                    "ejercicios",
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

    private Ejercicio convertirCursor(Cursor cursor) {
        Ejercicio ejercicio = new Ejercicio();

        ejercicio.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        ejercicio.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        ejercicio.setFecha(cursor.getString(cursor.getColumnIndexOrThrow("fecha")));
        ejercicio.setEjercicio(cursor.getString(cursor.getColumnIndexOrThrow("ejercicio")));
        ejercicio.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
        ejercicio.setMusculosTrabajados(cursor.getString(cursor.getColumnIndexOrThrow("musculos_trabajados")));

        int indiceTiempo = cursor.getColumnIndexOrThrow("tiempo_segundos");
        if (!cursor.isNull(indiceTiempo)) {
            ejercicio.setTiempoSegundos(cursor.getInt(indiceTiempo));
        }

        int indiceDistancia = cursor.getColumnIndexOrThrow("distancia");
        if (!cursor.isNull(indiceDistancia)) {
            ejercicio.setDistancia(cursor.getDouble(indiceDistancia));
        }

        int indiceCantidad = cursor.getColumnIndexOrThrow("cantidad");
        if (!cursor.isNull(indiceCantidad)) {
            ejercicio.setCantidad(cursor.getInt(indiceCantidad));
        }

        int indicePeso = cursor.getColumnIndexOrThrow("peso");
        if (!cursor.isNull(indicePeso)) {
            ejercicio.setPeso(cursor.getDouble(indicePeso));
        }

        ejercicio.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        ejercicio.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        ejercicio.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        ejercicio.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return ejercicio;
    }

    private int obtenerRevision(SQLiteDatabase db, String id) {
        try (Cursor cursor = db.query(
                "ejercicios",
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
                "ejercicios",
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
                    "ejercicios",
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
            values.put("entity_type", "ejercicios");
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
