package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.SyncReceipt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SyncReceiptRepository {

    private static final String TABLA = "sync_receipts";

    private final DatabaseHelper dbHelper;

    public SyncReceiptRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // ---------------------------------------------------------
    // INSERTAR (IDEMPOTENTE)
    // ---------------------------------------------------------

    public boolean insertar(SyncReceipt recibo) {

        if (recibo.getOwnerId() == null
                || recibo.getDeviceId() == null
                || recibo.getChangeId() == null) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String fechaActual = recibo.getReceivedAt();

        if (fechaActual == null || fechaActual.trim().isEmpty()) {
            fechaActual = obtenerFechaActual();
            recibo.setReceivedAt(fechaActual);
        }

        ContentValues values = new ContentValues();
        values.put("owner_id", recibo.getOwnerId());
        values.put("device_id", recibo.getDeviceId());
        values.put("change_id", recibo.getChangeId());
        values.put("received_at", fechaActual);

        long resultado = db.insertWithOnConflict(
                TABLA,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        // Si ya existía la combinación device_id + change_id,
        // SQLite ignora la inserción. Eso también se considera éxito.
        if (resultado != -1) {
            return true;
        }

        return existe(
                recibo.getDeviceId(),
                recibo.getChangeId()
        );
    }

    // ---------------------------------------------------------
    // REGISTRAR RECEPCIÓN
    // ---------------------------------------------------------

    public boolean registrarRecepcion(
            String ownerId,
            String deviceId,
            String changeId
    ) {

        SyncReceipt recibo = new SyncReceipt();

        recibo.setOwnerId(ownerId);
        recibo.setDeviceId(deviceId);
        recibo.setChangeId(changeId);
        recibo.setReceivedAt(obtenerFechaActual());

        return insertar(recibo);
    }

    // ---------------------------------------------------------
    // VERIFICAR EXISTENCIA
    // ---------------------------------------------------------

    public boolean existe(String deviceId, String changeId) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                new String[]{"change_id"},
                "device_id = ? AND change_id = ?",
                new String[]{deviceId, changeId},
                null,
                null,
                null,
                "1"
        )) {
            return cursor.moveToFirst();
        }
    }

    // ---------------------------------------------------------
    // OBTENER POR DISPOSITIVO Y CAMBIO
    // ---------------------------------------------------------

    public SyncReceipt obtenerPorDispositivoYCambio(
            String deviceId,
            String changeId
    ) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "device_id = ? AND change_id = ?",
                new String[]{deviceId, changeId},
                null,
                null,
                null,
                "1"
        )) {
            if (cursor.moveToFirst()) {
                return convertirCursor(cursor);
            }

            return null;
        }
    }

    // ---------------------------------------------------------
    // OBTENER RECIBOS DE UN DISPOSITIVO
    // ---------------------------------------------------------

    public List<SyncReceipt> obtenerPorDispositivo(String deviceId) {

        List<SyncReceipt> recibos = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "device_id = ?",
                new String[]{deviceId},
                null,
                null,
                "received_at ASC"
        )) {
            while (cursor.moveToNext()) {
                recibos.add(convertirCursor(cursor));
            }
        }

        return recibos;
    }

    // ---------------------------------------------------------
    // OBTENER RECIBOS DE UN CAMBIO
    // ---------------------------------------------------------

    public List<SyncReceipt> obtenerPorCambio(String changeId) {

        List<SyncReceipt> recibos = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "change_id = ?",
                new String[]{changeId},
                null,
                null,
                "received_at ASC"
        )) {
            while (cursor.moveToNext()) {
                recibos.add(convertirCursor(cursor));
            }
        }

        return recibos;
    }

    // ---------------------------------------------------------
    // OBTENER TODOS
    // ---------------------------------------------------------

    public List<SyncReceipt> obtenerTodos() {

        List<SyncReceipt> recibos = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                null,
                null,
                null,
                null,
                "received_at DESC"
        )) {
            while (cursor.moveToNext()) {
                recibos.add(convertirCursor(cursor));
            }
        }

        return recibos;
    }

    // ---------------------------------------------------------
    // ELIMINAR UN RECIBO
    // ---------------------------------------------------------

    public boolean eliminar(String deviceId, String changeId) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.delete(
                TABLA,
                "device_id = ? AND change_id = ?",
                new String[]{deviceId, changeId}
        );

        return filas > 0;
    }

    // ---------------------------------------------------------
    // ELIMINAR RECIBOS DE UN DISPOSITIVO
    // ---------------------------------------------------------

    public int eliminarPorDispositivo(String deviceId) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db.delete(
                TABLA,
                "device_id = ?",
                new String[]{deviceId}
        );
    }

    // ---------------------------------------------------------
    // ELIMINAR RECIBOS DE UN CAMBIO
    // ---------------------------------------------------------

    public int eliminarPorCambio(String changeId) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db.delete(
                TABLA,
                "change_id = ?",
                new String[]{changeId}
        );
    }

    // ---------------------------------------------------------
    // CONVERTIR CURSOR A MODELO
    // ---------------------------------------------------------

    private SyncReceipt convertirCursor(Cursor cursor) {

        SyncReceipt recibo = new SyncReceipt();

        recibo.setOwnerId(cursor.getString(
                cursor.getColumnIndexOrThrow("owner_id")
        ));

        recibo.setDeviceId(cursor.getString(
                cursor.getColumnIndexOrThrow("device_id")
        ));

        recibo.setChangeId(cursor.getString(
                cursor.getColumnIndexOrThrow("change_id")
        ));

        recibo.setReceivedAt(cursor.getString(
                cursor.getColumnIndexOrThrow("received_at")
        ));

        return recibo;
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