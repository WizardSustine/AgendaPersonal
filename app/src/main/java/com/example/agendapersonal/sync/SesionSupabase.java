package com.example.agendapersonal.sync;
import org.json.*;
public final class SesionSupabase {
 public final String url,clave,accessToken,refreshToken,owner,email;public final long vence;
 public SesionSupabase(String u,String k,String a,String r,String o,String e,long v){url=ReglasSync.urlProyecto(u);clave=k;accessToken=a;refreshToken=r;owner=ReglasSync.uuid(o);email=e;vence=v;if(a.isEmpty()||r.isEmpty())throw new IllegalArgumentException("Sesión incompleta");}
 public boolean porVencer(){return System.currentTimeMillis()/1000>=vence-60;}
 public JSONObject json()throws JSONException{return new JSONObject().put("url",url).put("clave",clave).put("access",accessToken).put("refresh",refreshToken).put("owner",owner).put("email",email).put("vence",vence);}
 public static SesionSupabase de(JSONObject j)throws JSONException{return new SesionSupabase(j.getString("url"),j.getString("clave"),j.getString("access"),j.getString("refresh"),j.getString("owner"),j.optString("email"),j.getLong("vence"));}
}
