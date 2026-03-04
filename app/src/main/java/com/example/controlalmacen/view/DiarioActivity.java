package com.example.controlalmacen.view;

import android.os.Bundle;
import android.widget.TextView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.controller.Sesion;

public class DiarioActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        TextView tv = findViewById(R.id.tvDiario);
        tv.setText("Usuario activo: " + Sesion.getUsuario().getNombre());
    }
}