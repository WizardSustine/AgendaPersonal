# Mantenimiento y diagnóstico

## Una sincronización muestra cero

En la versión actual, Sin cambios nuevos corresponde sólo al último ciclo. Revisar también la última transferencia, los confirmados acumulados y las cantidades por enviar, por aplicar y en conflicto.

Las primeras versiones reemplazaban el mensaje anterior por cuatro ceros después de un ciclo vacío. Si ocurre esa presentación, comprobar que la APK incluya la corrección de contadores durables. No volver a importar datos para cambiar el contador.

## Un registro sólo está en el log

Revisar desde SQL Editor:

```sql
select s.entity_type, s.entity_id, e.status, e.detail
from public.sync_changes s
join agenda_internal.change_state e using (change_id)
where e.status in ('WAITING', 'CONFLICT');
```

`WAITING` indica que no se completó la materialización; `CONFLICT` conserva una rama divergente. No vaciar estas tablas para ocultar el síntoma. Comprobar revisiones, relaciones y migraciones. Un envío válido de un teléfono no debe necesitar que la PC se conecte.

## Sesión o acceso

Comprobar URL, clave pública, usuario de Authentication y migraciones del mismo proyecto. Diferenciar la cuenta de la app del acceso al panel. No sustituir la clave pública por una credencial administrativa para resolver un error.

El cliente distingue sesión vencida, permiso insuficiente, limitación de solicitudes y migración faltante. Mantiene la salida no confirmada y reintenta con demora creciente. Los detalles de implementación están en `SupabaseRest` y `CoordinadorSync`.

## Notificación ausente

Comprobar fecha de inicio o de evento, hora y anticipación; el inicio de tarea es distinto del vencimiento. Sin hora, la referencia es medianoche. Verificar estado activo y permisos de notificaciones, alarmas exactas y superposición según la función que se esté comprobando.

La ventana flotante requiere habilitar tanto el permiso del sistema como la opción de la app. Una entrega de notificación normal no demuestra que la superposición esté autorizada. Comprobar también las restricciones de batería del dispositivo de destino sin cambiar reglas de negocio para compensarlas.

## Cambio de esquema

1. Definir migración incremental y compatibilidad con datos anteriores.
2. Actualizar `DatabaseHelper`, modelos, repositorio y `Definicion`/`Fuentes`.
3. Revisar validación de columnas, payload de sincronización y whitelist del respaldo.
4. Revisar importación CSV/histórica si el cambio afecta sus campos.
5. Probar rechazo de payload incompatible, transacciones, FK, conflictos y recuperación.
6. Regenerar el diccionario de datos y los árboles si cambiaron archivos o tablas.

No modificar retrospectivamente una migración ya desplegada para ocultar una actualización necesaria.

## Publicación y recuperación

Conservar la clave de firma fuera del repositorio. Una APK con otro certificado no actualiza la instalación existente. Incrementar `versionCode` cuando se establezca la política de releases y conservar notas de cada entrega.

Antes de operar sobre datos reales, guardar un respaldo completo. La sincronización propaga también errores y eliminaciones. La recuperación real en otra instalación y la revisión de la primera limpieza programada siguen siendo comprobaciones operativas pendientes.

## Documentación derivada

Los árboles, el inventario y el DDL SQLite documentan el código que existe al generar este conjunto; no son una segunda fuente editable del esquema. Después de modificar código o tablas, regenerarlos o corregirlos junto con las fuentes.

## Historial del programador

El historial de ejecuciones de Cron en cron.job_run_details es independiente del historial de sincronización. La migración 004 no limpia esas ejecuciones. Revisar su crecimiento como parte del mantenimiento del servidor; Supabase indica que no se eliminan automáticamente. Referencia: [inspección y limpieza del historial de Cron](https://supabase.com/docs/guides/cron/quickstart).
