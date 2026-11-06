package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Persona;
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

public class PersonaRepository {

    private static final String TABLE = "personas";
    private static final String ENTITY_TYPE = "personas";

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public PersonaRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    // INSERTAR

    public boolean insertar(Persona persona) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String id = persona.getId();

        if (id == null || id.trim().isEmpty()) {
            id = UuidGenerator.generate();
            persona.setId(id);
        }

        ContentValues values = new ContentValues();

        values.put("id", id);
        values.put("owner_id", persona.getOwnerId());
        values.put("nombre", persona.getNombre());
        values.put("otro_dato", persona.getOtroDato());
        values.put("revision", 1);

        db.beginTransaction();

        try {

            db.insertOrThrow(TABLE, null, values);

            registrarCambio(
                    db,
                    persona.getOwnerId(),
                    id,
                    0,
                    1,
                    "UPSERT"
            );

            db.setTransactionSuccessful();

            persona.setRevision(1);

            return true;

        } finally {
            db.endTransaction();
        }
    }

    // ACTUALIZAR

    public boolean actualizar(Persona persona) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.beginTransaction();

        try {

            int revisionActual = obtenerRevision(db, persona.getId());

            if (revisionActual < 1) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;
            String ahora = obtenerFechaActual();

            ContentValues values = new ContentValues();

            values.put("nombre", persona.getNombre());
            values.put("otro_dato", persona.getOtroDato());
            values.put("updated_at", ahora);
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    TABLE,
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{persona.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    persona.getOwnerId(),
                    persona.getId(),
                    revisionActual,
                    nuevaRevision,
                    "UPSERT"
            );

            db.setTransactionSuccessful();

            persona.setRevision(nuevaRevision);
            persona.setUpdatedAt(ahora);

            return true;

        } finally {
            db.endTransaction();
        }
    }

    // CONSULTAR POR ID

    public Persona obtenerPorId(String id) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE,
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

    // CONSULTAR TODOS

    public List<Persona> obtenerTodos() {

        List<Persona> personas = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE,
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "nombre COLLATE NOCASE ASC"
        )) {

            while (cursor.moveToNext()) {
                personas.add(convertirCursor(cursor));
            }
        }

        return personas;
    }

    // ELIMINACIÓN LÓGICA

    public boolean eliminar(String id) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.beginTransaction();

        try {

            int revisionActual = obtenerRevision(db, id);

            if (revisionActual < 1) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;
            String ahora = obtenerFechaActual();

            ContentValues values = new ContentValues();

            values.put("deleted_at", ahora);
            values.put("updated_at", ahora);
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    TABLE,
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

    // CONVERSIÓN CURSOR A PERSONA

    private Persona convertirCursor(Cursor cursor) {

        Persona persona = new Persona();

        persona.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        persona.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        persona.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
        persona.setOtroDato(cursor.getString(cursor.getColumnIndexOrThrow("otro_dato")));

        persona.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        persona.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));

        persona.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));

        persona.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return persona;
    }

    // OBTENER REVISIÓN ACTUAL

    private int obtenerRevision(SQLiteDatabase db, String id) {

        try (Cursor cursor = db.query(
                TABLE,
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

    // OBTENER OWNER

    private String obtenerOwnerId(SQLiteDatabase db, String id) {

        try (Cursor cursor = db.query(
                TABLE,
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

    // REGISTRAR CAMBIO PARA SINCRONIZACIÓN

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

            Cursor cursor = db.query(
                    "personas",
                    null,
                    "id = ?",
                    new String[]{entityId},
                    null,
                    null,
                    null
            );

            try {
                if (!cursor.moveToFirst()) {
                    throw new IllegalStateException(
                            "No se encontró el registro para generar el payload."
                    );
                }

                payload = JsonCursorHelper.toJsonObject(cursor);
            } finally {
                cursor.close();
            }

            ContentValues values = new ContentValues();
            values.put("change_id", UuidGenerator.generate());
            values.put("owner_id", ownerId);
            values.put("device_id", deviceId);
            values.put("entity_type", "personas");
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

    // FECHA ACTUAL UTC

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