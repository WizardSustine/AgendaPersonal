package com.example.agendapersonal;
import android.content.*;import android.database.DatabaseErrorHandler;import android.database.sqlite.SQLiteDatabase;import java.io.File;
/** Sustituye la clase de pruebas antigua de Android y mantiene bases y preferencias separadas. */
public final class ContextoPrueba extends ContextWrapper {
 private final String prefijo;
 public ContextoPrueba(Context base,String prefijo){super(base);this.prefijo=prefijo;}
 @Override public Context getApplicationContext(){return this;}
 @Override public File getDatabasePath(String nombre){return super.getDatabasePath(prefijo+nombre);}
 @Override public SQLiteDatabase openOrCreateDatabase(String nombre,int mode,SQLiteDatabase.CursorFactory f){return super.openOrCreateDatabase(prefijo+nombre,mode,f);}
 @Override public SQLiteDatabase openOrCreateDatabase(String nombre,int mode,SQLiteDatabase.CursorFactory f,DatabaseErrorHandler error){return super.openOrCreateDatabase(prefijo+nombre,mode,f,error);}
 @Override public boolean deleteDatabase(String nombre){return super.deleteDatabase(prefijo+nombre);}
 @Override public SharedPreferences getSharedPreferences(String nombre,int mode){return super.getSharedPreferences(prefijo+nombre,mode);}
}
