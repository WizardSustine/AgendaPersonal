package com.example.agendapersonal;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.agendapersonal.model.Tarea;
import com.example.agendapersonal.repository.TareaRepository;
import com.example.agendapersonal.ui.AgendaActivity;
import com.example.agendapersonal.ui.Destinos;
import com.example.agendapersonal.ui.Iconos;
import com.example.agendapersonal.repository.FrasePersonalRepository;
import com.example.agendapersonal.model.FrasePersonal;
import com.example.agendapersonal.ui.EstiloApp;
import com.example.agendapersonal.ui.MenuNavegacion;
import com.example.agendapersonal.ui.TareasActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends com.example.agendapersonal.ui.ActividadTematica {
    private MenuNavegacion navegacion;
    private TareaRepository tareaRepository;
    private View inicio;
    private final android.os.Handler refrescoDia=new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable actualizarDia=new Runnable(){public void run(){if(inicio!=null)actualizarProgreso();refrescoDia.postDelayed(this,60000);}};


    private static final String[] MODULOS = {
            "Agenda", "Tareas", "Estudios", "Sociales", "Salud",
            "Trabajo", "Freelance", "Economía", "Ejercicio", "Estado emocional"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("agenda_identidad", MODE_PRIVATE);
        String deviceId = prefs.getString("device_id", null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString("device_id", deviceId).apply();
        }
        tareaRepository = new TareaRepository(this, deviceId);
        navegacion = new MenuNavegacion(this);
        setContentView(navegacion.getRaiz());
        navegacion.establecerContenido(R.layout.activity_main);
        inicio = navegacion.getContenido().getChildAt(0);
        inicio.findViewById(R.id.botonMenu).setOnClickListener(v -> navegacion.alternar());
        inicio.findViewById(R.id.avisosInicio).setOnClickListener(v->startActivity(new Intent(this,com.example.agendapersonal.ui.NotificacionesActivity.class)));
        inicio.findViewById(R.id.alternarRadar).setOnClickListener(v->{View radar=inicio.findViewById(R.id.contenedorGrafico);boolean abrir=radar.getVisibility()!=View.VISIBLE;radar.setVisibility(abrir?View.VISIBLE:View.GONE);((android.widget.Button)v).setText(abrir?"Ocultar radar":"Ver radar");});
        inicio.findViewById(R.id.botonVerTareas).setOnClickListener(v -> abrirTareas());
        android.widget.Spinner periodo=inicio.findViewById(R.id.periodoInicio);
        periodo.setAdapter(new android.widget.ArrayAdapter<>(this,com.example.agendapersonal.R.layout.item_opcion_tema,new String[]{"Todo el historial","Este mes","Este año"}));
        periodo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?>p,View v,int n,long id){actualizarProgreso();}public void onNothingSelected(android.widget.AdapterView<?>p){}});
        ((TextView) inicio.findViewById(R.id.textoFrasePersonal))
                .setText(R.string.inicio_frase_pendiente);
        crearAccesos();
        inicio.findViewById(R.id.textoFrasePersonal).setOnClickListener(v -> startActivity(new Intent(this, com.example.agendapersonal.ui.FrasesActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (inicio != null && tareaRepository != null) {
            ((TextView) inicio.findViewById(R.id.textoFechaActual)).setText(
                    new SimpleDateFormat("EEEE d 'de' MMMM", new Locale("es"))
                            .format(new Date()));
            actualizarFrase();
            actualizarProgreso();
            actualizarProximasTareas();
            refrescoDia.removeCallbacks(actualizarDia);refrescoDia.postDelayed(actualizarDia,60000-System.currentTimeMillis()%60000);
        }
    }

    @Override protected void onPause(){refrescoDia.removeCallbacks(actualizarDia);super.onPause();}

    private void actualizarFrase() {
        String device=getSharedPreferences("agenda_identidad",MODE_PRIVATE).getString("device_id","");
        FrasePersonalRepository repo=new FrasePersonalRepository(this,device);
        try {
            List<FrasePersonal> frases=repo.obtenerTodos();
            ((TextView)inicio.findViewById(R.id.textoFrasePersonal)).setText(frases.isEmpty()?getString(R.string.inicio_frase_pendiente):frases.get(new java.util.Random().nextInt(frases.size())).getFrase());
        } finally {repo.cerrar();}
    }
    private void actualizarProgreso() {
        android.widget.Spinner periodo=inicio.findViewById(R.id.periodoInicio);
        java.time.LocalDate hoy=java.time.LocalDate.now();int seleccion=periodo.getSelectedItemPosition();
        java.time.LocalDate desde=seleccion==1?hoy.withDayOfMonth(1):seleccion==2?hoy.withDayOfYear(1):java.time.LocalDate.MIN;
        com.example.agendapersonal.ui.ProgresoInicio datos=com.example.agendapersonal.ui.ProgresoInicio.cargar(this,desde,seleccion==1?hoy.withDayOfMonth(hoy.lengthOfMonth()):seleccion==2?hoy.withDayOfYear(hoy.lengthOfYear()):java.time.LocalDate.MAX);
        int escala=getSharedPreferences("agenda_preferencias",MODE_PRIVATE).getInt("escala_radar",10);
        ((com.example.agendapersonal.ui.RadarView)inicio.findViewById(R.id.radarInicio)).datos(datos.azul,datos.rojo,escala);

        com.example.agendapersonal.ui.ResumenDia dia=com.example.agendapersonal.ui.ProgresoDia.cargar(this,java.time.LocalDateTime.now());
        ((com.example.agendapersonal.ui.ProgresoCircularView)inicio.findViewById(R.id.indicadorAnimo)).datos(dia.finalizadas,dia.total);
        ((com.example.agendapersonal.ui.ProgresoCircularView)inicio.findViewById(R.id.indicadorAnimo)).situacion(dia.vencidas,dia.pendientes);
        int porcentaje=dia.porcentaje();
        ((android.widget.ProgressBar)inicio.findViewById(R.id.barraProgresoInicio)).setProgress(porcentaje);
        ((TextView)inicio.findViewById(R.id.porcentajeInicio)).setText(porcentaje+" %");
        ((TextView)inicio.findViewById(R.id.textoProgreso)).setText(dia.titulo());
        ((TextView)inicio.findViewById(R.id.textoResumen)).setText(dia.detalle());
        ((TextView)inicio.findViewById(R.id.leyendaRadar)).setText("Azul: % de tareas no pendientes\nRojo: actividades finalizadas · escala 0–"+escala);
    }

    private void actualizarProximasTareas() {
        LinearLayout lista = inicio.findViewById(R.id.listaProximasTareas);
        lista.removeAllViews();
        List<Tarea> pendientes = tareaRepository.obtenerPendientes();
        inicio.findViewById(R.id.textoSinTareas)
                .setVisibility(pendientes.isEmpty() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < Math.min(3, pendientes.size()); i++) {
            Tarea tarea = pendientes.get(i);
            View fila = getLayoutInflater().inflate(R.layout.item_tarea_inicio, lista, false);
            ((TextView) fila.findViewById(R.id.tareaDescripcion)).setText(tarea.getDescripcion());
            ((TextView) fila.findViewById(R.id.tareaFecha))
                    .setText(formatearFecha(tarea.getFechaVencimiento()));
            fila.setOnClickListener(v -> abrirTareas());
            fila.setFocusable(true);
            lista.addView(fila);
        }
    }

    private void crearAccesos() {
        GridLayout grilla = inicio.findViewById(R.id.grillaAccesos);
        grilla.setColumnCount(getResources().getConfiguration().screenWidthDp >= 360 ? 4 : 3);
        for (int i = 0; i < MODULOS.length; i++) {
            final String modulo = MODULOS[i];
            View acceso = getLayoutInflater().inflate(R.layout.item_acceso_inicio, grilla, false);
            ((android.widget.ImageView) acceso.findViewById(R.id.accesoIcono)).setImageResource(Iconos.de(modulo));
            ((TextView) acceso.findViewById(R.id.accesoNombre)).setText(modulo);
            acceso.setOnClickListener(v -> {
                Class<? extends Activity> destino=Destinos.de(modulo);
                if(destino!=null)startActivity(new Intent(this,destino));
            });
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) acceso.getLayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            int margen = EstiloApp.dp(this, 4);
            params.setMargins(margen, margen, margen, margen);
            grilla.addView(acceso, params);
        }
    }

    private void abrirTareas() {
        startActivity(new Intent(this, TareasActivity.class));
    }

    private String formatearFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) return getString(R.string.inicio_sin_fecha);
        try{java.time.LocalDate f=java.time.LocalDate.parse(fecha);if(f.equals(java.time.LocalDate.now()))return "Hoy";if(f.equals(java.time.LocalDate.now().plusDays(1)))return "Mañana";}catch(RuntimeException ignored){}
        if (fecha.length() >= 10) return fecha.substring(8, 10) + "/" + fecha.substring(5, 7);
        return fecha;
    }

    @Override
    public void onBackPressed() {
        if (navegacion != null && navegacion.estaAbierto()) navegacion.cerrar();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) tareaRepository.cerrar();
        super.onDestroy();
    }
}