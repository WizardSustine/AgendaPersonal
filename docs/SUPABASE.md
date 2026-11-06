# Supabase: instalación, persistencia y retención

## Proyecto nuevo

Crear un proyecto Supabase y ejecutar en SQL Editor, en este orden:

| Archivo | Propósito |
| --- | --- |
| [001_inicial_supabase.sql](../migrations/001_inicial_supabase.sql) | Tablas funcionales, tipos, índices, RLS e infraestructura inicial |
| [002_historial_economia.sql](../migrations/002_historial_economia.sql) | Tipos de postergación de Ingresos, Pagos y Gastos |
| [003_persistencia_sync.sql](../migrations/003_persistencia_sync.sql) | Persistencia transaccional y recuperación por estado completo, protocolo 2 |
| [004_activar_retencion.sql](../migrations/004_activar_retencion.sql) | Activación de la limpieza diaria del historial técnico de más de 90 días |

La `004` es un paso operativo posterior: actualizar los clientes al protocolo 2, comprobar persistencia y recuperación y después activar la limpieza. No ejecutar todas las migraciones automáticamente contra un proyecto existente.

La `001` es una copia de la migración inicial revisada `agenda_supabase_migracion_001_v2.sql` entregada con el proyecto. Si la base ya tiene ese esquema, continuar desde la migración incremental que corresponda; no volver a inicializarla.

Para la `003`, si SQL Editor ofrece **Run without RLS** y **Run and enable RLS**, usar **Run without RLS** para ejecutar el script tal como está. Esto no desactiva el RLS existente: las tablas funcionales lo conservan y las nuevas tablas técnicas están en un esquema privado sin acceso directo del cliente. No desactivar manualmente la protección de las tablas.

Si una migración falla, conservar el error y revisar su causa antes de continuar con la siguiente.

## Usuario y conexión

Crear un usuario para la aplicación en Authentication → Users. La contraseña del panel administrativo de Supabase no sustituye la de ese usuario.

En Android, completar la URL HTTPS del proyecto, su clave pública `publishable` o `anon`, el correo y la contraseña del usuario de la app. Confirmar Vincular y sincronizar. No ingresar `service_role` ni una clave secreta administrativa.

La agenda local queda vinculada a un propietario y proyecto. El cliente impide mezclar otra cuenta con esa base vinculada.

## Persistencia del protocolo 2

1. La app registra las modificaciones en SQLite y su salida pendiente.
2. Envía lotes de hasta 100 cambios a `public.agenda_sync_push`.
3. El servidor valida cuenta, campos, referencias y revisiones. El estado aceptado se escribe en la tabla funcional de su módulo y se actualiza un índice privado del estado actual en la misma transacción.
4. La app verifica la respuesta antes de confirmar su envío. Una relación aún no válida impide confirmar el cambio como guardado en su módulo.
5. Cada ciclo lee `public.agenda_sync_snapshot` por páginas de hasta 100. Incluye estado actual, bajas lógicas y versiones divergentes o pendientes que deben conservarse.

Un teléfono puede ser el único cliente conectado: una tarea aceptada queda en `public.tareas` sin esperar un recibo de PC. Un conflicto conserva las versiones y requiere resolución manual; no sustituye silenciosamente el registro aceptado.

El índice privado `agenda_internal.heads` conserva una sola imagen actual por entidad, incluidos los metadatos de recurrencia y procedencia. No acumula versiones. La imagen permite mantener la representación exacta del cliente y se guarda junto con el registro tipado del módulo.

La `003` también materializa los cambios previos que sólo estaban en el historial. Los clientes antiguos que escribían directamente en `sync_changes` deben actualizarse: la nueva migración retira ese acceso para evitar escrituras descoordinadas.

## Retención

La `004` habilita `pg_cron` y programa `agenda-sync-retencion-90-dias` con la expresión `20 3 * * *`. El horario efectivo depende de la zona horaria configurada para Cron; revisar la configuración del proyecto antes de asociarlo a una hora local.

La antigüedad se calcula por `received_at`, fijado por el servidor, no por el reloj del teléfono. Se eliminan cambios técnicos aplicados o resueltos con más de 90 días y sus recibos asociados. Se conservan:

- Registros de las tablas funcionales y sus bajas lógicas.
- Estado actual y metadatos necesarios para recuperación.
- Conflictos sin resolver y cambios sin aplicar.
- Historial funcional de postergaciones y procedencia histórica.

Un cliente nuevo o inactivo durante más de 90 días obtiene el estado completo. Una copia local con modificaciones divergentes se conserva como conflicto; no se sobrescribe automáticamente.

Sincronización y retención no sustituyen una copia de seguridad independiente.

## Comprobación administrativa

Ejecutar estas consultas desde SQL Editor con acceso administrativo, no desde una clave pública:

```sql
select * from agenda_internal.retention;
select status, count(*) from agenda_internal.change_state group by status;
select jobid, jobname, schedule, active
from cron.job where jobname = 'agenda-sync-retencion-90-dias';
```

El resultado `schedule: 1` de la activación es el identificador del trabajo creado, no una confirmación de que ya se ejecutó una limpieza.

Para revisar ejecuciones del trabajo:

```sql
select d.jobid, d.status, d.return_message, d.start_time, d.end_time
from cron.job_run_details d
join cron.job j on j.jobid = d.jobid
where j.jobname = 'agenda-sync-retencion-90-dias'
order by d.start_time desc
limit 10;
```

Para pausar la eliminación sin quitar datos ni el trabajo:

```sql
update agenda_internal.retention set enabled = false where singleton;
```

No editar los registros funcionales directamente como parte del flujo normal: las escrituras de la app deben pasar por el protocolo para mantener coordinadas las revisiones y el estado actual.

Fuentes oficiales: [funciones PostgreSQL en Supabase](https://supabase.com/docs/guides/database/functions), [Supabase Cron](https://supabase.com/docs/guides/cron), [programar trabajos](https://supabase.com/docs/guides/cron/quickstart).
