package com.example.agendapersonal.ui;
import com.example.agendapersonal.R;
public class EstadoEmocionalActivity extends RegistroActivity {
 @Override protected String[] tablas(){return new String[]{"registros_emocionales","motivos_emocionales","ambitos_emocionales"};}
 @Override protected int layout(){return R.layout.activity_estado_emocional;}
}
