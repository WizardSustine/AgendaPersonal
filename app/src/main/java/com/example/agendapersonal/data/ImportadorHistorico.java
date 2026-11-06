package com.example.agendapersonal.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.agendapersonal.ui.registro.Definicion;
import com.example.agendapersonal.util.JsonCursorHelper;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Importación local atómica. Nunca reemplaza registros ni restaura borrados lógicos. */
public final class ImportadorHistorico {
    private ImportadorHistorico() {}
    public static JSONObject leer(InputStream entrada) throws IOException, JSONException {
        if(entrada==null)throw new IOException("No se pudo abrir el archivo");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[]buffer=new byte[8192];int n;
        try(InputStream in=entrada){while((n=in.read(buffer))!=-1){if(bytes.size()+n>30*1024*1024)throw new IOException("El paquete supera 30 MB");bytes.write(buffer,0,n);}}
        JSONObject paquete=new JSONObject(bytes.toString(StandardCharsets.UTF_8.name()));
        if(!"agenda_personal_historico_v1".equals(paquete.optString("format")))throw new IOException("No es un paquete histórico compatible");
        JSONArray registros=paquete.getJSONArray("records");if(registros.length()>100000)throw new IOException("El paquete contiene demasiados registros");
        Set<String> ids=new HashSet<>();
        for(int i=0;i<registros.length();i++){JSONObject registro=registros.getJSONObject(i);String tabla=registro.getString("tabla");Definicion.de(tabla);String id=registro.getJSONObject("values").getString("id");UUID.fromString(id);if(!ids.add(tabla+":"+id))throw new IOException("Hay identificadores repetidos en el paquete");}
        return paquete;
    }
    public static final class Resultado {
        public int agregados,existentes,pendientes;
        public String resumen(){return "Importación terminada: "+agregados+" registros agregados, "+existentes+" ya existentes y "+pendientes+" filas históricas conservadas para revisión.";}
    }
    public static Resultado importar(Context context,JSONObject paquete,String deviceId,String ownerId) throws JSONException {
        if(deviceId==null||deviceId.isEmpty()||ownerId==null||ownerId.isEmpty())throw new IllegalArgumentException("Falta la identidad local");
        if(!"agenda_personal_historico_v1".equals(paquete.optString("format")))throw new IllegalArgumentException("Formato no admitido");
        Resultado resultado=new Resultado();JSONArray registros=paquete.getJSONArray("records");String fuente=paquete.getString("source_sha256");
        if(!fuente.matches("[a-f0-9]{64}"))throw new IllegalArgumentException("Identificador de fuente inválido");
        try(DatabaseHelper helper=new DatabaseHelper(context)){
            SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();
            try{
                db.execSQL("CREATE TABLE IF NOT EXISTS historico_fuentes (tabla TEXT NOT NULL, entidad_id TEXT NOT NULL, fuente_sha256 TEXT NOT NULL, original_json TEXT NOT NULL, PRIMARY KEY(tabla,entidad_id,fuente_sha256))");
                db.execSQL("CREATE TABLE IF NOT EXISTS historico_archivo (fuente_sha256 TEXT NOT NULL, hoja TEXT NOT NULL, fila INTEGER NOT NULL, original_json TEXT NOT NULL, motivo_revision TEXT, PRIMARY KEY(fuente_sha256,hoja,fila))");
                Map<String,Set<String>> columnas=new HashMap<>();
                for(int i=0;i<registros.length();i++){
                    JSONObject entrada=registros.getJSONObject(i);String tabla=entrada.getString("tabla");Definicion.de(tabla);JSONObject datos=entrada.getJSONObject("values");String id=datos.getString("id");UUID.fromString(id);
                    if(!columnas.containsKey(tabla)){Set<String> nombres=new HashSet<>();try(Cursor c=db.rawQuery("PRAGMA table_info("+tabla+")",null)){while(c.moveToNext())nombres.add(c.getString(c.getColumnIndexOrThrow("name")));}columnas.put(tabla,nombres);}
                    boolean existe;try(Cursor c=db.query(tabla,new String[]{"id"},"id = ?",new String[]{id},null,null,null)){existe=c.moveToFirst();}
                    if(existe){resultado.existentes++;continue;}
                    ContentValues valores=new ContentValues();Iterator<String> claves=datos.keys();
                    while(claves.hasNext()){String clave=claves.next();if(!columnas.get(tabla).contains(clave)||Arrays.asList("owner_id","revision","created_at","updated_at","deleted_at").contains(clave))throw new IllegalArgumentException("Columna no admitida: "+clave);Object v=datos.get(clave);if(v==JSONObject.NULL)valores.putNull(clave);else if(v instanceof Boolean)valores.put(clave,((Boolean)v)?1:0);else if(v instanceof Integer||v instanceof Long)valores.put(clave,((Number)v).longValue());else if(v instanceof Number){double numero=((Number)v).doubleValue();if(!Double.isFinite(numero))throw new IllegalArgumentException("Número no finito");valores.put(clave,numero);}else if(v instanceof String)valores.put(clave,(String)v);else throw new IllegalArgumentException("Valor no escalar");}
                    valores.put("owner_id",ownerId);valores.put("revision",1);db.insertOrThrow(tabla,null,valores);
                    JSONObject payload;try(Cursor c=db.query(tabla,null,"id = ?",new String[]{id},null,null,null)){if(!c.moveToFirst())throw new IllegalStateException();payload=JsonCursorHelper.toJsonObject(c);}
                    ContentValues cambio=new ContentValues();cambio.put("change_id",UUID.randomUUID().toString());cambio.put("owner_id",ownerId);cambio.put("device_id",deviceId);cambio.put("entity_type",tabla);cambio.put("entity_id",id);cambio.put("base_revision",0);cambio.put("entity_revision",1);cambio.put("operation","UPSERT");payload.put(com.example.agendapersonal.sync.AlmacenSync.HISTORICO,new JSONObject().put("fuente_sha256",fuente).put("original",entrada.getJSONObject("source")));cambio.put("payload",payload.toString());db.insertOrThrow("sync_changes",null,cambio);
                    ContentValues origen=new ContentValues();origen.put("tabla",tabla);origen.put("entidad_id",id);origen.put("fuente_sha256",fuente);origen.put("original_json",entrada.getJSONObject("source").toString());db.insertOrThrow("historico_fuentes",null,origen);resultado.agregados++;
                }
                Map<String,String> revisiones=new HashMap<>();JSONArray pendientes=paquete.optJSONArray("review");if(pendientes!=null)for(int i=0;i<pendientes.length();i++){JSONObject r=pendientes.getJSONObject(i);revisiones.put(r.getString("hoja")+":"+r.getInt("fila"),r.getString("motivo"));}resultado.pendientes=revisiones.size();
                JSONArray originales=paquete.getJSONArray("raw_source_rows");for(int i=0;i<originales.length();i++){JSONObject o=originales.getJSONObject(i);String hoja=o.getString("hoja");int fila=o.getInt("fila");ContentValues archivo=new ContentValues();archivo.put("fuente_sha256",fuente);archivo.put("hoja",hoja);archivo.put("fila",fila);archivo.put("original_json",o.toString());archivo.put("motivo_revision",revisiones.get(hoja+":"+fila));db.insertWithOnConflict("historico_archivo",null,archivo,SQLiteDatabase.CONFLICT_IGNORE);ContentValues revision=new ContentValues();revision.put("motivo_revision",revisiones.get(hoja+":"+fila));db.update("historico_archivo",revision,"fuente_sha256 = ? AND hoja = ? AND fila = ?",new String[]{fuente,hoja,String.valueOf(fila)});}
                try(Cursor c=db.rawQuery("PRAGMA foreign_key_check",null)){if(c.moveToFirst())throw new IllegalStateException("Hay relaciones incompletas");}
                db.setTransactionSuccessful();
            }finally{db.endTransaction();}
        }
        com.example.agendapersonal.eventos.SeriesEvento.cambios(context);
        return resultado;
    }
    public static String datosDeOrigen(Context context,String tabla,String id){
        Definicion.de(tabla);
        try(DatabaseHelper helper=new DatabaseHelper(context)){
            SQLiteDatabase db=helper.getReadableDatabase();try(Cursor c=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='historico_fuentes'",null)){if(!c.moveToFirst())return null;}
            StringBuilder texto=new StringBuilder();
            try(Cursor c=db.query("historico_fuentes",new String[]{"original_json"},"tabla = ? AND entidad_id = ?",new String[]{tabla,id},null,null,null)){
                while(c.moveToNext()){
                    JSONObject origen=new JSONObject(c.getString(0));JSONArray celdas=origen.optJSONArray("celdas");if(celdas==null)continue;
                    texto.append(origen.optString("hoja")).append(" · fila ").append(origen.optInt("fila")).append("\n\n");
                    for(int i=0;i<celdas.length();i++){JSONObject celda=celdas.getJSONObject(i);String valor=celda.optString("text","");if(valor.isEmpty())valor=celda.optString("v","");if(valor.isEmpty())continue;String etiqueta=celda.optString("cabecera","");if(etiqueta.isEmpty())etiqueta="Columna "+celda.optInt("columna");texto.append(etiqueta).append(": ").append(valor).append('\n');}
                }
            }
            return texto.length()==0?null:texto.toString();
        }catch(JSONException e){throw new IllegalStateException("No se pudo leer el origen del registro",e);}
    }
}
