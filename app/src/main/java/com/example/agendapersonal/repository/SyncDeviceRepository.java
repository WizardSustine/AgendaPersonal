package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.SyncDevice;
import com.example.agendapersonal.util.UuidGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SyncDeviceRepository {

    private static final String TABLA = "sync_devices";

    private final DatabaseHelper dbHelper;

    public SyncDeviceRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // ---------------------------------------------------------
    // INSERTAR DISPOSITIVO
    // ---------------------------------------------------------

    public boolean insertar(SyncDevice dispositivo) {

        if (dispositivo.getDeviceId() == null
                || dispositivo.getDeviceId().trim().isEmpty()) {
            dispositivo.setDeviceId(UuidGenerator.generate());
        }

        String fechaActual = obtenerFechaActual();

        dispositivo.setCreatedAt(fechaActual);
        dispositivo.setLastSeenAt(fechaActual);

        ContentValues values = new ContentValues();
        values.put("device_id", dispositivo.getDeviceId());
        values.put("owner_id", dispositivo.getOwnerId());
        values.put("nombre", dispositivo.getNombre());
        values.put("created_at", dispositivo.getCreatedAt());
        values.put("last_seen_at", dispositivo.getLastSeenAt());

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long resultado = db.insert(TABLA, null, values);

        return resultado != -1;
    }

    // ---------------------------------------------------------
    // ACTUALIZAR DISPOSITIVO
    // ---------------------------------------------------------

    public boolean actualizar(SyncDevice dispositivo) {

        if (dispositivo.getDeviceId() == null
                || dispositivo.getDeviceId().trim().isEmpty()) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("owner_id", dispositivo.getOwnerId());
        values.put("nombre", dispositivo.getNombre());

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.update(
                TABLA,
                values,
                "device_id = ?",
                new String[]{dispositivo.getDeviceId()}
        );

        return filas > 0;
    }

    // ---------------------------------------------------------
    // REGISTRAR ACTIVIDAD DEL DISPOSITIVO
    // ---------------------------------------------------------

    public boolean registrarActividad(String deviceId) {

        if (deviceId == null || deviceId.trim().isEmpty()) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("last_seen_at", obtenerFechaActual());

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.update(
                TABLA,
                values,
                "device_id = ?",
                new String[]{deviceId}
        );

        return filas > 0;
    }

    // ---------------------------------------------------------
    // OBTENER POR ID
    // ---------------------------------------------------------

    public SyncDevice obtenerPorId(String deviceId) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "device_id = ?",
                new String[]{deviceId},
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

    public List<SyncDevice> obtenerTodos() {

        List<SyncDevice> dispositivos = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                null,
                null,
                null,
                null,
                "last_seen_at DESC"
        )) {
            while (cursor.moveToNext()) {
                dispositivos.add(convertirCursor(cursor));
            }
        }

        return dispositivos;
    }

    // ---------------------------------------------------------
    // OBTENER POR OWNER
    // ---------------------------------------------------------

    public List<SyncDevice> obtenerPorOwner(String ownerId) {

        List<SyncDevice> dispositivos = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "owner_id = ?",
                new String[]{ownerId},
                null,
                null,
                "last_seen_at DESC"
        )) {
            while (cursor.moveToNext()) {
                dispositivos.add(convertirCursor(cursor));
            }
        }

        return dispositivos;
    }

    // ---------------------------------------------------------
    // VERIFICAR EXISTENCIA
    // ---------------------------------------------------------

    public boolean existe(String deviceId) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                new String[]{"device_id"},
                "device_id = ?",
                new String[]{deviceId},
                null,
                null,
                null,
                "1"
        )) {
            return cursor.moveToFirst();
        }
    }

    // ---------------------------------------------------------
    // ELIMINAR DISPOSITIVO
    // ---------------------------------------------------------

    public boolean eliminar(String deviceId) {

        if (deviceId == null || deviceId.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.delete(
                TABLA,
                "device_id = ?",
                new String[]{deviceId}
        );

        return filas > 0;
    }

    // ---------------------------------------------------------
    // CONVERTIR CURSOR A MODELO
    // ---------------------------------------------------------

    private SyncDevice convertirCursor(Cursor cursor) {

        SyncDevice dispositivo = new SyncDevice();

        dispositivo.setDeviceId(cursor.getString(
                cursor.getColumnIndexOrThrow("device_id")
        ));

        dispositivo.setOwnerId(cursor.getString(
                cursor.getColumnIndexOrThrow("owner_id")
        ));

        dispositivo.setNombre(cursor.getString(
                cursor.getColumnIndexOrThrow("nombre")
        ));

        dispositivo.setCreatedAt(cursor.getString(
                cursor.getColumnIndexOrThrow("created_at")
        ));

        dispositivo.setLastSeenAt(cursor.getString(
                cursor.getColumnIndexOrThrow("last_seen_at")
        ));

        return dispositivo;
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