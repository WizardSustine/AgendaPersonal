package com.example.agendapersonal.ui;
import com.example.agendapersonal.R;
public class SaludActivity extends RegistroActivity {
 @Override protected String[] tablas(){return new String[]{"salud","especialidades"};}
 @Override protected int layout(){return R.layout.activity_salud;}
}
