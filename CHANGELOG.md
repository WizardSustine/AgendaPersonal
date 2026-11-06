# Historial de cambios

Se documenta el estado de desarrollo sin asignar versiones publicadas que todavía no existen. Las APK actuales siguen declarando versión 1.0 / código 1.

## Sin publicar — 2026-11-05

### Funciones incorporadas

- Módulos personales, catálogos y relaciones en SQLite.
- Sesión Supabase protegida y sincronización manual o automática con conflictos.
- Protocolo 2: persistencia transaccional en tablas funcionales, snapshots de estado y retención técnica de 90 días.
- Recordatorios Android y superposición opcional con iconos por categoría.
- Recurrencias semanales, mensuales, anuales y personalizadas, con excepciones y finalización opcional.
- CSV por módulo, importación histórica preparada y copia completa cifrada.
- Cinco temas con el mismo ícono de aplicación.

### Correcciones recientes

- Cara y porcentaje basados exclusivamente en actividades de hoy, separados del período del radar.
- Actividades históricas importadas excluidas del progreso del día.
- Totales de sincronización durables, revisión vacía diferenciada del último movimiento y reflejos propios sin contarlos como recepción.
- Botones azul pizarra y frase más clara en BOSQUE Night, conservando los verdes del resto del tema.

### Documentación

- Guías de instalación, migraciones, arquitectura, reglas, formatos de datos y pruebas.
- Migración inicial incluida para configurar proyectos nuevos.
- Preparación de repositorio sin datos personales, respaldos, sesiones ni claves de firma.
- Verificador PostgreSQL aislado con rutas portables.
