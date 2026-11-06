# Pruebas y validación

Las pruebas usan datos sintéticos. Las pruebas instrumentadas deben ejecutarse en un emulador o una instalación destinada a pruebas, no sobre la agenda personal.

## Pruebas Java

Desde la raíz del proyecto en Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Este comando ejecuta el conjunto local disponible en el repositorio. Hay pruebas de CSV, estados, prioridades, horarios de avisos, calendario recurrente, revisiones, motor y progreso diario.

El último lote local registrado para la corrección del día y los contadores incluyó `ReglasSyncTest`, `ReglasEstadoSyncTest`, `MotorSyncTest` y `ResumenDiaTest`: **33 pruebas aprobadas**. Esto no significa que se ejecutara nuevamente cada clase local del repositorio en esa entrega.

## Pruebas Android

Preparar un emulador API 34, seleccionarlo en Android Studio y ejecutar una clase concreta. También se puede usar:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.agendapersonal.DiaSyncInstrumentedTest"
```

`DiaSyncInstrumentedTest` incluye por herencia comprobaciones de persistencia anteriores: **23 pruebas aprobadas**, sin errores. Entre ellas:

- Recepción de una revisión alta sin disponer del historial anterior.
- Conservación de cambios locales y conflictos.
- Bajas que no resucitan ante una copia antigua.
- Limpieza de transporte sin borrar los registros.
- Recuperación de confirmaciones anteriores y contadores que sobreviven al reinicio.
- Reintentos que no duplican los contadores y reflejos propios que no cuentan como recepción.
- 602 tareas históricas creadas hoy excluidas del progreso diario.
- Recurrencias de hoy sin duplicar excepciones guardadas.

Los **dos casos de recurrencia acordados** se ejecutan aparte:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.agendapersonal.eventos.RecurrenciaDosCasosInstrumentedTest"
```

Resultado registrado: **2 pruebas aprobadas**. Cubren la serie semanal con excepción individual y el intervalo de dos meses con finalización inclusiva. No disparan sincronización ni alarmas del dispositivo.

`GestorRespaldoInstrumentedTest` tiene comprobaciones aisladas de snapshot, relaciones, restauración y rollback. La validación anterior de respaldo registró siete casos Android y otras pruebas de cifrado y reglas de cuenta. No se contabilizan otra vez como parte de las 23 pruebas actuales.

## PostgreSQL aislado

En `tools/pruebas-postgres/` se conserva un verificador de las migraciones con PGlite, sin conexión a Supabase real. Requiere Node.js con soporte de módulos ES y npm.

```powershell
cd tools/pruebas-postgres
npm ci
npm test
```

Resultado registrado: **26 pruebas aprobadas**. Verifica persistencia de los 25 módulos y catálogos, un único teléfono sin recibos de PC, reintentos, transacciones, RLS y propietarios, recuperación sin logs antiguos, conflictos, bajas, datos JSON, migración de cambios anteriores y retención por fecha de recepción.

El verificador crea una función `auth.uid()` de prueba y simula la programación de Cron. No prueba el servicio de autenticación alojado, la red del teléfono ni la ejecución real de `pg_cron`. La simulación se limita al entorno de pruebas.

## Evidencia de uso real

Durante la puesta en marcha se informó:

- Recepción de notificaciones y funcionamiento de la ventana flotante.
- Cancelación de avisos al finalizar, cancelar o postergar.
- Una tarea nueva visible en `public.tareas` después de instalar el protocolo 2.
- Creación de copia completa e importación del paquete histórico.
- Activación de sincronización automática.
- Ejecución de la migración 004 con identificador de trabajo devuelto.

Estas son confirmaciones del usuario; no sustituyen una inspección remota independiente de todas las entidades.

## Qué falta comprobar

- Recuperación real del estado completo en una segunda instalación después de la importación.
- Ejecución efectiva y resultados de Cron en el proyecto alojado.
- Restauración poblada y reconexión de extremo a extremo.
- Revisión de las filas históricas no incorporadas automáticamente.
- Apariencia final de cada tema y los últimos ajustes en el teléfono de destino.

Las entregas APK recientes compilaron y se verificó su firma con Android apksig. En el ajuste de colores BOSQUE Night se comprobaron XML, contraste, compilación y firma; no se repitió toda la batería funcional por un cambio visual.
