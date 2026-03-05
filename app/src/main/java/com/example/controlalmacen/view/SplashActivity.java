package com.example.controlalmacen.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.controlalmacen.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView ivLogo;
    private Handler handler;
    private Runnable bounceRunnable;

    private float posX = 0;
    private float posY = 0;
    private float velocidadX = 3;
    private float velocidadY = 3;
    private int screenWidth;
    private int screenHeight;
    private int logoSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ivLogo = findViewById(R.id.ivLogo);

        // Obtener dimensiones de pantalla
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        logoSize = (int) (400 * displayMetrics.density);

        // Iniciar animación de rebote
        handler = new Handler();
        bounceRunnable = new Runnable() {
            @Override
            public void run() {
                // Actualizar posición
                posX += velocidadX;
                posY += velocidadY;

                // Rebote en los bordes horizontales
                if (posX + logoSize >= screenWidth || posX <= 0) {
                    velocidadX = -velocidadX;
                }

                // Rebote en los bordes verticales
                if (posY + logoSize >= screenHeight || posY <= 0) {
                    velocidadY = -velocidadY;
                }

                // Aplicar posición
                ivLogo.setX(posX);
                ivLogo.setY(posY);

                // Repetir cada 16ms (~60fps)
                handler.postDelayed(this, 16);
            }
        };
        handler.post(bounceRunnable);

        // Al pulsar en cualquier parte de la pantalla, ir al login
        findViewById(R.id.splashContainer).setOnClickListener(v -> irALogin());

        // Desactivar el botón atrás en la splash screen
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // No hacer nada, el usuario debe pulsar en la pantalla
            }
        });
    }

    private void irALogin() {
        Log.d("SplashActivity", "Navegando a MainActivity...");
        if (handler != null && bounceRunnable != null) {
            handler.removeCallbacks(bounceRunnable);
        }
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && bounceRunnable != null) {
            handler.removeCallbacks(bounceRunnable);
        }
    }
}
