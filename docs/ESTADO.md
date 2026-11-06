# Estado del proyecto

Actualización de esta documentación: **5 de noviembre de 2026**. Describe el código disponible y las confirmaciones conocidas; las entregas de desarrollo todavía declaran `versionName=1.0` y `versionCode=1`.

## Implementado

- Módulos Android con almacenamiento local, relaciones, catálogos y bajas lógicas.
- Sesión protegida, sincronización manual/automática y resolución manual de conflictos.
- Persistencia remota en tablas funcionales mediante protocolo 2, recuperación por estado actual y limpieza técnica de 90 días.
- Recordatorios y ventana flotante opcional por categoría.
- Recurrencia estándar/personalizada y excepciones por ocurrencia.
- CSV por módulo, importación histórica preparada y respaldo completo cifrado.
- Inicio diario independiente del radar, contadores de sincronización durables y cinco temas.
- BOSQUE Night actualizado con botones azul oscuro suave y frase más clara.

## Confirmado durante la puesta en marcha personal

| Hecho | Alcance de la confirmación |
| --- | --- |
| Nueva tarea visible en `public.tareas` | Guardado funcional remoto desde el teléfono, sin depender de PC |
| Importación histórica finalizada | Resumen de importación confirmado por el usuario; filas para revisar permanecen pendientes |
| Sincronización automática activa | Informado por el usuario; no se auditó cada registro remoto |
| Migración 004 ejecutada | El usuario informó `schedule: 1`; no se comprobó una ejecución efectiva de limpieza |
| APK de BOSQUE Night generada | Compilación y certificado verificados; falta la revisión visual final del usuario |

Estas condiciones corresponden a la puesta en marcha existente. Un clon del repositorio no trae ese proyecto Supabase, cuenta, sesión ni datos personales.

## Pendientes

1. Comprobar recuperación real de la agenda importada en otra instalación, antes de depender de ella como único mecanismo de recuperación.
2. Revisar el primer resultado real del trabajo de retención en Supabase.
3. Resolver las filas históricas que requieren revisión individual, fuera del repositorio público.
4. Validar el flujo completo de restauración poblada y posterior reconexión.
5. Completar la revisión visual y de permisos/batería en los dispositivos de destino.
6. Resolver la diferencia con el requisito original de usar siempre CSV: hoy el respaldo completo y el paquete histórico usan formatos diferentes, conservados provisionalmente por decisión del usuario.
7. Definir licencia, firma de producción y numeración de releases.

No se presenta como producto publicado en Google Play, aplicación multiusuario ni cliente de escritorio terminado. El código de este repositorio es Android; una PC futura debe implementar el mismo protocolo de estado y conflictos.

## Documentación histórica

`REGISTRO_TECNICO_AGENDA_PERSONAL.md` es una bitácora local acumulativa con rutas de la computadora, hitos y notas antiguas. Puede contener afirmaciones que fueron reemplazadas por implementaciones posteriores. No se incluye en el paquete limpio para GitHub y se excluye de Git. La referencia vigente es este conjunto `README.md` + `docs/` y el código.
