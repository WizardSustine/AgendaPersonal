"""Regenera árboles, inventario Java y esquema SQLite de referencia. Sólo biblioteca estándar."""
from pathlib import Path
import json
import re
import sqlite3

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / 'app/src/main/java/com/example/agendapersonal'
DOC = ROOT / 'docs'
REF = DOC / 'referencia'
REF.mkdir(parents=True, exist_ok=True)

def leer(p):
    return p.read_text(encoding='utf-8')

def escribir(p, texto):
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(texto, encoding='utf-8')

def literales(texto):
    return [json.loads('"' + s + '"') for s in re.findall(r'"((?:\\.|[^"\\])*)"', texto)]

db = sqlite3.connect(':memory:')
db.execute('pragma foreign_keys=on')
helper = leer(JAVA / 'data/DatabaseHelper.java')
bloque = helper.split('private static final String[] TABLES = {', 1)[1].split('};', 1)[0]
tablas = [s for s in literales(bloque) if s.startswith('CREATE TABLE ')]
aux = leer(JAVA / 'respaldo/EsquemaRespaldo.java')
tablas += [s for s in literales(aux) if s.startswith('CREATE TABLE ') and s.rstrip().endswith(')')]
indices = helper.split('private static final String[] INDEXES = {', 1)[1].split('};', 1)[0]
indices = [s for s in literales(indices) if s.startswith('CREATE INDEX ')]
vistos = set()
ddl = []
for sql in tablas:
    nombre = sql.split('(', 1)[0].replace('CREATE TABLE ', '').strip()
    if nombre in vistos:
        continue
    vistos.add(nombre)
    db.execute(sql)
    ddl.append(sql + ';')
for sql in indices:
    db.execute(sql)
    ddl.append(sql + ';')
escribir(REF / 'esquema_sqlite.sql', '-- Referencia generada del código. Sin datos. No es una migración para una base existente.\n\n' + '\n\n'.join(ddl) + '\n')

lineas = ['# Diccionario de datos', '',
          'Generado desde `DatabaseHelper` y las tablas auxiliares admitidas por `EsquemaRespaldo`. El código y las migraciones son la fuente de verdad.', '',
          f'Se verificó la creación de {len(vistos)} tablas en SQLite en memoria y {len(indices)} índices del esquema base. Las tablas auxiliares se crean cuando la función correspondiente las necesita.', '',
          'Las entidades funcionales conservan UUID, propietario, revisión y baja lógica. En SQLite UUID, fechas y JSON se representan principalmente como TEXT; PostgreSQL usa sus tipos nativos. Los booleanos SQLite se guardan como 0/1.', '',
          'Las restricciones CHECK y UNIQUE completas están en [esquema_sqlite.sql](referencia/esquema_sqlite.sql). No ejecutar ese archivo para actualizar una agenda existente.', '',
          '## SQLite', '']
grafo = ['flowchart LR']
for nombre in sorted(vistos):
    columnas = db.execute(f'pragma table_info("{nombre}")').fetchall()
    lineas += [f'### `{nombre}`', '', '| Campo | Tipo SQLite | NOT NULL declarado | Valor predeterminado | PK |', '| --- | --- | --- | --- | --- |']
    for _, campo, tipo, requerido, defecto, pk in columnas:
        lineas.append(f'| `{campo}` | `{tipo}` | {"Sí" if requerido else "No"} | `{defecto}` | {pk or "—"} |' if defecto is not None else f'| `{campo}` | `{tipo}` | {"Sí" if requerido else "No"} | — | {pk or "—"} |')
    lineas.append('')
    grupos = {}
    for fila in db.execute(f'pragma foreign_key_list("{nombre}")').fetchall():
        grupos.setdefault(fila[0], []).append(fila)
    for filas in grupos.values():
        filas.sort(key=lambda x: x[1])
        padre = filas[0][2]
        origen = ', '.join(f[3] for f in filas)
        destino = ', '.join(f[4] for f in filas)
        lineas.append(f'- FK `({origen})` → `{padre}({destino})`, borrado `{filas[0][6]}`.')
        grafo.append(f'    {padre} -->|"{origen}"| {nombre}')
    if grupos:
        lineas.append('')

lineas += ['## Tablas de control que pueden aparecer', '',
           '`respaldo_control` contiene pares clave/valor para completar una restauración de forma durable. `historial_postergaciones_v1` puede conservarse como respaldo de la migración local 1→2. Su presencia depende de la historia de la instalación; no representan nuevos módulos funcionales.', '',
           '## Diferencias y controles remotos', '',
           'La migración [001](../migrations/001_inicial_supabase.sql) define los tipos PostgreSQL, índices y RLS. La [002](../migrations/002_historial_economia.sql) amplía los tipos del historial. La [003](../migrations/003_persistencia_sync.sql) incorpora los siguientes controles privados:', '']
sql3 = leer(ROOT / 'migrations/003_persistencia_sync.sql')
for match in re.finditer(r'create table if not exists (agenda_internal\.\w+)\((.*?)\);', sql3, re.S | re.I):
    lineas += [f'### `{match[1]}`', '', '```sql', match[0], '```', '']
lineas += ['`sync_changes.received_at` se añade en el servidor y determina la antigüedad de retención. No sustituye las fechas funcionales del payload.', '',
           'El esquema privado no se expone como una tabla editable por el cliente. Los RPC actualizan el estado técnico y las entidades en la misma transacción. Ver [protocolo](PROTOCOLO_SYNC.md).', '']
escribir(DOC / 'DICCIONARIO_DATOS.md', '\n'.join(lineas))
escribir(DOC / 'blueprints/06_relaciones_datos.mmd', '\n'.join(grafo) + '\n')

inventario = ['# Inventario de código Java', '', 'Generado de las fuentes del proyecto. Las líneas son orientativas y cambian con cada edición.', '', '| Archivo | Líneas | Ubicación |', '| --- | --- | --- |']
for p in sorted((ROOT / 'app/src').rglob('*.java')):
    ruta = p.relative_to(ROOT).as_posix()
    inventario.append(f'| `{p.stem}` | {len(leer(p).splitlines())} | [{ruta}](../{ruta}) |')
escribir(DOC / 'INVENTARIO_CODIGO.md', '\n'.join(inventario) + '\n')

# Árboles con rutas del repositorio, nunca rutas de la computadora que los genera.
escribir(DOC / 'ARBOLES_ARCHIVOS.md', '# Árboles de archivos\n')
fijos = ['README.md', 'CHANGELOG.md', 'CONTRIBUTING.md', '.gitignore', '.gitattributes', 'build.gradle.kts', 'settings.gradle.kts', 'gradle.properties', 'gradlew', 'gradlew.bat']
archivos = [ROOT / s for s in fijos if (ROOT / s).is_file()]
for carpeta in ['app/src', 'gradle', 'docs', 'migrations', 'tools']:
    for p in (ROOT / carpeta).rglob('*'):
        if p.is_file() and not any(x in p.relative_to(ROOT).parts for x in ['node_modules', 'build', '__pycache__']):
            archivos.append(p)
for s in ['app/build.gradle.kts', 'app/proguard-rules.pro', 'app/.gitignore']:
    if (ROOT / s).exists():
        archivos.append(ROOT / s)

def arbol(lista):
    raiz = {}
    for p in sorted(set(lista)):
        partes = p.relative_to(ROOT).parts
        actual = raiz
        for parte in partes:
            actual = actual.setdefault(parte, {})
    salida = ['AgendaPersonal/']
    def recorrer(nodo, prefijo=''):
        items = sorted(nodo.items())
        for i, (nombre, hijos) in enumerate(items):
            ultimo = i == len(items) - 1
            salida.append(prefijo + ('└── ' if ultimo else '├── ') + nombre + ('/' if hijos else ''))
            recorrer(hijos, prefijo + ('    ' if ultimo else '│   '))
    recorrer(raiz)
    return '\n'.join(salida)

texto = ['# Árboles de archivos', '', 'Generados desde los archivos que se preparan para versionar. Se excluyen compilaciones, configuración de la computadora, credenciales, datos personales y dependencias instaladas.', '',
         '## Código de producción', '', '```text', arbol([p for p in archivos if 'app/src/main/' in p.relative_to(ROOT).as_posix() and p.suffix == '.java']), '```', '',
         '## Recursos, pruebas y resto del repositorio', '', '```text', arbol(archivos), '```', '',
         'Regenerar después de cambios de estructura: `python tools/documentacion/generar_referencias.py`.', '']
escribir(DOC / 'ARBOLES_ARCHIVOS.md', '\n'.join(texto))
print(f'Referencias generadas: {len(vistos)} tablas SQLite, {len(indices)} índices, {len(list((ROOT / "app/src").rglob("*.java")))} archivos Java.')
db.close()
