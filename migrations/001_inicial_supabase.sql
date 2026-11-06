-- Agenda personal multiplataforma
-- Migración inicial para Supabase PostgreSQL (versión revisada para cuenta personal única)
-- Ejecutar una sola vez desde Supabase > SQL Editor.
-- No contiene datos personales ni credenciales.
-- Android/PC mantienen además sus propias bases SQLite locales.

begin;

-- -------------------------------------------------------------------
-- 1. Tipos controlados
-- -------------------------------------------------------------------

do $$ begin
  create type public.app_priority as enum ('ALTISIMA','ALTA','MEDIA','BAJA','BAJISIMA');
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.app_special_status as enum ('FINALIZADO','CANCELADO','POSTERGADO');
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.app_category as enum ('ESTUDIOS','SOCIALES','SALUD','TRABAJO','EJERCICIO','FREELANCE','OTRA');
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.app_exercise_type as enum ('AEROBICO','ANAEROBICO','ESTIRAMIENTO');
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.app_emotional_rating as enum ('POSITIVA','NEGATIVA');
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.app_recurrence_type as enum ('SEMANAL','MENSUAL','ANUAL','PERSONALIZADA');
exception when duplicate_object then null; end $$;

-- -------------------------------------------------------------------
-- 2. Función técnica común
-- -------------------------------------------------------------------

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

-- -------------------------------------------------------------------
-- 3. Personas y direcciones
-- -------------------------------------------------------------------

create table if not exists public.personas (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  nombre text not null,
  otro_dato text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create table if not exists public.direcciones (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  persona_id uuid not null references public.personas(id) on delete restrict,
  calle text not null,
  numero text not null,
  localidad text not null,
  provincia text not null,
  referencia text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz,
  unique (id, persona_id)
);

create index if not exists idx_direcciones_persona on public.direcciones(owner_id, persona_id);

-- -------------------------------------------------------------------
-- 4. Estudios, Sociales, Salud y Trabajo
-- -------------------------------------------------------------------

create table if not exists public.materias (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  institucion_id uuid not null references public.personas(id) on delete restrict,
  nombre text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz,
  unique (id, institucion_id)
);

create index if not exists idx_materias_institucion on public.materias(owner_id, institucion_id);

create table if not exists public.estudios (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  fecha_entrega date,
  hora time,
  actividad text,
  materia_id uuid,
  institucion_id uuid,
  prioridad public.app_priority not null default 'MEDIA',
  estado_especial public.app_special_status,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz,
  constraint estudios_materia_institucion_fk
    foreign key (materia_id, institucion_id)
    references public.materias(id, institucion_id) on delete restrict
);

create index if not exists idx_estudios_fecha on public.estudios(owner_id, fecha_entrega);

create table if not exists public.clasificaciones_sociales (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  nombre text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create table if not exists public.sociales (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  fecha date,
  hora time,
  actividad text,
  persona_id uuid not null references public.personas(id) on delete restrict,
  clasificacion_id uuid references public.clasificaciones_sociales(id) on delete restrict,
  prioridad public.app_priority not null default 'MEDIA',
  estado_especial public.app_special_status,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create index if not exists idx_sociales_fecha on public.sociales(owner_id, fecha);

create table if not exists public.especialidades (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  nombre text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create table if not exists public.salud (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  fecha date,
  hora time,
  actividad text,
  especialidad_id uuid not null references public.especialidades(id) on delete restrict,
  institucion_id uuid references public.personas(id) on delete restrict,
  prioridad public.app_priority not null default 'MEDIA',
  estado_especial public.app_special_status,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create index if not exists idx_salud_fecha on public.salud(owner_id, fecha);

create table if not exists public.trabajo (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  fecha_entrega date,
  hora time,
  actividad text,
  asunto text,
  clasificacion text,
  persona_id uuid references public.personas(id) on delete restrict,
  prioridad public.app_priority not null default 'MEDIA',
  estado_especial public.app_special_status,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create index if not exists idx_trabajo_fecha on public.trabajo(owner_id, fecha_entrega);

-- -------------------------------------------------------------------
-- 5. Freelance
-- -------------------------------------------------------------------

create table if not exists public.freelance (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  valor numeric(14,2),
  fecha_inicio date not null,
  hora_inicio time,
  fecha_finalizacion date,
  hora_finalizacion time,
  actividad text,
  cliente_id uuid not null references public.personas(id) on delete restrict,
  direccion_id uuid,
  clasificacion text,
  estado_especial public.app_special_status,
  observacion text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz,
  constraint freelance_direccion_cliente_fk
    foreign key (direccion_id, cliente_id)
    references public.direcciones(id, persona_id) on delete restrict
);

create index if not exists idx_freelance_fecha on public.freelance(owner_id, fecha_inicio);
create index if not exists idx_freelance_cliente on public.freelance(owner_id, cliente_id);

-- -------------------------------------------------------------------
-- 6. Economía
-- -------------------------------------------------------------------

create table if not exists public.clasificaciones_economia (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  nombre text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create table if not exists public.ingresos (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  fecha date,
  descripcion text,
  clasificacion_id uuid references public.clasificaciones_economia(id) on delete restrict,
  importe numeric(14,2) not null,
  estado_especial public.app_special_status,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create table if not exists public.pagos (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  fecha date,
  descripcion text,
  clasificacion_id uuid references public.clasificaciones_economia(id) on delete restrict,
  importe numeric(14,2) not null,
  periodicidad text,
  estado_especial public.app_special_status,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create table if not exists public.gastos (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  fecha date,
  descripcion text,
  clasificacion_id uuid references public.clasificaciones_economia(id) on delete restrict,
  importe numeric(14,2) not null,
  estado_especial public.app_special_status,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create index if not exists idx_ingresos_fecha on public.ingresos(owner_id, fecha);
create index if not exists idx_pagos_fecha on public.pagos(owner_id, fecha);
create index if not exists idx_gastos_fecha on public.gastos(owner_id, fecha);

-- -------------------------------------------------------------------
-- 7. Ejercicio
-- -------------------------------------------------------------------

create table if not exists public.ejercicios (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  fecha date not null,
  ejercicio text not null,
  tipo public.app_exercise_type not null,
  musculos_trabajados text not null,
  tiempo_segundos integer check (tiempo_segundos is null or tiempo_segundos >= 0),
  distancia numeric(12,3) check (distancia is null or distancia >= 0),
  cantidad numeric(12,3) check (cantidad is null or cantidad >= 0),
  peso numeric(12,3) check (peso is null or peso >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create index if not exists idx_ejercicios_fecha on public.ejercicios(owner_id, fecha);

-- -------------------------------------------------------------------
-- 8. Seguimiento emocional y frases personales
-- -------------------------------------------------------------------

create table if not exists public.ambitos_emocionales (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  nombre text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create table if not exists public.motivos_emocionales (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  nombre text not null,
  ambito_id uuid not null references public.ambitos_emocionales(id) on delete restrict,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create table if not exists public.registros_emocionales (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  fecha_hora timestamptz not null,
  valoracion public.app_emotional_rating not null,
  motivo_id uuid not null references public.motivos_emocionales(id) on delete restrict,
  accion_evento_asociado text,
  observaciones text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create index if not exists idx_registros_emocionales_fecha on public.registros_emocionales(owner_id, fecha_hora);

create table if not exists public.frases_personales (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  frase text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

-- -------------------------------------------------------------------
-- 9. Eventos, recurrencias y tareas
-- -------------------------------------------------------------------

create table if not exists public.series_eventos (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  frecuencia public.app_recurrence_type not null,
  -- Se completa cuando se defina el formato de recurrencia personalizada.
  configuracion_personalizada jsonb,
  fecha_fin date,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create table if not exists public.eventos (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  descripcion text not null,
  categoria public.app_category not null,
  fecha date not null,
  hora time,
  persona_id uuid references public.personas(id) on delete restrict,
  lugar text,
  estado_especial public.app_special_status,
  alarma_activada boolean not null default false,
  anticipacion_minutos integer check (anticipacion_minutos is null or anticipacion_minutos >= 0),
  serie_id uuid references public.series_eventos(id) on delete restrict,
  observaciones text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz,
  constraint eventos_alarma_requiere_anticipacion
    check (alarma_activada = false or anticipacion_minutos is not null)
);

create index if not exists idx_eventos_fecha on public.eventos(owner_id, fecha);
create index if not exists idx_eventos_serie on public.eventos(owner_id, serie_id);

create table if not exists public.tareas (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  descripcion text not null,
  fecha_inicio date,
  hora_inicio time,
  fecha_vencimiento date not null,
  hora_vencimiento time,
  categoria public.app_category not null,
  prioridad public.app_priority not null default 'MEDIA',
  estado_especial public.app_special_status,
  alarma_activada boolean not null default false,
  anticipacion_minutos integer check (anticipacion_minutos is null or anticipacion_minutos >= 0),
  observaciones text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz,
  constraint tareas_alarma_requiere_inicio
    check (alarma_activada = false or fecha_inicio is not null),
  constraint tareas_alarma_requiere_anticipacion
    check (alarma_activada = false or anticipacion_minutos is not null)
);

create index if not exists idx_tareas_vencimiento on public.tareas(owner_id, fecha_vencimiento);
create index if not exists idx_tareas_inicio on public.tareas(owner_id, fecha_inicio);

-- -------------------------------------------------------------------
-- 10. Historial común de postergaciones
-- -------------------------------------------------------------------

create table if not exists public.historial_postergaciones (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  tipo_entidad text not null check (
    tipo_entidad in ('EVENTO','TAREA','ESTUDIO','SOCIAL','SALUD','TRABAJO','FREELANCE')
  ),
  entidad_id uuid not null,
  fecha_original date not null,
  nueva_fecha date,
  fecha_postergacion timestamptz not null default now(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create index if not exists idx_postergaciones_entidad
  on public.historial_postergaciones(owner_id, tipo_entidad, entidad_id, fecha_postergacion);

-- -------------------------------------------------------------------
-- 11. Registros de investigaciones (RI)
-- -------------------------------------------------------------------

create table if not exists public.registros_investigacion (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  id_original text,
  datos_originales jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create index if not exists idx_ri_id_original on public.registros_investigacion(owner_id, id_original);

create table if not exists public.observaciones_ri (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid(),
  registro_investigacion_id uuid not null references public.registros_investigacion(id) on delete restrict,
  observacion text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision integer not null default 1 check (revision > 0),
  deleted_at timestamptz
);

create index if not exists idx_observaciones_ri_registro
  on public.observaciones_ri(owner_id, registro_investigacion_id);

-- -------------------------------------------------------------------
-- 12. Infraestructura de sincronización en la nube
-- -------------------------------------------------------------------

create table if not exists public.sync_devices (
  owner_id uuid not null default auth.uid(),
  device_id uuid not null,
  nombre text,
  created_at timestamptz not null default now(),
  last_seen_at timestamptz,
  primary key (owner_id, device_id)
);

create table if not exists public.sync_changes (
  change_id uuid primary key,
  owner_id uuid not null default auth.uid(),
  device_id uuid not null,
  entity_type text not null,
  entity_id uuid not null,
  base_revision integer not null check (base_revision >= 0),
  entity_revision integer not null check (entity_revision > 0),
  operation text not null check (operation in ('UPSERT','DELETE')),
  payload jsonb not null,
  created_at timestamptz not null default now(),
  unique (owner_id, change_id)
);

create index if not exists idx_sync_changes_owner_created
  on public.sync_changes(owner_id, created_at, change_id);

create table if not exists public.sync_receipts (
  owner_id uuid not null default auth.uid(),
  device_id uuid not null,
  change_id uuid not null,
  received_at timestamptz not null default now(),
  primary key (owner_id, device_id, change_id),
  constraint sync_receipts_change_fk
    foreign key (owner_id, change_id)
    references public.sync_changes(owner_id, change_id) on delete restrict
);

create index if not exists idx_sync_receipts_change
  on public.sync_receipts(owner_id, change_id);

-- -------------------------------------------------------------------
-- 13. Índices de búsqueda y pertenencia
-- -------------------------------------------------------------------

create index if not exists idx_personas_owner_nombre on public.personas(owner_id, nombre);
create index if not exists idx_clasificaciones_sociales_owner on public.clasificaciones_sociales(owner_id, nombre);
create index if not exists idx_especialidades_owner on public.especialidades(owner_id, nombre);
create index if not exists idx_clasificaciones_economia_owner on public.clasificaciones_economia(owner_id, nombre);
create index if not exists idx_ambitos_emocionales_owner on public.ambitos_emocionales(owner_id, nombre);
create index if not exists idx_motivos_emocionales_owner on public.motivos_emocionales(owner_id, nombre);

-- -------------------------------------------------------------------
-- 14. updated_at automático; revision la administra la aplicación
-- -------------------------------------------------------------------

do $$
declare
  t text;
begin
  foreach t in array array[
    'personas','direcciones','materias','estudios','clasificaciones_sociales',
    'sociales','especialidades','salud','trabajo','freelance',
    'clasificaciones_economia','ingresos','pagos','gastos','ejercicios',
    'ambitos_emocionales','motivos_emocionales','registros_emocionales',
    'frases_personales','series_eventos','eventos','tareas',
    'historial_postergaciones','registros_investigacion','observaciones_ri'
  ]
  loop
    execute format('drop trigger if exists trg_%I_updated_at on public.%I', t, t);
    execute format(
      'create trigger trg_%I_updated_at before update on public.%I for each row execute function public.set_updated_at()',
      t, t
    );
  end loop;
end $$;

-- -------------------------------------------------------------------
-- 15. Seguridad: RLS para la única cuenta autenticada de la aplicación
-- -------------------------------------------------------------------

do $$
declare
  t text;
begin
  foreach t in array array[
    'personas','direcciones','materias','estudios','clasificaciones_sociales',
    'sociales','especialidades','salud','trabajo','freelance',
    'clasificaciones_economia','ingresos','pagos','gastos','ejercicios',
    'ambitos_emocionales','motivos_emocionales','registros_emocionales',
    'frases_personales','series_eventos','eventos','tareas',
    'historial_postergaciones','registros_investigacion','observaciones_ri',
    'sync_devices','sync_changes','sync_receipts'
  ]
  loop
    execute format('alter table public.%I enable row level security', t);
    execute format('drop policy if exists owner_select on public.%I', t);
    execute format('drop policy if exists owner_insert on public.%I', t);
    execute format('drop policy if exists owner_update on public.%I', t);
    execute format('drop policy if exists owner_delete on public.%I', t);

    execute format(
      'create policy owner_select on public.%I for select to authenticated using (owner_id = (select auth.uid()))',
      t
    );
    execute format(
      'create policy owner_insert on public.%I for insert to authenticated with check (owner_id = (select auth.uid()))',
      t
    );
    execute format(
      'create policy owner_update on public.%I for update to authenticated using (owner_id = (select auth.uid())) with check (owner_id = (select auth.uid()))',
      t
    );
    execute format(
      'create policy owner_delete on public.%I for delete to authenticated using (owner_id = (select auth.uid()))',
      t
    );
  end loop;
end $$;

-- Este proyecto está destinado a una sola cuenta personal.
-- owner_id identifica esa cuenta; no estamos implementando gestión multiusuario.
--
-- Se retira el acceso heredado de PUBLIC y anon. La API sólo tendrá acceso
-- para el rol authenticated, y RLS limita las filas a auth.uid().
revoke all on schema public from anon, public;
grant usage on schema public to authenticated;

revoke all on all tables in schema public from anon, public;

grant select, insert, update, delete on
  public.personas, public.direcciones, public.materias, public.estudios,
  public.clasificaciones_sociales, public.sociales, public.especialidades,
  public.salud, public.trabajo, public.freelance,
  public.clasificaciones_economia, public.ingresos, public.pagos, public.gastos,
  public.ejercicios, public.ambitos_emocionales, public.motivos_emocionales,
  public.registros_emocionales, public.frases_personales, public.series_eventos,
  public.eventos, public.tareas, public.historial_postergaciones,
  public.registros_investigacion, public.observaciones_ri, public.sync_devices
to authenticated;

grant select, insert on public.sync_changes, public.sync_receipts to authenticated;

-- El historial de sincronización es inmutable desde el cliente:
revoke update, delete on public.sync_changes from authenticated;
revoke update, delete on public.sync_receipts from authenticated;

commit;

-- -------------------------------------------------------------------
-- Fin de la migración inicial revisada. No habilita acceso anónimo ni público.
--
-- Notas:
-- 1. Los estados dinámicos (MUCHO/PENDIENTE/URGENTE/CRITICO/ATRASADO)
--    se calculan en la aplicación; sólo se almacena estado_especial.
-- 2. revision la incrementa la aplicación junto con cada cambio.
-- 3. RI conserva sus campos históricos en datos_originales (JSONB)
--    hasta importar y mapear los datos originales.
-- 4. La recurrencia personalizada queda representada, pero su formato
--    se completa cuando se implemente esa función.
-- 5. Las fechas/horas y campos funcionales no marcados expresamente
--    como obligatorios en la especificación se dejan admitiendo NULL.
-- 6. Esta migración no carga catálogos ni datos históricos.
