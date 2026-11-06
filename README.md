# Agenda Personal

Aplicación Android para organizar tareas, eventos y actividades personales, con funcionamiento sin conexión, recordatorios, recurrencias y sincronización opcional con Supabase.

Los datos se guardan primero en SQLite. Al sincronizar, Supabase mantiene los registros en las tablas de cada módulo, aunque ningún otro dispositivo se conecte. El historial técnico puede depurarse automáticamente con la migración incluida, conservando los últimos 90 días.

## Funciones

- Agenda mensual y módulos de Tareas, Eventos, Estudios, Sociales, Salud, Trabajo, Freelance, Economía, Ejercicio y Estado emocional.
- Personas, direcciones y catálogos relacionados; frases personales e investigaciones con observaciones.
- Prioridades separadas de los estados: `ALTA` y `MEDIA` son prioridades; `URGENTE` es un estado calculado por fecha.
- Recordatorios Android y ventana flotante opcional agrupada por categoría.
- Recurrencias semanales, mensuales, anuales y personalizadas cada N días, semanas, meses o años, con finalización opcional.
- Inicio con progreso del día, cara según las actividades de hoy y radar inicialmente colapsado con período independiente.
- Cinco temas: Lavanda, BOSQUE Día, BOSQUE Night, ESPACIAL Noche y ESPACIAL Sunrise. BOSQUE Night combina fondos verdes con botones azul pizarra.
- Sincronización automática o manual, detección de conflictos y confirmaciones acumuladas.
- CSV por módulo, respaldo completo cifrado e importación histórica preparada en JSON. Son funciones distintas; ver [formatos de datos](docs/DATOS_Y_RESPALDOS.md).

## Empezar

1. Abrir la raíz del proyecto en Android Studio.
2. Instalar Android SDK 36 y usar el JDK integrado de Android Studio.
3. Esperar la sincronización de Gradle y compilar el módulo `app`.
4. Ejecutar en Android 8.0 / API 26 o superior.
5. Para habilitar la nube, seguir [la configuración de Supabase](docs/SUPABASE.md). La app permite uso local sin conectar una cuenta.

En Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

La APK de desarrollo se genera en `app/build/outputs/apk/debug/app-debug.apk`. No se incluye en Git: las APK destinadas a distribución pueden adjuntarse a GitHub Releases.

## Entorno del proyecto

| Elemento | Configuración actual |
| --- | --- |
| Lenguaje y vistas | Java y XML Android |
| Compatibilidad de código Java | Java 11 |
| Entorno usado para compilar | JDK 21 integrado en Android Studio |
| Gradle Wrapper | 8.11.1 |
| Android Gradle Plugin | 8.10.1 |
| Android mínimo | API 26 |
| `compileSdk` / `targetSdk` | 36 / 35 |
| Paquete | `com.example.agendapersonal` |
| Versión declarada | `1.0`, código `1` |
| Base local | SQLite, versión de esquema 2 |

La versión declarada todavía no diferencia las entregas de desarrollo realizadas hasta ahora. Se debe establecer una numeración de versiones antes de publicar releases sucesivas.

## Documentación

| Documento | Contenido |
| --- | --- |
| [Instalación y compilación](docs/INSTALACION.md) | Entorno, APK, actualizaciones y firma |
| [Supabase](docs/SUPABASE.md) | Migraciones, cuenta, sincronización, RLS y retención |
| [Arquitectura](docs/ARQUITECTURA.md) | Código Android, almacenamiento y flujo de datos |
| [Reglas funcionales](docs/REGLAS_FUNCIONALES.md) | Fechas, prioridades, estados, Inicio y recurrencias |
| [Datos y respaldos](docs/DATOS_Y_RESPALDOS.md) | CSV, JSON histórico y copia cifrada |
| [Pruebas](docs/PRUEBAS.md) | Cómo ejecutarlas y qué fue verificado |
| [Estado y pendientes](docs/ESTADO.md) | Situación conocida y límites de la validación |
| [Árboles de archivos](docs/ARBOLES_ARCHIVOS.md) | Estructura real de fuentes, recursos, pruebas y herramientas |
| [Blueprints](docs/BLUEPRINTS.md) | Contexto, componentes, flujos y relaciones editables |
| [Diccionario de datos](docs/DICCIONARIO_DATOS.md) | Tablas, columnas, claves y tipos |
| [Inventario Java](docs/INVENTARIO_CODIGO.md) | Archivos y enlaces de navegación |
| [Protocolo de sincronización](docs/PROTOCOLO_SYNC.md) | RPC, sobres, revisiones y conflictos |
| [Mantenimiento](docs/MANTENIMIENTO.md) | Diagnóstico y evolución del esquema |
| [Decisiones técnicas](docs/DECISIONES_TECNICAS.md) | Criterios y consecuencias de diseño |
| [Preparar GitHub](docs/PUBLICAR_EN_GITHUB.md) | Qué incluir, revisar y subir al repositorio |
| [Historial de cambios](CHANGELOG.md) | Resumen de la implementación actual |

## Estructura

```text
app/src/main/java/com/example/agendapersonal/
  avisos/       Recordatorios y ventana flotante
  data/         SQLite e importación histórica
  eventos/      Reglas y series recurrentes
  model/        Modelos de datos
  repository/   Persistencia y registro de cambios
  respaldo/     Copias cifradas y restauración
  sync/         Sesión, transporte y sincronización
  ui/           Pantallas, formularios y temas
  util/         CSV, validaciones y utilidades
app/src/main/res/   Layouts, iconos, colores y paisajes
app/src/test/       Pruebas locales
app/src/androidTest/ Pruebas instrumentadas
migrations/        Configuración y evolución de Supabase
docs/              Documentación vigente
```

## Datos y credenciales

El repositorio no necesita credenciales reales para compilar. La URL de Supabase, la clave pública y el usuario de la app se configuran desde la pantalla de conexión. Las credenciales administrativas no forman parte del cliente Android.

La base SQLite local y los datos funcionales de Supabase no tienen cifrado de extremo a extremo implementado. La sesión local usa Android Keystore y el respaldo completo usa cifrado propio. Ver las diferencias en [arquitectura](docs/ARQUITECTURA.md).

No subir bases personales, archivos históricos, respaldos, sesiones ni claves de firma. El `.gitignore` preparado excluye estos archivos habituales; también hay que revisar lo que se agregue a Git.

## Licencia

La licencia de distribución del proyecto todavía no está definida. No se incorpora una licencia de código abierto sin decisión de su titular.
