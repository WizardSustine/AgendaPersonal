package com.example.agendapersonal.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Base SQLite local. Los UUID se generan en Java; owner_id se asigna al vincular Supabase. */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "agenda_personal.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String[] esquemaTablasRespaldo(){ return TABLES.clone(); }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (String sql : TABLES) db.execSQL(sql);
            for (String sql : INDEXES) db.execSQL(sql);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Las definiciones conservan los nombres y campos de la migración Supabase.
    // En SQLite, UUID/fechas/horas/JSON se almacenan como TEXT.
    private static final String[] TABLES = {
            "CREATE TABLE personas (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, nombre TEXT NOT NULL, otro_dato TEXT, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE direcciones (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, persona_id TEXT NOT NULL, calle TEXT NOT NULL, numero TEXT NOT NULL, localidad TEXT NOT NULL, provincia TEXT NOT NULL, referencia TEXT, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, UNIQUE(id,persona_id), FOREIGN KEY(persona_id) REFERENCES personas(id) ON DELETE RESTRICT)",
            "CREATE TABLE materias (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, institucion_id TEXT NOT NULL, nombre TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, UNIQUE(id,institucion_id), FOREIGN KEY(institucion_id) REFERENCES personas(id) ON DELETE RESTRICT)",
            "CREATE TABLE estudios (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, fecha_entrega TEXT, hora TEXT, actividad TEXT, materia_id TEXT, institucion_id TEXT, prioridad TEXT NOT NULL DEFAULT 'MEDIA' CHECK(prioridad IN ('ALTISIMA','ALTA','MEDIA','BAJA','BAJISIMA')), estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(materia_id,institucion_id) REFERENCES materias(id,institucion_id) ON DELETE RESTRICT)",
            "CREATE TABLE clasificaciones_sociales (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, nombre TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE sociales (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, fecha TEXT, hora TEXT, actividad TEXT, persona_id TEXT NOT NULL, clasificacion_id TEXT, prioridad TEXT NOT NULL DEFAULT 'MEDIA' CHECK(prioridad IN ('ALTISIMA','ALTA','MEDIA','BAJA','BAJISIMA')), estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(persona_id) REFERENCES personas(id) ON DELETE RESTRICT, FOREIGN KEY(clasificacion_id) REFERENCES clasificaciones_sociales(id) ON DELETE RESTRICT)",
            "CREATE TABLE especialidades (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, nombre TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE salud (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, fecha TEXT, hora TEXT, actividad TEXT, especialidad_id TEXT NOT NULL, institucion_id TEXT, prioridad TEXT NOT NULL DEFAULT 'MEDIA' CHECK(prioridad IN ('ALTISIMA','ALTA','MEDIA','BAJA','BAJISIMA')), estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(especialidad_id) REFERENCES especialidades(id) ON DELETE RESTRICT, FOREIGN KEY(institucion_id) REFERENCES personas(id) ON DELETE RESTRICT)",
            "CREATE TABLE trabajo (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, fecha_entrega TEXT, hora TEXT, actividad TEXT, asunto TEXT, clasificacion TEXT, persona_id TEXT, prioridad TEXT NOT NULL DEFAULT 'MEDIA' CHECK(prioridad IN ('ALTISIMA','ALTA','MEDIA','BAJA','BAJISIMA')), estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(persona_id) REFERENCES personas(id) ON DELETE RESTRICT)",
            "CREATE TABLE freelance (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, valor NUMERIC, fecha_inicio TEXT NOT NULL, hora_inicio TEXT, fecha_finalizacion TEXT, hora_finalizacion TEXT, actividad TEXT, cliente_id TEXT NOT NULL, direccion_id TEXT, clasificacion TEXT, estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), observacion TEXT, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(cliente_id) REFERENCES personas(id) ON DELETE RESTRICT, FOREIGN KEY(direccion_id,cliente_id) REFERENCES direcciones(id,persona_id) ON DELETE RESTRICT)",
            "CREATE TABLE clasificaciones_economia (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, nombre TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE ingresos (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, fecha TEXT, descripcion TEXT, clasificacion_id TEXT, importe NUMERIC NOT NULL, estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(clasificacion_id) REFERENCES clasificaciones_economia(id) ON DELETE RESTRICT)",
            "CREATE TABLE pagos (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, fecha TEXT, descripcion TEXT, clasificacion_id TEXT, importe NUMERIC NOT NULL, periodicidad TEXT, estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(clasificacion_id) REFERENCES clasificaciones_economia(id) ON DELETE RESTRICT)",
            "CREATE TABLE gastos (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, fecha TEXT, descripcion TEXT, clasificacion_id TEXT, importe NUMERIC NOT NULL, estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(clasificacion_id) REFERENCES clasificaciones_economia(id) ON DELETE RESTRICT)",
            "CREATE TABLE ejercicios (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, fecha TEXT NOT NULL, ejercicio TEXT NOT NULL, tipo TEXT NOT NULL CHECK(tipo IN ('AEROBICO','ANAEROBICO','ESTIRAMIENTO')), musculos_trabajados TEXT NOT NULL, tiempo_segundos INTEGER CHECK(tiempo_segundos IS NULL OR tiempo_segundos>=0), distancia NUMERIC CHECK(distancia IS NULL OR distancia>=0), cantidad NUMERIC CHECK(cantidad IS NULL OR cantidad>=0), peso NUMERIC CHECK(peso IS NULL OR peso>=0), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE ambitos_emocionales (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, nombre TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE motivos_emocionales (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, nombre TEXT NOT NULL, ambito_id TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(ambito_id) REFERENCES ambitos_emocionales(id) ON DELETE RESTRICT)",
            "CREATE TABLE registros_emocionales (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, fecha_hora TEXT NOT NULL, valoracion TEXT NOT NULL CHECK(valoracion IN ('POSITIVA','NEGATIVA')), motivo_id TEXT NOT NULL, accion_evento_asociado TEXT, observaciones TEXT, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(motivo_id) REFERENCES motivos_emocionales(id) ON DELETE RESTRICT)",
            "CREATE TABLE frases_personales (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, frase TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE series_eventos (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, frecuencia TEXT NOT NULL CHECK(frecuencia IN ('SEMANAL','MENSUAL','ANUAL','PERSONALIZADA')), configuracion_personalizada TEXT, fecha_fin TEXT, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE eventos (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, descripcion TEXT NOT NULL, categoria TEXT NOT NULL CHECK(categoria IN ('ESTUDIOS','SOCIALES','SALUD','TRABAJO','EJERCICIO','FREELANCE','OTRA')), fecha TEXT NOT NULL, hora TEXT, persona_id TEXT, lugar TEXT, estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), alarma_activada INTEGER NOT NULL DEFAULT 0 CHECK(alarma_activada IN (0,1)), anticipacion_minutos INTEGER CHECK(anticipacion_minutos IS NULL OR anticipacion_minutos>=0), serie_id TEXT, observaciones TEXT, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, CHECK(alarma_activada=0 OR anticipacion_minutos IS NOT NULL), FOREIGN KEY(persona_id) REFERENCES personas(id) ON DELETE RESTRICT, FOREIGN KEY(serie_id) REFERENCES series_eventos(id) ON DELETE RESTRICT)",
            "CREATE TABLE tareas (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, descripcion TEXT NOT NULL, fecha_inicio TEXT, hora_inicio TEXT, fecha_vencimiento TEXT NOT NULL, hora_vencimiento TEXT, categoria TEXT NOT NULL CHECK(categoria IN ('ESTUDIOS','SOCIALES','SALUD','TRABAJO','EJERCICIO','FREELANCE','OTRA')), prioridad TEXT NOT NULL DEFAULT 'MEDIA' CHECK(prioridad IN ('ALTISIMA','ALTA','MEDIA','BAJA','BAJISIMA')), estado_especial TEXT CHECK(estado_especial IS NULL OR estado_especial IN ('FINALIZADO','CANCELADO','POSTERGADO')), alarma_activada INTEGER NOT NULL DEFAULT 0 CHECK(alarma_activada IN (0,1)), anticipacion_minutos INTEGER CHECK(anticipacion_minutos IS NULL OR anticipacion_minutos>=0), observaciones TEXT, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, CHECK(alarma_activada=0 OR fecha_inicio IS NOT NULL), CHECK(alarma_activada=0 OR anticipacion_minutos IS NOT NULL))",
            "CREATE TABLE historial_postergaciones (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, tipo_entidad TEXT NOT NULL CHECK(tipo_entidad IN ('EVENTO','TAREA','ESTUDIO','SOCIAL','SALUD','TRABAJO','FREELANCE','INGRESO','PAGO','GASTO')), entidad_id TEXT NOT NULL, fecha_original TEXT NOT NULL, nueva_fecha TEXT, fecha_postergacion TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE registros_investigacion (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, id_original TEXT, datos_originales TEXT NOT NULL DEFAULT '{}', created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT)",
            "CREATE TABLE observaciones_ri (id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, registro_investigacion_id TEXT NOT NULL, observacion TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), revision INTEGER NOT NULL DEFAULT 1 CHECK(revision>0), deleted_at TEXT, FOREIGN KEY(registro_investigacion_id) REFERENCES registros_investigacion(id) ON DELETE RESTRICT)",
            "CREATE TABLE sync_devices (device_id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, nombre TEXT, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), last_seen_at TEXT)",
            "CREATE TABLE sync_changes (change_id TEXT PRIMARY KEY NOT NULL, owner_id TEXT, device_id TEXT NOT NULL, entity_type TEXT NOT NULL, entity_id TEXT NOT NULL, base_revision INTEGER NOT NULL CHECK(base_revision>=0), entity_revision INTEGER NOT NULL CHECK(entity_revision>0), operation TEXT NOT NULL CHECK(operation IN ('UPSERT','DELETE')), payload TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')))",
            "CREATE TABLE sync_receipts (owner_id TEXT, device_id TEXT NOT NULL, change_id TEXT NOT NULL, received_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), PRIMARY KEY(device_id,change_id), FOREIGN KEY(change_id) REFERENCES sync_changes(change_id) ON DELETE RESTRICT)"
    };

    private static final String[] INDEXES = {
            "CREATE INDEX idx_direcciones_persona ON direcciones(persona_id)",
            "CREATE INDEX idx_materias_institucion ON materias(institucion_id)",
            "CREATE INDEX idx_estudios_fecha ON estudios(fecha_entrega)",
            "CREATE INDEX idx_sociales_fecha ON sociales(fecha)",
            "CREATE INDEX idx_salud_fecha ON salud(fecha)",
            "CREATE INDEX idx_trabajo_fecha ON trabajo(fecha_entrega)",
            "CREATE INDEX idx_freelance_fecha ON freelance(fecha_inicio)",
            "CREATE INDEX idx_freelance_cliente ON freelance(cliente_id)",
            "CREATE INDEX idx_ingresos_fecha ON ingresos(fecha)",
            "CREATE INDEX idx_pagos_fecha ON pagos(fecha)",
            "CREATE INDEX idx_gastos_fecha ON gastos(fecha)",
            "CREATE INDEX idx_ejercicios_fecha ON ejercicios(fecha)",
            "CREATE INDEX idx_registros_emocionales_fecha ON registros_emocionales(fecha_hora)",
            "CREATE INDEX idx_eventos_fecha ON eventos(fecha)",
            "CREATE INDEX idx_eventos_serie ON eventos(serie_id)",
            "CREATE INDEX idx_tareas_vencimiento ON tareas(fecha_vencimiento)",
            "CREATE INDEX idx_tareas_inicio ON tareas(fecha_inicio)",
            "CREATE INDEX idx_postergaciones_entidad ON historial_postergaciones(tipo_entidad,entidad_id,fecha_postergacion)",
            "CREATE INDEX idx_ri_id_original ON registros_investigacion(id_original)",
            "CREATE INDEX idx_observaciones_ri_registro ON observaciones_ri(registro_investigacion_id)",
            "CREATE INDEX idx_sync_changes_created ON sync_changes(created_at,change_id)"
    };

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            // SQLiteOpenHelper ejecuta onUpgrade dentro de una transacción.
            // Se conserva la tabla anterior como respaldo; no se borra ningún registro.
            db.execSQL("ALTER TABLE historial_postergaciones RENAME TO historial_postergaciones_v1");
            for (String sql : TABLES) {
                if (sql.startsWith("CREATE TABLE historial_postergaciones (")) db.execSQL(sql);
            }
            db.execSQL("INSERT INTO historial_postergaciones SELECT * FROM historial_postergaciones_v1");
            db.execSQL("CREATE INDEX idx_postergaciones_entidad_v2 ON historial_postergaciones(tipo_entidad,entidad_id,fecha_postergacion)");
            return;
        }
        throw new IllegalStateException("No hay migración implementada: " + oldVersion + " -> " + newVersion);
    }
}
