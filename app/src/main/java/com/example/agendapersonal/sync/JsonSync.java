package com.example.agendapersonal.sync;
import org.json.*;import java.math.BigDecimal;import java.util.*;
public final class JsonSync {
 private JsonSync(){}
 public static boolean iguales(Object a,Object b)throws JSONException{
  if(a==JSONObject.NULL||a==null)return b==JSONObject.NULL||b==null;
  if(b==JSONObject.NULL||b==null)return false;
  if(a instanceof JSONObject&&b instanceof JSONObject){JSONObject x=(JSONObject)a,y=(JSONObject)b;if(x.length()!=y.length())return false;Iterator<String>keys=x.keys();while(keys.hasNext()){String k=keys.next();if(!y.has(k)||!iguales(x.get(k),y.get(k)))return false;}return true;}
  if(a instanceof JSONArray&&b instanceof JSONArray){JSONArray x=(JSONArray)a,y=(JSONArray)b;if(x.length()!=y.length())return false;for(int i=0;i<x.length();i++)if(!iguales(x.get(i),y.get(i)))return false;return true;}
  if(a instanceof Number&&b instanceof Number)return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()))==0;
  return a.getClass().equals(b.getClass())&&a.equals(b);
 }
 public static JSONObject copia(JSONObject o)throws JSONException{return new JSONObject(o.toString());}
 public static boolean mismoCambio(JSONObject a,JSONObject b)throws JSONException{
  for(String k:new String[]{"change_id","owner_id","device_id","entity_type","entity_id","base_revision","entity_revision","operation","payload"})if(!a.has(k)||!b.has(k)||!iguales(a.get(k),b.get(k)))return false;
  return true;
 }
}
