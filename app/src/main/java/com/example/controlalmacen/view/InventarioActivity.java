package com.example.controlalmacen.view;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.controller.Sesion;
import com.example.controlalmacen.data.AppDatabase;
import com.example.controlalmacen.model.Producto;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InventarioActivity extends BaseActivity {

    private TextView tvTotalProductos;
    private TextView tvUnidadesTotales;
    private TextView tvLimiteMinimo;
    private TextView tvDeshabilitados;
    private TextView tvFiltroActual;

    private MaterialButton btnFiltroTodos;
    private MaterialButton btnFiltroMinimo;
    private MaterialButton btnFiltroDeshabilitados;

    private ImageView btnCerrarInventario;
    private RecyclerView rvInventario;

    private InventarioAdapter adapter;

    private final List<Producto> productosTotales = new ArrayList<>();
    private String filtroActual = "TODOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Sesion.getUsuario() == null || !Sesion.getUsuario().isAdministrador()) {
            Toast.makeText(this, "Acceso denegado: solo administradores", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_inventario);

        inicializarVistas();
        configurarRecycler();
        configurarListeners();
        actualizarEstiloFiltros();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarInventario();
    }

    private void inicializarVistas() {
        tvTotalProductos = findViewById(R.id.tvTotalProductosValor);
        tvUnidadesTotales = findViewById(R.id.tvUnidadesTotalesValor);
        tvLimiteMinimo = findViewById(R.id.tvLimiteMinimoValor);
        tvDeshabilitados = findViewById(R.id.tvDeshabilitadosValor);
        tvFiltroActual = findViewById(R.id.tvFiltroActualInventario);

        btnFiltroTodos = findViewById(R.id.btnFiltroTodosInventario);
        btnFiltroMinimo = findViewById(R.id.btnFiltroMinimoInventario);
        btnFiltroDeshabilitados = findViewById(R.id.btnFiltroDeshabilitadosInventario);

        btnCerrarInventario = findViewById(R.id.btnCerrarInventario);
        rvInventario = findViewById(R.id.rvInventario);
    }

    private void configurarRecycler() {
        rvInventario.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventarioAdapter(new ArrayList<>());
        rvInventario.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnCerrarInventario.setOnClickListener(v -> finish());

        btnFiltroTodos.setOnClickListener(v -> {
            filtroActual = "TODOS";
            actualizarEstiloFiltros();
            aplicarFiltro();
        });

        btnFiltroMinimo.setOnClickListener(v -> {
            filtroActual = "MINIMO";
            actualizarEstiloFiltros();
            aplicarFiltro();
        });

        btnFiltroDeshabilitados.setOnClickListener(v -> {
            filtroActual = "DESHABILITADOS";
            actualizarEstiloFiltros();
            aplicarFiltro();
        });
    }

    private void cargarInventario() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Producto> productos = db.productoDao().getAllOrdenado();

            if (productos == null) {
                productos = new ArrayList<>();
            }

            ordenarProductosParaInventario(productos);

            List<Producto> finalProductos = productos;
            runOnUiThread(() -> {
                productosTotales.clear();
                productosTotales.addAll(finalProductos);

                actualizarResumen();
                aplicarFiltro();
            });
        }).start();
    }

    private void ordenarProductosParaInventario(List<Producto> productos) {
        Collections.sort(productos, new Comparator<Producto>() {
            @Override
            public int compare(Producto p1, Producto p2) {
                int prioridad1 = getPrioridadInventario(p1);
                int prioridad2 = getPrioridadInventario(p2);

                if (prioridad1 != prioridad2) {
                    return Integer.compare(prioridad1, prioridad2);
                }

                return p1.getNombre().compareToIgnoreCase(p2.getNombre());
            }
        });
    }

    private int getPrioridadInventario(Producto p) {
        if (estaEnLimiteMinimo(p)) return 0;
        if (!p.isHabilitado()) return 2;
        return 1;
    }

    private boolean estaEnLimiteMinimo(Producto p) {
        return p.isHabilitado()
                && p.getCantidadMin() > 0
                && p.getCantidad() <= p.getCantidadMin();
    }

    private void actualizarResumen() {
        int totalProductos = productosTotales.size();
        int unidadesTotales = 0;
        int totalEnMinimo = 0;
        int totalDeshabilitados = 0;

        for (Producto p : productosTotales) {
            unidadesTotales += p.getCantidad();

            if (estaEnLimiteMinimo(p)) {
                totalEnMinimo++;
            }

            if (!p.isHabilitado()) {
                totalDeshabilitados++;
            }
        }

        tvTotalProductos.setText(String.valueOf(totalProductos));
        tvUnidadesTotales.setText(String.valueOf(unidadesTotales));
        tvLimiteMinimo.setText(String.valueOf(totalEnMinimo));
        tvDeshabilitados.setText(String.valueOf(totalDeshabilitados));
    }

    private void aplicarFiltro() {
        List<Producto> filtrados = new ArrayList<>();

        for (Producto p : productosTotales) {
            switch (filtroActual) {
                case "MINIMO":
                    if (estaEnLimiteMinimo(p)) {
                        filtrados.add(p);
                    }
                    break;

                case "DESHABILITADOS":
                    if (!p.isHabilitado()) {
                        filtrados.add(p);
                    }
                    break;

                case "TODOS":
                default:
                    filtrados.add(p);
                    break;
            }
        }

        adapter.updateData(filtrados);
        actualizarTextoFiltro(filtrados.size());
    }

    private void actualizarTextoFiltro(int totalFiltrados) {
        String texto;

        switch (filtroActual) {
            case "MINIMO":
                texto = "Mostrando: productos en límite mínimo (" + totalFiltrados + ")";
                break;
            case "DESHABILITADOS":
                texto = "Mostrando: productos deshabilitados (" + totalFiltrados + ")";
                break;
            case "TODOS":
            default:
                texto = "Mostrando: inventario total (" + totalFiltrados + ")";
                break;
        }

        tvFiltroActual.setText(texto);
    }

    private void actualizarEstiloFiltros() {
        pintarBotonFiltro(btnFiltroTodos, "TODOS".equals(filtroActual));
        pintarBotonFiltro(btnFiltroMinimo, "MINIMO".equals(filtroActual));
        pintarBotonFiltro(btnFiltroDeshabilitados, "DESHABILITADOS".equals(filtroActual));
    }

    private void pintarBotonFiltro(MaterialButton boton, boolean seleccionado) {
        if (seleccionado) {
            boton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6C63FF")));
            boton.setTextColor(Color.WHITE);
            boton.setStrokeWidth(0);
        } else {
            boton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            boton.setTextColor(Color.parseColor("#4B5563"));
            boton.setStrokeWidth(2);
            boton.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#D8DCE3")));
        }
    }
}