package com.example.agendapersonal.respaldo;
import org.junit.*;import static org.junit.Assert.*;import java.io.*;import java.nio.file.*;import java.util.*;import java.util.zip.*;

public class ArchivoRespaldoTest {
    private static final char[] CLAVE="Mi-copia-de-prueba-2026".toCharArray();
    private byte[] proteger(byte[] datos)throws Exception {ByteArrayOutputStream out=new ByteArrayOutputStream();CifradoRespaldo.cifrar(new ByteArrayInputStream(datos),out,CLAVE);return out.toByteArray();}
    private byte[] abrir(byte[] archivo,char[] clave)throws Exception {ByteArrayOutputStream out=new ByteArrayOutputStream();CifradoRespaldo.descifrar(new ByteArrayInputStream(archivo),out,clave);return out.toByteArray();}
    private byte[] datos(int n){byte[] d=new byte[n];new Random(35).nextBytes(d);return d;}
    private void rechaza(byte[] d,char[] clave)throws Exception {try{abrir(d,clave);fail("Debió rechazar el archivo");}catch(IOException esperado){assertNotNull(esperado.getMessage());}}
    @Test public void recuperaArchivoConVariosBloques()throws Exception {byte[] d=datos(200003);assertArrayEquals(d,abrir(proteger(d),CLAVE));}
    @Test public void recuperaArchivoVacio()throws Exception {assertArrayEquals(new byte[0],abrir(proteger(new byte[0]),CLAVE));}
    @Test public void archivosIgualesTienenCifradoDistinto()throws Exception {byte[] d=datos(500);assertFalse(Arrays.equals(proteger(d),proteger(d)));}
    @Test public void rechazaPasswordIncorrecta()throws Exception {rechaza(proteger(datos(300)),"otra-contraseña".toCharArray());}
    @Test public void rechazaContenidoModificado()throws Exception {byte[] d=proteger(datos(100000));d[500]^=1;rechaza(d,CLAVE);}
    @Test public void autenticaCabeceraDelNonce()throws Exception {byte[] d=proteger(datos(200));d[35]^=1;rechaza(d,CLAVE);}
    @Test public void rechazaTruncadoYFaltaDeMarcadorFinal()throws Exception {byte[] d=proteger(datos(65536));rechaza(Arrays.copyOf(d,d.length-20),CLAVE);}
    @Test public void rechazaDatosAgregadosAlFinal()throws Exception {byte[] d=proteger(datos(50));rechaza(Arrays.copyOf(d,d.length+1),CLAVE);}
    @Test public void rechazaCabeceraDesconocida()throws Exception {byte[] d=proteger(datos(20));d[0]='X';rechaza(d,CLAVE);}
    @Test public void rechazaLongitudDeBloqueMaliciosa()throws Exception {byte[] d=proteger(datos(100));d[36]=127;rechaza(d,CLAVE);}
    @Test public void noAceptaZipConRutaDeEscape()throws Exception {File dir=Files.createTempDirectory("agenda_zip_").toFile(),zip=new File(dir,"malo.zip");try{try(ZipOutputStream out=new ZipOutputStream(new FileOutputStream(zip))){out.putNextEntry(new ZipEntry("../escape"));out.write(1);out.closeEntry();}try{ArchivoRespaldo.extraer(zip,new File(dir,"agenda.db"),new File(dir,"manifest.json"));fail();}catch(IOException esperado){}assertFalse(new File(dir.getParentFile(),"escape").exists());}finally{for(File f:dir.listFiles())f.delete();dir.delete();}}
    @Test public void empaquetaYRecuperaAmbasPartesSinCambios()throws Exception {File dir=Files.createTempDirectory("agenda_zip_").toFile();try{File db=new File(dir,"base"),meta=new File(dir,"meta"),zip=new File(dir,"copia.zip"),d2=new File(dir,"restaurada"),m2=new File(dir,"resumen");Files.write(db.toPath(),datos(100003));Files.write(meta.toPath(),"{\"version\":1}".getBytes(java.nio.charset.StandardCharsets.UTF_8));ArchivoRespaldo.empaquetar(db,meta,zip);ArchivoRespaldo.extraer(zip,d2,m2);assertEquals(ArchivoRespaldo.sha256(db),ArchivoRespaldo.sha256(d2));assertArrayEquals(Files.readAllBytes(meta.toPath()),Files.readAllBytes(m2.toPath()));}finally{for(File f:dir.listFiles())f.delete();dir.delete();}}
    @Test public void zipIncompletoSeRechaza()throws Exception {File dir=Files.createTempDirectory("agenda_zip_").toFile();try{File zip=new File(dir,"copia.zip");try(ZipOutputStream out=new ZipOutputStream(new FileOutputStream(zip))){out.putNextEntry(new ZipEntry("manifest.json"));out.write("{}".getBytes());out.closeEntry();}try{ArchivoRespaldo.extraer(zip,new File(dir,"agenda.db"),new File(dir,"manifest.json"));fail();}catch(IOException esperado){}}finally{for(File f:dir.listFiles())f.delete();dir.delete();}}
}
