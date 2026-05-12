package com.example.controlalmacen.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import android.content.res.ColorStateList;
import android.graphics.Color;

public class PerfilActivity extends AppCompatActivity implements UsuarioAdminAdapter.Listener {

    private RecyclerView rvUsuarios;

    private Uri imagenPerfilUri;
    private ImageView imgPreviewPerfilDialog;

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
                    Usuario admin = new Usuario("Admin", null, true, "1234", "aadmin892@gmail.com", true,   "idda gkhk quir hktv");
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
    private final ActivityResultLauncher<String> galeriaPerfilLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Uri uriInterna = copiarImagenPerfilAInterno(uri);

                    if (uriInterna != null) {
                        imagenPerfilUri = uriInterna;

                        if (imgPreviewPerfilDialog != null) {
                            imgPreviewPerfilDialog.setImageURI(uriInterna);
                            imgPreviewPerfilDialog.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Uri> camaraPerfilLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && imagenPerfilUri != null) {
                    if (imgPreviewPerfilDialog != null) {
                        imgPreviewPerfilDialog.setImageURI(imagenPerfilUri);
                        imgPreviewPerfilDialog.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                } else {
                    Toast.makeText(this, "No se pudo sacar la foto", Toast.LENGTH_SHORT).show();
                }
            });


    private final ActivityResultLauncher<String> permisoCamaraPerfilLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    Uri uri = crearUriParaFotoPerfil();

                    if (uri != null) {
                        imagenPerfilUri = uri;
                        camaraPerfilLauncher.launch(uri);
                    }
                } else {
                    Toast.makeText(this, "Debes conceder permiso de cámara", Toast.LENGTH_SHORT).show();
                }
            });

    private Uri crearUriParaFotoPerfil() {
        try {
            File dir = new File(getFilesDir(), "perfiles");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = File.createTempFile("perfil_", ".jpg", dir);

            return FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error preparando la cámara", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Uri copiarImagenPerfilAInterno(Uri uriOrigen) {
        try {
            File dir = new File(getFilesDir(), "perfiles");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = File.createTempFile("perfil_galeria_", ".jpg", dir);

            try (InputStream in = getContentResolver().openInputStream(uriOrigen);
                 FileOutputStream out = new FileOutputStream(file)) {

                if (in == null) {
                    return null;
                }

                byte[] buffer = new byte[4096];
                int leido;

                while ((leido = in.read(buffer)) != -1) {
                    out.write(buffer, 0, leido);
                }
            }

            return FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error copiando la imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void mostrarEditarPerfil(Usuario usuario) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.item_editar_perfil);

        ImageView imgPreview = dialog.findViewById(R.id.imgPreview);
        imgPreviewPerfilDialog = imgPreview;
        imagenPerfilUri = null;

        TextInputEditText etNombre = dialog.findViewById(R.id.etNombre);
        CheckBox cbAdmin = dialog.findViewById(R.id.cbAdmin);

        LinearLayout layoutAdminFields = dialog.findViewById(R.id.layoutAdminFields);
        TextInputLayout tilEmail = dialog.findViewById(R.id.tilEmail);
        TextInputLayout tilPassword = dialog.findViewById(R.id.tilPassword);
        TextInputEditText etEmail = dialog.findViewById(R.id.etEmail);
        TextInputEditText etPassword = dialog.findViewById(R.id.etPassword);

        TextInputLayout tilPasswordCorreo = dialog.findViewById(R.id.tilPasswordCorreo);
        TextInputEditText etPasswordCorreo = dialog.findViewById(R.id.etPasswordCorreo);

        Button btnTomarFoto = dialog.findViewById(R.id.btnTomarFoto);
        Button btnSubirFoto = dialog.findViewById(R.id.btnSubirFoto);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

        // Rellenar datos si es edición
        if (usuario != null) {
            etNombre.setText(usuario.getNombre());
            cbAdmin.setChecked(usuario.isAdministrador());

            if (usuario.getFotoUri() != null && !usuario.getFotoUri().isEmpty()) {
                imagenPerfilUri = Uri.parse(usuario.getFotoUri());
                imgPreview.setImageURI(imagenPerfilUri);
                imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            if (usuario.isAdministrador()) {
                layoutAdminFields.setVisibility(View.VISIBLE);

                if (usuario.getEmail() != null) {
                    etEmail.setText(usuario.getEmail());
                }

                if (usuario.getPassword() != null) {
                    etPassword.setText(usuario.getPassword());
                }
                if (usuario.getPasswordCorreo() != null) {
                    etPasswordCorreo.setText(usuario.getPasswordCorreo());
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
            tilPasswordCorreo.setError(null);

            if (!isChecked) {
                etEmail.setText("");
                etPassword.setText("");
                etPasswordCorreo.setText("");
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnTomarFoto.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {

                Uri uri = crearUriParaFotoPerfil();

                if (uri != null) {
                    imagenPerfilUri = uri;
                    camaraPerfilLauncher.launch(uri);
                }

            } else {
                permisoCamaraPerfilLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnSubirFoto.setOnClickListener(v -> {
            galeriaPerfilLauncher.launch("image/*");
        });

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            boolean admin = cbAdmin.isChecked();
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            String passwordCorreo = etPasswordCorreo.getText() != null
                    ? etPasswordCorreo.getText().toString().trim()
                    : "";

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

                if (passwordCorreo.isEmpty()) {
                    tilPasswordCorreo.setError("Introduce la contraseña de aplicación de Gmail");
                    return;
                }
            }

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);

                String fotoFinal = imagenPerfilUri != null ? imagenPerfilUri.toString() : null;


                if (usuario == null) {
                    String emailFinal = admin ? email : null;
                    String passwordFinal = admin ? password : null;
                    String passwordCorreoFinal = admin ? passwordCorreo : null;

                    Usuario nuevo = new Usuario(
                            nombre,
                            null,
                            admin,
                            passwordFinal,
                            emailFinal,
                            true,
                            passwordCorreoFinal
                    );

                    db.usuarioDao().insert(nuevo);

                } else {
                    usuario.setNombre(nombre);
                    usuario.setAdministrador(admin);

                    if (imagenPerfilUri != null) {
                        usuario.setFotoUri(imagenPerfilUri.toString());
                    }

                    if (admin) {
                        usuario.setEmail(email);
                        usuario.setPassword(password);
                        usuario.setPasswordCorreo(passwordCorreo);
                    } else {
                        usuario.setEmail(null);
                        usuario.setPassword(null);
                        usuario.setPasswordCorreo(null);
                    }

                    if (imagenPerfilUri != null) {
                        usuario.setFotoUri(imagenPerfilUri.toString());
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
        Dialog dialog = new Dialog(this, R.style.DialogRedondeado);
        dialog.setContentView(R.layout.dialog_deshabilitar_perfil);

        TextView btnCerrarDialog = dialog.findViewById(R.id.btnCerrarDialogDeshabilitar);
        TextView tvTitulo = dialog.findViewById(R.id.tvTituloDeshabilitarPerfil);
        TextView tvMensaje = dialog.findViewById(R.id.tvMensajeDeshabilitarPerfil);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarDeshabilitarPerfil);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmarDeshabilitarPerfil);

        boolean estaHabilitado = u.isHabilitado();

        if (estaHabilitado) {
            tvTitulo.setText("Deshabilitar Perfil");
            tvMensaje.setText("¿Deseas deshabilitar el perfil de \""
                    + u.getNombre()
                    + "\"? Este usuario no podrá acceder a la aplicación.");
            btnConfirmar.setText("Deshabilitar");
        } else {
            tvTitulo.setText("Habilitar Perfil");
            tvMensaje.setText("¿Deseas habilitar el perfil de \""
                    + u.getNombre()
                    + "\"? Este usuario volverá a poder acceder a la aplicación.");
            btnConfirmar.setText("Habilitar");
        }

        btnCerrarDialog.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);

                u.setHabilitado(!u.isHabilitado());
                db.usuarioDao().update(u);

                runOnUiThread(() -> {
                    dialog.dismiss();

                    Toast.makeText(
                            this,
                            u.isHabilitado() ? "Perfil habilitado" : "Perfil deshabilitado",
                            Toast.LENGTH_SHORT
                    ).show();

                    cargarUsuarios();
                });
            }).start();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            int ancho = (int) (500 * getResources().getDisplayMetrics().density);

            dialog.getWindow().setLayout(
                    ancho,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public void onBorrar(Usuario u) {
        Dialog dialog = new Dialog(this, R.style.DialogRedondeado);
        dialog.setContentView(R.layout.dialog_borrar_perfil);

        TextView btnCerrarDialog = dialog.findViewById(R.id.btnCerrarDialog);
        TextView tvMensaje = dialog.findViewById(R.id.tvMensajeBorrarPerfil);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarBorrarPerfil);
        Button btnBorrar = dialog.findViewById(R.id.btnConfirmarBorrarPerfil);

        tvMensaje.setText("¿Estás seguro de que deseas eliminar el perfil de \""
                + u.getNombre()
                + "\"? Esta acción no se puede deshacer.");

        btnCerrarDialog.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnBorrar.setOnClickListener(v -> {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                db.usuarioDao().delete(u);

                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Perfil eliminado", Toast.LENGTH_SHORT).show();
                    cargarUsuarios();
                });
            }).start();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            int ancho = (int) (500 * getResources().getDisplayMetrics().density);

            dialog.getWindow().setLayout(
                    ancho,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}