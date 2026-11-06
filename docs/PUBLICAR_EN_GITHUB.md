# Preparar y subir el repositorio

Esta preparación no crea ni publica un repositorio de GitHub automáticamente. El paquete contiene código y documentación para que su titular elija dónde subirlo.

## Contenido que se conserva

- Módulo `app`, fuentes Java, recursos e iconos.
- Pruebas con datos sintéticos.
- Gradle Wrapper, scripts y catálogo de dependencias.
- Migraciones SQL 001 a 004 sin credenciales.
- Documentación, `.gitignore` y `.gitattributes`.
- Verificador PostgreSQL de desarrollo y su archivo de dependencias.

## Contenido excluido

- `local.properties`, `.idea`, `.gradle`, compilaciones y cachés.
- APK generadas y claves de firma (`.jks`, `.keystore`, `keystore.properties`).
- `.env`, credenciales, sesiones y tokens exportados.
- Bases SQLite, respaldos `.agendabak`, ODS y CSV personales.
- Paquetes históricos JSON personales y bitácora local acumulativa.
- `node_modules`, archivos de trabajo y salidas locales.

`.gitignore` no elimina archivos ya versionados ni limpia commits antiguos. Revisar el contenido que se agregue. Si el proyecto ya estaba versionado y un archivo privado figura en el índice o en el historial, no alcanza con agregar una regla nueva.

## Método con GitHub Desktop

1. Extraer el paquete limpio a una carpeta de trabajo.
2. Agregar esa carpeta o crear allí un repositorio local con GitHub Desktop.
3. Revisar los archivos seleccionados para el primer commit.
4. Crear el commit y elegir Publish repository.
5. Elegir visibilidad. Una agenda personal puede tener un repositorio de código sin incluir ninguno de sus datos.

No cargar simplemente el ZIP como un único archivo: GitHub debe recibir los archivos extraídos para mostrar el código y el README.

## Método con Git

Desde la raíz extraída, si todavía no existe repositorio local:

```sh
git init
git add .
git diff --cached --stat
git diff --cached
git commit -m "Documenta y prepara Agenda Personal"
git branch -M main
```

Crear en GitHub un repositorio vacío. Sustituir `URL_DEL_REPOSITORIO` por su URL real:

```sh
git remote add origin URL_DEL_REPOSITORIO
git push -u origin main
```

Si ya existe historial o un remoto, adaptar esos pasos; no reemplazarlo sin revisarlo.

## Antes de publicar releases

Definir una licencia si se quiere conceder permiso de reutilización, establecer una clave de firma conservada fuera de Git e incrementar la versión del APK. El proyecto no incluye esos secretos ni inventa una licencia por el titular.

Las APK se distribuyen como adjuntos de una release, con su versión y notas, en lugar de agregarlas al código fuente. Mantener el mismo certificado para actualizar instalaciones existentes; consultar [INSTALACION.md](INSTALACION.md).

No adjuntar respaldos ni capturas que contengan datos reales. Para ilustrar la app, usar una instalación con datos ficticios y aclarar que son ejemplos.
