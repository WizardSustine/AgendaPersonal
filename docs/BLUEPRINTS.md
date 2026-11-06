# Blueprints de arquitectura

Los diagramas se muestran en GitHub con Mermaid. Cada uno tiene una fuente `.mmd` editable en `docs/blueprints/`. Se corresponden con el código actual; no describen una plataforma multiusuario ni un cliente PC implementado.

## Contexto y límites

La aplicación entregada es Android. El cliente PC aparece como una extensión futura, no como software incluido.

[Fuente editable](blueprints/01_contexto.mmd)

```mermaid
flowchart LR
    Persona[Persona usuaria] --> Android[Agenda Personal Android]
    Android --> OS[Servicios Android: permisos y avisos]
    Android <--> Nube[Supabase: Auth y PostgreSQL]
    Android <--> Copia[Archivo de respaldo cifrado]
    Android --> CSV[CSV por módulo]
    Historico[Paquete histórico preparado] --> Android
    Futuro[Cliente PC futuro: no incluido] -. mismo protocolo .-> Nube
```

## Componentes Android

Dependencias conceptuales entre presentación, reglas, persistencia y servicios. No es un diagrama exhaustivo de cada llamada.

[Fuente editable](blueprints/02_componentes.mmd)

```mermaid
flowchart TB
    subgraph Presentacion[Presentación]
        Main[MainActivity y AgendaActivity]
        Registro[RegistroActivity y módulos]
        Dia[ProgresoDia y ResumenDia]
        Tema[Tema y ActividadTematica]
        ModeloUI[Definicion y Fuentes]
    end
    subgraph Persistencia[Persistencia local]
        Repos[Repositorios tipados]
        DB[DatabaseHelper y SQLite]
        Series[SeriesEvento y ReglaRecurrencia]
        Importar[ImportadorHistorico y Csv]
    end
    subgraph Servicios[Servicios]
        Sync[CoordinadorSync y MotorSync]
        LocalSync[AlmacenSync]
        REST[SupabaseRest y SesionSegura]
        Avisos[ProgramadorAvisos y VentanaAvisos]
        Respaldo[GestorRespaldo y CifradoRespaldo]
    end
    Main --> Dia
    Main --> ModeloUI
    Registro --> ModeloUI
    Tema --> Main
    Tema --> Registro
    ModeloUI --> Repos
    Repos --> DB
    Series --> DB
    Importar --> DB
    Sync --> LocalSync
    LocalSync --> DB
    Sync --> REST
    REST --> Supabase[Supabase]
    Avisos --> DB
    Avisos --> Series
    Respaldo --> DB
```

## Escritura y confirmación remota

Un registro aceptado queda persistido sin esperar que se conecte otra instalación. Un conflicto conserva una rama, sin convertirla en la versión aceptada.

[Fuente editable](blueprints/03_envio.mmd)

```mermaid
sequenceDiagram
    actor U as Persona
    participant UI as Pantalla
    participant L as SQLite y repositorio
    participant M as MotorSync
    participant RPC as agenda_sync_push
    participant PG as PostgreSQL
    U->>UI: Guardar registro
    UI->>L: Entidad y cambio en transacción
    L-->>UI: Guardado local
    M->>L: Leer salida pendiente
    M->>RPC: Hasta 100 sobres completos
    RPC->>PG: Bloqueo por propietario y validación
    alt Cambio aplicable
        PG->>PG: Entidad + estado actual + historial, misma transacción
        RPC-->>M: APPLIED y sobre confirmado
        M->>L: Confirmación y contador, misma transacción
    else Rama divergente
        PG->>PG: Conservar rama y versión aceptada
        RPC-->>M: CONFLICT y sobre conservado
        M->>L: Conservar recepción para resolver
    else Dependencia no disponible
        RPC-->>M: WAITING
        M->>L: Mantener salida sin confirmación funcional
    end
    Note over M,PG: No se espera un recibo de PC
```

## Recuperación y aplicación del estado

La recuperación usa revisiones actuales, base conocida y preservación de cambios locales. Las páginas pueden tener dependencias.

[Fuente editable](blueprints/04_recuperacion.mmd)

```mermaid
flowchart TD
    Inicio[Instalación nueva o cliente que vuelve] --> Subir[Enviar cambios locales pendientes]
    Subir --> Pagina[Leer página de agenda_sync_snapshot]
    Pagina --> Existe{Existe entidad local?}
    Existe -->|No| Aplicar[Aplicar estado completo y metadatos]
    Existe -->|Sí| Igual{Contenido idéntico?}
    Igual -->|Sí| Base[Reconocer y conservar base remota]
    Igual -->|No| Limpia{Base conocida y copia local sin cambios ni conflicto?}
    Limpia -->|Sí y revisión mayor| Aplicar
    Limpia -->|No| Conflicto[Guardar versiones para decisión manual]
    Aplicar --> FK{Relaciones disponibles?}
    FK -->|No| Diferir[Conservar y reintentar dependencias]
    FK -->|Sí| Base
    Base --> Otra{Otra página?}
    Diferir --> Otra
    Conflicto --> Otra
    Otra -->|Sí| Pagina
    Otra -->|No| Fin[Actualizar resumen y limpiar sólo transporte confirmado]
```

## Copia y restauración

El formato cifrado y la restauración transaccional son independientes de la sincronización.

[Fuente editable](blueprints/05_respaldo.mmd)

```mermaid
sequenceDiagram
    actor U as Persona
    participant G as GestorRespaldo
    participant DB as SQLite
    participant C as CifradoRespaldo
    participant A as Archivo externo
    U->>G: Exportar con contraseña
    G->>DB: Snapshot consistente
    DB-->>G: Filas, esquema y metadatos
    G->>C: Manifiesto y base
    C-->>A: agendabak autenticado y cifrado
    U->>G: Restaurar archivo
    A->>C: Contenido y contraseña ingresada
    C-->>G: Paquete autenticado
    G->>G: Validar esquema, integridad y cuenta
    G->>DB: Guardar estado anterior y restaurar en transacción
    G->>DB: Registrar finalización pendiente durable
    G->>G: Nueva identidad de dispositivo y sesión pausada
    G-->>U: Volver a iniciar sesión para sincronizar
```

## Relaciones declaradas en SQLite

Generado desde las claves foráneas reales. Las flechas van de tabla referenciada a dependiente; no representan cardinalidad. Consultar nulabilidad y claves compuestas en el diccionario.

[Fuente editable](blueprints/06_relaciones_datos.mmd)

```mermaid
flowchart LR
    personas -->|"persona_id"| direcciones
    materias -->|"materia_id, institucion_id"| estudios
    series_eventos -->|"serie_id"| eventos
    personas -->|"persona_id"| eventos
    direcciones -->|"direccion_id, cliente_id"| freelance
    personas -->|"cliente_id"| freelance
    clasificaciones_economia -->|"clasificacion_id"| gastos
    clasificaciones_economia -->|"clasificacion_id"| ingresos
    personas -->|"institucion_id"| materias
    ambitos_emocionales -->|"ambito_id"| motivos_emocionales
    registros_investigacion -->|"registro_investigacion_id"| observaciones_ri
    clasificaciones_economia -->|"clasificacion_id"| pagos
    motivos_emocionales -->|"motivo_id"| registros_emocionales
    personas -->|"institucion_id"| salud
    especialidades -->|"especialidad_id"| salud
    clasificaciones_sociales -->|"clasificacion_id"| sociales
    personas -->|"persona_id"| sociales
    sync_changes -->|"change_id"| sync_receipts
    personas -->|"persona_id"| trabajo
```

## Navegación técnica

- [Arquitectura y responsabilidades](ARQUITECTURA.md)
- [Protocolo y contratos RPC](PROTOCOLO_SYNC.md)
- [Diccionario de datos](DICCIONARIO_DATOS.md)
- [Árboles de archivos](ARBOLES_ARCHIVOS.md)
- [Inventario de código](INVENTARIO_CODIGO.md)
- [Decisiones técnicas](DECISIONES_TECNICAS.md)

Los diagramas 1 a 5 se mantienen como fuentes editables. El diagrama 6 y el DDL de referencia se regeneran con `python tools/documentacion/generar_referencias.py`. Después de cambiar la arquitectura o el esquema, actualizar tanto el diagrama fuente como su bloque de visualización.
