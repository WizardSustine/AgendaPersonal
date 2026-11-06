# Diccionario de datos

Generado desde `DatabaseHelper` y las tablas auxiliares admitidas por `EsquemaRespaldo`. El código y las migraciones son la fuente de verdad.

Se verificó la creación de 36 tablas en SQLite en memoria y 21 índices del esquema base. Las tablas auxiliares se crean cuando la función correspondiente las necesita.

Las entidades funcionales conservan UUID, propietario, revisión y baja lógica. En SQLite UUID, fechas y JSON se representan principalmente como TEXT; PostgreSQL usa sus tipos nativos. Los booleanos SQLite se guardan como 0/1.

Las restricciones CHECK y UNIQUE completas están en [esquema_sqlite.sql](referencia/esquema_sqlite.sql). No ejecutar ese archivo para actualizar una agenda existente.

## SQLite

### `ambitos_emocionales`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `nombre` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `clasificaciones_economia`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `nombre` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `clasificaciones_sociales`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `nombre` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `direcciones`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `persona_id` | `TEXT` | Sí | — | — |
| `calle` | `TEXT` | Sí | — | — |
| `numero` | `TEXT` | Sí | — | — |
| `localidad` | `TEXT` | Sí | — | — |
| `provincia` | `TEXT` | Sí | — | — |
| `referencia` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(persona_id)` → `personas(id)`, borrado `RESTRICT`.

### `ejercicios`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `fecha` | `TEXT` | Sí | — | — |
| `ejercicio` | `TEXT` | Sí | — | — |
| `tipo` | `TEXT` | Sí | — | — |
| `musculos_trabajados` | `TEXT` | Sí | — | — |
| `tiempo_segundos` | `INTEGER` | No | — | — |
| `distancia` | `NUMERIC` | No | — | — |
| `cantidad` | `NUMERIC` | No | — | — |
| `peso` | `NUMERIC` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `especialidades`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `nombre` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `estudios`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `fecha_entrega` | `TEXT` | No | — | — |
| `hora` | `TEXT` | No | — | — |
| `actividad` | `TEXT` | No | — | — |
| `materia_id` | `TEXT` | No | — | — |
| `institucion_id` | `TEXT` | No | — | — |
| `prioridad` | `TEXT` | Sí | `'MEDIA'` | — |
| `estado_especial` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(materia_id, institucion_id)` → `materias(id, institucion_id)`, borrado `RESTRICT`.

### `eventos`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `descripcion` | `TEXT` | Sí | — | — |
| `categoria` | `TEXT` | Sí | — | — |
| `fecha` | `TEXT` | Sí | — | — |
| `hora` | `TEXT` | No | — | — |
| `persona_id` | `TEXT` | No | — | — |
| `lugar` | `TEXT` | No | — | — |
| `estado_especial` | `TEXT` | No | — | — |
| `alarma_activada` | `INTEGER` | Sí | `0` | — |
| `anticipacion_minutos` | `INTEGER` | No | — | — |
| `serie_id` | `TEXT` | No | — | — |
| `observaciones` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(serie_id)` → `series_eventos(id)`, borrado `RESTRICT`.
- FK `(persona_id)` → `personas(id)`, borrado `RESTRICT`.

### `eventos_ocurrencias`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `evento_id` | `TEXT` | Sí | — | 1 |
| `serie_id` | `TEXT` | Sí | — | — |
| `ordinal` | `INTEGER` | Sí | — | — |
| `fecha_origen` | `TEXT` | Sí | — | — |

### `frases_personales`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `frase` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `freelance`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `valor` | `NUMERIC` | No | — | — |
| `fecha_inicio` | `TEXT` | Sí | — | — |
| `hora_inicio` | `TEXT` | No | — | — |
| `fecha_finalizacion` | `TEXT` | No | — | — |
| `hora_finalizacion` | `TEXT` | No | — | — |
| `actividad` | `TEXT` | No | — | — |
| `cliente_id` | `TEXT` | Sí | — | — |
| `direccion_id` | `TEXT` | No | — | — |
| `clasificacion` | `TEXT` | No | — | — |
| `estado_especial` | `TEXT` | No | — | — |
| `observacion` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(direccion_id, cliente_id)` → `direcciones(id, persona_id)`, borrado `RESTRICT`.
- FK `(cliente_id)` → `personas(id)`, borrado `RESTRICT`.

### `gastos`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `fecha` | `TEXT` | No | — | — |
| `descripcion` | `TEXT` | No | — | — |
| `clasificacion_id` | `TEXT` | No | — | — |
| `importe` | `NUMERIC` | Sí | — | — |
| `estado_especial` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(clasificacion_id)` → `clasificaciones_economia(id)`, borrado `RESTRICT`.

### `historial_postergaciones`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `tipo_entidad` | `TEXT` | Sí | — | — |
| `entidad_id` | `TEXT` | Sí | — | — |
| `fecha_original` | `TEXT` | Sí | — | — |
| `nueva_fecha` | `TEXT` | No | — | — |
| `fecha_postergacion` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `historico_archivo`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `fuente_sha256` | `TEXT` | Sí | — | 1 |
| `hoja` | `TEXT` | Sí | — | 2 |
| `fila` | `INTEGER` | Sí | — | 3 |
| `original_json` | `TEXT` | Sí | — | — |
| `motivo_revision` | `TEXT` | No | — | — |

### `historico_fuentes`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `tabla` | `TEXT` | Sí | — | 1 |
| `entidad_id` | `TEXT` | Sí | — | 2 |
| `fuente_sha256` | `TEXT` | Sí | — | 3 |
| `original_json` | `TEXT` | Sí | — | — |

### `ingresos`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `fecha` | `TEXT` | No | — | — |
| `descripcion` | `TEXT` | No | — | — |
| `clasificacion_id` | `TEXT` | No | — | — |
| `importe` | `NUMERIC` | Sí | — | — |
| `estado_especial` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(clasificacion_id)` → `clasificaciones_economia(id)`, borrado `RESTRICT`.

### `materias`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `institucion_id` | `TEXT` | Sí | — | — |
| `nombre` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(institucion_id)` → `personas(id)`, borrado `RESTRICT`.

### `motivos_emocionales`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `nombre` | `TEXT` | Sí | — | — |
| `ambito_id` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(ambito_id)` → `ambitos_emocionales(id)`, borrado `RESTRICT`.

### `observaciones_ri`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `registro_investigacion_id` | `TEXT` | Sí | — | — |
| `observacion` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(registro_investigacion_id)` → `registros_investigacion(id)`, borrado `RESTRICT`.

### `pagos`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `fecha` | `TEXT` | No | — | — |
| `descripcion` | `TEXT` | No | — | — |
| `clasificacion_id` | `TEXT` | No | — | — |
| `importe` | `NUMERIC` | Sí | — | — |
| `periodicidad` | `TEXT` | No | — | — |
| `estado_especial` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(clasificacion_id)` → `clasificaciones_economia(id)`, borrado `RESTRICT`.

### `personas`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `nombre` | `TEXT` | Sí | — | — |
| `otro_dato` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `registros_emocionales`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `fecha_hora` | `TEXT` | Sí | — | — |
| `valoracion` | `TEXT` | Sí | — | — |
| `motivo_id` | `TEXT` | Sí | — | — |
| `accion_evento_asociado` | `TEXT` | No | — | — |
| `observaciones` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(motivo_id)` → `motivos_emocionales(id)`, borrado `RESTRICT`.

### `registros_investigacion`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `id_original` | `TEXT` | No | — | — |
| `datos_originales` | `TEXT` | Sí | `'{}'` | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `salud`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `fecha` | `TEXT` | No | — | — |
| `hora` | `TEXT` | No | — | — |
| `actividad` | `TEXT` | No | — | — |
| `especialidad_id` | `TEXT` | Sí | — | — |
| `institucion_id` | `TEXT` | No | — | — |
| `prioridad` | `TEXT` | Sí | `'MEDIA'` | — |
| `estado_especial` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(institucion_id)` → `personas(id)`, borrado `RESTRICT`.
- FK `(especialidad_id)` → `especialidades(id)`, borrado `RESTRICT`.

### `series_eventos`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `frecuencia` | `TEXT` | Sí | — | — |
| `configuracion_personalizada` | `TEXT` | No | — | — |
| `fecha_fin` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `sociales`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `fecha` | `TEXT` | No | — | — |
| `hora` | `TEXT` | No | — | — |
| `actividad` | `TEXT` | No | — | — |
| `persona_id` | `TEXT` | Sí | — | — |
| `clasificacion_id` | `TEXT` | No | — | — |
| `prioridad` | `TEXT` | Sí | `'MEDIA'` | — |
| `estado_especial` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(clasificacion_id)` → `clasificaciones_sociales(id)`, borrado `RESTRICT`.
- FK `(persona_id)` → `personas(id)`, borrado `RESTRICT`.

### `sync_changes`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `change_id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `device_id` | `TEXT` | Sí | — | — |
| `entity_type` | `TEXT` | Sí | — | — |
| `entity_id` | `TEXT` | Sí | — | — |
| `base_revision` | `INTEGER` | Sí | — | — |
| `entity_revision` | `INTEGER` | Sí | — | — |
| `operation` | `TEXT` | Sí | — | — |
| `payload` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |

### `sync_conflicts`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `owner_id` | `TEXT` | Sí | — | 1 |
| `change_id` | `TEXT` | Sí | — | 2 |
| `entity_type` | `TEXT` | Sí | — | — |
| `entity_id` | `TEXT` | Sí | — | — |
| `local_payload` | `TEXT` | No | — | — |
| `remote_change` | `TEXT` | Sí | — | — |
| `created_at` | `TEXT` | Sí | — | — |
| `resolved_at` | `TEXT` | No | — | — |
| `resolution_change_id` | `TEXT` | No | — | — |

### `sync_deferred`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `owner_id` | `TEXT` | Sí | — | 1 |
| `change_id` | `TEXT` | Sí | — | 2 |
| `cambio_json` | `TEXT` | Sí | — | — |
| `estado` | `TEXT` | Sí | `'PENDIENTE'` | — |
| `motivo` | `TEXT` | No | — | — |

### `sync_devices`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `device_id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `nombre` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `last_seen_at` | `TEXT` | No | — | — |

### `sync_inbox_receipts`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `owner_id` | `TEXT` | Sí | — | 1 |
| `device_id` | `TEXT` | Sí | — | 2 |
| `change_id` | `TEXT` | Sí | — | 3 |
| `applied_at` | `TEXT` | Sí | — | — |
| `remote_confirmed` | `INTEGER` | Sí | `0` | — |

### `sync_meta`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `clave` | `TEXT` | Sí | — | 1 |
| `valor` | `TEXT` | Sí | — | — |

### `sync_outbox_sent`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `owner_id` | `TEXT` | Sí | — | 1 |
| `change_id` | `TEXT` | Sí | — | 2 |
| `confirmed_at` | `TEXT` | Sí | — | — |

### `sync_receipts`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `owner_id` | `TEXT` | No | — | — |
| `device_id` | `TEXT` | Sí | — | 1 |
| `change_id` | `TEXT` | Sí | — | 2 |
| `received_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |

- FK `(change_id)` → `sync_changes(change_id)`, borrado `RESTRICT`.

### `tareas`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `descripcion` | `TEXT` | Sí | — | — |
| `fecha_inicio` | `TEXT` | No | — | — |
| `hora_inicio` | `TEXT` | No | — | — |
| `fecha_vencimiento` | `TEXT` | Sí | — | — |
| `hora_vencimiento` | `TEXT` | No | — | — |
| `categoria` | `TEXT` | Sí | — | — |
| `prioridad` | `TEXT` | Sí | `'MEDIA'` | — |
| `estado_especial` | `TEXT` | No | — | — |
| `alarma_activada` | `INTEGER` | Sí | `0` | — |
| `anticipacion_minutos` | `INTEGER` | No | — | — |
| `observaciones` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

### `trabajo`

| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |
| --- | --- | --- | --- | --- |
| `id` | `TEXT` | Sí | — | 1 |
| `owner_id` | `TEXT` | No | — | — |
| `fecha_entrega` | `TEXT` | No | — | — |
| `hora` | `TEXT` | No | — | — |
| `actividad` | `TEXT` | No | — | — |
| `asunto` | `TEXT` | No | — | — |
| `clasificacion` | `TEXT` | No | — | — |
| `persona_id` | `TEXT` | No | — | — |
| `prioridad` | `TEXT` | Sí | `'MEDIA'` | — |
| `estado_especial` | `TEXT` | No | — | — |
| `created_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `updated_at` | `TEXT` | Sí | `strftime('%Y-%m-%dT%H:%M:%fZ','now')` | — |
| `revision` | `INTEGER` | Sí | `1` | — |
| `deleted_at` | `TEXT` | No | — | — |

- FK `(persona_id)` → `personas(id)`, borrado `RESTRICT`.

## Tablas de control que pueden aparecer

`respaldo_control` contiene pares clave/valor para completar una restauración de forma durable. `historial_postergaciones_v1` puede conservarse como respaldo de la migración local 1→2. Su presencia depende de la historia de la instalación; no representan nuevos módulos funcionales.

## Diferencias y controles remotos

La migración [001](../migrations/001_inicial_supabase.sql) define los tipos PostgreSQL, índices y RLS. La [002](../migrations/002_historial_economia.sql) amplía los tipos del historial. La [003](../migrations/003_persistencia_sync.sql) incorpora los siguientes controles privados:

### `agenda_internal.heads`

```sql
create table if not exists agenda_internal.heads(
 owner_id uuid not null, entity_type text not null, entity_id uuid not null,
 change_id uuid not null, device_id uuid not null, revision integer not null,
 -- Una sola imagen actual por registro. Conserva la representación de Android,
 -- procedencia histórica y recurrencia; NO acumula versiones del registro.
 payload jsonb not null, created_at timestamptz not null,
 primary key(owner_id,entity_type,entity_id));
```

### `agenda_internal.change_state`

```sql
create table if not exists agenda_internal.change_state(
 change_id uuid primary key references public.sync_changes(change_id) on delete cascade,
 status text not null check(status in ('WAITING','APPLIED','CONFLICT','RESOLVED')),
 detail text);
```

### `agenda_internal.retention`

```sql
create table if not exists agenda_internal.retention(
 singleton boolean primary key default true check(singleton), enabled boolean not null default false,
 days integer not null default 90 check(days=90), last_run timestamptz, last_deleted bigint);
```

`sync_changes.received_at` se añade en el servidor y determina la antigüedad de retención. No sustituye las fechas funcionales del payload.

El esquema privado no se expone como una tabla editable por el cliente. Los RPC actualizan el estado técnico y las entidades en la misma transacción. Ver [protocolo](PROTOCOLO_SYNC.md).
