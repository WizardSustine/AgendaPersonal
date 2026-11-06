# Cambios en el proyecto

Antes de modificar comportamiento, leer [las reglas funcionales](docs/REGLAS_FUNCIONALES.md), [la arquitectura](docs/ARQUITECTURA.md) y [el estado conocido](docs/ESTADO.md).

- Conservar los nombres y campos funcionales existentes, salvo una migración acordada.
- Separar prioridades, estados especiales y estados calculados.
- Mantener escrituras locales y cambios de sincronización consistentes y transaccionales.
- No introducir una limpieza que elimine registros funcionales o conflictos sin resolver.
- Mantener UUID, revisiones, bajas y metadatos de recurrencia/procedencia en importación, copia y sincronización.
- Usar fixtures sintéticos y bases aisladas para probar.
- No incorporar credenciales, datos personales ni claves de firma.
- Documentar el comportamiento resultante, las comprobaciones realizadas y sus límites.

Ejecutar las pruebas que correspondan al cambio según [PRUEBAS.md](docs/PRUEBAS.md). Un cambio visual requiere al menos compilación y revisión de contraste; una modificación de persistencia requiere pruebas de pérdida de respuesta, reintento, conflictos y recuperación.

No se ha definido todavía una licencia de distribución. Consultar al titular antes de establecer condiciones de uso o publicar una release en su nombre.
