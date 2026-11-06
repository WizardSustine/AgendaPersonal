# Instalación y compilación

## Preparar el entorno

1. Instalar Android Studio y abrir la carpeta que contiene `settings.gradle.kts`.
2. Seleccionar el JDK integrado de Android Studio para Gradle. Las compilaciones verificadas usaron JDK 21.
3. Instalar Android SDK Platform 36 desde SDK Manager y permitir que Gradle obtenga las dependencias declaradas.
4. Configurar un emulador o conectar un teléfono Android API 26 o superior. Las pruebas instrumentadas registradas se ejecutaron en un emulador Android 14 / API 34.

Android Studio crea `local.properties` con la ubicación del SDK de esa computadora. Ese archivo no se debe copiar entre equipos ni subir al repositorio.

La compatibilidad de fuentes y bytecode Java está configurada en Java 11. Es distinta del JDK con el que se ejecuta Gradle.

## Compilar

Windows / PowerShell, desde la raíz:

```powershell
.\gradlew.bat :app:assembleDebug
```

Linux o macOS:

```sh
chmod +x gradlew
./gradlew :app:assembleDebug
```

Resultado: `app/build/outputs/apk/debug/app-debug.apk`.

También se puede compilar y ejecutar la configuración `app` desde Android Studio. Comprobar el dispositivo seleccionado antes de ejecutarla: una prueba debe apuntar al emulador previsto, no a un teléfono con datos reales.

## Instalar o actualizar una APK

Abrir la APK en Android y aceptar la instalación o actualización. Para conservar los datos, instalar encima de la aplicación existente, sin desinstalarla ni borrar su almacenamiento.

Una actualización requiere el mismo identificador de aplicación y un certificado de firma compatible. Compilar en otra computadora con otra clave Debug puede producir una APK que no actualice la instalación anterior. Si Android rechaza la actualización, revisar la firma antes de desinstalar.

No se incluye una clave de firma privada en el repositorio. La configuración actual es de desarrollo; no tiene un proceso de firma de producción definido. Antes de distribuir versiones estables, definir una clave de release conservada fuera de Git e incrementar `versionCode` con cada entrega.

## Configurar la app

- Uso local: crear registros sin conectar Supabase.
- Nube: Ajustes → Conectar con Supabase; ver [SUPABASE.md](SUPABASE.md).
- Avisos: Ajustes → Notificaciones. Android puede solicitar permisos de notificaciones y de alarmas exactas.
- Ventana flotante: requiere además el permiso de superposición y activar la opción de la app. Es una ventana propia con categorías, no una implementación del protocolo de burbujas de conversación de Android.
- Aspecto: Ajustes → Apariencia → elegir tema → Aplicar tema.

## Primera comprobación de sincronización

Crear una tarea sintética, sincronizar y verificar su descripción en la tabla `public.tareas` de Supabase. No alcanza con verla en `sync_changes`. El guardado remoto no depende de que otra computadora reciba los cambios.

El resumen de conexión distingue la revisión más reciente, la última transferencia con movimientos, los totales confirmados y los cambios actualmente pendientes. Los contadores representan cambios o versiones, no registros únicos.
