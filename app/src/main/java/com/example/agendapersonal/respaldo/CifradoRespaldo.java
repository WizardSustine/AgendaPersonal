package com.example.agendapersonal.respaldo;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.nio.ByteBuffer;

/** Formato versionado: PBKDF2-HMAC-SHA256 y AES-256-GCM. No guarda la contraseña. */
public final class CifradoRespaldo {
    private static final byte[] CABECERA="AGENDAB1".getBytes(StandardCharsets.US_ASCII);
    private static final int ITERACIONES=210000;
    public static final long LIMITE=512L*1024*1024;
    private CifradoRespaldo() {}
    private static SecretKeySpec clave(char[] password,byte[] sal)throws GeneralSecurityException {
        if(password==null||password.length<8)throw new IllegalArgumentException("Usá una contraseña de al menos 8 caracteres");
        PBEKeySpec spec=new PBEKeySpec(password,sal,ITERACIONES,256);byte[] bytes=null;
        try{bytes=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();return new SecretKeySpec(bytes,"AES");}
        finally{spec.clearPassword();if(bytes!=null)Arrays.fill(bytes,(byte)0);}
    }
    public static void cifrar(InputStream entrada,OutputStream salida,char[] password)throws IOException {
        byte[] sal=new byte[16],iv=new byte[12];SecureRandom random=new SecureRandom();random.nextBytes(sal);random.nextBytes(iv);
        try{SecretKeySpec key=clave(password,sal);salida.write(CABECERA);salida.write(sal);salida.write(iv);DataOutputStream out=new DataOutputStream(salida);byte[] buffer=new byte[65536];long total=0;int secuencia=0;while(true){int n=0,leidos;while(n<buffer.length&&(leidos=entrada.read(buffer,n,buffer.length-n))!=-1){if(leidos==0)continue;n+=leidos;}total+=n;if(total>LIMITE)throw new IOException("La copia supera el límite de 512 MB");Cipher cipher=bloque(Cipher.ENCRYPT_MODE,key,iv,secuencia++,n);out.writeInt(n);out.write(cipher.doFinal(buffer,0,n));if(n==0)break;}out.flush();}
        catch(GeneralSecurityException e){throw new IOException("No se pudo proteger la copia",e);}
    }
    public static void descifrar(InputStream entrada,OutputStream salida,char[] password)throws IOException {
        DataInputStream in=new DataInputStream(entrada);byte[] cabecera=new byte[8],sal=new byte[16],iv=new byte[12];
        try{in.readFully(cabecera);if(!Arrays.equals(CABECERA,cabecera))throw new IOException("No es una copia completa compatible de Agenda Personal");in.readFully(sal);in.readFully(iv);SecretKeySpec key=clave(password,sal);long total=0;int secuencia=0;while(true){int n=in.readInt();if(n<0||n>65536)throw new IOException("El archivo tiene un bloque inválido");total+=n;if(total>LIMITE)throw new IOException("La copia supera el límite de 512 MB");byte[] bloque=new byte[n+16];in.readFully(bloque);Cipher cipher=bloque(Cipher.DECRYPT_MODE,key,iv,secuencia++,n);salida.write(cipher.doFinal(bloque));if(n==0){if(in.read()!=-1)throw new IOException("El archivo contiene datos inesperados");break;}}salida.flush();}
        catch(AEADBadTagException e){throw new IOException("La contraseña no coincide o el archivo está dañado",e);}
        catch(GeneralSecurityException e){throw new IOException("No se pudo abrir la copia",e);}
        catch(EOFException e){throw new IOException("El archivo de copia está incompleto",e);}
    }
    private static Cipher bloque(int modo,SecretKeySpec key,byte[] base,int secuencia,int longitud)throws GeneralSecurityException {byte[] iv=Arrays.copyOf(base,12);ByteBuffer.wrap(iv).putInt(8,secuencia);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(modo,key,new GCMParameterSpec(128,iv));c.updateAAD(ByteBuffer.allocate(28).put(CABECERA).put(base).putInt(secuencia).putInt(longitud).array());return c;}
}
