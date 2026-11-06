package com.example.agendapersonal.sync;
import android.content.Context;import android.content.SharedPreferences;import android.database.Cursor;import com.example.agendapersonal.data.DatabaseHelper;import java.util.UUID;
public final class IdentidadLocal {
 private IdentidadLocal(){}
 public static synchronized String dispositivo(Context c){SharedPreferences p=c.getSharedPreferences("agenda_identidad",Context.MODE_PRIVATE);String id=p.getString("device_id",null);if(id==null){id=UUID.randomUUID().toString();p.edit().putString("device_id",id).commit();}return id;}
 public static synchronized String propietario(Context c){SharedPreferences p=c.getSharedPreferences("agenda_identidad",Context.MODE_PRIVATE);String owner=p.getString("owner_id",null);try(DatabaseHelper h=new DatabaseHelper(c);Cursor tablas=h.getReadableDatabase().rawQuery("SELECT name FROM sqlite_master WHERE name='sync_meta' AND type='table'",null)){if(tablas.moveToFirst())try(Cursor cuenta=h.getReadableDatabase().query("sync_meta",new String[]{"valor"},"clave='cuenta'",null,null,null,null)){if(cuenta.moveToFirst())owner=cuenta.getString(0);}}if(owner==null)owner=UUID.randomUUID().toString();p.edit().putString("owner_id",owner).commit();return owner;}
}
