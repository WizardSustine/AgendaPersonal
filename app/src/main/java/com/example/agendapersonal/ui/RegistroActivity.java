package com.example.agendapersonal.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import com.example.agendapersonal.R;
import com.example.agendapersonal.eventos.*;
import com.example.agendapersonal.ui.registro.*;
import com.example.agendapersonal.util.ReglasAgenda;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/** Grillas y formularios comparten controles; los campos y repositorios siguen siendo tipados. */
public abstract class RegistroActivity extends com.example.agendapersonal.ui.ActividadTematica {
    protected abstract String[] tablas();
    protected abstract int layout();
    protected MenuNavegacion navegacion;
    private Fuente fuente;
    private Definicion definicion;
    private String deviceId, ownerId;
    private LinearLayout lista;
    private TextView vacio, resumen;
    private EditText busqueda, desde, hasta;
    private Spinner secciones, estados;
    private int seleccion;
    private String filtroEstado = "Todos";
    private List<Registro> datos = new ArrayList<>();
    private final Map<String,List<Registro>> relacionados = new HashMap<>();
    private final Map<String,Spinner> filtrosRelacion = new LinkedHashMap<>();
    private final Map<String,List<Registro>> valoresFiltro = new HashMap<>();
    private Formulario formularioAbierto;
    private final java.util.Deque<Formulario> pilaFormularios=new java.util.ArrayDeque<>();
    private Bundle restaurar;
    private boolean intencionProcesada;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences prefs = getSharedPreferences("agenda_identidad", MODE_PRIVATE);
        deviceId = prefs.getString("device_id", null);
        ownerId = com.example.agendapersonal.sync.IdentidadLocal.propietario(this);
        if(deviceId==null) deviceId=UUID.randomUUID().toString();
        if(ownerId==null) ownerId=UUID.randomUUID().toString();
        prefs.edit().putString("device_id",deviceId).putString("owner_id",ownerId).apply();
        navegacion = new MenuNavegacion(this);
        setContentView(navegacion.getRaiz());
        navegacion.establecerContenido(layout());
        lista=findViewById(R.id.listaRegistros); vacio=findViewById(R.id.textoVacio);
        resumen=findViewById(R.id.resumenRegistros);
        busqueda=findViewById(R.id.busquedaRegistros); desde=findViewById(R.id.filtroDesde); hasta=findViewById(R.id.filtroHasta);
        desde.setHint("Desde: AAAA-MM-DD"); hasta.setHint("Hasta: AAAA-MM-DD");
        desde.setOnClickListener(v->fecha(desde)); hasta.setOnClickListener(v->fecha(hasta));
        secciones=findViewById(R.id.selectorSeccion); estados=findViewById(R.id.filtroEstado);
        List<String> titulos=new ArrayList<>(); for(String t:tablas())titulos.add(Definicion.de(t).titulo);
        selector(secciones,titulos);
        selector(estados,Arrays.asList("Todos","MUCHO","PENDIENTE","URGENTE","CRITICO","ATRASADO","FINALIZADO","CANCELADO","POSTERGADO"));
        seleccion=state==null?getIntent().getIntExtra("seccionInicial",0):state.getInt("seccion",0);
        seleccion=Math.min(seleccion,tablas().length-1);
        secciones.setSelection(seleccion);
        secciones.setVisibility(tablas().length>1?View.VISIBLE:View.GONE);
        secciones.setOnItemSelectedListener(oyente(()->{int n=secciones.getSelectedItemPosition(); if(definicion==null||n!=seleccion){seleccion=n;abrirSeccion();}}));
        estados.setOnItemSelectedListener(oyente(()->{filtroEstado=estados.getSelectedItem().toString();render();}));
        TextWatcher watcher=new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){render();}public void afterTextChanged(Editable e){}};
        busqueda.addTextChangedListener(watcher);desde.addTextChangedListener(watcher);hasta.addTextChangedListener(watcher);
        findViewById(R.id.botonMenu).setOnClickListener(v->navegacion.alternar());
        findViewById(R.id.botonBuscar).setOnClickListener(v->{View panel=findViewById(R.id.panelFiltros);panel.setVisibility(panel.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);});
        findViewById(R.id.botonOpciones).setOnClickListener(v->{PopupMenu menu=new PopupMenu(this,v);menu.getMenu().add("Personas y catálogos");menu.getMenu().add("Frases personales");menu.setOnMenuItemClickListener(item->{startActivity(new Intent(this,item.getTitle().toString().equals("Frases personales")?FrasesActivity.class:CatalogosActivity.class));return true;});menu.show();});
        findViewById(R.id.botonAgregar).setOnClickListener(v->editar(definicion,new Registro(),true,()->cargar()));
        restaurar=state;intencionProcesada=state!=null;
        abrirSeccion();
        if(state!=null){busqueda.setText(state.getString("buscar",""));desde.setText(state.getString("desde",""));hasta.setText(state.getString("hasta",""));estados.setSelection(state.getInt("estado",0));}
    }
    @Override protected void onResume(){super.onResume();if(definicion!=null)cargar();}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);intencionProcesada=false;cargar();}
    private void abrirSeccion(){
        if(fuente!=null)fuente.close();
        definicion=Definicion.de(tablas()[seleccion]);
        fuente=Fuentes.abrir(this,deviceId,definicion.tabla);
        ((TextView)findViewById(R.id.tituloPantalla)).setText(definicion.titulo);
        findViewById(R.id.botonAgregar).setVisibility(definicion.soloConsulta()?View.GONE:View.VISIBLE);
        estados.setVisibility(definicion.campo("estadoEspecial")!=null?View.VISIBLE:View.GONE);
        findViewById(R.id.filtrosFecha).setVisibility(definicion.fecha().isEmpty()?View.GONE:View.VISIBLE);
        construirFiltros();cargar();
        if(restaurar!=null&&restaurar.containsKey("borrador")){
            String tabla=restaurar.getString("tablaFormulario");Bundle b=restaurar.getBundle("borrador");Registro r=new Registro();for(String k:b.keySet())r.put(k,b.getString(k));boolean nuevo=restaurar.getBoolean("nuevo");restaurar=null;
            editar(Definicion.de(tabla),r,nuevo,()->cargar());
        }
    }
    private void construirFiltros(){
        LinearLayout cont=findViewById(R.id.filtrosRelacion);cont.removeAllViews();filtrosRelacion.clear();valoresFiltro.clear();
        for(Campo c:definicion.campos)if(!c.relacion.isEmpty()&&!c.clave.equals("serieId")){
            View campo=getLayoutInflater().inflate(R.layout.campo_selector,cont,false);
            ((TextView)campo.findViewById(R.id.etiquetaCampo)).setText(c.etiqueta);
            campo.findViewById(R.id.agregarRelacionado).setVisibility(View.GONE);
            Spinner spinner=campo.findViewById(R.id.valorSelector);filtrosRelacion.put(c.clave,spinner);cont.addView(campo);
        }
    }
    private void cargar(){
        try{relacionados.clear();datos=fuente.todos();if(definicion.tabla.equals("eventos")){List<Registro> completos=new ArrayList<>();for(Registro e:datos){Registro real=SeriesEvento.cargar(this,e.texto("id"));if(real!=null)completos.add(real);}datos=completos;}
            for(Campo c:definicion.campos)if(!c.relacion.isEmpty())relacion(c.relacion);
            for(Map.Entry<String,Spinner> entry:filtrosRelacion.entrySet()){
                Campo c=definicion.campo(entry.getKey());List<Registro> opciones=relacion(c.relacion);int anterior=entry.getValue().getSelectedItemPosition();
                List<String> nombres=new ArrayList<>();nombres.add("Todos");for(Registro r:opciones)nombres.add(nombre(c.relacion,r));valoresFiltro.put(c.clave,opciones);selector(entry.getValue(),nombres);entry.getValue().setSelection(Math.max(0,Math.min(anterior,nombres.size()-1)));entry.getValue().setOnItemSelectedListener(oyente(()->render()));
            }
            render();
            if(!intencionProcesada){intencionProcesada=true;String id=getIntent().getStringExtra("registroId");if(definicion.tabla.equals("eventos")&&getIntent().hasExtra("serieId")){Registro ocurrencia=SeriesEvento.ocurrencia(this,getIntent().getStringExtra("serieId"),getIntent().getLongExtra("ordinal",0));if(ocurrencia!=null)detalle(ocurrencia);id=null;}if(id!=null)for(Registro r:datos)if(id.equals(r.texto("id"))){detalle(r);break;}if(getIntent().getBooleanExtra("nuevo",false)){Registro r=new Registro();String fecha=getIntent().getStringExtra("fechaInicial");if(fecha!=null)r.put(definicion.tabla.equals("tareas")?"fechaVencimiento":"fecha",fecha);editar(definicion,r,true,()->cargar());}}
        }catch(RuntimeException e){vacio.setText("No se pudieron leer los registros. Volvé a abrir el módulo.");vacio.setVisibility(View.VISIBLE);mensaje("No se pudieron cargar los datos");android.util.Log.e("AgendaPersonal","Cargar "+definicion.tabla,e);}
    }
    private List<Registro> relacion(String tabla){
        if(!relacionados.containsKey(tabla)){try(Fuente f=Fuentes.abrir(this,deviceId,tabla)){relacionados.put(tabla,f.todos());}}
        return relacionados.get(tabla);
    }
    private String nombre(String tabla,Registro r){
        if(tabla.equals("direcciones"))return r.texto("calle")+" "+r.texto("numero")+", "+r.texto("localidad");
        for(String c:new String[]{"nombre","frase","idOriginal","descripcion","observacion","actividad"})if(!r.texto(c).isEmpty())return r.texto(c);
        return r.texto("id");
    }
    private String mostrar(Campo c,Registro r){
        if(c==null)return "";String valor=r.texto(c.clave);
        if(!c.relacion.isEmpty())for(Registro otro:relacion(c.relacion))if(otro.texto("id").equals(valor))return nombre(c.relacion,otro);
        if(c.clave.equals("estadoEspecial"))return estado(r);
        if(c.tipo.equals("booleano"))return Boolean.parseBoolean(valor)?"Sí":"No";
        if(c.tipo.equals("fecha")&&!valor.isEmpty()){
            try{LocalDate fecha=LocalDate.parse(valor);String hora=r.texto(c.clave.equals("fechaInicio")?"horaInicio":c.clave.equals("fechaVencimiento")?"horaVencimiento":"hora");return fecha.equals(LocalDate.now())?(hora.isEmpty()?"Hoy":hora):String.format(Locale.getDefault(),"%02d/%02d/%04d",fecha.getDayOfMonth(),fecha.getMonthValue(),fecha.getYear())+(hora.isEmpty()?"":" · "+hora);}catch(RuntimeException ignored){}
        }
        return valor.isEmpty()?"—":valor;
    }
    private String estado(Registro r){return ReglasAgenda.estado(r.texto(definicion.fecha()),r.get("estadoEspecial"),LocalDate.now());}
    private void render(){
        if(definicion==null||lista==null)return;
        lista.removeAllViews();String buscar=busqueda.getText().toString().trim().toLowerCase(Locale.getDefault());
        String ini=desde.getText().toString().trim(),fin=hasta.getText().toString().trim();
        if(!ini.isEmpty()&&ReglasAgenda.validarValor("fecha",ini)!=null){desde.setError("Fecha inválida");return;}
        if(!fin.isEmpty()&&ReglasAgenda.validarValor("fecha",fin)!=null){hasta.setError("Fecha inválida");return;}
        if(!ini.isEmpty()&&!fin.isEmpty()&&ini.compareTo(fin)>0){hasta.setError("Debe ser igual o posterior a Desde");return;}
        List<Registro> visibles=new ArrayList<>();List<Registro> candidatos=new ArrayList<>(datos);if(definicion.tabla.equals("eventos")){try{LocalDate inicio=ini.isEmpty()?LocalDate.now():LocalDate.parse(ini),finaliza=fin.isEmpty()?inicio.plusYears(1):LocalDate.parse(fin);candidatos.addAll(SeriesEvento.virtuales(this,inicio,finaliza));}catch(RuntimeException e){mensaje(e.getMessage());return;}}
        for(Registro r:candidatos){
            if(!"Todos".equals(filtroEstado)&&definicion.campo("estadoEspecial")!=null&&!estado(r).equals(filtroEstado))continue;
            String fecha=r.texto(definicion.fecha());if(!ini.isEmpty()&&fecha.compareTo(ini)<0)continue;if(!fin.isEmpty()&&(fecha.isEmpty()||fecha.substring(0,Math.min(10,fecha.length())).compareTo(fin)>0))continue;
            boolean coincide=true;for(Map.Entry<String,Spinner> entry:filtrosRelacion.entrySet()){int p=entry.getValue().getSelectedItemPosition();List<Registro> opciones=valoresFiltro.get(entry.getKey());if(p>0&&opciones!=null&&p<=opciones.size()&&!r.texto(entry.getKey()).equals(opciones.get(p-1).texto("id"))){coincide=false;break;}}
            if(!coincide)continue;
            StringBuilder texto=new StringBuilder();for(Campo c:definicion.campos)texto.append(' ').append(mostrar(c,r));
            if(!buscar.isEmpty()&&!texto.toString().toLowerCase(Locale.getDefault()).contains(buscar))continue;
            visibles.add(r);
        }
        String fecha=definicion.fecha();String hora=fecha.equals("fechaVencimiento")?"horaVencimiento":fecha.equals("fechaInicio")?"horaInicio":"hora";
        visibles.sort((a,b)->{int n=ordenVacio(a.texto(fecha),b.texto(fecha));if(n==0)n=ordenVacio(a.texto(hora),b.texto(hora));if(n==0)n=b.texto("createdAt").compareTo(a.texto("createdAt"));return n;});
        for(Registro r:visibles){
            View fila=getLayoutInflater().inflate(definicion.tabla.equals("tareas")?R.layout.item_tarea_completa:R.layout.item_registro,lista,false);
            LinearLayout columnas=fila.findViewById(R.id.columnasRegistro);
            if(definicion.tabla.equals("tareas")){
                TextView titulo=fila.findViewById(R.id.tareaTitulo);titulo.setText(r.texto("descripcion"));if("FINALIZADO".equals(r.texto("estadoEspecial")))titulo.setPaintFlags(titulo.getPaintFlags()|android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                ((TextView)fila.findViewById(R.id.tareaFechaCompleta)).setText(mostrar(definicion.campo("fechaVencimiento"),r));
                ((TextView)fila.findViewById(R.id.tareaCategoriaCompleta)).setText(etiqueta(r.texto("categoria")));
                TextView prioridad=fila.findViewById(R.id.tareaPrioridadCompleta);prioridad.setText(etiqueta(r.texto("prioridad")));boolean alta=ReglasAgenda.prioridad(r.texto("prioridad"))<2;prioridad.setTextColor(alta?0xffb3261e:0xff765313);prioridad.setBackground(EstiloApp.fondo(alta?0xfffce8e6:0xfffff2d4,16,Color.TRANSPARENT,this));
                fila.findViewById(R.id.editarTarea).setOnClickListener(v->editar(definicion,r.copia(),false,()->cargar()));fila.findViewById(R.id.opcionesTarea).setOnClickListener(v->detalle(r));
            }else if(definicion.tabla.equals("personas")){
                StringBuilder direcciones=new StringBuilder(),referencias=new StringBuilder();for(Registro direccion:relacion("direcciones"))if(direccion.texto("personaId").equals(r.texto("id"))){if(direcciones.length()>0)direcciones.append("\n");direcciones.append(direccion.texto("calle")).append(' ').append(direccion.texto("numero"));if(!direccion.texto("referencia").isEmpty()){if(referencias.length()>0)referencias.append("\n");referencias.append(direccion.texto("referencia"));}}
                for(String valor:new String[]{r.texto("nombre"),direcciones.length()==0?"Sin dirección":direcciones.toString(),referencias.toString()}){TextView celda=(TextView)getLayoutInflater().inflate(R.layout.celda_registro,columnas,false);celda.setText(valor);columnas.addView(celda);}
            }else for(String clave:definicion.columnas){TextView celda=(TextView)getLayoutInflater().inflate(R.layout.celda_registro,columnas,false);celda.setText(mostrar(definicion.campo(clave),r));columnas.addView(celda);}
            TextView estadoVista=fila.findViewById(R.id.estadoRegistro);CheckBox completar=fila.findViewById(R.id.finalizarRegistro);
            if(definicion.campo("estadoEspecial")!=null){String s=estado(r);estadoVista.setText(etiqueta(s));estadoVista.setTextColor(colorEstado(s));fila.findViewById(R.id.bordeEstado).setBackgroundColor(colorEstado(s));completar.setChecked("FINALIZADO".equals(s));completar.setContentDescription("Finalizar registro");completar.setOnClickListener(v->cambiarEstado(r,completar.isChecked()?"FINALIZADO":null));}
            else{estadoVista.setVisibility(View.GONE);completar.setVisibility(View.GONE);}
            
            fila.setOnClickListener(v->detalle(r));fila.setFocusable(true);lista.addView(fila);
        }
        vacio.setVisibility(visibles.isEmpty()?View.VISIBLE:View.GONE);vacio.setText(datos.isEmpty()?"Todavía no hay registros":"No hay registros con estos filtros");
        resumen.setText(visibles.size()+" de "+datos.size()+" registros");
        if(definicion.campo("importe")!=null){java.math.BigDecimal total=java.math.BigDecimal.ZERO;for(Registro r:visibles)if("FINALIZADO".equals(r.texto("estadoEspecial")))try{total=total.add(new java.math.BigDecimal(r.texto("importe")));}catch(NumberFormatException ignored){} resumen.append(" · Efectivo finalizado: "+total.toPlainString());}
    }
    private String etiqueta(String s){switch(s){case "ALTISIMA":return "Altísima";case "BAJISIMA":return "Bajísima";case "CRITICO":return "Crítico";case "POSTERGADO":return "Postergado";default:if(s.isEmpty())return s;return s.substring(0,1).toUpperCase(java.util.Locale.ROOT)+s.substring(1).toLowerCase(new java.util.Locale("es"));}}
    private int ordenVacio(String a,String b){if(a.isEmpty())return b.isEmpty()?0:1;if(b.isEmpty())return -1;return a.compareTo(b);}
    private int colorEstado(String s){switch(s){case "FINALIZADO":return Color.rgb(77,155,91);case "CRITICO":return Color.rgb(204,81,83);case "ATRASADO":return Color.rgb(171,137,191);case "CANCELADO":return Color.rgb(216,137,49);case "POSTERGADO":return Color.rgb(188,113,61);case "URGENTE":return Color.rgb(66,174,210);case "PENDIENTE":return Color.rgb(115,160,210);default:return Color.DKGRAY;}}
    private void detalle(Registro r){
        View vista=getLayoutInflater().inflate(R.layout.detalle_registro,null,false);LinearLayout campos=vista.findViewById(R.id.detalleCampos);
        for(Campo c:definicion.campos){TextView t=new TextView(this);t.setText(c.etiqueta+"\n"+mostrar(c,r));t.setTextColor(com.example.agendapersonal.ui.Tema.color(this,R.attr.agenda_text));t.setTextSize(16);t.setPadding(0,dp(8),0,dp(8));t.setTextIsSelectable(true);campos.addView(t);}
        AlertDialog dialogo=new AlertDialog.Builder(this).setTitle(definicion.titulo).setView(vista).setNegativeButton("Cerrar",null).create();
        LinearLayout acciones=vista.findViewById(R.id.detalleAcciones);
        String datosOrigen=com.example.agendapersonal.data.ImportadorHistorico.datosDeOrigen(this,definicion.tabla,r.texto("id"));
        if(datosOrigen!=null)boton(acciones,"Datos de origen",()->{TextView t=new TextView(this);t.setText(datosOrigen);t.setTextIsSelectable(true);t.setPadding(dp(20),dp(12),dp(20),dp(12));ScrollView scroll=new ScrollView(this);scroll.addView(t);new AlertDialog.Builder(this).setTitle("Agenda anterior").setView(scroll).setPositiveButton("Cerrar",null).show();});
        if(definicion.tabla.equals("registros_investigacion")){
            try(Fuente f=Fuentes.abrir(this,deviceId,"observaciones_ri")){for(Registro o:f.todos())if(o.texto("registroInvestigacionId").equals(r.texto("id"))){TextView t=new TextView(this);t.setText(o.texto("createdAt")+"\n"+o.texto("observacion"));t.setPadding(0,dp(12),0,dp(12));campos.addView(t);}}
            boton(acciones,"Agregar observación",()->{Registro o=new Registro();o.put("registroInvestigacionId",r.texto("id"));editar(Definicion.de("observaciones_ri"),o,true,()->{dialogo.dismiss();detalle(r);});});
        }else if(!definicion.soloConsulta()){
            if(definicion.campo("estadoEspecial")!=null){boton(acciones,"FINALIZADO",()->{dialogo.dismiss();cambiarEstado(r,"FINALIZADO");});boton(acciones,"POSTERGADO",()->{dialogo.dismiss();postergar(r);});boton(acciones,"CANCELADO",()->{dialogo.dismiss();cambiarEstado(r,"CANCELADO");});boton(acciones,"Reactivar",()->{dialogo.dismiss();cambiarEstado(r,null);});}
            boton(acciones,"Editar",()->{dialogo.dismiss();editar(definicion,r.copia(),false,()->cargar());});
            boton(acciones,"Eliminar",()->{if(definicion.tabla.equals("eventos")){alcance(r,a->{try{SeriesEvento.eliminar(this,r,a);dialogo.dismiss();cargar();}catch(RuntimeException e){error(e);}});return;}new AlertDialog.Builder(this).setMessage("¿Eliminar este registro?").setNegativeButton("Conservar",null).setPositiveButton("Eliminar",(d,w)->{try{if(!fuente.eliminar(r.texto("id")))throw new IllegalStateException();dialogo.dismiss();cargar();}catch(RuntimeException e){error(e);}}).show();});
        }
        dialogo.show();
    }
    private void cambiarEstado(Registro original,String estado){Registro r=original.copia();r.put("estadoEspecial",estado);try{if(definicion.tabla.equals("eventos"))SeriesEvento.guardar(this,r,original,SeriesEvento.SOLO,"NINGUNA",ReglaRecurrencia.Unidad.DIAS,1,null);else if(!fuente.guardar(r,false))throw new IllegalStateException();cargar();}catch(RuntimeException e){error(e);cargar();}}
    private void postergar(Registro r){
        EditText nueva=new EditText(this);nueva.setHint("Nueva fecha opcional: AAAA-MM-DD");nueva.setOnClickListener(v->fecha(nueva));
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Postergar · se conserva el historial").setView(nueva).setNegativeButton("Volver",null).setPositiveButton("Postergar",null).create();
        d.setOnShowListener(v->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b->{String valor=nueva.getText().toString().trim();String e=ReglasAgenda.validarValor("fecha",valor);if(e!=null){nueva.setError(e);return;}try{Postergaciones.guardar(this,deviceId,definicion.tabla,r.texto("id"),definicion.fecha(),valor,r);d.dismiss();cargar();}catch(RuntimeException ex){error(ex);}}));d.show();
    }
    private void boton(LinearLayout cont,String texto,Runnable accion){Button b=(Button)getLayoutInflater().inflate(R.layout.boton_accion,cont,false);b.setText(texto);b.setOnClickListener(v->accion.run());cont.addView(b);}
    private void alcance(Registro r,java.util.function.IntConsumer accion){if(r.texto("serieId").isEmpty()){accion.accept(SeriesEvento.SOLO);return;}new AlertDialog.Builder(this).setTitle("Aplicar a").setItems(new String[]{"Solo esta ocurrencia","Esta y las siguientes","Toda la serie"},(v,n)->accion.accept(n)).setNegativeButton("Cancelar",null).show();}
    private void editar(Definicion d,Registro registro,boolean nuevo,Runnable guardado){Formulario f=new Formulario(d,registro,nuevo,guardado);formularioAbierto=f;pilaFormularios.push(f);f.mostrar();}
    private class Formulario {
        Spinner repeticion,unidad;EditText intervalo,finSerie;final Registro anterior;
        final Definicion d;final Registro registro;final boolean nuevo;final Runnable guardado;final String estadoOriginal;
        final Map<String,View> controles=new LinkedHashMap<>();final Map<String,List<String>> valores=new HashMap<>();
        final Map<String,View> filas=new HashMap<>();AlertDialog dialogo;LinearLayout campos;
        Formulario(Definicion d,Registro r,boolean n,Runnable g){this.d=d;registro=r;anterior=r.copia();nuevo=n;guardado=g;estadoOriginal=r.texto("estadoEspecial");}
        void mostrar(){
            View vista=getLayoutInflater().inflate(R.layout.formulario_registro,null,false);campos=vista.findViewById(R.id.camposFormulario);
            for(Campo c:d.campos){
                if(c.clave.equals("serieId"))continue;
                if(!c.relacion.isEmpty()||c.opciones.length>0){
                    View fila=getLayoutInflater().inflate(R.layout.campo_selector,campos,false);filas.put(c.clave,fila);
                    ((TextView)fila.findViewById(R.id.etiquetaCampo)).setText(c.etiqueta+(c.obligatorio?" *":""));
                    Spinner control=fila.findViewById(R.id.valorSelector);controles.put(c.clave,control);campos.addView(fila);
                    View agregar=fila.findViewById(R.id.agregarRelacionado);agregar.setVisibility(c.relacion.isEmpty()?View.GONE:View.VISIBLE);
                    agregar.setOnClickListener(v->{capturar();Registro otro=new Registro();if(!c.padre.isEmpty()){String padre=registro.texto(c.padre);if(padre.isEmpty()){mensaje("Seleccioná primero "+d.campo(c.padre).etiqueta);return;}otro.put(c.destinoPadre,padre);}Formulario anterior=this;editar(Definicion.de(c.relacion),otro,true,()->{relacionados.remove(c.relacion);registro.put(c.clave,otro.texto("id"));anterior.refrescarSelectores();formularioAbierto=anterior;});});
                }else if(c.tipo.equals("booleano")){
                    CheckBox control=(CheckBox)getLayoutInflater().inflate(R.layout.campo_casilla,campos,false);control.setText(c.etiqueta);control.setChecked(Boolean.parseBoolean(registro.texto(c.clave)));controles.put(c.clave,control);filas.put(c.clave,control);campos.addView(control);control.setOnCheckedChangeListener((v,b)->visibilidad());
                }else{
                    View fila=getLayoutInflater().inflate(R.layout.campo_entrada,campos,false);filas.put(c.clave,fila);((TextView)fila.findViewById(R.id.etiquetaCampo)).setText(c.etiqueta+(c.obligatorio?" *":""));
                    EditText control=fila.findViewById(R.id.valorCampo);control.setText(registro.texto(c.clave));controles.put(c.clave,control);campos.addView(fila);
                    if(c.tipo.equals("decimal"))control.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    else if(c.tipo.equals("entero"))control.setInputType(InputType.TYPE_CLASS_NUMBER);
                    else if(c.tipo.equals("fecha")){control.setHint("AAAA-MM-DD");control.setOnClickListener(v->fecha(control));}
                    else if(c.tipo.equals("hora")){control.setHint("HH:mm");control.setOnClickListener(v->hora(control));}
                    else if(c.tipo.equals("fechaHora"))control.setHint("AAAA-MM-DDTHH:mm");
                    if(c.clave.equals("observaciones")||c.clave.equals("observacion")||c.clave.equals("frase")){control.setMinLines(2);control.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);}
                }
            }
            if(d.tabla.equals("eventos"))recurrencia();refrescarSelectores();visibilidad();
            dialogo=new AlertDialog.Builder(RegistroActivity.this).setTitle((nuevo?"Agregar · ":"Editar · ")+d.titulo).setView(vista).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",null).create();
            dialogo.setOnDismissListener(v->{pilaFormularios.remove(this);formularioAbierto=pilaFormularios.peek();});
            dialogo.setOnShowListener(v->dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b->guardar()));dialogo.show();
        }
        void recurrencia(){View v=getLayoutInflater().inflate(R.layout.campos_recurrencia,campos,false);campos.addView(v);repeticion=v.findViewById(R.id.frecuenciaEvento);unidad=v.findViewById(R.id.unidadEvento);intervalo=v.findViewById(R.id.intervaloEvento);finSerie=v.findViewById(R.id.finSerieEvento);selector(repeticion,Arrays.asList("Sin repetición","Semanal","Mensual","Anual","Personalizada"));selector(unidad,Arrays.asList("Días","Semanas","Meses","Años"));intervalo.setText("1");finSerie.setOnClickListener(w->fecha(finSerie));org.json.JSONObject cfg=SeriesEvento.reglaActual(RegistroActivity.this,registro);if(cfg!=null){String f=cfg.optString("frecuencia");repeticion.setSelection(Arrays.asList("NINGUNA","SEMANAL","MENSUAL","ANUAL","PERSONALIZADA").indexOf(f));intervalo.setText(String.valueOf(cfg.optInt("cada",1)));unidad.setSelection(ReglaRecurrencia.Unidad.valueOf(cfg.optString("unidad")).ordinal());finSerie.setText(cfg.isNull("fecha_fin")?"":cfg.optString("fecha_fin"));}if(!registro.texto("_repeticion").isEmpty()){repeticion.setSelection(Integer.parseInt(registro.texto("_repeticion")));unidad.setSelection(Integer.parseInt(registro.texto("_unidadControl")));intervalo.setText(registro.texto("_intervaloControl"));finSerie.setText(registro.texto("_finControl"));}repeticion.setOnItemSelectedListener(oyente(()->{boolean p=repeticion.getSelectedItemPosition()==4;unidad.setVisibility(p?View.VISIBLE:View.GONE);intervalo.setVisibility(p?View.VISIBLE:View.GONE);finSerie.setVisibility(repeticion.getSelectedItemPosition()==0?View.GONE:View.VISIBLE);}));}
        void guardarEvento(){int pos=repeticion.getSelectedItemPosition();String f=new String[]{"NINGUNA","SEMANAL","MENSUAL","ANUAL","PERSONALIZADA"}[pos];int n=1;try{if(pos==4){n=Integer.parseInt(intervalo.getText().toString());if(n<1||n>10000)throw new NumberFormatException();}}catch(NumberFormatException e){intervalo.setError("Ingresá un intervalo entre 1 y 10000");return;}String fin=finSerie.getText().toString().trim();if(!fin.isEmpty()&&ReglasAgenda.validarValor("fecha",fin)!=null){finSerie.setError("Fecha inválida");return;}ReglaRecurrencia.Unidad u=pos==1?ReglaRecurrencia.Unidad.SEMANAS:pos==2?ReglaRecurrencia.Unidad.MESES:pos==3?ReglaRecurrencia.Unidad.ANIOS:ReglaRecurrencia.Unidad.values()[unidad.getSelectedItemPosition()];final int cada=n;alcance(registro,a->{try{SeriesEvento.guardar(RegistroActivity.this,registro,anterior,a,f,u,cada,fin);dialogo.dismiss();guardado.run();}catch(RuntimeException e){error(e);}});}
        String valor(Campo c){View v=controles.get(c.clave);if(v instanceof EditText)return ((EditText)v).getText().toString().trim();if(v instanceof CheckBox)return String.valueOf(((CheckBox)v).isChecked());if(v instanceof Spinner){int n=((Spinner)v).getSelectedItemPosition();List<String> opciones=valores.get(c.clave);return opciones!=null&&n>=0&&n<opciones.size()?opciones.get(n):"";}return registro.texto(c.clave);}
        void capturar(){if(repeticion!=null){registro.put("_repeticion",String.valueOf(repeticion.getSelectedItemPosition()));registro.put("_unidadControl",String.valueOf(unidad.getSelectedItemPosition()));registro.put("_intervaloControl",intervalo.getText().toString());registro.put("_finControl",finSerie.getText().toString());}for(Campo c:d.campos)if(controles.containsKey(c.clave))registro.put(c.clave,valor(c));}
        void refrescarSelectores(){
            for(Campo c:d.campos)if(controles.get(c.clave) instanceof Spinner){Spinner control=(Spinner)controles.get(c.clave);List<String> ids=new ArrayList<>(),nombres=new ArrayList<>();
                if(!c.relacion.isEmpty()){
                    ids.add("");nombres.add("Sin seleccionar");String padre=registro.texto(c.padre);
                    for(Registro otro:relacion(c.relacion)){if(!c.padre.isEmpty()&&(padre.isEmpty()||!padre.equals(otro.texto(c.destinoPadre))))continue;ids.add(otro.texto("id"));nombres.add(nombre(c.relacion,otro));}
                }else{ids.addAll(Arrays.asList(c.opciones));for(String s:c.opciones)nombres.add(s.isEmpty()?"Automático según fecha":s);}
                control.setOnItemSelectedListener(null);if(!c.relacion.isEmpty()&&c.padre.isEmpty()&&!registro.texto(c.clave).isEmpty()&&!ids.contains(registro.texto(c.clave))){ids.add(registro.texto(c.clave));nombres.add(registro.texto(c.clave)+" (valor anterior)");}
                valores.put(c.clave,ids);selector(control,nombres);String elegido=registro.texto(c.clave);if(elegido.isEmpty()&&c.clave.equals("prioridad"))elegido="MEDIA";int indice=ids.indexOf(elegido);control.setSelection(Math.max(0,indice));
                if(!c.padre.isEmpty()&&!registro.texto(c.clave).isEmpty()&&indice<0)registro.put(c.clave,"");
                control.setOnItemSelectedListener(oyente(()->{String anterior=registro.texto(c.clave),actual=valor(c);registro.put(c.clave,actual);if(!actual.equals(anterior)){boolean dependientes=false;for(Campo hijo:d.campos)if(hijo.padre.equals(c.clave)){registro.put(hijo.clave,"");dependientes=true;}if(dependientes)refrescarSelectores();}visibilidad();}));
            }
        }
        void visibilidad(){
            if(filas.containsKey("anticipacionMinutos")){View alarma=controles.get("alarmaActivada");filas.get("anticipacionMinutos").setVisibility(alarma instanceof CheckBox&&((CheckBox)alarma).isChecked()?View.VISIBLE:View.GONE);}
            if(d.tabla.equals("ejercicios")){Campo tipo=d.campo("tipo");String t=valor(tipo);for(String k:new String[]{"tiempoSegundos","distancia","cantidad","peso"}){boolean mostrar=t.equals("AEROBICO")?!k.equals("peso"):t.equals("ANAEROBICO")&&(k.equals("cantidad")||k.equals("peso"));filas.get(k).setVisibility(mostrar?View.VISIBLE:View.GONE);}}
        }
        void guardar(){
            String estadoAnterior=estadoOriginal;
            capturar();
            if("POSTERGADO".equals(registro.texto("estadoEspecial"))&&!"POSTERGADO".equals(estadoAnterior)){mensaje("Guardá el registro y usá Postergar en su detalle para conservar el historial");return;}
            for(Campo c:d.campos){if(!controles.containsKey(c.clave))continue;String valor=registro.texto(c.clave);
                if(filas.get(c.clave)!=null&&filas.get(c.clave).getVisibility()==View.GONE){if(c.clave.equals("anticipacionMinutos")||d.tabla.equals("ejercicios"))registro.put(c.clave,null);continue;}
                if(c.obligatorio&&valor.isEmpty()){problema(c,"Completá "+c.etiqueta);return;}
                String error=ReglasAgenda.validarValor(c.tipo,valor);if(error!=null){problema(c,error);return;}
                if(c.tipo.equals("decimal"))registro.put(c.clave,valor.replace(',','.'));
                if(valor.isEmpty())registro.put(c.clave,null);
            }
            if(Boolean.parseBoolean(registro.texto("alarmaActivada"))){String ref=d.tabla.equals("tareas")?registro.texto("fechaInicio"):registro.texto("fecha");if(ref.isEmpty()){mensaje("Para activar la alarma, completá la fecha de inicio");return;}if(registro.texto("anticipacionMinutos").isEmpty()){problema(d.campo("anticipacionMinutos"),"Indicá la anticipación, o cero para la hora de inicio");return;}}
            if(d.tabla.equals("tareas")&&!registro.texto("fechaInicio").isEmpty()&&registro.texto("fechaInicio").compareTo(registro.texto("fechaVencimiento"))>0){problema(d.campo("fechaVencimiento"),"El vencimiento debe ser igual o posterior al inicio");return;}
            if(d.tabla.equals("freelance")&&!registro.texto("fechaFinalizacion").isEmpty()&&registro.texto("fechaFinalizacion").compareTo(registro.texto("fechaInicio"))<0){problema(d.campo("fechaFinalizacion"),"La finalización debe ser igual o posterior al inicio");return;}
            for(Campo c:d.campos)if(c.tipo.equals("hora")&&!registro.texto(c.clave).isEmpty()){String fecha=c.clave.equals("hora")?d.fecha():c.clave.replace("hora","fecha");if(registro.texto(fecha).isEmpty()){problema(c,"Completá también la fecha correspondiente");return;}}
            if(d.tabla.equals("estudios")&&!registro.texto("materiaId").isEmpty()&&registro.texto("institucionId").isEmpty()){mensaje("Seleccioná la institución de la materia");return;}
            if(nuevo)registro.put("ownerId",ownerId);if(d.tabla.equals("eventos")){guardarEvento();return;}
            try(Fuente f=Fuentes.abrir(RegistroActivity.this,deviceId,d.tabla)){if(!f.guardar(registro,nuevo))throw new IllegalStateException();dialogo.dismiss();guardado.run();}catch(RuntimeException e){error(e);}
        }
        void problema(Campo c,String mensaje){View v=controles.get(c.clave);if(v instanceof EditText){((EditText)v).setError(mensaje);v.requestFocus();}else mensaje(mensaje);}
    }
    private void fecha(EditText control){LocalDate actual=LocalDate.now();try{actual=LocalDate.parse(control.getText().toString());}catch(RuntimeException ignored){} new DatePickerDialog(this,(v,y,m,d)->control.setText(LocalDate.of(y,m+1,d).toString()),actual.getYear(),actual.getMonthValue()-1,actual.getDayOfMonth()).show();}
    private void hora(EditText control){LocalTime actual=LocalTime.now();try{actual=LocalTime.parse(control.getText().toString());}catch(RuntimeException ignored){}new TimePickerDialog(this,(v,h,m)->control.setText(String.format(Locale.US,"%02d:%02d",h,m)),actual.getHour(),actual.getMinute(),true).show();}
    private void selector(Spinner spinner,List<String> nombres){ArrayAdapter<String>a=new ArrayAdapter<>(this,com.example.agendapersonal.R.layout.item_opcion_tema,nombres);a.setDropDownViewResource(com.example.agendapersonal.R.layout.item_opcion_tema);spinner.setAdapter(a);}
    private AdapterView.OnItemSelectedListener oyente(Runnable r){return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int n,long id){r.run();}public void onNothingSelected(AdapterView<?> p){}};}
    private int dp(int n){return EstiloApp.dp(this,n);}
    private void mensaje(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private void error(RuntimeException e){android.util.Log.e("AgendaPersonal","Operación de registro",e);mensaje("No se pudo guardar. Revisá los campos y los registros relacionados.");}
    @Override public void onBackPressed(){if(navegacion!=null&&navegacion.estaAbierto())navegacion.cerrar();else super.onBackPressed();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putInt("seccion",seleccion);out.putString("buscar",busqueda.getText().toString());out.putString("desde",desde.getText().toString());out.putString("hasta",hasta.getText().toString());out.putInt("estado",estados.getSelectedItemPosition());if(formularioAbierto!=null){formularioAbierto.capturar();Bundle b=new Bundle();for(Map.Entry<String,String>e:formularioAbierto.registro.entrySet())b.putString(e.getKey(),e.getValue());out.putBundle("borrador",b);out.putString("tablaFormulario",formularioAbierto.d.tabla);out.putBoolean("nuevo",formularioAbierto.nuevo);}}
    @Override protected void onDestroy(){if(fuente!=null)fuente.close();super.onDestroy();}
}
