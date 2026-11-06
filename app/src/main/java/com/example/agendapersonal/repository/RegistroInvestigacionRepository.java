package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.RegistroInvestigacion;
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

public class RegistroInvestigacionRepository {

    private static final String TABLA = "registros_investigacion";
    private static final String TIPO_ENTIDAD = "registros_investigacion";

    private final DatabaseHelper dbHelper;
    private final String deviceId;

    public RegistroInvestigacionRepository(Context context, String deviceId) {
        this.dbHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    // ---------------------------------------------------------
    // INSERTAR
    // ---------------------------------------------------------

    public boolean insertar(RegistroInvestigacion registro) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            String id = registro.getId();

            if (id == null || id.trim().isEmpty()) {
                id = UuidGenerator.generate();
                registro.setId(id);
            }

            String fechaActual = obtenerFechaActual();

            registro.setCreatedAt(fechaActual);
            registro.setUpdatedAt(fechaActual);
            registro.setRevision(1);
            registro.setDeletedAt(null);

            if (registro.getDatosOriginales() == null
                    || registro.getDatosOriginales().trim().isEmpty()) {
                registro.setDatosOriginales("{}");
            }

            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("owner_id", registro.getOwnerId());
            values.put("id_original", registro.getIdOriginal());
            values.put("datos_originales", registro.getDatosOriginales());
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
                    registro.getOwnerId(),
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

    public boolean actualizar(RegistroInvestigacion registro) {

        if (registro.getId() == null || registro.getId().trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int revisionAnterior = obtenerRevision(db, registro.getId());

            if (revisionAnterior < 0) {
                return false;
            }

            int nuevaRevision = revisionAnterior + 1;
            String fechaActual = obtenerFechaActual();

            ContentValues values = new ContentValues();
            values.put("id_original", registro.getIdOriginal());
            values.put("datos_originales", registro.getDatosOriginales());
            values.put("updated_at", fechaActual);
            values.put("revision", nuevaRevision);

            int filas = db.update(
                    TABLA,
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{registro.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registro.setUpdatedAt(fechaActual);
            registro.setRevision(nuevaRevision);

            String ownerId = obtenerOwnerId(db, registro.getId());

            registrarCambio(
                    db,
                    registro.getId(),
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

    public RegistroInvestigacion obtenerPorId(String id) {

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

    public List<RegistroInvestigacion> obtenerTodos() {

        List<RegistroInvestigacion> registros = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "created_at DESC"
        )) {
            while (cursor.moveToNext()) {
                registros.add(convertirCursor(cursor));
            }
        }

        return registros;
    }

    // ---------------------------------------------------------
    // OBTENER POR ID ORIGINAL
    // ---------------------------------------------------------

    public RegistroInvestigacion obtenerPorIdOriginal(String idOriginal) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "id_original = ? AND deleted_at IS NULL",
                new String[]{idOriginal},
                null,
                null,
                "created_at DESC",
                "1"
        )) {
            if (cursor.moveToFirst()) {
                return convertirCursor(cursor);
            }

            return null;
        }
    }

    // ---------------------------------------------------------
    // CONVERTIR CURSOR A MODELO
    // ---------------------------------------------------------

    private RegistroInvestigacion convertirCursor(Cursor cursor) {

        RegistroInvestigacion registro = new RegistroInvestigacion();

        registro.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        registro.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        registro.setIdOriginal(cursor.getString(cursor.getColumnIndexOrThrow("id_original")));
        registro.setDatosOriginales(cursor.getString(cursor.getColumnIndexOrThrow("datos_originales")));
        registro.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        registro.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        registro.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));

        int deletedAtIndex = cursor.getColumnIndexOrThrow("deleted_at");

        registro.setDeletedAt(
                cursor.isNull(deletedAtIndex)
                        ? null
                        : cursor.getString(deletedAtIndex)
        );

        return registro;
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