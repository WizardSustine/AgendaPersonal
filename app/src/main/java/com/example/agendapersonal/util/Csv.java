package com.example.agendapersonal.util;
import java.io.*;
import java.util.*;
/** CSV UTF-8 con comillas, comas, CRLF y saltos dentro de las celdas. */
public final class Csv {
 private Csv(){}
 public static void escribir(Writer out,List<String> fila)throws IOException {
  for(int i=0;i<fila.size();i++){if(i>0)out.write(',');String v=fila.get(i);out.write('"');if(v!=null)out.write(v.replace("\"","\"\""));out.write('"');}out.write("\r\n");
 }
 public static List<List<String>> leer(Reader input)throws IOException {
  PushbackReader in=new PushbackReader(input,2);List<List<String>> filas=new ArrayList<>();List<String> fila=new ArrayList<>();StringBuilder celda=new StringBuilder();boolean comillas=false,cerrada=false,iniciada=false;int v;boolean first=true;
  while((v=in.read())!=-1){char c=(char)v;if(first){first=false;if(c=='\ufeff')continue;}
   if(comillas){if(c=='"'){int next=in.read();if(next=='"')celda.append('"');else{comillas=false;cerrada=true;if(next!=-1)in.unread(next);}}else celda.append(c);continue;}
   if(c=='"'){if(iniciada||cerrada||celda.length()>0)throw new IOException("Comillas inválidas en CSV");comillas=true;iniciada=true;continue;}
   if(c==','||c=='\n'||c=='\r'){fila.add(celda.toString());celda.setLength(0);iniciada=false;cerrada=false;if(c!=','){if(c=='\r'){int next=in.read();if(next!='\n'&&next!=-1)in.unread(next);}filas.add(fila);fila=new ArrayList<>();}continue;}
   if(cerrada)throw new IOException("Contenido después del cierre de comillas");celda.append(c);iniciada=true;
  }
  if(comillas)throw new IOException("Celda CSV sin cerrar");if(iniciada||cerrada||celda.length()>0||!fila.isEmpty()){fila.add(celda.toString());filas.add(fila);}return filas;
 }
}
