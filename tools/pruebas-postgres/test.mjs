import {PGlite} from '@electric-sql/pglite';
import fs from 'node:fs';import assert from 'node:assert/strict';
const repo=new URL('../../',import.meta.url);
const db=new PGlite();await db.waitReady;
await db.exec(`create role anon;create role authenticated;create schema auth;
create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;`);
await db.exec(fs.readFileSync(new URL('migrations/001_inicial_supabase.sql',repo),'utf8'));
await db.exec(fs.readFileSync(new URL('migrations/002_historial_economia.sql',repo),'utf8'));
const migration=fs.readFileSync(new URL('migrations/003_persistencia_sync.sql',repo),'utf8');
await db.exec(migration);
const owner='00000000-0000-4000-8000-000000000001',other='00000000-0000-4000-8000-000000000099',dev='00000000-0000-4000-8000-000000000002';
await db.query(`select set_config('request.jwt.claim.sub',$1,false)`,[owner]);
let n=10,passed=0;
function uuid(){return `00000000-0000-4000-8000-${String(n++).padStart(12,'0')}`;}
function task(id=uuid(),revision=1,descripcion='Prueba'){return {id,owner_id:owner,descripcion,fecha_inicio:'2026-11-05',hora_inicio:'13:00',fecha_vencimiento:'2026-11-05',hora_vencimiento:null,categoria:'FREELANCE',prioridad:'ALTA',estado_especial:null,alarma_activada:1,anticipacion_minutos:3,observaciones:null,created_at:'2026-11-05T15:44:51.562Z',updated_at:'2026-11-05T15:44:51.562Z',revision,deleted_at:null};}
function change(p,table='tareas',base=p.revision-1){return {change_id:uuid(),owner_id:owner,device_id:dev,entity_type:table,entity_id:p.id,base_revision:base,entity_revision:p.revision,operation:p.deleted_at?'DELETE':'UPSERT',payload:p,created_at:'2026-11-05T15:44:51.562Z'};}
async function push(c){return (await db.query('select public.agenda_sync_push($1::jsonb) r',[JSON.stringify(c)])).rows[0].r;}
async function snapshot(after=null,limit=100){return (await db.query('select public.agenda_sync_snapshot($1::uuid,$2) r',[after,limit])).rows[0].r;}
async function test(name,fn){await fn();passed++;console.log('OK',name);}
let a=task(),ca=change(a);
await test('un solo teléfono: confirma sólo después de insertar en tareas',async()=>{assert.equal((await push([ca]))[0].persistencia,'APPLIED');const rows=(await db.query('select * from public.tareas where id=$1',[a.id])).rows;assert.equal(rows[0].descripcion,a.descripcion);assert.equal(rows[0].alarma_activada,true);assert.equal((await db.query('select count(*) n from public.sync_receipts')).rows[0].n,0);});
await test('reintento idempotente',async()=>{await push([ca]);assert.equal((await db.query('select count(*) n from public.tareas')).rows[0].n,1);});
await test('actualización conserva campos y revisión',async()=>{a={...a,revision:2,descripcion:'Editada'};await push([change(a)]);assert.equal((await db.query('select descripcion,revision from public.tareas where id=$1',[a.id])).rows[0].revision,2);});
let conflict;
await test('dos dispositivos: conflicto no pisa el estado',async()=>{conflict=change({...a,descripcion:'Divergente'});assert.equal((await push([conflict]))[0].persistencia,'CONFLICT');assert.equal((await db.query('select descripcion from public.tareas where id=$1',[a.id])).rows[0].descripcion,'Editada');});
await test('snapshot incluye estado actual y conflicto conservado',async()=>{const s=await snapshot();assert(s.some(x=>x._snapshot&&x.payload.descripcion==='Editada'));assert(s.some(x=>!x._snapshot&&x.payload.descripcion==='Divergente'));});
await test('baja durable y protección ante copia antigua',async()=>{a={...a,revision:3,deleted_at:'2026-11-06T00:00:00Z'};await push([change(a)]);await push([change({...a,revision:2,deleted_at:null})]);assert.notEqual((await db.query('select deleted_at from public.tareas where id=$1',[a.id])).rows[0].deleted_at,null);});
await test('limpieza desactivada inicialmente',async()=>{await db.exec(`update public.sync_changes set received_at=now()-interval '91 days'`);assert.equal((await db.query('select public.agenda_sync_limpiar() n')).rows[0].n,0);});
await test('retención 90 días sin depender de PC ni recibos',async()=>{await db.exec(`update agenda_internal.retention set enabled=true`);assert((await db.query('select public.agenda_sync_limpiar() n')).rows[0].n>=3);assert.equal((await db.query('select count(*) n from public.tareas')).rows[0].n,1);assert.equal((await db.query("select count(*) n from public.sync_changes s join agenda_internal.change_state e using(change_id) where e.status='CONFLICT'")).rows[0].n,2);});
await test('nuevo dispositivo recupera revisión 3 y baja sin revisiones 1/2',async()=>{const s=await snapshot();const head=s.find(x=>x._snapshot);assert.equal(head.payload.revision,3);assert(head.payload.deleted_at);});
await test('UUID reutilizado con contenido diferente se rechaza',async()=>{await assert.rejects(push([{...conflict,payload:{...conflict.payload,descripcion:'Manipulada'}}]));});
await test('cuenta incorrecta rechazada',async()=>{await assert.rejects(push([{...change(task()),owner_id:other}]));});
await test('sin sesión no hay acceso',async()=>{await db.exec(`select set_config('request.jwt.claim.sub','',false)`);await assert.rejects(snapshot());await db.query(`select set_config('request.jwt.claim.sub',$1,false)`,[owner]);});
await test('JSON y padres fuera de orden se materializan antes de confirmar',async()=>{const id=uuid(),parent={id,owner_id:owner,nombre:'Institución',otro_dato:null,created_at:a.created_at,updated_at:a.updated_at,revision:1,deleted_at:null};const subject={id:uuid(),owner_id:owner,institucion_id:id,nombre:'Materia',created_at:a.created_at,updated_at:a.updated_at,revision:1,deleted_at:null};const c=await push([change(subject,'materias'),change(parent,'personas')]);assert(c.every(x=>x.persistencia==='APPLIED'));assert.equal((await db.query('select count(*) n from public.materias')).rows[0].n,1);});
await test('histórico JSON como texto se convierte a jsonb sin perder wire',async()=>{const p={id:uuid(),owner_id:owner,id_original:'9',datos_originales:'{"origen":"anterior"}',created_at:a.created_at,updated_at:a.updated_at,revision:1,deleted_at:null};await push([change(p,'registros_investigacion')]);assert.equal((await db.query('select datos_originales from public.registros_investigacion')).rows[0].datos_originales.origen,'anterior');});
await test('FK ausente queda WAITING y no se poda',async()=>{const p={id:uuid(),owner_id:owner,nombre:'Materia pendiente',institucion_id:uuid(),created_at:a.created_at,updated_at:a.updated_at,revision:1,deleted_at:null};const c=change(p,'materias');assert.equal((await push([c]))[0].persistencia,'WAITING');await db.exec(`update public.sync_changes set received_at=now()-interval '100 days'`);await db.query('select public.agenda_sync_limpiar()');assert.equal((await db.query('select count(*) n from public.sync_changes where change_id=$1',[c.change_id])).rows[0].n,1);});
await test('paginación completa sin depender de fechas del cliente',async()=>{let page,after=null,ids=[];do{page=await snapshot(after,2);ids.push(...page.map(x=>x.change_id));after=page.at(-1)?.change_id;}while(page.length===2);assert.equal(new Set(ids).size,ids.length);assert(ids.length>=4);});
await test('RLS: otro usuario no ve cabezas ni datos',async()=>{await db.query(`select set_config('request.jwt.claim.sub',$1,false)`,[other]);assert.equal((await snapshot()).length,0);await db.exec('set role authenticated');assert.equal((await db.query('select count(*) n from public.tareas')).rows[0].n,0);await assert.rejects(db.exec('select * from agenda_internal.heads'));await assert.rejects(db.exec('select public.agenda_sync_limpiar()'));await db.exec('reset role');await db.query(`select set_config('request.jwt.claim.sub',$1,false)`,[owner]);});
await test('reintento de última versión después de podar el log',async()=>{const p=task(),c=change(p);await push([c]);await db.query("update public.sync_changes set received_at=now()-interval '100 days' where change_id=$1",[c.change_id]);await db.query('select public.agenda_sync_limpiar()');assert.equal((await push([c]))[0].persistencia,'APPLIED');});
await test('la antigüedad usa reloj de servidor, no fecha del teléfono',async()=>{const c=change(task());c.created_at='2000-01-01T00:00:00Z';await push([c]);await db.query('select public.agenda_sync_limpiar()');assert.equal((await db.query('select count(*) n from public.sync_changes where change_id=$1',[c.change_id])).rows[0].n,1);});
await test('resolución conserva elección y libera sólo conflictos señalados',async()=>{const conflicts=(await db.query("select s.* from public.sync_changes s join agenda_internal.change_state e using(change_id) where s.entity_id=$1 and e.status='CONFLICT'",[a.id])).rows;const p={...a,revision:4,descripcion:'Resuelta',_agenda_sync_resolution_v1:{candidates:[a,conflicts[0].payload],supersedes:conflicts.map(x=>x.change_id)}};const c=change(p);assert.equal((await push([c]))[0].persistencia,'APPLIED');assert.equal((await db.query('select descripcion from public.tareas where id=$1',[a.id])).rows[0].descripcion,'Resuelta');assert.equal((await db.query("select count(*) n from public.sync_changes s join agenda_internal.change_state e using(change_id) where s.entity_id=$1 and e.status='CONFLICT'",[a.id])).rows[0].n,0);});
await test('lote inválido revierte también los cambios válidos de ese lote',async()=>{const good=change(task()),bad=change({...task(),alarma_activada:8});await assert.rejects(push([good,bad]));assert.equal((await db.query('select count(*) n from public.tareas where id=$1',[good.entity_id])).rows[0].n,0);assert.equal((await db.query('select count(*) n from public.sync_changes where change_id=$1',[good.change_id])).rows[0].n,0);});
await test('authenticated no puede saltar la escritura transaccional por REST',async()=>{await db.exec('set role authenticated');await assert.rejects(db.query('update public.tareas set descripcion=$1 where id=$2',['Por fuera',a.id]));await assert.rejects(db.exec('delete from public.sync_changes'));await db.exec('reset role');});
await test('migración repetible mantiene los datos',async()=>{await db.exec(migration);assert.equal((await db.query('select descripcion from public.tareas where id=$1',[a.id])).rows[0].descripcion,'Resuelta');});
await test('persistencia en los 25 módulos, catálogos, series e históricos',async()=>{
 const tables=(await db.query('select agenda_internal.tablas() t')).rows[0].t, fixtures={};
 for(const table of tables){
  const cols=(await db.query("select column_name,data_type,udt_name,is_nullable from information_schema.columns where table_schema='public' and table_name=$1 order by ordinal_position",[table])).rows;
  const p={};
  for(const col of cols){const k=col.column_name;
   if(k==='id')p[k]=uuid();else if(k==='owner_id')p[k]=owner;else if(k==='revision')p[k]=1;else if(k==='created_at'||k==='updated_at')p[k]=a.created_at;
   else if(col.is_nullable==='YES')p[k]=null;
   else if(col.data_type==='USER-DEFINED')p[k]=(await db.query('select e.enumlabel from pg_catalog.pg_enum e join pg_catalog.pg_type t on t.oid=e.enumtypid where t.typname=$1 order by e.enumsortorder limit 1',[col.udt_name])).rows[0].enumlabel;
   else if(col.data_type==='date')p[k]='2026-11-05';else if(col.data_type.includes('timestamp'))p[k]=a.created_at;
   else if(col.data_type.includes('time'))p[k]='13:00';else if(col.data_type==='boolean')p[k]=0;
   else if(['integer','numeric','bigint'].includes(col.data_type))p[k]=1;else if(col.data_type==='jsonb')p[k]='{}';else p[k]='Prueba';
  }
  if(table==='historial_postergaciones'){p.tipo_entidad='TAREA';p.entidad_id=fixtures.tareas.id;}
  const fks=(await db.query(`select con.confrelid::regclass::text tabla,a.attname origen,b.attname destino from pg_catalog.pg_constraint con cross join lateral unnest(con.conkey,con.confkey) x(a,b) join pg_catalog.pg_attribute a on a.attrelid=con.conrelid and a.attnum=x.a join pg_catalog.pg_attribute b on b.attrelid=con.confrelid and b.attnum=x.b where con.contype='f' and con.conrelid=($1::regclass)`,['public.'+table])).rows;
  for(const fk of fks){const parent=fixtures[fk.tabla.replace('public.','')];assert(parent,'Padre ausente '+fk.tabla+' de '+table);p[fk.origen]=parent[fk.destino];}
  fixtures[table]=p;const result=await push([change(p,table)]);assert.equal(result[0].persistencia,'APPLIED',table);
  assert.equal((await db.query(`select count(*) n from public.${table} where id=$1`,[p.id])).rows[0].n,1,table);
 }
 assert.equal(Object.keys(fixtures).length,25);
});
await test('migración materializa los cambios antiguos que sólo estaban en sync',async()=>{const c=change(task());const keys=Object.keys(c),values=keys.map(k=>typeof c[k]==='object'?JSON.stringify(c[k]):c[k]);await db.query(`insert into public.sync_changes(${keys.join(',')}) values(${keys.map((k,i)=>'$'+(i+1)+(k==='payload'?'::jsonb':'')).join(',')})`,values);assert.equal((await db.query('select count(*) n from public.tareas where id=$1',[c.entity_id])).rows[0].n,0);await db.exec(migration);assert.equal((await db.query('select count(*) n from public.tareas where id=$1',[c.entity_id])).rows[0].n,1);});
await test('activación de limpieza rechaza pendientes y programa una tarea diaria',async()=>{
 await db.exec(`create schema cron;create table cron.job(jobname text primary key,schedule text,command text);create function cron.schedule(n text,s text,c text) returns bigint language plpgsql as $$begin insert into cron.job values(n,s,c) on conflict(jobname) do update set schedule=excluded.schedule,command=excluded.command;return 1;end$$;`);
 const activate=fs.readFileSync(new URL('migrations/004_activar_retencion.sql',repo),'utf8').replace('create extension if not exists pg_cron;','-- pg_cron sustituido sólo en este entorno aislado');
 await assert.rejects(db.exec(activate));await db.exec('rollback');
 await db.exec("update agenda_internal.change_state set status='CONFLICT' where status='WAITING'");
 await db.exec(activate);await db.exec(activate);
 const jobs=(await db.query('select * from cron.job')).rows;assert.equal(jobs.length,1);assert.equal(jobs[0].schedule,'20 3 * * *');assert.equal(jobs[0].command,'select public.agenda_sync_limpiar();');
});
console.log('TOTAL',passed,'pruebas PostgreSQL');await db.close();
