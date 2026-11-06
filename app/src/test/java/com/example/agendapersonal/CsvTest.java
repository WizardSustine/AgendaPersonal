package com.example.agendapersonal;
import org.junit.Test;import static org.junit.Assert.*;import java.io.*;import java.util.*;import com.example.agendapersonal.util.Csv;
public class CsvTest {
 @Test public void conservaComillasAcentosYLineas()throws Exception{List<String> fila=Arrays.asList("áé Ñ","Cita, control","Dijo \"sí\"\nSegunda línea","");StringWriter s=new StringWriter();Csv.escribir(s,fila);assertEquals(Collections.singletonList(fila),Csv.leer(new StringReader(s.toString())));}
 @Test(expected=IOException.class) public void rechazaCsvTruncado()throws Exception{Csv.leer(new StringReader("\"sin cierre"));}
 @Test public void admiteBomYCeldasVaciasAlFinal()throws Exception{assertEquals(Collections.singletonList(Arrays.asList("a","b","")),Csv.leer(new StringReader("\ufeffa,b,")));}
}
