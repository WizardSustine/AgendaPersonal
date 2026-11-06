-- Aplicar DESPUÉS de instalar 003 y actualizar los dispositivos al protocolo 2.
-- Conserva 90 días desde la recepción en el servidor. No borra datos de módulos,
-- tombstones, procedencia histórica, recurrencia ni conflictos sin resolver.
begin;
do $$begin
 if to_regprocedure('public.agenda_sync_snapshot(uuid,integer)') is null then
  raise exception 'Primero aplicar migración 003';end if;
 if exists(select 1 from agenda_internal.change_state where status='WAITING') then
  raise exception 'Hay cambios aún sin persistir en sus módulos. Resolver antes de activar limpieza';end if;
end$$;
create extension if not exists pg_cron;
update agenda_internal.retention set enabled=true where singleton;
select cron.schedule('agenda-sync-retencion-90-dias','20 3 * * *','select public.agenda_sync_limpiar();');
commit;

-- Inspección administrativa:
-- select * from agenda_internal.retention;
-- select e.status,count(*) from agenda_internal.change_state e group by e.status;
-- select * from cron.job where jobname='agenda-sync-retencion-90-dias';
-- Para pausar sin eliminar nada:
-- update agenda_internal.retention set enabled=false where singleton;
