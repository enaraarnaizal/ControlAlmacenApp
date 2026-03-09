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

public class DiarioActivity extends BaseActivity implements ProductoAdapter.Listener { //se implementa el archivo de baseactivity para controlar los 10misn de inactividad

    //menu lateral oculto
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private LinearLayout userContainer; //menu de arriba de usuarios
    private Button btnTodos, btnLimiteMinimo, btnStockNormal; //botones
    private ImageView imgSalir; //para el logout

    //listado de usuarios
    private List<Usuario> usuarios = new ArrayList<>();
    private Usuario selectedUser; // siempre sera el logueado
    private String selectedFilter = "Todos";

    // listado de Productos
    private ProductoAdapter adapter;
    private final List<Producto> topList = new ArrayList<>(); //productos mas usado por el usuario
    private final List<Producto> restList = new ArrayList<>(); //basicamente el resto de productos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        //para q el menu sea la pantalla principal
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //para canectar el menu lateral con la toolbar para el icono del menu se abra y se cierre
        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigationView);
        Menu menu = navigationView.getMenu();

        //desactivar o activar dependiendo el rol con el que te hayas logueado
        boolean esAdmin = Sesion.getUsuario().isAdministrador();
        menu.findItem(R.id.nav_perfiles).setEnabled(esAdmin);
        menu.findItem(R.id.nav_albaranes).setEnabled(esAdmin);
        menu.findItem(R.id.nav_inventario).setEnabled(esAdmin);

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawers();
            return true;
        });

        initViews();

        // usuario fijo, el de sesion (no se puede cambiar)
        selectedUser = Sesion.getUsuario();

        // filtro por defecto
        selectFilter("Todos", btnTodos);

        setupRecycler(); //lista visual de productos

        // cargar usuarios arriba (solo para mostrar)
        loadUsers();
    }

    private void initViews() {
        //metemos acciones a todos los botones
        userContainer = findViewById(R.id.userContainer);

        btnTodos = findViewById(R.id.btnTodos);
        btnLimiteMinimo = findViewById(R.id.btnLimiteMinimo);
        btnStockNormal = findViewById(R.id.btnStockNormal);

        imgSalir = findViewById(R.id.imgSalir);

        //aqui para los filtros tmb
        btnTodos.setOnClickListener(v -> selectFilter("Todos", btnTodos));
        btnLimiteMinimo.setOnClickListener(v -> selectFilter("Límite Mínimo", btnLimiteMinimo));
        btnStockNormal.setOnClickListener(v -> selectFilter("Stock Normal", btnStockNormal));

        // Logout (cerrar sesion y volver al login, mainactivity)
        imgSalir.setOnClickListener(v -> {
            Sesion.setUsuario(null);
            Intent i = new Intent(DiarioActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    //aqui configuramos la lsita de productos
    private void setupRecycler() {
        RecyclerView rvProductos = findViewById(R.id.rvProductos);

        adapter = new ProductoAdapter(this);

        GridLayoutManager glm = new GridLayoutManager(this, 2); //para poner que haya dos columnas
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int type = adapter.getItemViewType(position);
                // Header ocupa 2 columnas
                if (type == 2) return 2;
                //lo demas 1 (los productos)
                return 1;
            }
        });

        rvProductos.setLayoutManager(glm);
        rvProductos.setAdapter(adapter);
    }

    // cargamos los usuarios desde la bd
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

    //pone los usuarios arriba
    private void displayUsers() {
        userContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Usuario u : usuarios) {
            View userView = inflater.inflate(R.layout.item_usuario_horizontal, userContainer, false);

            LinearLayout userCard = userView.findViewById(R.id.userCard);
            TextView tvUserName = userView.findViewById(R.id.tvUserName);
            FrameLayout userCircle = userView.findViewById(R.id.userCircle);
            ImageView imgUserIcon = userView.findViewById(R.id.imgUserIcon);

            tvUserName.setText(u.getNombre()); //para poner el nombre debajo de cada icono

            boolean isSelected = (selectedUser != null && u.getId() == selectedUser.getId()); //marcar el usuario con el que esta

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

            //bloquear el cambio de usuario
            userView.setOnClickListener(v ->
                    Toast.makeText(this, "No puedes cambiar de usuario sin cerrar sesión", Toast.LENGTH_SHORT).show()
            );

            userContainer.addView(userView);
        }
    }

    // cambior el filtro q esta puesto por default
    private void selectFilter(String filter, Button selectedButton) {
        selectedFilter = filter;

        //reseteamos todos
        resetFilterButton(btnTodos);
        resetFilterButton(btnLimiteMinimo);
        resetFilterButton(btnStockNormal);

        //ahora el que se pulsa se marca como activo (estilo)
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white));

        // Aplicar filtro real al listado
        if (selectedUser != null) {
            cargarProductos(selectedUser.getId());
        }
    }

    private void resetFilterButton(Button button) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light));
        button.setTextColor(ContextCompat.getColor(this, R.color.black));
    }

    //PRODUCTOS (top10 + resto)

    private void cargarProductos(long usuarioId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this); //abrimos el hilo en 2 plano

            List<Long> idsTop = db.interaccionDao().getTop10Ids(usuarioId); //busca el top10 del usuario con el q estamos

            topList.clear();
            restList.clear();

            //creamos listas temporales para luego aplicar a las reales el filtro
            List<Producto> tempTop = new ArrayList<>();
            List<Producto> tempRest = new ArrayList<>();

            //si no tiene historial, osea si no se ha hecho nada aun pilla el orden por alfabeto
            if (idsTop == null || idsTop.isEmpty()) {

                tempTop.addAll(db.productoDao().getPrimeros10());

                List<Producto> all = db.productoDao().getAllHabilitadosOrdenados(); //aqui los seprara entre el 10 y el resto (osea coge los que no estan metidos ya en el top10)
                for (Producto p : all) {
                    boolean estaEnTop = false;
                    for (Producto t : tempTop) {
                        if (t.getId() == p.getId()) { estaEnTop = true; break; }
                    }
                    if (!estaEnTop) tempRest.add(p);
                }

                //si el usuario si tiene hisotrial entonces devuelve los productos recientes usado por el usuario
            } else {
                tempTop.addAll(db.interaccionDao().getTop10Recientes(usuarioId));
                tempRest.addAll(db.productoDao().getRestoExcluyendoIds(idsTop));
            }

            // aplicar filtro segun selectedFilter
            for (Producto p : tempTop) {
                if (pasaFiltro(p)) topList.add(p);
            }
            for (Producto p : tempRest) {
                if (pasaFiltro(p)) restList.add(p);
            }

            runOnUiThread(() -> adapter.setData(topList, restList));
        }).start();
    }


    private boolean pasaFiltro(Producto p) {
        switch (selectedFilter) {
            case "Límite Mínimo":
                return p.getCantidad() <= p.getCantidadMin();
            case "Stock Normal":
                return p.getCantidad() > p.getCantidadMin();
            case "Todos":
            default:
                return true;
        }
    }

    // -------------------- LISTENER del adapter --------------------

    @Override
    public void onClickAddProducto() {
        //cuando pulsa el el boton de anadir se abre un dig nuevo (formulario)
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