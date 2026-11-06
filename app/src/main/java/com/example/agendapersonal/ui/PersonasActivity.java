package com.example.agendapersonal.ui;
import com.example.agendapersonal.R;
public class PersonasActivity extends RegistroActivity {
 @Override protected String[] tablas(){return new String[]{"personas","direcciones"};}
 @Override protected int layout(){return R.layout.activity_personas;}
}
