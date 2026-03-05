package com.example.controlalmacen.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.controller.Sesion;
import com.example.controlalmacen.data.AppDatabase;
import com.example.controlalmacen.data.UsuarioDao;
import com.example.controlalmacen.model.Interaccion;
import com.example.controlalmacen.model.Producto;
import com.example.controlalmacen.model.Usuario;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class DiarioActivity extends BaseActivity implements ProductoAdapter.Listener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    // UI usuarios + filtros
    private LinearLayout userContainer;
    private Button btnTodos, btnLimiteMinimo, btnStockNormal;
    private ImageView imgSalir;

    private List<Usuario> usuarios = new ArrayList<>();
    private Usuario selectedUser;              // siempre será el logueado
    private String selectedFilter = "Todos";

    // Productos
    private ProductoAdapter adapter;
    private final List<Producto> topList = new ArrayList<>();
    private final List<Producto> restList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        // Toolbar + Drawer (como antes)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigationView);
        Menu menu = navigationView.getMenu();

        boolean esAdmin = Sesion.getUsuario().isAdministrador();
        menu.findItem(R.id.nav_perfiles).setEnabled(esAdmin);
        menu.findItem(R.id.nav_albaranes).setEnabled(esAdmin);
        menu.findItem(R.id.nav_inventario).setEnabled(esAdmin);

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawers();
            return true;
        });

        initViews();

        // ✅ Usuario fijo: el de sesión (no se puede cambiar)
        selectedUser = Sesion.getUsuario();

        // Filtro por defecto
        selectFilter("Todos", btnTodos);

        setupRecycler();

        // Cargar usuarios arriba (solo para mostrar)
        loadUsers();
    }

    private void initViews() {
        userContainer = findViewById(R.id.userContainer);

        btnTodos = findViewById(R.id.btnTodos);
        btnLimiteMinimo = findViewById(R.id.btnLimiteMinimo);
        btnStockNormal = findViewById(R.id.btnStockNormal);

        imgSalir = findViewById(R.id.imgSalir);

        btnTodos.setOnClickListener(v -> selectFilter("Todos", btnTodos));
        btnLimiteMinimo.setOnClickListener(v -> selectFilter("Límite Mínimo", btnLimiteMinimo));
        btnStockNormal.setOnClickListener(v -> selectFilter("Stock Normal", btnStockNormal));

        // ✅ Logout -> MainActivity + limpiar sesión
        imgSalir.setOnClickListener(v -> {
            Sesion.setUsuario(null); // <-- si tu Sesion usa otro método, dímelo y lo ajusto
            Intent i = new Intent(DiarioActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void setupRecycler() {
        RecyclerView rvProductos = findViewById(R.id.rvProductos);

        adapter = new ProductoAdapter(this);

        GridLayoutManager glm = new GridLayoutManager(this, 2);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int type = adapter.getItemViewType(position);
                // Header ocupa 2 columnas
                if (type == 2) return 2;
                return 1;
            }
        });

        rvProductos.setLayoutManager(glm);
        rvProductos.setAdapter(adapter);
    }

    // -------------------- USUARIOS (solo mostrar, no cambiar) --------------------

    private void loadUsers() {
        new Thread(() -> {
            UsuarioDao dao = AppDatabase.getInstance(this).usuarioDao();
            usuarios = dao.getUsuariosActivos();

            runOnUiThread(() -> {
                displayUsers();
                if (selectedUser != null) cargarProductos(selectedUser.getId());
            });
        }).start();
    }

    private void displayUsers() {
        userContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Usuario u : usuarios) {
            View userView = inflater.inflate(R.layout.item_usuario_horizontal, userContainer, false);

            LinearLayout userCard = userView.findViewById(R.id.userCard);
            TextView tvUserName = userView.findViewById(R.id.tvUserName);
            FrameLayout userCircle = userView.findViewById(R.id.userCircle);
            ImageView imgUserIcon = userView.findViewById(R.id.imgUserIcon);

            tvUserName.setText(u.getNombre());

            boolean isSelected = (selectedUser != null && u.getId() == selectedUser.getId());

            if (isSelected) {
                userCard.setBackgroundResource(R.drawable.bg_card_selected);
                userCircle.setBackgroundResource(R.drawable.bg_circle_white);
                imgUserIcon.setImageResource(R.drawable.ic_user_blue);
                tvUserName.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                userCard.setBackgroundResource(R.drawable.bg_card_unselected);
                userCircle.setBackgroundResource(R.drawable.bg_circle_blue);
                imgUserIcon.setImageResource(R.drawable.ic_user_white);
                tvUserName.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            }

            // ✅ Bloqueado: no se puede cambiar de usuario
            userView.setOnClickListener(v ->
                    Toast.makeText(this, "No puedes cambiar de usuario sin cerrar sesión", Toast.LENGTH_SHORT).show()
            );

            userContainer.addView(userView);
        }
    }

    // -------------------- FILTROS (solo UI por ahora) --------------------

    private void selectFilter(String filter, Button selectedButton) {
        selectedFilter = filter;

        resetFilterButton(btnTodos);
        resetFilterButton(btnLimiteMinimo);
        resetFilterButton(btnStockNormal);

        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white));

        // Más adelante: aplicar filtro real al listado.
    }

    private void resetFilterButton(Button button) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light));
        button.setTextColor(ContextCompat.getColor(this, R.color.black));
    }

    // -------------------- PRODUCTOS (top10 + resto) --------------------

    private void cargarProductos(long usuarioId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            List<Long> idsTop = db.interaccionDao().getTop10Ids(usuarioId);

            topList.clear();
            restList.clear();

            if (idsTop == null || idsTop.isEmpty()) {

                topList.addAll(db.productoDao().getPrimeros10());

                List<Producto> all = db.productoDao().getAllHabilitadosOrdenados();
                for (Producto p : all) {
                    boolean estaEnTop = false;
                    for (Producto t : topList) {
                        if (t.getId() == p.getId()) { estaEnTop = true; break; }
                    }
                    if (!estaEnTop) restList.add(p);
                }

            } else {
                topList.addAll(db.interaccionDao().getTop10Recientes(usuarioId));
                restList.addAll(db.productoDao().getRestoExcluyendoIds(idsTop));
            }

            runOnUiThread(() -> adapter.setData(topList, restList));
        }).start();
    }

    // -------------------- LISTENER del adapter --------------------

    @Override
    public void onClickAddProducto() {
        android.app.Dialog dialog = new android.app.Dialog(this, R.style.DialogAddProducto);
        dialog.setContentView(R.layout.dialog_add_producto);

        dialog.findViewById(R.id.btnCerrar).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnTomarFoto).setOnClickListener(v ->
                Toast.makeText(this, "Tomar foto (pendiente)", Toast.LENGTH_SHORT).show());

        dialog.findViewById(R.id.btnSubirFoto).setOnClickListener(v ->
                Toast.makeText(this, "Subir foto (pendiente)", Toast.LENGTH_SHORT).show());

        dialog.findViewById(R.id.btnGuardar).setOnClickListener(v ->
                Toast.makeText(this, "Guardar (pendiente)", Toast.LENGTH_SHORT).show());

        dialog.show();
    }

    @Override
    public void onSumar(Producto p) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            p.setCantidad(p.getCantidad() + 1);
            db.productoDao().update(p);

            long uid = (selectedUser != null) ? selectedUser.getId() : Sesion.getUsuario().getId();

            db.interaccionDao().insert(new Interaccion(
                    uid,
                    p.getId(),
                    System.currentTimeMillis(),
                    "ADD"
            ));

            runOnUiThread(() -> adapter.notifyProductoChangedById(p.getId()));
        }).start();
    }

    @Override
    public void onRestar(Producto p) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            if (p.getCantidad() > 0) {
                p.setCantidad(p.getCantidad() - 1);
                db.productoDao().update(p);

                long uid = (selectedUser != null) ? selectedUser.getId() : Sesion.getUsuario().getId();

                db.interaccionDao().insert(new Interaccion(
                        uid,
                        p.getId(),
                        System.currentTimeMillis(),
                        "REMOVE"
                ));
            }

            runOnUiThread(() -> adapter.notifyProductoChangedById(p.getId()));
        }).start();
    }
}