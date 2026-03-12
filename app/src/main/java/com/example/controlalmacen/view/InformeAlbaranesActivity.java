package com.example.controlalmacen.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.data.AppDatabase;
import com.example.controlalmacen.model.Albaran;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class InformeAlbaranesActivity extends BaseActivity {

    private TextView tvTotalImporte, tvTotalAlbaranes, tvPagados, tvSinPagar, tvListaCifs, tvListaNombres;
    private RecyclerView rvAlbaranesInforme;
    private Button btnCerrar;
    private List<Long> idsSeleccionados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_albaranes);

        inicializarVistas();

        // Recibir IDs seleccionados
        idsSeleccionados = (List<Long>) getIntent().getSerializableExtra("ids_albaranes");

        if (idsSeleccionados != null && !idsSeleccionados.isEmpty()) {
            cargarInforme();
        }

        btnCerrar.setOnClickListener(v -> finish());
    }

    private void inicializarVistas() {
        tvTotalImporte = findViewById(R.id.tvTotalImporte);
        tvTotalAlbaranes = findViewById(R.id.tvTotalAlbaranes);
        tvPagados = findViewById(R.id.tvPagados);
        tvSinPagar = findViewById(R.id.tvSinPagar);
        tvListaCifs = findViewById(R.id.tvListaCifs);
        tvListaNombres = findViewById(R.id.tvListaNombres);
        rvAlbaranesInforme = findViewById(R.id.rvAlbaranesInforme);
        btnCerrar = findViewById(R.id.btnCerrarInforme);
    }

    private void cargarInforme() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Albaran> albaranes = db.albaranDao().getAlbaranesPorIds(idsSeleccionados);

            if (albaranes == null || albaranes.isEmpty()) {
                runOnUiThread(() -> {
                    tvTotalAlbaranes.setText("0");
                    tvTotalImporte.setText("0,00 €");
                });
                return;
            }

            // Calcular totales
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

            runOnUiThread(() -> {
                tvTotalAlbaranes.setText(String.valueOf(albaranes.size()));
                tvTotalImporte.setText(importeFormateado);
                tvPagados.setText(String.valueOf(finalTotalPagados));
                tvSinPagar.setText(String.valueOf(finalTotalSinPagar));
                tvListaCifs.setText(finalCifsTexto);
                tvListaNombres.setText(finalNombresTexto);

                // Mostrar lista de albaranes
                rvAlbaranesInforme.setLayoutManager(new LinearLayoutManager(this));
                rvAlbaranesInforme.setAdapter(new AlbaranAdapter(albaranes, albaran -> {
                    // No hacer nada al hacer click
                }, null)); // null porque no necesitamos listener de selección en el informe
            });
        }).start();
    }
}

