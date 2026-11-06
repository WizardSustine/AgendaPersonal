package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.SyncChange;
import com.example.agendapersonal.util.UuidGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SyncChangeRepository {

    private static final String TABLA = "sync_changes";
    private static final String TABLA_RECIBOS = "sync_receipts";

    private final DatabaseHelper dbHelper;

    public SyncChangeRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // ---------------------------------------------------------
    // INSERTAR
    // ---------------------------------------------------------

    public boolean insertar(SyncChange cambio) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String changeId = cambio.getChangeId();

        if (changeId == null || changeId.trim().isEmpty()) {
            changeId = UuidGenerator.generate();
            cambio.setChangeId(changeId);
        }

        if (cambio.getCreatedAt() == null
                || cambio.getCreatedAt().trim().isEmpty()) {
            cambio.setCreatedAt(obtenerFechaActual());
        }

        ContentValues values = new ContentValues();
        values.put("change_id", changeId);
        values.put("owner_id", cambio.getOwnerId());
        values.put("device_id", cambio.getDeviceId());
        values.put("entity_type", cambio.getEntityType());
        values.put("entity_id", cambio.getEntityId());
        values.put("base_revision", cambio.getBaseRevision());
        values.put("entity_revision", cambio.getEntityRevision());
        values.put("operation", cambio.getOperation());
        values.put("payload", cambio.getPayload());
        values.put("created_at", cambio.getCreatedAt());

        long resultado = db.insert(TABLA, null, values);

        return resultado != -1;
    }

    // ---------------------------------------------------------
    // OBTENER POR ID
    // ---------------------------------------------------------

    public SyncChange obtenerPorId(String changeId) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "change_id = ?",
                new String[]{changeId},
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

    public List<SyncChange> obtenerTodos() {

        List<SyncChange> cambios = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                null,
                null,
                null,
                null,
                "created_at ASC"
        )) {
            while (cursor.moveToNext()) {
                cambios.add(convertirCursor(cursor));
            }
        }

        return cambios;
    }

    // ---------------------------------------------------------
    // OBTENER CAMBIOS PENDIENTES PARA UN DISPOSITIVO RECEPTOR
    // ---------------------------------------------------------

    public List<SyncChange> obtenerPendientes(
            String ownerId,
            String deviceId
    ) {

        List<SyncChange> cambios = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT c.* " +
                        "FROM sync_changes c " +
                        "WHERE c.owner_id = ? " +
                        "AND c.device_id <> ? " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM sync_receipts r " +
                        "    WHERE r.device_id = ? " +
                        "    AND r.change_id = c.change_id " +
                        ") " +
                        "ORDER BY c.created_at ASC";

        try (Cursor cursor = db.rawQuery(
                sql,
                new String[]{ownerId, deviceId, deviceId}
        )) {
            while (cursor.moveToNext()) {
                cambios.add(convertirCursor(cursor));
            }
        }

        return cambios;
    }

    // ---------------------------------------------------------
    // OBTENER CAMBIOS DE UN DISPOSITIVO DE ORIGEN
    // ---------------------------------------------------------

    public List<SyncChange> obtenerPorDispositivo(String deviceId) {

        List<SyncChange> cambios = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "device_id = ?",
                new String[]{deviceId},
                null,
                null,
                "created_at ASC"
        )) {
            while (cursor.moveToNext()) {
                cambios.add(convertirCursor(cursor));
            }
        }

        return cambios;
    }

    // ---------------------------------------------------------
    // OBTENER CAMBIOS POR ENTIDAD
    // ---------------------------------------------------------

    public List<SyncChange> obtenerPorEntidad(
            String entityType,
            String entityId
    ) {

        List<SyncChange> cambios = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "entity_type = ? AND entity_id = ?",
                new String[]{entityType, entityId},
                null,
                null,
                "entity_revision ASC"
        )) {
            while (cursor.moveToNext()) {
                cambios.add(convertirCursor(cursor));
            }
        }

        return cambios;
    }

    // ---------------------------------------------------------
    // ELIMINAR UN CAMBIO Y SUS RECIBOS
    // ---------------------------------------------------------

    public boolean eliminar(String changeId) {

        if (changeId == null || changeId.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(
                    TABLA_RECIBOS,
                    "change_id = ?",
                    new String[]{changeId}
            );

            int filas = db.delete(
                    TABLA,
                    "change_id = ?",
                    new String[]{changeId}
            );

            db.setTransactionSuccessful();

            return filas > 0;

        } finally {
            db.endTransaction();
        }
    }

    // ---------------------------------------------------------
    // ELIMINAR CAMBIOS ORIGINADOS POR UN DISPOSITIVO
    // ---------------------------------------------------------

    public int eliminarPorDispositivo(String deviceId) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            String condicion = "change_id IN (" +
                    "SELECT change_id FROM sync_changes WHERE device_id = ?" +
                    ")";

            db.delete(
                    TABLA_RECIBOS,
                    condicion,
                    new String[]{deviceId}
            );

            int filas = db.delete(
                    TABLA,
                    "device_id = ?",
                    new String[]{deviceId}
            );

            db.setTransactionSuccessful();

            return filas;

        } finally {
            db.endTransaction();
        }
    }

    // ---------------------------------------------------------
    // ELIMINAR TODOS LOS CAMBIOS DE UN OWNER
    // ---------------------------------------------------------

    public int eliminarPorOwner(String ownerId) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            String condicion = "change_id IN (" +
                    "SELECT change_id FROM sync_changes WHERE owner_id = ?" +
                    ")";

            db.delete(
                    TABLA_RECIBOS,
                    condicion,
                    new String[]{ownerId}
            );

            int filas = db.delete(
                    TABLA,
                    "owner_id = ?",
                    new String[]{ownerId}
            );

            db.setTransactionSuccessful();

            return filas;

        } finally {
            db.endTransaction();
        }
    }

    // ---------------------------------------------------------
    // CONVERTIR CURSOR A MODELO
    // ---------------------------------------------------------

    private SyncChange convertirCursor(Cursor cursor) {

        SyncChange cambio = new SyncChange();

        cambio.setChangeId(cursor.getString(
                cursor.getColumnIndexOrThrow("change_id")
        ));

        cambio.setOwnerId(cursor.getString(
                cursor.getColumnIndexOrThrow("owner_id")
        ));

        cambio.setDeviceId(cursor.getString(
                cursor.getColumnIndexOrThrow("device_id")
        ));

        cambio.setEntityType(cursor.getString(
                cursor.getColumnIndexOrThrow("entity_type")
        ));

        cambio.setEntityId(cursor.getString(
                cursor.getColumnIndexOrThrow("entity_id")
        ));

        cambio.setBaseRevision(cursor.getInt(
                cursor.getColumnIndexOrThrow("base_revision")
        ));

        cambio.setEntityRevision(cursor.getInt(
                cursor.getColumnIndexOrThrow("entity_revision")
        ));

        cambio.setOperation(cursor.getString(
                cursor.getColumnIndexOrThrow("operation")
        ));

        cambio.setPayload(cursor.getString(
                cursor.getColumnIndexOrThrow("payload")
        ));

        cambio.setCreatedAt(cursor.getString(
                cursor.getColumnIndexOrThrow("created_at")
        ));

        return cambio;
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