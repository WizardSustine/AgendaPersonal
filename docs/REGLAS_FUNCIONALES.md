# Reglas funcionales

## Prioridad y estado

Prioridades persistidas: `ALTISIMA`, `ALTA`, `MEDIA`, `BAJA`, `BAJISIMA`. La prioridad inicial de los formularios correspondientes es `MEDIA`.

Estados especiales persistidos: `FINALIZADO`, `CANCELADO`, `POSTERGADO`. Tienen precedencia sobre el cálculo por fecha.

Los demás estados se calculan con relación al día local del dispositivo:

| Distancia a la fecha funcional | Estado |
| --- | --- |
| Fecha anterior a hoy | `ATRASADO` |
| Hoy | `CRITICO` |
| De 1 a 7 días | `URGENTE` |
| De 8 a 30 días | `PENDIENTE` |
| Más de 30 días | `MUCHO` |
| Sin fecha | `SIN FECHA` |

`URGENTE` no es una prioridad ni se agrega al enumerado de prioridades.

## Cara y progreso del día

La barra y el anillo representan actividades finalizadas de hoy sobre actividades de hoy. No usan la fecha de creación o importación. Las tareas usan su vencimiento; los otros módulos usan la fecha funcional definida en `Definicion.fecha()`.

El resumen actual comprende Estudios, Sociales, Salud, Trabajo, Freelance, Eventos y Tareas. También incluye las ocurrencias virtuales de eventos de hoy. No incorpora automáticamente módulos que no tienen el mismo concepto de finalización, como Ejercicio o Estado emocional.

Se excluyen bajas, canceladas y postergadas. La categoría `OTRA` sí se cuenta. Las actividades históricas o futuras no influyen en el indicador del día.

- Preocupación: hay actividades pendientes de hoy cuyo horario ya pasó.
- Expresión neutra: hay pendientes de hoy sin horario vencido, o no hay actividades para hoy.
- Sonrisa: todas las actividades computables de hoy están finalizadas.

Sin actividades computables, el porcentaje es cero y se informa Sin actividades para hoy. El resumen se actualiza al volver a Inicio y cada minuto mientras la pantalla permanece activa.

El radar se inicia colapsado y conserva un selector de historial, mes o año independiente. Los módulos de Economía conservan su gráfico propio; no hay gráfico lineal en Inicio.

## Tareas y agenda

Las tareas tienen fecha y hora de inicio y de vencimiento. En el calendario se muestran al inicio y al vencimiento, no se expanden a cada día intermedio. Los recordatorios de tareas toman el inicio menos la anticipación en minutos, no el vencimiento.

Finalizar, cancelar o postergar afecta sus avisos activos. La postergación funcional registra el cambio y su historial; se debe usar la acción de postergar, no modificar ese estado como un texto aislado.

## Recurrencias de eventos

Se admiten semanas, meses, años e intervalos personalizados cada N días, semanas, meses o años. La fecha final es opcional e inclusiva. Las acciones distinguen una ocurrencia, esta y siguientes, o toda la serie, según la operación presentada.

Las ocurrencias no modificadas se calculan. Editar sólo una ocurrencia materializa una excepción sin desplazar automáticamente las demás. El ordinal mantiene el vínculo con la serie y su fecha original.

Ejemplos probados:

- Serie semanal: cancelar sólo el segundo evento conserva los otros.
- Desde el 31 de diciembre, cada dos meses hasta el 31 de agosto: 31/12, 28/02, 30/04, 30/06 y 31/08; no hay evento posterior al límite. Postergar febrero no modifica los siguientes.

## Separación entre módulos

El valor acordado en Freelance no crea un ingreso automático en Economía. La periodicidad de un Pago no genera ocurrencias de eventos. Los registros emocionales no establecen causalidades automáticas con otras actividades. Investigaciones conserva los datos de origen y permite observaciones sin presentar todas las operaciones de alta, edición y baja de los módulos comunes.
