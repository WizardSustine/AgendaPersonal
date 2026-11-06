package com.example.agendapersonal.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.agendapersonal.MainActivity;
import com.example.agendapersonal.R;

import java.util.LinkedHashMap;
import java.util.Map;

public class MenuNavegacion {

    private final Activity activity;
    private final View raiz;
    private final FrameLayout contenido;
    private final FrameLayout capaMenu;
    private final LinearLayout opciones;

    private boolean abierto = false;

    private final Map<String, Class<? extends Activity>> destinos =
            new LinkedHashMap<>();

    private static final String[][] MODULOS = {{"Inicio",""},{"Agenda",""},{"Tareas",""},{"Eventos",""},{"Estudios",""},{"Sociales",""},{"Salud",""},{"Trabajo",""},{"Freelance",""},{"Economía",""},{"Ejercicio",""},{"Estado emocional",""},{"Investigaciones",""},{"Personas",""},{"Frases personales",""},{"Ajustes",""},{"Ayuda",""}};

    public MenuNavegacion(Activity activity) {
        this.activity = activity;

        raiz = LayoutInflater.from(activity).inflate(
                R.layout.layout_contenedor_navegacion,
                null,
                false
        );

        contenido = raiz.findViewById(R.id.contenedorPrincipal);
        capaMenu = raiz.findViewById(R.id.capaMenu);
        opciones = raiz.findViewById(R.id.menuOpciones);

        View fondo = raiz.findViewById(R.id.fondoMenu);
        fondo.setOnClickListener(v -> cerrar());

        registrarDestinos();
        construirOpciones();
    }

    private void registrarDestinos() {
        destinos.putAll(Destinos.todos());
    }

    public View getRaiz() {
        return raiz;
    }

    public FrameLayout getContenido() {
        return contenido;
    }

    public void establecerContenido(View vista) {
        if (vista.getParent() instanceof ViewGroup) {
            ((ViewGroup) vista.getParent()).removeView(vista);
        }

        contenido.removeAllViews();

        contenido.addView(
                vista,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
    }

    public void establecerContenido(int layoutId) {
        View vista = LayoutInflater.from(activity).inflate(
                layoutId,
                contenido,
                false
        );

        establecerContenido(vista);
    }

    private void construirOpciones() {
        opciones.removeAllViews();

        LayoutInflater inflater =
                LayoutInflater.from(activity);

        for (String[] modulo : MODULOS) {
            final String nombre = modulo[0];
            final String icono = modulo[1];

            View item = inflater.inflate(
                    R.layout.item_menu_lateral,
                    opciones,
                    false
            );

            TextView texto = item.findViewById(R.id.menuTexto);
            android.widget.ImageView imagen = item.findViewById(R.id.menuIcono);

            texto.setText(nombre);
            imagen.setImageResource(Iconos.de(nombre));

            Class<? extends Activity> destino =
                    destinos.get(nombre);

            boolean seleccionado =
                    destino != null
                            && activity.getClass().equals(destino);

            if (seleccionado) {
                item.setBackgroundResource(
                        R.drawable.bg_menu_selected
                );

                texto.setTypeface(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                );

                texto.setTextColor(
                        com.example.agendapersonal.ui.Tema.color(activity,R.attr.agenda_text)
                );
            }

            item.setOnClickListener(v -> navegar(nombre));
            opciones.addView(item);
        }
    }

    public void registrarDestino(
            String nombre,
            Class<? extends Activity> destino
    ) {
        destinos.put(nombre, destino);
        construirOpciones();
    }

    public void abrir() {
        if (abierto) return;

        capaMenu.setVisibility(View.VISIBLE);
        abierto = true;
    }

    public void cerrar() {
        if (!abierto) return;

        capaMenu.setVisibility(View.GONE);
        abierto = false;
    }

    public void alternar() {
        if (abierto) {
            cerrar();
        } else {
            abrir();
        }
    }

    public boolean estaAbierto() {
        return abierto;
    }

    private void navegar(String nombre) {
        Class<? extends Activity> destino =
                destinos.get(nombre);

        if (destino == null) {
            cerrar();
            return;
        }

        cerrar();

        if (activity.getClass().equals(destino)) {
            return;
        }

        Intent intent = new Intent(activity, destino);

        if (destino == MainActivity.class) {
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );
        }

        activity.startActivity(intent);

        if (destino == MainActivity.class) {
            activity.finish();
        }
    }
}
