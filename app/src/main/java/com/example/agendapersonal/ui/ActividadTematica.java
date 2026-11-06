package com.example.agendapersonal.ui;
import android.app.Activity;import android.os.Bundle;
/** Aplica la elección antes de inflar y actualiza las pantallas que quedaron en la pila. */
public abstract class ActividadTematica extends Activity {
 private int tema;
 @Override protected void onCreate(Bundle b){tema=Tema.elegido(this);Tema.aplicar(this);super.onCreate(b);}
 @Override protected void onResume(){super.onResume();if(tema!=Tema.elegido(this))recreate();}
}
