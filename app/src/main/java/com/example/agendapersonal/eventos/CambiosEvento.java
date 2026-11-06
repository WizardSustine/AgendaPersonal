package com.example.agendapersonal.eventos;
import android.content.ContentValues;import android.database.Cursor;import android.database.sqlite.SQLiteDatabase;import org.json.*;import com.example.agendapersonal.util.JsonCursorHelper;import java.util.*;
/** Diario transaccional común, incluyendo el índice de una ocurrencia cuando corresponde. */
public final class CambiosEvento {
 private CambiosEvento(){}
 public static void registrar(SQLiteDatabase db,String device,String tabla,String id,int base,int revision,String operacion){try{JSONObject payload;try(Cursor c=db.query(tabla,null,"id=?",new String[]{id},null,null,null)){if(!c.moveToFirst())throw new IllegalStateException("No se encontró el registro");payload=JsonCursorHelper.toJsonObject(c);}if(tabla.equals("eventos")){JSONObject indice=SeriesEvento.metadatos(db,id);if(indice!=null)payload.put(SeriesEvento.META,indice);}ContentValues v=new ContentValues();v.put("change_id",UUID.randomUUID().toString());v.put("owner_id",payload.isNull("owner_id")?null:payload.getString("owner_id"));v.put("device_id",device);v.put("entity_type",tabla);v.put("entity_id",id);v.put("base_revision",base);v.put("entity_revision",revision);v.put("operation",operacion);v.put("payload",payload.toString());db.insertOrThrow("sync_changes",null,v);}catch(JSONException e){throw new IllegalStateException(e);}}
}
