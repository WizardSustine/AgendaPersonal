package com.example.agendapersonal.ui;
import com.example.agendapersonal.R;
public class MovimientosActivity extends RegistroActivity {
 @Override protected String[] tablas(){return new String[]{"ingresos","pagos","gastos","clasificaciones_economia"};}
 @Override protected int layout(){return R.layout.activity_movimientos;}
}
