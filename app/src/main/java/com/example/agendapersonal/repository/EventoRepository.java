package com.example.agendapersonal.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.agendapersonal.data.DatabaseHelper;
import com.example.agendapersonal.model.Evento;
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

public class EventoRepository {

    private static final String TABLA = "eventos";
    private static final String TIPO_ENTIDAD = "eventos";

    private final DatabaseHelper databaseHelper;
    private final String deviceId;

    public EventoRepository(Context context, String deviceId) {
        this.databaseHelper = new DatabaseHelper(context);
        this.deviceId = deviceId;
    }

    public boolean insertar(Evento evento) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        if (evento.getId() == null || evento.getId().trim().isEmpty()) {
            evento.setId(UuidGenerator.generate());
        }

        String fechaActual = obtenerFechaActual();

        evento.setCreatedAt(fechaActual);
        evento.setUpdatedAt(fechaActual);
        evento.setRevision(1);
        evento.setDeletedAt(null);

        ContentValues values = obtenerValores(evento);
        values.put("id", evento.getId());
        values.put("owner_id", evento.getOwnerId());
        values.put("created_at", fechaActual);
        values.put("updated_at", fechaActual);
        values.put("revision", 1);
        values.putNull("deleted_at");

        db.beginTransaction();

        try {
            db.insertOrThrow(TABLA, null, values);

            registrarCambio(
                    db,
                    evento.getOwnerId(),
                    evento.getId(),
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

    public boolean actualizar(Evento evento) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.beginTransaction();

        try {
            int revisionActual = obtenerRevision(db, evento.getId());

            if (revisionActual < 0) {
                return false;
            }

            int nuevaRevision = revisionActual + 1;
            String fechaActual = obtenerFechaActual();

            evento.setRevision(nuevaRevision);
            evento.setUpdatedAt(fechaActual);
            evento.setDeletedAt(null);

            ContentValues values = obtenerValores(evento);
            values.put("updated_at", fechaActual);
            values.put("revision", nuevaRevision);
            values.putNull("deleted_at");

            int filas = db.update(
                    TABLA,
                    values,
                    "id = ? AND deleted_at IS NULL",
                    new String[]{evento.getId()}
            );

            if (filas == 0) {
                return false;
            }

            registrarCambio(
                    db,
                    obtenerOwnerId(db, evento.getId()),
                    evento.getId(),
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

    public Evento obtenerPorId(String id) {
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

    public List<Evento> obtenerTodos() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Evento> eventos = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "deleted_at IS NULL",
                null,
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                eventos.add(convertirCursor(cursor));
            }
        }

        return eventos;
    }

    public List<Evento> obtenerPorCategoria(String categoria) {
        return obtenerPorCampo("categoria", categoria);
    }

    public List<Evento> obtenerPorEstadoEspecial(String estado) {
        return obtenerPorCampo("estado_especial", estado);
    }

    public List<Evento> obtenerPorPersona(String personaId) {
        return obtenerPorCampo("persona_id", personaId);
    }

    public List<Evento> obtenerPorFecha(String fecha) {
        return obtenerPorCampo("fecha", fecha);
    }

    public List<Evento> obtenerEntreFechas(
            String fechaDesde,
            String fechaHasta) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Evento> eventos = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "deleted_at IS NULL AND fecha >= ? AND fecha <= ?",
                new String[]{fechaDesde, fechaHasta},
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                eventos.add(convertirCursor(cursor));
            }
        }

        return eventos;
    }

    private List<Evento> obtenerPorCampo(String campo, String valor) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Evento> eventos = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLA,
                null,
                "deleted_at IS NULL AND " + campo + " = ?",
                new String[]{valor},
                null,
                null,
                "fecha ASC, hora ASC"
        )) {
            while (cursor.moveToNext()) {
                eventos.add(convertirCursor(cursor));
            }
        }

        return eventos;
    }

    private ContentValues obtenerValores(Evento evento) {
        ContentValues values = new ContentValues();

        values.put("descripcion", evento.getDescripcion());
        values.put("categoria", evento.getCategoria());
        values.put("fecha", evento.getFecha());
        values.put("hora", evento.getHora());
        values.put("persona_id", evento.getPersonaId());
        values.put("lugar", evento.getLugar());
        values.put("estado_especial", evento.getEstadoEspecial());
        values.put("alarma_activada", evento.isAlarmaActivada() ? 1 : 0);

        if (evento.getAnticipacionMinutos() != null) {
            values.put("anticipacion_minutos",
                    evento.getAnticipacionMinutos());
        } else {
            values.putNull("anticipacion_minutos");
        }

        values.put("serie_id", evento.getSerieId());
        values.put("observaciones", evento.getObservaciones());

        return values;
    }

    private Evento convertirCursor(Cursor cursor) {
        Evento evento = new Evento();

        evento.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        evento.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow("owner_id")));
        evento.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow("descripcion")));
        evento.setCategoria(cursor.getString(cursor.getColumnIndexOrThrow("categoria")));
        evento.setFecha(cursor.getString(cursor.getColumnIndexOrThrow("fecha")));
        evento.setHora(cursor.getString(cursor.getColumnIndexOrThrow("hora")));
        evento.setPersonaId(cursor.getString(cursor.getColumnIndexOrThrow("persona_id")));
        evento.setLugar(cursor.getString(cursor.getColumnIndexOrThrow("lugar")));
        evento.setEstadoEspecial(cursor.getString(cursor.getColumnIndexOrThrow("estado_especial")));
        evento.setAlarmaActivada(
                cursor.getInt(cursor.getColumnIndexOrThrow("alarma_activada")) == 1
        );

        int indiceAnticipacion =
                cursor.getColumnIndexOrThrow("anticipacion_minutos");

        evento.setAnticipacionMinutos(
                cursor.isNull(indiceAnticipacion)
                        ? null
                        : cursor.getInt(indiceAnticipacion)
        );

        evento.setSerieId(cursor.getString(cursor.getColumnIndexOrThrow("serie_id")));
        evento.setObservaciones(cursor.getString(cursor.getColumnIndexOrThrow("observaciones")));
        evento.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        evento.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        evento.setRevision(cursor.getInt(cursor.getColumnIndexOrThrow("revision")));
        evento.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow("deleted_at")));

        return evento;
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

                payload = JsonCursorHelper.toJsonObject(cursor);org.json.JSONObject indice=com.example.agendapersonal.eventos.SeriesEvento.metadatos(db,entityId);if(indice!=null)payload.put(com.example.agendapersonal.eventos.SeriesEvento.META,indice);
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
                    "Error al generar el payload del evento.",
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