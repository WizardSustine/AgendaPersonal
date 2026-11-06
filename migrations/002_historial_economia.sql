-- Ampliación incremental del historial para los estados transversales de Economía.
-- Revisar y aplicar una sola vez sobre el proyecto que ya recibió migración_001_v2.
-- No elimina tablas ni registros. Esta migración todavía NO fue ejecutada en Supabase.
begin;
alter table public.historial_postergaciones
  drop constraint if exists historial_postergaciones_tipo_entidad_check;
alter table public.historial_postergaciones
  add constraint historial_postergaciones_tipo_entidad_check
  check (tipo_entidad in ('EVENTO','TAREA','ESTUDIO','SOCIAL','SALUD','TRABAJO','FREELANCE','INGRESO','PAGO','GASTO'));
commit;
