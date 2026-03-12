package com.example.controlalmacen.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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


        // grid para poner/ver tablet >>>>>>> main
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

    private void hacerLogin(Usuario u) {

        // SI ES ADMIN → pedir contraseña antes de entrar
        if (u.isAdministrador()) {
            mostrarDialogoAdmin(u);
            return;
        }

        // SI NO ES ADMIN → entrar directo
        Sesion.setUsuario(u);
        startActivity(new Intent(this, DiarioActivity.class));
        finish();
    }


    private void mostrarDialogoAdmin(Usuario u) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confrimr_admin);

        EditText etPass = dialog.findViewById(R.id.etPassAdmin);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar); // ← AQUÍ

        btnCancelar.setOnClickListener(v -> dialog.dismiss()); // ← FUNCIONA

        btnConfirmar.setOnClickListener(v -> {
            String pass = etPass.getText().toString().trim();

            if (pass.isEmpty()) {
                Toast.makeText(this, "Introduce la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(u.getPassword())) {
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                return;
            }

            // Contraseña correcta → iniciar sesión
            Sesion.setUsuario(u);
            dialog.dismiss();
            startActivity(new Intent(this, DiarioActivity.class));
            finish();
        });

        dialog.show();
    }

}
