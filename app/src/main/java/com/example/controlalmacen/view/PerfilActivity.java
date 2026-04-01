package com.example.controlalmacen.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.data.AppDatabase;
import com.example.controlalmacen.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class PerfilActivity extends AppCompatActivity implements UsuarioAdminAdapter.Listener {

    private RecyclerView rvUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvUsuarios = findViewById(R.id.rvUsuarios);
        rvUsuarios.setLayoutManager(new GridLayoutManager(this, 3));

        cargarUsuarios();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void cargarUsuarios() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                List<Usuario> usuariosDeBD = db.usuarioDao().getAllOrdenado();
                List<Usuario> listaParaAdapter = new ArrayList<>(usuariosDeBD);

                if (listaParaAdapter.isEmpty()) {
                    Usuario admin = new Usuario("Admin", null, true, "1234", "admin@mail.com", true);
                    long id = db.usuarioDao().insert(admin);
                    admin.setId((int) id);
                    listaParaAdapter.add(admin);
                }

                runOnUiThread(() -> {
                    listaParaAdapter.add(0, null);
                    UsuarioAdminAdapter adapter = new UsuarioAdminAdapter(listaParaAdapter, this);
                    rvUsuarios.setAdapter(adapter);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void mostrarEditarPerfil(Usuario usuario) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.item_editar_perfil);

        ImageView imgPreview = dialog.findViewById(R.id.imgPreview);
        TextInputEditText etNombre = dialog.findViewById(R.id.etNombre);
        CheckBox cbAdmin = dialog.findViewById(R.id.cbAdmin);

        LinearLayout layoutAdminFields = dialog.findViewById(R.id.layoutAdminFields);
        TextInputLayout tilEmail = dialog.findViewById(R.id.tilEmail);
        TextInputLayout tilPassword = dialog.findViewById(R.id.tilPassword);
        TextInputEditText etEmail = dialog.findViewById(R.id.etEmail);
        TextInputEditText etPassword = dialog.findViewById(R.id.etPassword);

        Button btnTomarFoto = dialog.findViewById(R.id.btnTomarFoto);
        Button btnSubirFoto = dialog.findViewById(R.id.btnSubirFoto);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

        // Rellenar datos si es edición
        if (usuario != null) {
            etNombre.setText(usuario.getNombre());
            cbAdmin.setChecked(usuario.isAdministrador());

            if (usuario.isAdministrador()) {
                layoutAdminFields.setVisibility(View.VISIBLE);

                if (usuario.getEmail() != null) {
                    etEmail.setText(usuario.getEmail());
                }

                if (usuario.getPassword() != null) {
                    etPassword.setText(usuario.getPassword());
                }
            } else {
                layoutAdminFields.setVisibility(View.GONE);
            }
        } else {
            layoutAdminFields.setVisibility(View.GONE);
        }

        cbAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutAdminFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            tilEmail.setError(null);
            tilPassword.setError(null);

            if (!isChecked) {
                etEmail.setText("");
                etPassword.setText("");
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            boolean admin = cbAdmin.isChecked();
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            tilEmail.setError(null);
            tilPassword.setError(null);

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Introduce un nombre", Toast.LENGTH_SHORT).show();
                return;
            }

            if (admin) {
                if (email.isEmpty()) {
                    tilEmail.setError("Introduce el email del administrador");
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.setError("Introduce un email válido");
                    return;
                }

                if (password.isEmpty()) {
                    tilPassword.setError("Introduce la contraseña del administrador");
                    return;
                }
            }

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);

                if (usuario == null) {
                    String emailFinal = admin ? email : null;
                    String passwordFinal = admin ? password : null;

                    Usuario nuevo = new Usuario(nombre, null, admin, passwordFinal, emailFinal, true);
                    db.usuarioDao().insert(nuevo);
                } else {
                    usuario.setNombre(nombre);
                    usuario.setAdministrador(admin);

                    if (admin) {
                        usuario.setEmail(email);
                        usuario.setPassword(password);
                    } else {
                        usuario.setEmail(null);
                        usuario.setPassword(null);
                    }

                    db.usuarioDao().update(usuario);
                }

                runOnUiThread(() -> {
                    dialog.dismiss();
                    cargarUsuarios();
                });

            }).start();
        });

        dialog.show();
    }

    @Override
    public void onNuevo() {
        mostrarEditarPerfil(null);
    }

    @Override
    public void onEditar(Usuario u) {
        mostrarEditarPerfil(u);
    }

    @Override
    public void onDeshabilitar(Usuario u) {
        String mensaje = u.isHabilitado()
                ? "¿Seguro que deseas DESHABILITAR este perfil?"
                : "¿Seguro que deseas HABILITAR este perfil?";

        new AlertDialog.Builder(this)
                .setTitle("Confirmar acción")
                .setMessage(mensaje)
                .setPositiveButton("Sí", (d, w) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(this);

                        u.setHabilitado(!u.isHabilitado());
                        db.usuarioDao().update(u);

                        runOnUiThread(() -> {
                            Toast.makeText(this,
                                    u.isHabilitado() ? "Perfil habilitado" : "Perfil deshabilitado",
                                    Toast.LENGTH_SHORT).show();

                            cargarUsuarios();
                        });
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onBorrar(Usuario u) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar perfil")
                .setMessage("¿Seguro que deseas borrar a " + u.getNombre() + "?")
                .setPositiveButton("Sí", (d, w) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(this);
                        db.usuarioDao().delete(u);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Perfil eliminado", Toast.LENGTH_SHORT).show();
                            cargarUsuarios();
                        });
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}