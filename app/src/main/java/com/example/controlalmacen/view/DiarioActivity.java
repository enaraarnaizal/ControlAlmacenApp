package com.example.controlalmacen.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.controlalmacen.R;
import com.example.controlalmacen.controller.Sesion;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.widget.Toast;

public class DiarioActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(
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

            int id = item.getItemId();

            switch (id) {
/*
                case R.id.nav_editar_producto:
                    Toast.makeText(this, "Editar Producto", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_deshabilitar_producto:
                    Toast.makeText(this, "Deshabilitar Producto", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_borrar_producto:
                    Toast.makeText(this, "Borrar Producto", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_perfiles:
                    Toast.makeText(this, "Perfiles (Admin)", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_albaranes:
                    Toast.makeText(this, "Albaranes (Admin)", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_inventario:
                    Toast.makeText(this, "Inventario (Admin)", Toast.LENGTH_SHORT).show();
                    break;
*/
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }
}
