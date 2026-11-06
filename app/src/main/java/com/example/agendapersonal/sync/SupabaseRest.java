package com.example.agendapersonal.sync;
import android.util.Base64;import org.json.*;import java.net.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;

/** API REST con TLS y sesiones de usuario. No utiliza ni admite claves administrativas. */
public final class SupabaseRest implements TransporteSync {
 public static final class ApiException extends IOException {public final int estado;public ApiException(int estado,String mensaje){super(mensaje);this.estado=estado;}}
 private final String url,clave,token;
 public SupabaseRest(String u,String k,String t){url=ReglasSync.urlProyecto(u);clave=validarClave(k);token=t;}
 public static String validarClave(String k){k=k.trim();if(k.startsWith("sb_publishable_")&&k.length()>25)return k;if(k.startsWith("sb_secret_"))throw new IllegalArgumentException("Usá la clave pública, nunca una clave secreta");try{String[]p=k.split("\\.");if(p.length!=3)throw new IllegalArgumentException();JSONObject j=new JSONObject(new String(Base64.decode(p[1],Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING),StandardCharsets.UTF_8));if(!"anon".equals(j.optString("role")))throw new IllegalArgumentException();return k;}catch(Exception e){throw new IllegalArgumentException("Ingresá una clave pública publishable o anon válida");}}
 private static String enc(String valor)throws UnsupportedEncodingException{return URLEncoder.encode(valor,StandardCharsets.UTF_8.name());}
 private String pedir(String metodo,String ruta,Object cuerpo,String preferencia)throws IOException,JSONException{
  HttpURLConnection c=(HttpURLConnection)new URL(url+ruta).openConnection();c.setInstanceFollowRedirects(false);c.setRequestMethod(metodo);c.setConnectTimeout(10000);c.setReadTimeout(20000);c.setRequestProperty("apikey",clave);c.setRequestProperty("Accept","application/json");if(token!=null)c.setRequestProperty("Authorization","Bearer "+token);if(preferencia!=null)c.setRequestProperty("Prefer",preferencia);
  try{if(cuerpo!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");try(OutputStream out=c.getOutputStream()){out.write(cuerpo.toString().getBytes(StandardCharsets.UTF_8));}}
   int estado=c.getResponseCode();if(estado<200||estado>=300){String mensaje=estado==404?"Falta instalar la actualización de persistencia en Supabase (migración 003).":estado==401?"La sesión venció o fue rechazada. Volvé a iniciar sesión.":estado==403?"Supabase no permite esta operación con la cuenta actual. Revisá RLS y los permisos.":estado==429?"Supabase pidió esperar antes de reintentar.":"Supabase respondió HTTP "+estado;throw new ApiException(estado,mensaje);}
   try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[]b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>16*1024*1024)throw new IOException("La respuesta supera el tamaño esperado");out.write(b,0,n);}return out.toString(StandardCharsets.UTF_8.name());}
  }finally{c.disconnect();}
 }
 public static SesionSupabase iniciar(String u,String k,String email,String password)throws Exception{
  SupabaseRest api=new SupabaseRest(u,k,null);JSONObject respuesta=new JSONObject(api.pedir("POST","/auth/v1/token?grant_type=password",new JSONObject().put("email",email.trim()).put("password",password),null));return api.sesion(respuesta);
 }
 private SesionSupabase sesion(JSONObject respuesta)throws Exception{
  String access=respuesta.getString("access_token");SupabaseRest autenticado=new SupabaseRest(url,clave,access);JSONObject user=new JSONObject(autenticado.pedir("GET","/auth/v1/user",null,null));long vence=respuesta.optLong("expires_at",System.currentTimeMillis()/1000+respuesta.getLong("expires_in"));return new SesionSupabase(url,clave,access,respuesta.getString("refresh_token"),user.getString("id"),user.optString("email"),vence);
 }
 public static SesionSupabase renovar(SesionSupabase s)throws Exception{try{SupabaseRest api=new SupabaseRest(s.url,s.clave,null);SesionSupabase nueva=api.sesion(new JSONObject(api.pedir("POST","/auth/v1/token?grant_type=refresh_token",new JSONObject().put("refresh_token",s.refreshToken),null)));if(!nueva.owner.equals(s.owner))throw new IOException("La cuenta de la sesión cambió");return nueva;}catch(ApiException e){if(e.estado==400||e.estado==401)throw new ApiException(401,"La sesión ya no se puede renovar. Volvé a iniciar sesión.");throw e;}}
 public void dispositivo(String owner,String device)throws Exception{JSONObject registro=new JSONObject().put("owner_id",owner).put("device_id",device).put("nombre","Agenda Personal Android");pedir("POST","/rest/v1/sync_devices?on_conflict=owner_id,device_id",new JSONArray().put(registro),"resolution=ignore-duplicates");JSONArray encontrados=new JSONArray(pedir("GET","/rest/v1/sync_devices?owner_id=eq."+owner+"&device_id=eq."+device+"&select=device_id",null,null));if(encontrados.length()!=1)throw new IOException("No se confirmó el registro del dispositivo");}
 private String ids(List<String> ids){for(String id:ids)ReglasSync.uuid(id);return String.join(",",ids);}
 public void subirVerificados(List<CambioSync> cambios)throws Exception{
  if(cambios.isEmpty())return;JSONArray cuerpo=new JSONArray();for(CambioSync c:cambios)cuerpo.put(new JSONObject(c.documento));
  JSONArray respuesta=new JSONArray(pedir("POST","/rest/v1/rpc/agenda_sync_push",new JSONObject().put("cambios",cuerpo),null));
  Map<String,JSONObject> porId=new HashMap<>();for(int i=0;i<respuesta.length();i++){JSONObject r=respuesta.getJSONObject(i);porId.put(r.getString("change_id"),r);}
  for(CambioSync c:cambios){JSONObject r=porId.get(c.id);if(r==null||!JsonSync.mismoCambio(new JSONObject(c.documento),r))throw new IOException("Supabase no confirmó el contenido del cambio. Se conserva pendiente.");
   String estado=r.getString("persistencia");if(!estado.equals("APPLIED")&&!estado.equals("RESOLVED")&&!estado.equals("CONFLICT"))throw new IllegalStateException("Supabase conserva un cambio, pero todavía no pudo guardarlo en su módulo. Revisá las relaciones del registro; no se confirmó el envío.");
  }
 }

 public List<CambioSync> pagina(String owner,String device,String fecha,String id,int limite)throws Exception{
  ReglasSync.uuid(owner);ReglasSync.uuid(device);if(id!=null)ReglasSync.uuid(id);
  JSONObject solicitud=new JSONObject().put("despues",id==null?JSONObject.NULL:id).put("limite",Math.min(100,Math.max(1,limite)));
  JSONArray filas=new JSONArray(pedir("POST","/rest/v1/rpc/agenda_sync_snapshot",solicitud,null));List<CambioSync> lista=new ArrayList<>();
  for(int i=0;i<filas.length();i++){JSONObject c=filas.getJSONObject(i);lista.add(new CambioSync(c.getString("change_id"),c.getString("created_at"),c.toString()));}return lista;
 }

 /** Protocolo 2: la recepción de otro dispositivo no condiciona la persistencia ni la retención. */
 public void recibosVerificados(String owner,String device,List<String> cambios){ReglasSync.uuid(owner);ReglasSync.uuid(device);for(String id:cambios)ReglasSync.uuid(id);}
}
