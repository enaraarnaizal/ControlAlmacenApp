package com.example.controlalmacen.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.controlalmacen.R;
import com.example.controlalmacen.controller.Sesion;
import com.example.controlalmacen.data.AppDatabase;
import com.example.controlalmacen.data.UsuarioDao;
import com.example.controlalmacen.model.Usuario;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class DiarioActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout userContainer;
    private Button btnTodos, btnLimiteMinimo, btnStockNormal;
    private TextView tvDiario;
    private ImageView imgMenu;

    private List<Usuario> usuarios;
    private Usuario selectedUser;
    private String selectedFilter = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
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
        selectFilter("Todos", btnTodos);
        loadUsers();
        setupListeners();
    }

    private void initViews() {
        userContainer = findViewById(R.id.userContainer);
        btnTodos = findViewById(R.id.btnTodos);
        btnLimiteMinimo = findViewById(R.id.btnLimiteMinimo);
        btnStockNormal = findViewById(R.id.btnStockNormal);
        tvDiario = findViewById(R.id.tvDiario);
        imgMenu = findViewById(R.id.imgMenu);

        selectedUser = Sesion.getUsuario();
    }

    private void loadUsers() {
        new Thread(() -> {
            UsuarioDao dao = AppDatabase.getInstance(this).usuarioDao();
            usuarios = dao.getUsuariosActivos();

            runOnUiThread(this::displayUsers);
        }).start();
    }

    private void displayUsers() {
        userContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Usuario usuario : usuarios) {
            View userView = inflater.inflate(R.layout.item_usuario_horizontal, userContainer, false);

            LinearLayout userCard = userView.findViewById(R.id.userCard);
            TextView tvUserName = userView.findViewById(R.id.tvUserName);
            FrameLayout userCircle = userView.findViewById(R.id.userCircle);
            ImageView imgUserIcon = userView.findViewById(R.id.imgUserIcon);

            tvUserName.setText(usuario.getNombre());

            boolean isSelected = selectedUser != null && usuario.getId() == selectedUser.getId();
            if (isSelected) {
                // Tarjeta azul con texto e icono blanco
                userCard.setBackgroundResource(R.drawable.bg_card_selected);
                userCircle.setBackgroundResource(R.drawable.bg_circle_white);
                imgUserIcon.setImageResource(R.drawable.ic_user_blue);
                tvUserName.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                // Sin fondo, círculo azul con icono blanco
                userCard.setBackgroundResource(R.drawable.bg_card_unselected);
                userCircle.setBackgroundResource(R.drawable.bg_circle_blue);
                imgUserIcon.setImageResource(R.drawable.ic_user_white);
                tvUserName.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            }

            userView.setOnClickListener(v -> {
                selectedUser = usuario;
                displayUsers();
                updateContent();
            });

            userContainer.addView(userView);
        }
    }

    private void setupListeners() {
        btnTodos.setOnClickListener(v -> selectFilter("Todos", btnTodos));
        btnLimiteMinimo.setOnClickListener(v -> selectFilter("Límite Mínimo", btnLimiteMinimo));
        btnStockNormal.setOnClickListener(v -> selectFilter("Stock Normal", btnStockNormal));

        imgMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void selectFilter(String filter, Button selectedButton) {
        selectedFilter = filter;

        resetFilterButton(btnTodos);
        resetFilterButton(btnLimiteMinimo);
        resetFilterButton(btnStockNormal);

        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white));

        updateContent();
    }

    private void resetFilterButton(Button button) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light));
        button.setTextColor(ContextCompat.getColor(this, R.color.black));
    }

    private void updateContent() {
        String userName = selectedUser != null ? selectedUser.getNombre() : "Ninguno";
        tvDiario.setText("Usuario: " + userName + "\nFiltro: " + selectedFilter);
    }
}
