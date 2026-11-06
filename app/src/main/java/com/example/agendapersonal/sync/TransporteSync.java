package com.example.agendapersonal.sync;import java.util.*;
public interface TransporteSync{
void dispositivo(String owner,String device)throws Exception;
void subirVerificados(List<CambioSync>cambios)throws Exception;
List<CambioSync> pagina(String owner,String device,String fecha,String id,int limite)throws Exception;
void recibosVerificados(String owner,String device,List<String>ids)throws Exception;
}