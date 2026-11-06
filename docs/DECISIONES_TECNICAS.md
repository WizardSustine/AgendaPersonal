# Decisiones técnicas

## SQLite como base local

La agenda debe funcionar sin conexión. Las operaciones de usuario se guardan localmente y el transporte se reintenta. Consecuencia: la sincronización no debe destruir cambios locales ante una respuesta perdida o una versión remota diferente.

## Estado remoto permanente e historial temporal

El primer protocolo transportaba el estado mediante logs. Se incorporó la persistencia transaccional en los módulos y un índice de estado actual, con retención de logs de 90 días. Consecuencia: una PC desconectada no condiciona el guardado del teléfono y un cliente nuevo no depende de logs ya eliminados.

## Conflictos explícitos

No se elige automáticamente la última fecha de reloj como ganador. Se usan revisiones, base conocida y candidatos de resolución. Consecuencia: se conservan ramas divergentes y el usuario debe decidir en los casos que no se puedan aplicar de forma segura.

## Bajas lógicas

Las entidades mantienen UUID, revisión y `deleted_at`. Consecuencia: una copia antigua no debe recrear silenciosamente un registro eliminado. La limpieza técnica no borra estas marcas.

## Recurrencias virtuales con excepciones

Se calcula la serie y se materializa lo que se modifica, con un UUID estable por ordinal. Consecuencia: no se crean infinitas filas para eventos futuros y la sincronización debe conservar la identidad de las excepciones.

## Inicio diario y radar independiente

El indicador principal responde al día funcional actual. El radar permite consultar períodos históricos. Consecuencia: la importación de actividades antiguas no altera el porcentaje diario por el hecho de importar o crear filas hoy.

## Formatos de datos provisionalmente distintos

El requisito original era CSV. Se implementaron CSV por módulo, JSON para el paquete histórico y `.agendabak` para restauración completa cifrada. El usuario decidió conservar esa situación por ahora. Consecuencia: no se debe prometer una copia completa CSV ni cambiar los formatos sin acordarlo.

## Temas por atributos

Se separan colores semánticos de fondos, botones, módulos y frases. Consecuencia: el azul pizarra aprobado para BOSQUE Night no recolorea los fondos verdes ni los otros temas.

## Entregas de desarrollo

Se compilaron APK Debug y se verificó compatibilidad de certificado entre entregas. Todavía falta definir licencia, firma de producción y numeración de versiones. Consecuencia: un clon no debe generar y distribuir una clave distinta asumiendo que actualizará instalaciones previas.
