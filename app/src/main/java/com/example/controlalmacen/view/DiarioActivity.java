package com.example.controlalmacen.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlalmacen.R;
import com.example.controlalmacen.controller.Sesion;

public class DiarioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si no hay sesión, volver a login
        if (Sesion.getUsuario() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_diario);

        TextView tv = findViewById(R.id.tvDiario);
        tv.setText("Usuario activo: " + Sesion.getUsuario().getNombre());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Timeout 10 min
        if (Sesion.isExpired()) {
            Sesion.clear();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Sesion.touch();
        }
    }
}