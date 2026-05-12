package com.example.controlalmacen.view;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.controller.EmailSender;
import com.example.controlalmacen.controller.Sesion;
import com.example.controlalmacen.data.AppDatabase;
import com.example.controlalmacen.model.Albaran;
import com.example.controlalmacen.model.Usuario;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class InformeAlbaranesActivity extends BaseActivity {

    private TextView tvTotalImporte;
    private TextView tvTotalAlbaranes;
    private TextView tvPagados;
    private TextView tvSinPagar;
    private TextView tvListaCifs;
    private TextView tvListaNombres;
    private TextView tvEmailAdminInforme;

    private RecyclerView rvAlbaranesInforme;

    private Button btnCerrar;
    private MaterialButton btnEnviarInforme;
    private TextInputEditText etEmailDestinoInforme;

    private List<Long> idsSeleccionados;

    private String cuerpoInforme = "";
    private final String asuntoInforme = "Informe de albaranes";

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_albaranes);

        inicializarVistas();

        idsSeleccionados = (List<Long>) getIntent().getSerializableExtra("ids_albaranes");

        mostrarEmailAdmin();

        if (idsSeleccionados != null && !idsSeleccionados.isEmpty()) {
            cargarInforme();
        }

        btnCerrar.setOnClickListener(v -> finish());
        btnEnviarInforme.setOnClickListener(v -> enviarInformeDesdeApp());
    }

    private void inicializarVistas() {
        tvTotalImporte = findViewById(R.id.tvTotalImporte);
        tvTotalAlbaranes = findViewById(R.id.tvTotalAlbaranes);
        tvPagados = findViewById(R.id.tvPagados);
        tvSinPagar = findViewById(R.id.tvSinPagar);
        tvListaCifs = findViewById(R.id.tvListaCifs);
        tvListaNombres = findViewById(R.id.tvListaNombres);

        tvEmailAdminInforme = findViewById(R.id.tvEmailAdminInforme);
        etEmailDestinoInforme = findViewById(R.id.etEmailDestinoInforme);
        btnEnviarInforme = findViewById(R.id.btnEnviarInforme);

        rvAlbaranesInforme = findViewById(R.id.rvAlbaranesInforme);
        btnCerrar = findViewById(R.id.btnCerrarInforme);
    }

    private void mostrarEmailAdmin() {
        String emailAdmin = obtenerEmailAdmin();

        if (emailAdmin.isEmpty()) {
            tvEmailAdminInforme.setText("Desde: administrador sin email configurado");
        } else {
            tvEmailAdminInforme.setText("Desde: " + emailAdmin);
        }
    }

    private String obtenerEmailAdmin() {
        Usuario usuario = Sesion.getUsuario();

        if (usuario == null) return "";
        if (!usuario.isAdministrador()) return "";
        if (usuario.getEmail() == null) return "";

        return usuario.getEmail().trim();
    }

    private void cargarInforme() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Albaran> albaranes = db.albaranDao().getAlbaranesPorIds(idsSeleccionados);

            if (albaranes == null || albaranes.isEmpty()) {
                runOnUiThread(() -> {
                    tvTotalAlbaranes.setText("0");
                    tvTotalImporte.setText("0,00 €");
                    Toast.makeText(this, "No hay albaranes para generar el informe", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            double totalImporte = 0;
            int totalPagados = 0;
            int totalSinPagar = 0;

            Set<String> cifs = new HashSet<>();
            StringBuilder nombres = new StringBuilder();

            for (Albaran a : albaranes) {
                totalImporte += a.getImporte();

                if (a.isPagado()) {
                    totalPagados++;
                } else {
                    totalSinPagar++;
                }

                cifs.add(a.getCif());

                if (nombres.length() > 0) nombres.append(", ");
                nombres.append(a.getNombre());
            }

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
            String importeFormateado = currencyFormat.format(totalImporte);

            StringBuilder cifsTexto = new StringBuilder();
            for (String cif : cifs) {
                if (cifsTexto.length() > 0) cifsTexto.append(", ");
                cifsTexto.append(cif);
            }

            int finalTotalPagados = totalPagados;
            int finalTotalSinPagar = totalSinPagar;
            String finalCifsTexto = cifsTexto.toString();
            String finalNombresTexto = nombres.toString();

            cuerpoInforme = crearCuerpoInforme(
                    albaranes,
                    importeFormateado,
                    finalTotalPagados,
                    finalTotalSinPagar,
                    finalCifsTexto,
                    finalNombresTexto
            );

            runOnUiThread(() -> {
                tvTotalAlbaranes.setText(String.valueOf(albaranes.size()));
                tvTotalImporte.setText(importeFormateado);
                tvPagados.setText(String.valueOf(finalTotalPagados));
                tvSinPagar.setText(String.valueOf(finalTotalSinPagar));
                tvListaCifs.setText(finalCifsTexto);
                tvListaNombres.setText(finalNombresTexto);

                rvAlbaranesInforme.setLayoutManager(new LinearLayoutManager(this));
                rvAlbaranesInforme.setAdapter(new AlbaranAdapter(albaranes, albaran -> {
                    // En el informe no hace falta abrir detalle
                }, null));
            });
        }).start();
    }

    private String crearCuerpoInforme(
            List<Albaran> albaranes,
            String importeTotal,
            int pagados,
            int sinPagar,
            String cifs,
            String nombres
    ) {
        String emailAdmin = obtenerEmailAdmin();

        StringBuilder sb = new StringBuilder();

        sb.append("INFORME DE ALBARANES\n");
        sb.append("====================\n\n");

        sb.append("Correo administrador: ");
        sb.append(emailAdmin.isEmpty() ? "No configurado" : emailAdmin);
        sb.append("\n\n");

        sb.append("RESUMEN\n");
        sb.append("Total de albaranes: ").append(albaranes.size()).append("\n");
        sb.append("Importe total: ").append(importeTotal).append("\n");
        sb.append("Pagados: ").append(pagados).append("\n");
        sb.append("Sin pagar: ").append(sinPagar).append("\n");
        sb.append("CIFs: ").append(cifs).append("\n");
        sb.append("Nombres: ").append(nombres).append("\n\n");

        sb.append("DETALLE DE ALBARANES\n");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

        for (Albaran a : albaranes) {
            sb.append("--------------------\n");
            sb.append("Nombre: ").append(a.getNombre()).append("\n");
            sb.append("CIF: ").append(a.getCif()).append("\n");
            sb.append("Importe: ").append(currencyFormat.format(a.getImporte())).append("\n");
            sb.append("Fecha: ").append(dateFormat.format(new Date(a.getFechaMs()))).append("\n");
            sb.append("Estado: ").append(a.isPagado() ? "Pagado" : "Sin pagar").append("\n");

            if (a.getFechaPagoMs() != null) {
                sb.append("Fecha de pago: ")
                        .append(dateFormat.format(new Date(a.getFechaPagoMs())))
                        .append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private void enviarInformeDesdeApp() {
        Usuario admin = Sesion.getUsuario();

        if (admin == null || !admin.isAdministrador()) {
            Toast.makeText(this, "Solo un administrador puede enviar informes", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailAdmin = admin.getEmail() != null ? admin.getEmail().trim() : "";



        String passwordAppGmail = admin.getPasswordCorreo() != null
                ? admin.getPasswordCorreo().trim().replace(" ", "")
                : "";

        String emailDestino = etEmailDestinoInforme.getText() != null
                ? etEmailDestinoInforme.getText().toString().trim()
                : "";

        if (emailAdmin.isEmpty()) {
            Toast.makeText(this, "El administrador no tiene email configurado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordAppGmail.isEmpty()) {
            Toast.makeText(this, "Falta la contraseña de aplicación de Gmail", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emailDestino.isEmpty()) {
            Toast.makeText(this, "Introduce el email destinatario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailDestino).matches()) {
            Toast.makeText(this, "Introduce un email válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cuerpoInforme == null || cuerpoInforme.trim().isEmpty()) {
            Toast.makeText(this, "El informe todavía no está preparado", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviarInforme.setEnabled(false);
        btnEnviarInforme.setText("Enviando...");

        new Thread(() -> {
            try {
                EmailSender.enviarGmail(
                        emailAdmin,
                        passwordAppGmail,
                        emailDestino,
                        asuntoInforme,
                        cuerpoInforme
                );

                runOnUiThread(() -> {
                    btnEnviarInforme.setEnabled(true);
                    btnEnviarInforme.setText("Enviar informe");
                    Toast.makeText(this, "Informe enviado correctamente", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(() -> {
                    btnEnviarInforme.setEnabled(true);
                    btnEnviarInforme.setText("Enviar informe");
                    Toast.makeText(this, "Error enviando el informe: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}