package com.example.agendapersonal.ui.registro;
import java.util.List;
public interface Fuente extends AutoCloseable {
    List<Registro> todos();
    boolean guardar(Registro registro, boolean nuevo);
    boolean eliminar(String id);
    void close();
}
