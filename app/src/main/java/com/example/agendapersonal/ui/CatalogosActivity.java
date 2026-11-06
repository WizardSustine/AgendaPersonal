package com.example.agendapersonal.ui;
import com.example.agendapersonal.R;
public class CatalogosActivity extends RegistroActivity {
 @Override protected String[] tablas(){return new String[]{"personas","direcciones","materias","clasificaciones_sociales","especialidades","clasificaciones_economia","ambitos_emocionales","motivos_emocionales","frases_personales"};}
 @Override protected int layout(){return R.layout.activity_catalogos;}
}
