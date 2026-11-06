package com.example.agendapersonal.ui;
import android.content.Context;import android.util.TypedValue;import com.example.agendapersonal.R;
/** Paleta local elegida por la persona. No altera datos ni la identidad de la aplicación. */
public final class Tema {
 private Tema(){} public static final String[] NOMBRES={"Original · Lavanda","BOSQUE · Día","BOSQUE · Night","ESPACIAL · Noche","ESPACIAL · Sunrise"};
 private static final int[] ESTILOS={R.style.Theme_AgendaPersonal,R.style.TemaBosque,R.style.TemaBosqueNight,R.style.TemaEspacial,R.style.TemaSunrise};
 public static int elegido(Context c){int n=c.getSharedPreferences("agenda_apariencia",0).getInt("tema",0);return n>=0&&n<ESTILOS.length?n:0;}
 public static void aplicar(Context c){c.setTheme(ESTILOS[elegido(c)]);}
 public static void guardar(Context c,int n){if(n<0||n>=ESTILOS.length)throw new IllegalArgumentException("Tema inválido");c.getSharedPreferences("agenda_apariencia",0).edit().putInt("tema",n).apply();}
 public static int color(Context c,int attr){TypedValue v=new TypedValue();if(!c.getTheme().resolveAttribute(attr,v,true))throw new IllegalStateException("Falta color del tema");return v.resourceId!=0?c.getResources().getColor(v.resourceId,c.getTheme()):v.data;}
 public static int atributo(int recurso){
if(recurso==R.color.app_background)return R.attr.agenda_background;
if(recurso==R.color.app_surface)return R.attr.agenda_surface;
if(recurso==R.color.app_primary)return R.attr.agenda_primary;
if(recurso==R.color.app_primary_dark)return R.attr.agenda_primary_dark;
if(recurso==R.color.app_primary_light)return R.attr.agenda_primary_light;
if(recurso==R.color.app_text)return R.attr.agenda_text;
if(recurso==R.color.app_text_secondary)return R.attr.agenda_text_secondary;
if(recurso==R.color.app_border)return R.attr.agenda_border;
if(recurso==R.color.app_success)return R.attr.agenda_success;
if(recurso==R.color.app_error)return R.attr.agenda_error;
if(recurso==R.color.app_warning)return R.attr.agenda_warning;
if(recurso==R.color.app_info)return R.attr.agenda_info;
if(recurso==R.color.app_overlay)return R.attr.agenda_overlay;
if(recurso==R.color.app_transparent)return R.attr.agenda_transparent;
return 0;}
}
