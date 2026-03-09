package com.example.controlalmacen.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlalmacen.R;
import com.example.controlalmacen.data.AppDatabase;
import com.example.controlalmacen.model.Usuario;
import java.util.ArrayList;
import java.util.List;

public class PerfilActivity extends AppCompatActivity implements UsuarioAdminAdapter.Listener {

    private RecyclerView rvUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        rvUsuarios = findViewById(R.id.rvUsuarios);
        rvUsuarios.setLayoutManager(new GridLayoutManager(this, 3));

        // CORRECCIÓN: Llamar al método que realmente carga los datos
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                //usamos getAllOrdenado para que incluya todos (activos e inactivos)
                List<Usuario> usuariosDeBD = db.usuarioDao().getAllOrdenado();
                List<Usuario> listaParaAdapter = new ArrayList<>(usuariosDeBD);

                //si esta vacia, añadimos un admin por defecto
                if (listaParaAdapter.isEmpty()) {
                    Usuario admin = new Usuario("Admin", null, true, "1234", "admin@mail.com", true);
                    long id = db.usuarioDao().insert(admin);
                    admin.setId((int) id);
                    listaParaAdapter.add(admin);
                }

                runOnUiThread(() -> {
                    //el primer elemento es el boton +
                    listaParaAdapter.add(0, null);

                    //usar el adapter de admin
                    UsuarioAdminAdapter adapter = new UsuarioAdminAdapter(listaParaAdapter, this);
                    rvUsuarios.setAdapter(adapter);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onEditar(Usuario u) {
        Toast.makeText(this, "Editar: " + u.getNombre(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeshabilitar(Usuario u) {
        Toast.makeText(this, "Deshabilitar: " + u.getNombre(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBorrar(Usuario u) {
        Toast.makeText(this, "Borrar: " + u.getNombre(), Toast.LENGTH_SHORT).show();
    }
}