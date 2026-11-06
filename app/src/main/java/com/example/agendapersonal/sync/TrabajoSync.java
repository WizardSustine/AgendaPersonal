package com.example.agendapersonal.sync;
import android.app.job.*;
public class TrabajoSync extends JobService {
 @Override public boolean onStartJob(JobParameters params){CoordinadorSync sync=CoordinadorSync.de(this);if(!sync.automatico()||!new SesionSegura(this).existe()||getSharedPreferences("agenda_sync_estado",MODE_PRIVATE).getBoolean("requiere_login",false))return false;new android.os.Handler(android.os.Looper.getMainLooper()).post(()->sync.solicitar(false,()->jobFinished(params,false)));return true;}
 @Override public boolean onStopJob(JobParameters params){CoordinadorSync.de(this).cancelar();return true;}
}
