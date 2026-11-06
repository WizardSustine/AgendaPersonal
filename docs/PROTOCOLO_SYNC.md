# Contrato del protocolo de sincronización 2

Referencia de implementación: `sync/SupabaseRest.java`, `sync/MotorSync.java`, `sync/AlmacenSync.java` y `migrations/003_persistencia_sync.sql`.

## Transporte y autenticación

HTTPS con cabeceras `apikey` y `Authorization: Bearer <access_token>`. El token corresponde al usuario de la aplicación. Los endpoints públicos de funciones obtienen `auth.uid()` del servidor y validan el propietario. No se utiliza una credencial administrativa en Android.

| Operación | Endpoint | Resultado |
| --- | --- | --- |
| Iniciar sesión | `POST /auth/v1/token?grant_type=password` | Tokens; verificación adicional de usuario |
| Renovar sesión | `POST /auth/v1/token?grant_type=refresh_token` | Tokens renovados con el mismo propietario |
| Registrar dispositivo | REST de `sync_devices` | Registro idempotente del UUID del dispositivo |
| Enviar cambios | `POST /rest/v1/rpc/agenda_sync_push` | Sobres confirmados con estado de persistencia |
| Leer estado | `POST /rest/v1/rpc/agenda_sync_snapshot` | Página del estado actual y divergencias conservadas |

La URL del proyecto no admite rutas, consultas ni credenciales incrustadas. Las solicitudes tienen tiempos de espera y un límite de respuesta definido en el cliente. Una respuesta de error no autoriza a marcar datos como enviados.

## Sobre de un cambio

Ejemplo estructural con valores sintéticos:

```json
{
  "change_id": "00000000-0000-4000-8000-000000000010",
  "owner_id": "00000000-0000-4000-8000-000000000001",
  "device_id": "00000000-0000-4000-8000-000000000002",
  "entity_type": "personas",
  "entity_id": "00000000-0000-4000-8000-000000000020",
  "base_revision": 0,
  "entity_revision": 1,
  "operation": "UPSERT",
  "payload": {
    "id": "00000000-0000-4000-8000-000000000020",
    "owner_id": "00000000-0000-4000-8000-000000000001",
    "nombre": "Persona de prueba",
    "otro_dato": null,
    "created_at": "2026-11-05T12:00:00Z",
    "updated_at": "2026-11-05T12:00:00Z",
    "revision": 1,
    "deleted_at": null
  },
  "created_at": "2026-11-05T12:00:00Z"
}
```

`payload` es la entidad completa, no un parche parcial. El conjunto de columnas debe coincidir con el esquema admitido, más los metadatos reconocidos. La lista de entidades es cerrada: `agenda_internal.tablas()` en el servidor y `Definicion` en Android.

La revisión nueva es la base más uno. El propietario, UUID y revisión del payload coinciden con el sobre. `DELETE` exige `deleted_at`; es una baja lógica, no un borrado físico del registro funcional.

## Envío

Petición RPC:

```json
{ "cambios": [] }
```

La lista contiene hasta 100 sobres. El servidor serializa operaciones por propietario con un bloqueo transaccional, verifica las relaciones también por propietario y procesa dependencias en orden de módulos y revisión. Booleans y columnas JSON se convierten desde su representación SQLite sin alterar el payload de transporte conservado.

Cada respuesta incluye el sobre y `persistencia`:

| Estado | Significado | Cliente |
| --- | --- | --- |
| `APPLIED` | Registro materializado en el módulo | Puede confirmar el envío |
| `RESOLVED` | Versión resuelta por una resolución conservada | Puede confirmar el envío |
| `CONFLICT` | Rama divergente guardada sin pisar la aceptada | Confirma su conservación; se presenta para resolver |
| `WAITING` | Faltan revisiones o relaciones para materializar | No confirma el envío como guardado en su módulo |

El cliente compara el contenido devuelto mediante `JsonSync.mismoCambio`. Un UUID repetido con contenido distinto se rechaza. La pérdida de una respuesta permite reintentar sin duplicar el cambio confirmado. Una rama guardada como conflicto no equivale a que esa rama sea la versión funcional aceptada.

No se espera la conexión de otra instalación ni un recibo de PC para persistir los datos.

## Lectura del estado actual

Petición:

```json
{ "despues": null, "limite": 100 }
```

`despues` es el UUID de cambio al final de la página anterior. El recorrido se reinicia en cada ciclo, sin cursor permanente ni dependencia del reloj del teléfono. Se incluyen los registros del propio dispositivo para verificar su estado y establecer una base local conocida.

Los sobres de estado actual llevan `_snapshot: true` en el nivel superior. También pueden aparecer sobres conservados de cambios en conflicto o espera. No se persiste un historial infinito de snapshots remotos: `heads` mantiene una imagen vigente por entidad.

En Android:

- Una entidad inexistente puede incorporarse directamente en una revisión alta, aunque el historial intermedio ya haya sido depurado.
- Una entidad idéntica se reconoce sin contabilizar una incorporación nueva.
- Para sustituir una entidad diferente por una revisión remota mayor se exige que siga igual a su base remota conocida, sin salida pendiente ni conflicto de esa entidad.
- Si no se cumple esa condición, se conserva un conflicto.
- Las FK aún pendientes se reintentan después de aplicar dependencias de otras páginas.

El recorrido no es una instantánea global inmutable de toda la cuenta: otra instalación puede escribir entre páginas. Se vuelve a recorrer el estado en ciclos siguientes, conservando las revisiones y divergencias.

## Metadatos de entidad

| Clave | Uso |
| --- | --- |
| `_agenda_sync_resolution_v1` | Dos candidatos y cambios superados por una resolución manual |
| `_agenda_historico_v1` | Huella de la fuente y registro original de procedencia |
| `_agenda_recurrencia_v1` | Serie, ordinal y fecha original de una ocurrencia |

Se separan de las columnas funcionales antes de escribirlas. El índice privado remoto y las tablas auxiliares locales conservan la información necesaria. No eliminar estos metadatos al añadir importadores o clientes nuevos.

## Conflictos, contadores y limpieza

Una resolución genera un cambio nuevo con revisión superior y candidatos explícitos. El servidor valida que incluya el estado vigente antes de liberar los conflictos señalados. Las marcas de baja participan del mismo mecanismo y no se eliminan con el log técnico.

Los contadores locales se guardan en `sync_meta` junto con sus confirmaciones. Se recuperan confirmaciones previas del historial conservado al inicializarlos. Un reintento no suma dos veces y la recepción del reflejo propio no aumenta las incorporaciones de otros dispositivos. No son un conteo de registros únicos.

La limpieza remota usa la fecha de recepción del servidor y requiere estado aplicado o resuelto. La limpieza local exige transporte confirmado y se pospone si quedan entradas sin aplicar. Los registros funcionales no se purgan por estas operaciones.

## Evolución del contrato

Un nuevo campo requiere migración remota, esquema local y adaptación de modelos, repositorio, definición, payload y respaldo. Un cliente de PC futuro debe respetar estas mismas reglas. No escribir directamente las tablas como alternativa al RPC ni añadir un segundo canal de guardado independiente.
