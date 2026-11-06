package com.example.agendapersonal.avisos;
import android.content.*;
public class ReceptorAvisos extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){PendingResult p=goAsync();ProgramadorAvisos.ejecutar(c,p::finish);}
}
