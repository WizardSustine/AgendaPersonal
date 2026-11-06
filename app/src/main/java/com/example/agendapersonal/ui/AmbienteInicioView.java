package com.example.agendapersonal.ui;
import android.content.Context;import android.util.AttributeSet;import android.graphics.*;import android.view.View;
/** Paisaje decorativo vectorial: no contiene datos ni indicadores ficticios. */
public class AmbienteInicioView extends View {
 private final Paint p=new Paint(3); private Bitmap paisaje;
 public AmbienteInicioView(Context c,AttributeSet a){super(c,a);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
 int[] imagenes={0,com.example.agendapersonal.R.drawable.paisaje_bosque,com.example.agendapersonal.R.drawable.paisaje_bosque_night,com.example.agendapersonal.R.drawable.paisaje_espacial,com.example.agendapersonal.R.drawable.paisaje_sunrise};int tema=Tema.elegido(c);if(tema!=0){BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=2;paisaje=BitmapFactory.decodeResource(getResources(),imagenes[tema],o);}}
 @Override protected void onDraw(Canvas c){super.onDraw(c);if(paisaje!=null){dibujarPaisaje(c);return;}c.save();c.scale(getWidth()/360f,getHeight()/330f);Path clip=new Path();clip.addRoundRect(new RectF(0,0,360,330),25,25,Path.Direction.CW);c.clipPath(clip);p.setShader(new LinearGradient(0,0,0,330,new int[]{0xfff0e7f2,0xffffeee3,0xfffbf7f1},null,Shader.TileMode.CLAMP));c.drawRect(0,0,360,330,p);p.setShader(null);p.setColor(0xfffffbe7);c.drawCircle(269,199,37,p);
  p.setColor(0xffe8d0d7);Path fondo=new Path();fondo.moveTo(0,224);fondo.cubicTo(55,190,89,215,137,229);fondo.cubicTo(201,249,252,186,310,197);fondo.cubicTo(340,202,349,210,360,216);fondo.lineTo(360,330);fondo.lineTo(0,330);fondo.close();c.drawPath(fondo,p);
  p.setColor(0xffd5c9df);Path loma=new Path();loma.moveTo(0,235);loma.cubicTo(81,263,107,196,187,216);loma.cubicTo(251,234,277,259,360,229);loma.lineTo(360,330);loma.lineTo(0,330);loma.close();c.drawPath(loma,p);
  planta(c,23,267,1,false);planta(c,340,268,.78f,true);p.setColor(0xfffcf8f3);Path suelo=new Path();suelo.moveTo(0,281);suelo.cubicTo(90,251,260,255,360,285);suelo.lineTo(360,330);suelo.lineTo(0,330);suelo.close();c.drawPath(suelo,p);c.restore();}
 private void planta(Canvas c,float x,float y,float escala,boolean espejo){c.save();c.translate(x,y);c.scale(espejo?-escala:escala,escala);p.setColor(0xffb5b29b);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);Path tallo=new Path();tallo.moveTo(0,0);tallo.cubicTo(9,-24,6,-51,13,-81);c.drawPath(tallo,p);p.setStyle(Paint.Style.FILL);hoja(c,7,-59,-32,-90);hoja(c,11,-70,31,-109);hoja(c,5,-39,34,-66);hoja(c,3,-25,-25,-57);hoja(c,2,-9,29,-36);c.restore();}
 private void hoja(Canvas c,float x,float y,float a,float b){Path h=new Path();h.moveTo(x,y);h.cubicTo(a,b,a+12,b-6,x,y-10);h.close();c.drawPath(h,p);}
 private void dibujarPaisaje(Canvas c){
  float w=getWidth(),h=getHeight();int fondo=Tema.color(getContext(),com.example.agendapersonal.R.attr.agenda_background);
  c.drawColor(fondo);p.setShader(null);c.drawBitmap(paisaje,null,new RectF(0,0,w,h*.80f),p);
  p.setShader(new LinearGradient(0,h*.68f,0,h*.83f,new int[]{fondo&0x00ffffff,fondo},null,Shader.TileMode.CLAMP));c.drawRect(0,h*.68f,w,h*.84f,p);p.setShader(null);
 }
}
