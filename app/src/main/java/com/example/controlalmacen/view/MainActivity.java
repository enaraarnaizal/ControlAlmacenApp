package com.example.controlalmacen.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.controller.Sesion;
import com.example.controlalmacen.data.AppDatabase;
import com.example.controlalmacen.model.Usuario;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvPerfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvPerfiles = findViewById(R.id.rvPerfiles);

        // Grid para tablet (4 columnas, ajusta si quieres)
        rvPerfiles.setLayoutManager(new GridLayoutManager(this, 3));

        cargarPerfiles();
    }

    private void cargarPerfiles() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Usuario> usuarios = db.usuarioDao().getUsuariosActivos();

            runOnUiThread(() -> {
                UsuarioAdapter adapter = new UsuarioAdapter(usuarios, this::hacerLogin);
                rvPerfiles.setAdapter(adapter);
            });
        }).start();
    }

    //eligimos el perfil, guardamos la seison y entramos en diario (la parte de productos)
    private void hacerLogin(Usuario u) {
        Sesion.setUsuario(u);
        startActivity(new Intent(this, DiarioActivity.class));
        finish();
    }
}