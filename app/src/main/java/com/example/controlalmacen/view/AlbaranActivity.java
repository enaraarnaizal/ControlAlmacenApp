package com.example.controlalmacen.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.controller.Sesion;
import com.example.controlalmacen.data.AppDatabase;
import com.example.controlalmacen.model.Albaran;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AlbaranActivity extends BaseActivity {

    private RecyclerView rvAlbaranes;
    private AlbaranAdapter adapter;
    private TextView tvFiltroActual;
    private LinearLayout layoutRangoFechas;
    private Button btnFiltroMes, btnFiltroSemana, btnFiltroRango, btnGenerarInforme, btnSeleccionarTodos;
    private Button btnFechaInicio, btnFechaFin, btnAplicarRango;
    private FloatingActionButton fabNuevoAlbaran;
    private ImageView btnCerrarAlbaran;

    private long fechaInicioSeleccionada = 0;
    private long fechaFinSeleccionada = 0;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private Uri imagenAlbaranUri;
    private ImageView imgPreviewDialog;

    private List<Albaran> albaranesActuales = new ArrayList<>();
    private Set<Long> idsSeleccionados = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar que el usuario sea administrador
        if (Sesion.getUsuario() == null || !Sesion.getUsuario().isAdministrador()) {
            Toast.makeText(this, "Acceso denegado: Solo administradores", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_albaran);

        inicializarVistas();
        configurarRecyclerView();
        configurarListeners();

        cargarAlbaranesMesActual();
    }

    private void inicializarVistas() {
        rvAlbaranes = findViewById(R.id.rvAlbaranes);
        tvFiltroActual = findViewById(R.id.tvFiltroActual);
        layoutRangoFechas = findViewById(R.id.layoutRangoFechas);
        btnFiltroMes = findViewById(R.id.btnFiltroMes);
        btnFiltroSemana = findViewById(R.id.btnFiltroSemana);
        btnFiltroRango = findViewById(R.id.btnFiltroRango);
        btnFechaInicio = findViewById(R.id.btnFechaInicio);
        btnFechaFin = findViewById(R.id.btnFechaFin);
        btnAplicarRango = findViewById(R.id.btnAplicarRango);
        fabNuevoAlbaran = findViewById(R.id.fabNuevoAlbaran);
        btnGenerarInforme = findViewById(R.id.btnGenerarInforme);
        btnSeleccionarTodos = findViewById(R.id.btnSeleccionarTodos);
        btnCerrarAlbaran = findViewById(R.id.btnCerrarAlbaran);
    }

    private void configurarRecyclerView() {
        rvAlbaranes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlbaranAdapter(new ArrayList<>(), this::mostrarDialogVerAlbaran, this::onSelectionChanged);
        rvAlbaranes.setAdapter(adapter);
    }

    private void onSelectionChanged(Set<Long> idsSeleccionados) {
        this.idsSeleccionados = idsSeleccionados;
        actualizarContadorSeleccion();
    }

    private void configurarListeners() {
        btnCerrarAlbaran.setOnClickListener(v -> {
            finish(); // Vuelve a la actividad anterior (DiarioActivity)
        });

        btnFiltroMes.setOnClickListener(v -> {
            layoutRangoFechas.setVisibility(View.GONE);
            cargarAlbaranesMesActual();
        });

        btnFiltroSemana.setOnClickListener(v -> {
            layoutRangoFechas.setVisibility(View.GONE);
            cargarAlbaranesSemanaActual();
        });

        btnFiltroRango.setOnClickListener(v -> {
            layoutRangoFechas.setVisibility(View.VISIBLE);
        });

        btnFechaInicio.setOnClickListener(v -> mostrarSelectorFecha(true));
        btnFechaFin.setOnClickListener(v -> mostrarSelectorFecha(false));

        btnAplicarRango.setOnClickListener(v -> {
            if (fechaInicioSeleccionada > 0 && fechaFinSeleccionada > 0) {
                if (fechaInicioSeleccionada > fechaFinSeleccionada) {
                    Toast.makeText(this, "La fecha de inicio no puede ser posterior a la fecha de fin", Toast.LENGTH_SHORT).show();
                    return;
                }
                cargarAlbaranesRango(fechaInicioSeleccionada, fechaFinSeleccionada);
            } else {
                Toast.makeText(this, "Seleccione ambas fechas", Toast.LENGTH_SHORT).show();
            }
        });

        fabNuevoAlbaran.setOnClickListener(v -> mostrarDialogNuevoAlbaran());

        btnGenerarInforme.setOnClickListener(v -> {
            if (idsSeleccionados.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un albarán para generar el informe", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, InformeAlbaranesActivity.class);
            intent.putExtra("ids_albaranes", (Serializable) new ArrayList<>(idsSeleccionados));
            startActivity(intent);
        });

        btnSeleccionarTodos.setOnClickListener(v -> {
            if (idsSeleccionados.size() == albaranesActuales.size()) {
                // Deseleccionar todos
                adapter.limpiarSeleccion();
                btnSeleccionarTodos.setText("Seleccionar todos");
            } else {
                // Seleccionar todos
                adapter.seleccionarTodos();
                btnSeleccionarTodos.setText("Deseleccionar todos");
            }
        });
    }

    private void mostrarSelectorFecha(boolean esInicio) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    if (esInicio) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                        selectedDate.set(Calendar.MINUTE, 0);
                        selectedDate.set(Calendar.SECOND, 0);
                        fechaInicioSeleccionada = selectedDate.getTimeInMillis();
                        btnFechaInicio.setText(dateFormat.format(selectedDate.getTime()));
                    } else {
                        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
                        selectedDate.set(Calendar.MINUTE, 59);
                        selectedDate.set(Calendar.SECOND, 59);
                        fechaFinSeleccionada = selectedDate.getTimeInMillis();
                        btnFechaFin.setText(dateFormat.format(selectedDate.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void cargarAlbaranesMesActual() {
        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long inicioMes = calendar.getTimeInMillis();

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            long finMes = calendar.getTimeInMillis();

            AppDatabase db = AppDatabase.getInstance(this);
            List<Albaran> albaranes = db.albaranDao().getAlbaranesPorRango(inicioMes, finMes);

            runOnUiThread(() -> {
                albaranesActuales = albaranes;
                adapter.updateData(albaranes);
                tvFiltroActual.setText("Mostrando: Mes actual (" + albaranes.size() + " albaranes)");
                // idsSeleccionados ya se limpia en adapter.updateData()
            });
        }).start();
    }

    private void cargarAlbaranesSemanaActual() {
        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long inicioSemana = calendar.getTimeInMillis();

            calendar.add(Calendar.DAY_OF_WEEK, 6);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            long finSemana = calendar.getTimeInMillis();

            AppDatabase db = AppDatabase.getInstance(this);
            List<Albaran> albaranes = db.albaranDao().getAlbaranesPorRango(inicioSemana, finSemana);

            runOnUiThread(() -> {
                albaranesActuales = albaranes;
                adapter.updateData(albaranes);
                tvFiltroActual.setText("Mostrando: Semana actual (" + albaranes.size() + " albaranes)");
                // idsSeleccionados ya se limpia en adapter.updateData()
            });
        }).start();
    }

    private void cargarAlbaranesRango(long inicio, long fin) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Albaran> albaranes = db.albaranDao().getAlbaranesPorRango(inicio, fin);

            String textoRango = dateFormat.format(new Date(inicio)) + " - " + dateFormat.format(new Date(fin));
            runOnUiThread(() -> {
                albaranesActuales = albaranes;
                adapter.updateData(albaranes);
                tvFiltroActual.setText("Mostrando: " + textoRango + " (" + albaranes.size() + " albaranes)");
                // idsSeleccionados ya se limpia en adapter.updateData()
            });
        }).start();
    }

    private void actualizarContadorSeleccion() {
        if (idsSeleccionados.isEmpty()) {
            btnGenerarInforme.setText("Generar informe");
            btnSeleccionarTodos.setText("Seleccionar todos");
        } else {
            btnGenerarInforme.setText("Generar informe (" + idsSeleccionados.size() + ")");
            if (idsSeleccionados.size() == albaranesActuales.size() && !albaranesActuales.isEmpty()) {
                btnSeleccionarTodos.setText("Deseleccionar todos");
            } else {
                btnSeleccionarTodos.setText("Seleccionar todos");
            }
        }
    }

    private void mostrarDialogNuevoAlbaran() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_nuevo_albaran);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextInputLayout tilCif = dialog.findViewById(R.id.tilCif);
        TextInputLayout tilNombreAlbaran = dialog.findViewById(R.id.tilNombreAlbaran);
        TextInputLayout tilImporte = dialog.findViewById(R.id.tilImporte);

        TextInputEditText etCif = dialog.findViewById(R.id.etCif);
        TextInputEditText etNombreAlbaran = dialog.findViewById(R.id.etNombreAlbaran);
        TextInputEditText etImporte = dialog.findViewById(R.id.etImporte);

        CheckBox cbPagado = dialog.findViewById(R.id.cbPagado);
        imgPreviewDialog = dialog.findViewById(R.id.imgPreviewAlbaran);

        MaterialButton btnTomarFotoAlbaran = dialog.findViewById(R.id.btnTomarFotoAlbaran);
        MaterialButton btnSubirFotoAlbaran = dialog.findViewById(R.id.btnSubirFotoAlbaran);
        MaterialButton btnCancelarAlbaran = dialog.findViewById(R.id.btnCancelarAlbaran);
        MaterialButton btnGuardarAlbaran = dialog.findViewById(R.id.btnGuardarAlbaran);

        imagenAlbaranUri = null;

        btnCancelarAlbaran.setOnClickListener(v -> dialog.dismiss());

        btnTomarFotoAlbaran.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Uri uri = crearUriParaFoto();
                if (uri != null) {
                    imagenAlbaranUri = uri;
                    camaraLauncher.launch(uri);
                }
            } else {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnSubirFotoAlbaran.setOnClickListener(v -> galeriaLauncher.launch("image/*"));

        btnGuardarAlbaran.setOnClickListener(v -> {
            if (tilCif != null) tilCif.setError(null);
            if (tilNombreAlbaran != null) tilNombreAlbaran.setError(null);
            if (tilImporte != null) tilImporte.setError(null);

            String cif = etCif.getText() != null ? etCif.getText().toString().trim() : "";
            String nombre = etNombreAlbaran.getText() != null ? etNombreAlbaran.getText().toString().trim() : "";
            String importeTxt = etImporte.getText() != null ? etImporte.getText().toString().trim() : "";

            boolean hayError = false;

            if (cif.isEmpty()) {
                tilCif.setError("Introduce el CIF");
                hayError = true;
            }

            if (nombre.isEmpty()) {
                tilNombreAlbaran.setError("Introduce el nombre");
                hayError = true;
            }

            if (importeTxt.isEmpty()) {
                tilImporte.setError("Introduce el importe");
                hayError = true;
            }

            if (imagenAlbaranUri == null) {
                Toast.makeText(this, "Debes añadir una imagen del albarán", Toast.LENGTH_SHORT).show();
                hayError = true;
            }

            if (hayError) return;

            double importe;
            try {
                importe = Double.parseDouble(importeTxt.replace(",", "."));
            } catch (NumberFormatException e) {
                tilImporte.setError("El importe debe ser numérico");
                return;
            }

            if (importe < 0) {
                tilImporte.setError("El importe no puede ser negativo");
                return;
            }

            final String cifFinal = cif;
            final String nombreFinal = nombre;
            final double importeFinal = importe;
            final boolean pagadoFinal = cbPagado.isChecked();
            final String imagenFinal = imagenAlbaranUri.toString();

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);

                long fechaActual = System.currentTimeMillis();
                Long fechaPago = pagadoFinal ? fechaActual : null;

                Albaran nuevo = new Albaran(
                    Sesion.getUsuario().getId(),
                    fechaActual,
                    cifFinal,
                    nombreFinal,
                    importeFinal,
                    pagadoFinal,
                    fechaPago,
                    imagenFinal
                );

                db.albaranDao().insert(nuevo);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Albarán añadido correctamente", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    cargarAlbaranesMesActual();
                });
            }).start();
        });

        dialog.show();
    }

    private void mostrarDialogVerAlbaran(Albaran albaran) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_ver_albaran);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        TextView tvVerNombre = dialog.findViewById(R.id.tvVerNombre);
        TextView tvVerCif = dialog.findViewById(R.id.tvVerCif);
        TextView tvVerImporte = dialog.findViewById(R.id.tvVerImporte);
        TextView tvVerFecha = dialog.findViewById(R.id.tvVerFecha);
        TextView tvVerEstado = dialog.findViewById(R.id.tvVerEstado);
        TextView tvVerFechaPago = dialog.findViewById(R.id.tvVerFechaPago);
        LinearLayout layoutFechaPago = dialog.findViewById(R.id.layoutFechaPago);
        ImageView imgVerAlbaran = dialog.findViewById(R.id.imgVerAlbaran);
        MaterialButton btnCerrarVer = dialog.findViewById(R.id.btnCerrarVer);

        tvVerNombre.setText(albaran.getNombre());
        tvVerCif.setText(albaran.getCif());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        tvVerImporte.setText(currencyFormat.format(albaran.getImporte()));

        tvVerFecha.setText(dateFormat.format(new Date(albaran.getFechaMs())));

        if (albaran.isPagado()) {
            tvVerEstado.setText("PAGADO");
            tvVerEstado.setTextColor(getColor(android.R.color.holo_green_dark));

            if (albaran.getFechaPagoMs() != null) {
                layoutFechaPago.setVisibility(View.VISIBLE);
                tvVerFechaPago.setText(dateFormat.format(new Date(albaran.getFechaPagoMs())));
            }
        } else {
            tvVerEstado.setText("PENDIENTE");
            tvVerEstado.setTextColor(getColor(android.R.color.holo_orange_dark));
            layoutFechaPago.setVisibility(View.GONE);
        }

        if (albaran.getImagenUri() != null && !albaran.getImagenUri().isEmpty()) {
            // Si es la imagen por defecto (android.resource), mostrar el drawable
            if (albaran.getImagenUri().startsWith("android.resource://")) {
                imgVerAlbaran.setImageResource(R.drawable.ic_albaran_default);
            } else {
                // Es una imagen real de la galería o cámara
                imgVerAlbaran.setImageURI(Uri.parse(albaran.getImagenUri()));
            }
        } else {
            // Si no hay imagen, mostrar la imagen por defecto
            imgVerAlbaran.setImageResource(R.drawable.ic_albaran_default);
        }


        btnCerrarVer.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private Uri crearUriParaFoto() {
        try {
            File dir = new File(getFilesDir(), "albaranes");
            if (!dir.exists()) dir.mkdirs();

            File file = File.createTempFile("albaran_", ".jpg", dir);

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

    private final ActivityResultLauncher<String> galeriaLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenAlbaranUri = uri;
                    if (imgPreviewDialog != null) {
                        imgPreviewDialog.setImageURI(uri);
                        imgPreviewDialog.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                }
            });

    private final ActivityResultLauncher<Uri> camaraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && imagenAlbaranUri != null) {
                    if (imgPreviewDialog != null) {
                        imgPreviewDialog.setImageURI(imagenAlbaranUri);
                        imgPreviewDialog.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                } else {
                    Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> permisoCamaraLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    Uri uri = crearUriParaFoto();
                    if (uri != null) {
                        imagenAlbaranUri = uri;
                        camaraLauncher.launch(uri);
                    }
                } else {
                    Toast.makeText(this, "Debes conceder permiso de cámara", Toast.LENGTH_SHORT).show();
                }
            });
}

