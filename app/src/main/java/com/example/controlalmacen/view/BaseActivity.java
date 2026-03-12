package com.example.controlalmacen.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlalmacen.controller.Sesion;

public class BaseActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    // se ejecuta cada 1 segundo y comprueba si la sesion ha caducado
    private final Runnable checkTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (Sesion.getUsuario() == null || Sesion.isExpired()) {
                Sesion.clear();
                startActivity(new Intent(BaseActivity.this, MainActivity.class));
                finish();
                return;
            }

            // volver a comprobar en 1 segundo
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // al volver a la pantalla
        handler.removeCallbacks(checkTimeoutRunnable);
        handler.postDelayed(checkTimeoutRunnable, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // al salir de la pantalla, paramos
        handler.removeCallbacks(checkTimeoutRunnable);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // cualquier toque renueva la sesión
        if (Sesion.getUsuario() != null) {
            Sesion.touch();
        }
        return super.dispatchTouchEvent(ev);
    }
}
