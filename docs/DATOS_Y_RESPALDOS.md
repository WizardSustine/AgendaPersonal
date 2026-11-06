# Datos, importaciones y respaldos

## Formatos actuales

| Función | Formato | Alcance |
| --- | --- | --- |
| Importar / exportar por módulo | CSV UTF-8 | Registros del módulo seleccionado |
| Importar agenda anterior | JSON preparado | Paquete validado con registros y filas para revisar |
| Copia completa | `.agendabak` cifrado | SQLite, relaciones, historial y metadatos de la app |

El requerimiento original era trabajar con CSV. La importación histórica y el respaldo completo se implementaron con otros formatos; se aceptó mantenerlos provisionalmente. No deben documentarse como una restauración completa en CSV ni considerarse ese requerimiento definitivamente resuelto.

## CSV

La app permite elegir un módulo desde Ajustes y usar el selector de archivos de Android. Se preservan Unicode, comillas y saltos de línea dentro de celdas. Los identificadores existentes se omiten, incluidos los registros dados de baja, para evitar sobrescribir o resucitar datos silenciosamente.

Importar Personas y catálogos antes que sus registros dependientes. La importación CSV puede detenerse después de haber guardado parte del archivo; informa el resultado. No es una restauración transaccional completa de toda la agenda.

## Importación histórica

`ImportadorHistorico` procesa un paquete JSON previamente preparado; no interpreta directamente el archivo ODS original ni cualquier JSON arbitrario. Conserva identificadores, relaciones y datos de procedencia. La carga preparada es transaccional y omite los UUID que ya existen.

El paquete personal usado durante la puesta en marcha, sus archivos ODS/CSV y las filas pendientes de revisión no se incluyen en Git. Para reproducir pruebas, usar datos sintéticos de las clases de prueba. No volver a importar el paquete personal al instalar una actualización de APK.

La importación en el teléfono fue confirmada por su usuario. Esto no equivale a una auditoría individual de todas las filas ni resuelve automáticamente las filas que el paquete dejó pendientes de revisión.

## Respaldo completo

Desde Ajustes → Copia completa y recuperación se exporta una copia `.agendabak` protegida por contraseña. El paquete contiene un manifiesto y una imagen consistente de SQLite; no es un CSV y no se abre con una hoja de cálculo.

El formato usa AES-256-GCM en bloques autenticados y PBKDF2-HMAC-SHA256. La contraseña no se guarda. La copia no incluye una sesión autenticada reutilizable.

Al restaurar:

1. Se verifica contraseña, integridad, versión y estructura admitida.
2. Se conserva un estado previo interno y se reemplazan filas en una transacción.
3. Se mantiene la pertenencia a la cuenta y se asigna un identificador de dispositivo nuevo.
4. Se pausa la sincronización y se solicita volver a iniciar sesión con la cuenta correspondiente.

Las pruebas aisladas cubren integridad, relaciones, rollback y conservación de metadatos. Sigue pendiente una comprobación de extremo a extremo con una agenda poblada, reconexión y recuperación real en otra instalación.

## Sincronización y recuperación

Supabase conserva el estado compartido; no es una copia histórica independiente frente a cualquier error del usuario. Una baja también se sincroniza. Guardar copias separadas antes de importaciones o restauraciones importantes.

La limpieza del historial técnico a los 90 días no elimina los datos funcionales ni el historial funcional de postergaciones. Ver [SUPABASE.md](SUPABASE.md).
