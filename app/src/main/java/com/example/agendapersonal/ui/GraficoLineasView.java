package com.example.agendapersonal.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.time.LocalDate;
import java.util.*;

/** Gráfico local de líneas: nunca incluye valores de ejemplo ni modifica movimientos. */
public class GraficoLineasView extends View {
    private final Paint pincel=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SortedMap<LocalDate,double[]> datos=new TreeMap<>();
    private final int[] colores={Color.rgb(77,155,91),Color.rgb(104,69,119),Color.rgb(204,81,83)};
    public GraficoLineasView(Context c,AttributeSet a){super(c,a);setContentDescription("Evolución de ingresos, pagos y gastos finalizados");}
    public void datos(SortedMap<LocalDate,double[]> valores){datos.clear();datos.putAll(valores);invalidate();}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float dens=getResources().getDisplayMetrics().density;
        float left=54*dens,top=24*dens,right=getWidth()-20*dens,bottom=getHeight()-42*dens;
        pincel.setStrokeWidth(dens);pincel.setColor(Tema.color(getContext(),com.example.agendapersonal.R.attr.agenda_border));canvas.drawLine(left,top,left,bottom,pincel);canvas.drawLine(left,bottom,right,bottom,pincel);
        pincel.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);pincel.setColor(Tema.color(getContext(),com.example.agendapersonal.R.attr.agenda_text_secondary));
        if(datos.isEmpty()){canvas.drawText("Sin registros finalizados en este período",left,top+60*dens,pincel);return;}
        double max=1;for(double[] vs:datos.values())for(double v:vs)max=Math.max(max,v);
        for(int i=0;i<=4;i++){float y=bottom-(bottom-top)*i/4f;pincel.setColor(Tema.color(getContext(),com.example.agendapersonal.R.attr.agenda_border));canvas.drawLine(left,y,right,y,pincel);pincel.setColor(Tema.color(getContext(),com.example.agendapersonal.R.attr.agenda_text_secondary));canvas.drawText(String.format(Locale.getDefault(),"%.0f",max*i/4),2*dens,y,pincel);}
        long minDay=datos.firstKey().toEpochDay(),maxDay=datos.lastKey().toEpochDay();long span=Math.max(1,maxDay-minDay);
        for(int serie=0;serie<3;serie++){Path linea=new Path();boolean first=true;pincel.setColor(colores[serie]);pincel.setStrokeWidth(2*dens);pincel.setStyle(Paint.Style.STROKE);
            for(Map.Entry<LocalDate,double[]>e:datos.entrySet()){float x=left+(right-left)*(e.getKey().toEpochDay()-minDay)/span;float y=bottom-(float)((bottom-top)*e.getValue()[serie]/max);if(first){linea.moveTo(x,y);first=false;}else linea.lineTo(x,y);}
            canvas.drawPath(linea,pincel);pincel.setStyle(Paint.Style.FILL);for(Map.Entry<LocalDate,double[]>e:datos.entrySet()){float x=left+(right-left)*(e.getKey().toEpochDay()-minDay)/span;float y=bottom-(float)((bottom-top)*e.getValue()[serie]/max);canvas.drawCircle(x,y,3*dens,pincel);}
        }
        pincel.setColor(Tema.color(getContext(),com.example.agendapersonal.R.attr.agenda_text_secondary));canvas.drawText(datos.firstKey().toString(),left,getHeight()-15*dens,pincel);String end=datos.lastKey().toString();canvas.drawText(end,right-pincel.measureText(end),getHeight()-15*dens,pincel);
    }
}
