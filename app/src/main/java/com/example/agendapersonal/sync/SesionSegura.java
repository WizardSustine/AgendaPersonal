package com.example.agendapersonal.sync;
import android.content.Context;import android.content.SharedPreferences;import android.security.keystore.KeyGenParameterSpec;import android.security.keystore.KeyProperties;import android.util.Base64;
import javax.crypto.*;import javax.crypto.spec.GCMParameterSpec;import java.security.KeyStore;import java.nio.charset.StandardCharsets;import java.io.IOException;import org.json.*;

/** Solo persiste tokens cifrados; la contraseña no se guarda. La preferencia se excluye del backup. */
public final class SesionSegura {
 private static final String ALIAS="agenda_supabase_sesion_v1";private final SharedPreferences prefs;
 public SesionSegura(Context c){prefs=c.getSharedPreferences("supabase_sesion",Context.MODE_PRIVATE);}
 private javax.crypto.SecretKey clave()throws Exception{KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);if(store.containsAlias(ALIAS))return (javax.crypto.SecretKey)store.getKey(ALIAS,null);KeyGenerator generador=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");generador.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build());return generador.generateKey();}
 public synchronized void guardar(SesionSupabase sesion)throws IOException{try{Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,clave());byte[] cifrado=c.doFinal(sesion.json().toString().getBytes(StandardCharsets.UTF_8));String valor=Base64.encodeToString(c.getIV(),Base64.NO_WRAP)+":"+Base64.encodeToString(cifrado,Base64.NO_WRAP);if(!prefs.edit().putString("sesion",valor).commit())throw new IOException("No se pudo guardar la sesión");}catch(Exception e){throw new IOException("No se pudo proteger la sesión del dispositivo",e);}}
 public synchronized SesionSupabase leer()throws IOException{String valor=prefs.getString("sesion",null);if(valor==null)return null;try{String[]partes=valor.split(":",2);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,clave(),new GCMParameterSpec(128,Base64.decode(partes[0],Base64.NO_WRAP)));return SesionSupabase.de(new JSONObject(new String(c.doFinal(Base64.decode(partes[1],Base64.NO_WRAP)),StandardCharsets.UTF_8)));}catch(Exception e){throw new IOException("Volvé a iniciar sesión para conectar este dispositivo",e);}}
 public boolean existe(){return prefs.contains("sesion");}
 public synchronized void borrar(){prefs.edit().remove("sesion").commit();}
}
