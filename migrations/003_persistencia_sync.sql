-- Protocolo 2: el servidor persiste el estado antes de confirmar un envío.
-- Requiere 001_v2 y 002. No borra datos, no activa aún la limpieza.
begin;
create schema if not exists agenda_internal;
revoke all on schema agenda_internal from public, anon, authenticated;

create or replace function agenda_internal.tablas() returns text[]
language sql immutable set search_path='' as $$select array[
'personas','direcciones','materias','estudios','clasificaciones_sociales','sociales',
'especialidades','salud','trabajo','freelance','clasificaciones_economia','ingresos',
'pagos','gastos','ejercicios','ambitos_emocionales','motivos_emocionales',
'registros_emocionales','frases_personales','series_eventos','eventos','tareas',
'historial_postergaciones','registros_investigacion','observaciones_ri']::text[]$$;

alter table public.sync_changes add column if not exists received_at timestamptz not null default now();
create index if not exists sync_retention_received on public.sync_changes(received_at);
create table if not exists agenda_internal.heads(
 owner_id uuid not null, entity_type text not null, entity_id uuid not null,
 change_id uuid not null, device_id uuid not null, revision integer not null,
 -- Una sola imagen actual por registro. Conserva la representación de Android,
 -- procedencia histórica y recurrencia; NO acumula versiones del registro.
 payload jsonb not null, created_at timestamptz not null,
 primary key(owner_id,entity_type,entity_id));
create index if not exists agenda_head_page on agenda_internal.heads(owner_id,change_id);
create unique index if not exists agenda_head_change_id on agenda_internal.heads(change_id);
create table if not exists agenda_internal.change_state(
 change_id uuid primary key references public.sync_changes(change_id) on delete cascade,
 status text not null check(status in ('WAITING','APPLIED','CONFLICT','RESOLVED')),
 detail text);
create table if not exists agenda_internal.retention(
 singleton boolean primary key default true check(singleton), enabled boolean not null default false,
 days integer not null default 90 check(days=90), last_run timestamptz, last_deleted bigint);
insert into agenda_internal.retention(singleton) values(true) on conflict do nothing;

-- Las marcas de tiempo funcionales son las que versionó el dispositivo.
-- Sólo el tiempo técnico received_at lo fija el servidor.
do $$declare t text;begin
 foreach t in array agenda_internal.tablas() loop
  execute format('drop trigger if exists trg_%I_updated_at on public.%I',t,t);
  execute format('revoke insert,update,delete on public.%I from authenticated',t);
 end loop;
end$$;
revoke insert,update,delete on public.sync_changes,public.sync_receipts from authenticated;

create or replace function agenda_internal.validar(c jsonb,o uuid) returns void
language plpgsql set search_path='' as $$
declare t text:=c->>'entity_type'; p jsonb:=c->'payload'; esperado text[]; real text[]; begin
 if o is null or (c->>'owner_id')::uuid is distinct from o or
    (p->>'owner_id')::uuid is distinct from o then raise exception 'Cuenta no admitida';end if;
 if t is null or not(t=any(agenda_internal.tablas())) then raise exception 'Módulo no admitido';end if;
 if jsonb_typeof(p) is distinct from 'object' or c->>'change_id' is null or c->>'device_id' is null or
    c->>'entity_id' is null or p->>'id' is distinct from c->>'entity_id' or
    (c->>'entity_revision')::int is distinct from (p->>'revision')::int or
    (c->>'base_revision')::int<0 or
    (c->>'base_revision')::bigint+1 is distinct from (c->>'entity_revision')::bigint then
  raise exception 'Sobre de sincronización inválido';end if;
 perform (c->>'change_id')::uuid,(c->>'device_id')::uuid,(c->>'entity_id')::uuid,(c->>'created_at')::timestamptz;
 if c->>'operation' not in ('UPSERT','DELETE') or c->>'operation' is null or
    ((c->>'operation'='DELETE') is distinct from (p->>'deleted_at' is not null)) then raise exception 'Operación inválida';end if;
 select array_agg(a.attname::text order by a.attname) into esperado from pg_catalog.pg_attribute a
 where a.attrelid=format('public.%I',t)::regclass and a.attnum>0 and not a.attisdropped;
 select array_agg(k order by k) into real from jsonb_object_keys(p) k
 where k not in ('_agenda_sync_resolution_v1','_agenda_historico_v1','_agenda_recurrencia_v1');
 if esperado is distinct from real then raise exception 'Campos incompatibles en %',t;end if;
end$$;

-- Comprueba propiedad incluso cuando el FK normal sólo comprueba el UUID.
create or replace function agenda_internal.referencias(t text,p jsonb,o uuid) returns boolean
language plpgsql set search_path='' as $$
declare fk record; pares record; condicion text; completos boolean; valido boolean; begin
 for fk in select con.oid,con.confrelid,con.conkey,con.confkey from pg_catalog.pg_constraint con
 where con.conrelid=format('public.%I',t)::regclass and con.contype='f' loop
  condicion:='';completos:=true;
  for pares in select a.attname::text origen,b.attname::text destino
   from unnest(fk.conkey,fk.confkey) x(a,b)
   join pg_catalog.pg_attribute a on a.attrelid=format('public.%I',t)::regclass and a.attnum=x.a
   join pg_catalog.pg_attribute b on b.attrelid=fk.confrelid and b.attnum=x.b loop
   if p->>pares.origen is null then completos:=false;end if;
   condicion:=condicion||format(' and f.%I::text=%L',pares.destino,p->>pares.origen);
  end loop;
  if completos then
   execute format('select exists(select 1 from %s f where f.owner_id=$1 %s)',fk.confrelid::regclass,condicion) into valido using o;
   if not valido then return false;end if;
  end if;
 end loop;return true;
end$$;

create or replace function agenda_internal.materializar(o uuid) returns void
language plpgsql set search_path='' as $$
declare c record; h agenda_internal.heads%rowtype; p jsonb; tip record; columnas text; valores text; asignar text;
 avance boolean; actual_owner uuid; resolver boolean; candidato jsonb; maxrev int; begin
 perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(o::text,40501));
 insert into agenda_internal.change_state(change_id,status)
 select change_id,'WAITING' from public.sync_changes where owner_id=o on conflict do nothing;
 loop
  avance:=false;
  for c in select s.* from public.sync_changes s join agenda_internal.change_state e using(change_id)
   where s.owner_id=o and e.status='WAITING'
   order by array_position(agenda_internal.tablas(),s.entity_type),s.entity_id,s.entity_revision,s.received_at,s.change_id loop
   perform agenda_internal.validar(to_jsonb(c),o);
   select * into h from agenda_internal.heads where owner_id=o and entity_type=c.entity_type and entity_id=c.entity_id;
   resolver:=false;
   if h.entity_id is not null and jsonb_typeof(c.payload->'_agenda_sync_resolution_v1'->'candidates')='array' then
    maxrev:=0;
    for candidato in select value from jsonb_array_elements(c.payload->'_agenda_sync_resolution_v1'->'candidates') loop
     if candidato->>'id' is distinct from c.entity_id::text or candidato->>'owner_id' is distinct from o::text then raise exception 'Resolución de otro registro';end if;
     maxrev:=greatest(maxrev,(candidato->>'revision')::int);
     if candidato- '_agenda_sync_resolution_v1'-'_agenda_historico_v1'-'_agenda_recurrencia_v1' =
        h.payload-'_agenda_sync_resolution_v1'-'_agenda_historico_v1'-'_agenda_recurrencia_v1' then resolver:=true;end if;
    end loop;
    resolver:=resolver and jsonb_array_length(c.payload->'_agenda_sync_resolution_v1'->'candidates')=2 and c.entity_revision::bigint=maxrev::bigint+1;
   end if;
   if h.entity_id is not null and c.base_revision<>h.revision and not resolver then
    update agenda_internal.change_state set status='CONFLICT',detail='Revisión divergente; ambas versiones conservadas' where change_id=c.change_id;
    continue;
   end if;
   if h.entity_id is null and c.base_revision<>0 then continue;end if;
   if not agenda_internal.referencias(c.entity_type,c.payload,o) then continue;end if;
   execute format('select owner_id from public.%I where id=$1',c.entity_type) into actual_owner using c.entity_id;
   if actual_owner is not null and actual_owner<>o then raise exception 'UUID perteneciente a otra cuenta';end if;
   p:=c.payload-'_agenda_sync_resolution_v1'-'_agenda_historico_v1'-'_agenda_recurrencia_v1';
   -- SQLite representa booleanos como 0/1 y JSON como texto.
   for tip in select a.attname::text nombre,ty.typname from pg_catalog.pg_attribute a
    join pg_catalog.pg_type ty on ty.oid=a.atttypid
    where a.attrelid=format('public.%I',c.entity_type)::regclass and a.attnum>0 and not a.attisdropped loop
    if tip.typname='bool' and jsonb_typeof(p->tip.nombre)='number' then
     if p->>tip.nombre not in ('0','1') then raise exception 'Booleano inválido';end if;
     p:=jsonb_set(p,array[tip.nombre],to_jsonb(p->>tip.nombre='1'));
    elsif tip.typname in ('json','jsonb') and jsonb_typeof(p->tip.nombre)='string' then
     p:=jsonb_set(p,array[tip.nombre],(p->>tip.nombre)::jsonb);
    end if;
   end loop;
   select string_agg(format('%I',a.attname),',' order by a.attnum),
    string_agg(format('r.%I',a.attname),',' order by a.attnum),
    string_agg(format('%I=excluded.%I',a.attname,a.attname),',' order by a.attnum) filter(where a.attname<>'id')
   into columnas,valores,asignar from pg_catalog.pg_attribute a
   where a.attrelid=format('public.%I',c.entity_type)::regclass and a.attnum>0 and not a.attisdropped;
   execute format('insert into public.%I (%s) select %s from jsonb_populate_record(null::public.%I,$1) r on conflict(id) do update set %s',c.entity_type,columnas,valores,c.entity_type,asignar) using p;
   insert into agenda_internal.heads values(o,c.entity_type,c.entity_id,c.change_id,c.device_id,c.entity_revision,c.payload,c.created_at)
   on conflict(owner_id,entity_type,entity_id) do update set change_id=excluded.change_id,device_id=excluded.device_id,
    revision=excluded.revision,payload=excluded.payload,created_at=excluded.created_at;
   update agenda_internal.change_state set status='APPLIED',detail=null where change_id=c.change_id;
   if resolver then
    update agenda_internal.change_state e set status='RESOLVED',detail='Resolución manual persistida'
    from public.sync_changes s where s.change_id=e.change_id and s.owner_id=o and s.entity_type=c.entity_type
     and s.entity_id=c.entity_id and e.status in ('CONFLICT','WAITING') and s.entity_revision<c.entity_revision
     and exists(select 1 from jsonb_array_elements_text(c.payload->'_agenda_sync_resolution_v1'->'supersedes') x where x.value=s.change_id::text);
   end if;
   avance:=true;
  end loop;
  exit when not avance;
 end loop;
end$$;

create or replace function public.agenda_sync_push(cambios jsonb) returns jsonb
language plpgsql security definer set search_path='' as $$
declare o uuid:=auth.uid();c jsonb;anterior public.sync_changes%rowtype;h agenda_internal.heads%rowtype;salida jsonb;begin
 if o is null then raise exception 'Se requiere sesión';end if;
 if jsonb_typeof(cambios)<>'array' or jsonb_array_length(cambios)>100 then raise exception 'Lote inválido';end if;
 perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(o::text,40501));
 for c in select value from jsonb_array_elements(cambios) loop
  perform agenda_internal.validar(c,o);
  select * into anterior from public.sync_changes where change_id=(c->>'change_id')::uuid;
  if anterior.change_id is not null then
   if anterior.owner_id<>o or (to_jsonb(anterior)-'received_at'-'created_at') is distinct from
    (c-'created_at') then raise exception 'UUID con otro contenido';end if;
  else
   select * into h from agenda_internal.heads where change_id=(c->>'change_id')::uuid;
   if h.change_id is not null and (h.owner_id<>o or h.entity_type<>c->>'entity_type' or
     h.entity_id<>(c->>'entity_id')::uuid or h.device_id<>(c->>'device_id')::uuid or
     h.revision<>(c->>'entity_revision')::int or h.payload is distinct from c->'payload') then
    raise exception 'UUID de estado actual con otro contenido';end if;
   insert into public.sync_changes(change_id,owner_id,device_id,entity_type,entity_id,base_revision,entity_revision,operation,payload,created_at)
   values((c->>'change_id')::uuid,o,(c->>'device_id')::uuid,c->>'entity_type',(c->>'entity_id')::uuid,
    (c->>'base_revision')::int,(c->>'entity_revision')::int,c->>'operation',c->'payload',(c->>'created_at')::timestamptz);
   -- Reintentar la última versión después de una poda no la transforma en conflicto.
   if h.change_id is not null then insert into agenda_internal.change_state values((c->>'change_id')::uuid,'APPLIED',null);end if;
  end if;
 end loop;
 perform agenda_internal.materializar(o);
 select coalesce(jsonb_agg(to_jsonb(s)-'received_at'||jsonb_build_object('persistencia',e.status)),'[]') into salida
 from public.sync_changes s join agenda_internal.change_state e using(change_id)
 where s.owner_id=o and s.change_id in(select (value->>'change_id')::uuid from jsonb_array_elements(cambios));
 return salida;
end$$;

-- Se pagina por UUID, sin reloj del teléfono ni cursor persistente. Cada ciclo
-- recorre el estado completo, incluyendo bajas; no depende del historial de 90 días.
create or replace function public.agenda_sync_snapshot(despues uuid default null,limite integer default 100) returns jsonb
language plpgsql security definer set search_path='' as $$
declare o uuid:=auth.uid();r jsonb;begin
 if o is null then raise exception 'Se requiere sesión';end if;
 perform agenda_internal.materializar(o);
 select coalesce(jsonb_agg(q.doc order by q.id),'[]') into r from (
  select id,doc from (
   select h.change_id id,jsonb_build_object('change_id',h.change_id,'owner_id',o,'device_id',h.device_id,
    'entity_type',h.entity_type,'entity_id',h.entity_id,'base_revision',h.revision-1,'entity_revision',h.revision,
    'operation',case when h.payload->>'deleted_at' is null then 'UPSERT' else 'DELETE' end,
    'payload',h.payload,'created_at',h.created_at,'_snapshot',true) doc
   from agenda_internal.heads h where h.owner_id=o
   union all
   select s.change_id,to_jsonb(s)-'received_at' from public.sync_changes s join agenda_internal.change_state e using(change_id)
   where s.owner_id=o and e.status in ('CONFLICT','WAITING')
  ) x where despues is null or id>despues order by id limit greatest(1,least(limite,100))
 ) q;return r;
end$$;

create or replace function public.agenda_sync_limpiar() returns bigint
language plpgsql security definer set search_path='' as $$
declare o uuid;cantidad bigint:=0;n bigint;begin
 if not (select enabled from agenda_internal.retention where singleton) then return 0;end if;
 for o in select distinct owner_id from public.sync_changes loop
  perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(o::text,40501));
  delete from public.sync_receipts r using public.sync_changes s,agenda_internal.change_state e
   where r.owner_id=o and r.change_id=s.change_id and s.change_id=e.change_id
    and s.received_at<now()-interval '90 days' and e.status in ('APPLIED','RESOLVED');
  delete from public.sync_changes s using agenda_internal.change_state e
   where s.owner_id=o and s.change_id=e.change_id and s.received_at<now()-interval '90 days'
    and e.status in ('APPLIED','RESOLVED')
    and exists(select 1 from agenda_internal.heads h where h.owner_id=o and h.entity_type=s.entity_type and h.entity_id=s.entity_id);
  get diagnostics n=row_count;cantidad:=cantidad+n;
 end loop;
 update agenda_internal.retention set last_run=now(),last_deleted=cantidad where singleton;
 return cantidad;
end$$;

revoke all on all functions in schema agenda_internal from public,anon,authenticated;
revoke all on function public.agenda_sync_push(jsonb),public.agenda_sync_snapshot(uuid,integer),public.agenda_sync_limpiar() from public,anon,authenticated;
grant execute on function public.agenda_sync_push(jsonb),public.agenda_sync_snapshot(uuid,integer) to authenticated;
-- Limpieza sólo por el administrador / Cron, nunca desde una clave pública.

-- Migra los cambios que ya existían, sin exigir que otro dispositivo los reciba.
do $$declare o uuid;begin for o in select distinct owner_id from public.sync_changes loop
 perform agenda_internal.materializar(o);end loop;end$$;
notify pgrst,'reload schema';
commit;
