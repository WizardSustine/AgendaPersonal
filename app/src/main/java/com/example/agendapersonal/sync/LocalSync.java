package com.example.agendapersonal.sync;import java.util.*;
public interface LocalSync{
List<CambioSync> salientes(String owner,int limite)throws Exception;
void confirmarEnviados(String owner,List<CambioSync>cambios)throws Exception;
void preparar(CambioSync cambio,String owner,String device)throws Exception;
boolean aplicar(CambioSync cambio,String owner,String device)throws Exception;
List<CambioSync> pendientes(String owner)throws Exception;
List<String> recibosPendientes(String owner,String device,int limite);
void confirmarRecibos(String owner,String device,List<String>ids);
int cantidadConflictos(String owner)throws Exception;
int cantidadPendientes(String owner);
default long enviadosConfirmados(String owner){return -1;}
default long recibidosConfirmados(String owner){return -1;}
default int cantidadSalientes(String owner){return 0;}
default void limpiarConfirmados(String owner)throws Exception{}
}