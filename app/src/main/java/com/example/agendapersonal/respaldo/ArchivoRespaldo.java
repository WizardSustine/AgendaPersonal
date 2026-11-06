package com.example.agendapersonal.respaldo;

import java.io.*;import java.security.*;import java.util.*;import java.util.zip.*;

/** Solo admite las dos entradas previstas; nunca extrae rutas proporcionadas por el archivo. */
public final class ArchivoRespaldo {
    private ArchivoRespaldo(){}
    public static String sha256(File file)throws IOException {try{MessageDigest sha=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(file)){byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)sha.update(b,0,n);}StringBuilder s=new StringBuilder();for(byte b:sha.digest())s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}catch(NoSuchAlgorithmException e){throw new AssertionError(e);}}
    public static void empaquetar(File db,File meta,File zip)throws IOException {try(ZipOutputStream out=new ZipOutputStream(new FileOutputStream(zip))){for(File f:new File[]{meta,db}){out.putNextEntry(new ZipEntry(f.equals(db)?"agenda.db":"manifest.json"));try(InputStream in=new FileInputStream(f)){copiar(in,out,CifradoRespaldo.LIMITE);}out.closeEntry();}out.finish();}sincronizar(zip);}
    public static void extraer(File zip,File db,File meta)throws IOException {Set<String>vistos=new HashSet<>();long total=0;try(ZipInputStream in=new ZipInputStream(new FileInputStream(zip))){ZipEntry e;while((e=in.getNextEntry())!=null){String nombre=e.getName();if(e.isDirectory()||(!nombre.equals("agenda.db")&&!nombre.equals("manifest.json"))||!vistos.add(nombre))throw new IOException("La estructura de la copia no es válida");File destino=nombre.equals("agenda.db")?db:meta;try(OutputStream out=new FileOutputStream(destino)){total+=copiar(in,out,nombre.equals("manifest.json")?1024*1024:CifradoRespaldo.LIMITE-total);}in.closeEntry();}if(vistos.size()!=2)throw new IOException("Faltan partes de la copia");}sincronizar(db);sincronizar(meta);}
    public static long copiar(InputStream in,OutputStream out,long max)throws IOException {long total=0;byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1){total+=n;if(total>max)throw new IOException("El archivo supera el tamaño permitido");out.write(b,0,n);}return total;}
    public static void sincronizar(File f)throws IOException {try(RandomAccessFile archivo=new RandomAccessFile(f,"rw")){archivo.getFD().sync();}}
}
