package com.example.controlalmacen.view;


import java.util.Locale;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Dialog;
import android.net.Uri;
import android.widget.CheckBox;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;

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


import java.util.ArrayList;
import java.util.List;

//permisos para acceder a la camra de la tablet
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;


public class DiarioActivity extends BaseActivity implements ProductoAdapter.Listener { //se implementa el archivo de baseactivity para controlar los 10misn de inactividad

    //menu lateral oculto
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private LinearLayout userContainer; //menu de arriba de usuarios
    private Button btnTodos, btnLimiteMinimo, btnStockNormal; //botones
    private ImageView imgSalir; //para el logout
    private EditText etBuscarProducto;
    private String textoBusqueda = "";
    //listado de usuarios
    private List<Usuario> usuarios = new ArrayList<>();
    private Usuario selectedUser; // siempre sera el logueado
    private String selectedFilter = "Todos";

    // listado de Productos
    private ProductoAdapter adapter;
    private final List<Producto> topList = new ArrayList<>(); //productos mas usado por el usuario
    private final List<Producto> restList = new ArrayList<>(); //basicamente el resto de productos

    //variables para guardar img para el nuevo producto
    private Uri imagenProductoUri;
    private ImageView imgPreviewDialog;


//menu
    private LinearLayout itemEditarProducto, itemDeshabilitarProducto, itemBorrarProducto;
    private LinearLayout itemPerfiles, itemAlbaranes, itemInventario;
    private ImageView btnCerrarDrawer;
    private TextView tvSoloAdmins;
    private ImageView iconLockPerfiles, iconLockAlbaranes, iconLockInventario;

    private LinearLayout layoutAdminRestricted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        inicializarDrawerCustom();

        initViews();

        selectedUser = Sesion.getUsuario();

        actualizarMenuSegunUsuario();

        selectFilter("Todos", btnTodos);

        setupRecycler();

        loadUsers();
    }

    private void inicializarDrawerCustom() {
        btnCerrarDrawer = findViewById(R.id.btnCerrarDrawer);

        itemEditarProducto = findViewById(R.id.itemEditarProducto);
        itemDeshabilitarProducto = findViewById(R.id.itemDeshabilitarProducto);
        itemBorrarProducto = findViewById(R.id.itemBorrarProducto);

        itemPerfiles = findViewById(R.id.itemPerfiles);
        itemAlbaranes = findViewById(R.id.itemAlbaranes);
        itemInventario = findViewById(R.id.itemInventario);

        tvSoloAdmins = findViewById(R.id.tvSoloAdmins);
        layoutAdminRestricted = findViewById(R.id.layoutAdminRestricted);

        iconLockPerfiles = findViewById(R.id.iconLockPerfiles);
        iconLockAlbaranes = findViewById(R.id.iconLockAlbaranes);
        iconLockInventario = findViewById(R.id.iconLockInventario);

        btnCerrarDrawer.setOnClickListener(v -> drawerLayout.closeDrawers());

        itemEditarProducto.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            abrirSeleccionEditarProducto();
        });

        itemDeshabilitarProducto.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            abrirSeleccionDeshabilitarProducto();
        });

        itemBorrarProducto.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            abrirSeleccionBorrarProducto();
        });

        itemPerfiles.setOnClickListener(v -> {
            if (Sesion.getUsuario() != null && Sesion.getUsuario().isAdministrador()) {
                drawerLayout.closeDrawers();
                verificarAdminAntesDeEntrar(() ->
                        startActivity(new Intent(DiarioActivity.this, PerfilActivity.class))
                );
            } else {
                Toast.makeText(this, "Solo administradores", Toast.LENGTH_SHORT).show();
            }
        });

        itemAlbaranes.setOnClickListener(v -> {
            if (Sesion.getUsuario() != null && Sesion.getUsuario().isAdministrador()) {
                drawerLayout.closeDrawers();
                verificarAdminAntesDeEntrar(() ->
                        startActivity(new Intent(DiarioActivity.this, AlbaranActivity.class))
                );
            } else {
                Toast.makeText(this, "Solo administradores", Toast.LENGTH_SHORT).show();
            }
        });

        itemInventario.setOnClickListener(v -> {
            if (Sesion.getUsuario() != null && Sesion.getUsuario().isAdministrador()) {
                drawerLayout.closeDrawers();
                verificarAdminAntesDeEntrar(() ->
                        startActivity(new Intent(DiarioActivity.this, InventarioActivity.class))
                );
            } else {
                Toast.makeText(this, "Solo administradores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarMenuSegunUsuario();
        loadUsers();
    }
    private void cambiarUsuario(Usuario nuevoUsuario) {
        if (nuevoUsuario == null) {
            return;
        }

        // Si pulsa el mismo usuario, no hacemos nada
        if (selectedUser != null && selectedUser.getId() == nuevoUsuario.getId()) {
            return;
        }

        // Cambiamos el usuario activo real de la sesión
        selectedUser = nuevoUsuario;
        Sesion.setUsuario(nuevoUsuario);

        // Muy importante: al cambiar de perfil, la verificación admin se reinicia
        Sesion.setAdminVerificado(false);

        // Reiniciar tiempo de sesión por interacción
        Sesion.touch();

        // Opcional pero recomendable: limpiar búsqueda al cambiar de perfil
        textoBusqueda = "";
        if (etBuscarProducto != null) {
            etBuscarProducto.setText("");
        }

        // Refrescar barra superior, menú lateral y productos
        actualizarMenuSegunUsuario();
        displayUsers();
        cargarProductos(selectedUser.getId());
    }
    private void actualizarMenuSegunUsuario() {
        if (Sesion.getUsuario() == null) {
            return;
        }

        boolean esAdmin = Sesion.getUsuario().isAdministrador();

        if (esAdmin) {
            tvSoloAdmins.setVisibility(View.VISIBLE);
            layoutAdminRestricted.setVisibility(View.GONE);

            itemPerfiles.setVisibility(View.VISIBLE);
            itemAlbaranes.setVisibility(View.VISIBLE);
            itemInventario.setVisibility(View.VISIBLE);

            iconLockPerfiles.setVisibility(View.VISIBLE);
            iconLockAlbaranes.setVisibility(View.VISIBLE);
            iconLockInventario.setVisibility(View.VISIBLE);
        } else {
            tvSoloAdmins.setVisibility(View.GONE);
            layoutAdminRestricted.setVisibility(View.VISIBLE);

            itemPerfiles.setVisibility(View.GONE);
            itemAlbaranes.setVisibility(View.GONE);
            itemInventario.setVisibility(View.GONE);
        }
    }
    private void verificarAdminAntesDeEntrar(Runnable accion) {

     
        if (!Sesion.getUsuario().isAdministrador()) {
            accion.run();
            return;
        }

        
        if (Sesion.isAdminVerificado()) {
            accion.run();
            return;
        }

        
        mostrarDialogoAdmin(accion);
    }

    private void mostrarDialogoAdmin(Runnable accion) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confrimr_admin);

        EditText etPass = dialog.findViewById(R.id.etPassAdmin);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            String pass = etPass.getText().toString().trim();

            if (!pass.equals(Sesion.getUsuario().getPassword())) {
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                return;
            }

            Sesion.setAdminVerificado(true);
            dialog.dismiss();
            accion.run();
        });

        dialog.show();
    }



    private void initViews() {
        //metemos acciones a todos los botones
        userContainer = findViewById(R.id.userContainer);

        btnTodos = findViewById(R.id.btnTodos);
        btnLimiteMinimo = findViewById(R.id.btnLimiteMinimo);
        btnStockNormal = findViewById(R.id.btnStockNormal);

        etBuscarProducto = findViewById(R.id.etBuscarProducto);

        etBuscarProducto.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                textoBusqueda = s == null ? "" : s.toString().trim();

                if (selectedUser != null) {
                    cargarProductos(selectedUser.getId());
                }
            }
        });

        imgSalir = findViewById(R.id.imgSalir);

        //aqui para los filtros tmb
        btnTodos.setOnClickListener(v -> selectFilter("Todos", btnTodos));
        btnLimiteMinimo.setOnClickListener(v -> selectFilter("Límite Mínimo", btnLimiteMinimo));
        btnStockNormal.setOnClickListener(v -> selectFilter("Stock Normal", btnStockNormal));



        // Logout (cerrar sesion y volver al login, mainactivity)
        imgSalir.setOnClickListener(v -> {
            Sesion.clear();
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


    private final ActivityResultLauncher<String> galeriaLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenProductoUri = uri;
                    if (imgPreviewDialog != null) {
                        imgPreviewDialog.setImageURI(uri);
                        imgPreviewDialog.setScaleType(ImageView.ScaleType.FIT_CENTER);                    }
                }
            });

    private final ActivityResultLauncher<Uri> camaraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && imagenProductoUri != null) {
                    if (imgPreviewDialog != null) {
                        imgPreviewDialog.setImageURI(imagenProductoUri);
                        imgPreviewDialog.setScaleType(ImageView.ScaleType.FIT_CENTER);                    }
                } else {
                    Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> permisoCamaraLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    Uri uri = crearUriParaFoto();
                    if (uri != null) {
                        imagenProductoUri = uri;
                        camaraLauncher.launch(uri);
                    }
                } else {
                    Toast.makeText(this, "Debes conceder permiso de cámara", Toast.LENGTH_SHORT).show();
                }
            });
    //preparar la foto de la camara
    private Uri crearUriParaFoto() {
        try {
            File dir = new File(getFilesDir(), "productos");
            if (!dir.exists()) dir.mkdirs();

            File file = File.createTempFile("prod_", ".jpg", dir);

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

            userView.setOnClickListener(v -> cambiarUsuario(u));

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

    //productos (top10 + resto)

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
        if (!coincideBusqueda(p)) {
            return false;
        }

        switch (selectedFilter) {
            case "Límite Mínimo":
                return p.isHabilitado()
                        && p.getCantidadMin() > 0
                        && p.getCantidad() <= p.getCantidadMin();
            case "Stock Normal":
                return !p.isHabilitado()
                        || p.getCantidad() > p.getCantidadMin();
            case "Todos":
            default:
                return true;
        }
    }
    private boolean coincideBusqueda(Producto p) {
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            return true;
        }

        if (p.getNombre() == null) {
            return false;
        }

        return p.getNombre()
                .toLowerCase(Locale.ROOT)
                .contains(textoBusqueda.trim().toLowerCase(Locale.ROOT));
    }
    //listener del adapter

    @Override
    public void onClickAddProducto() {
        //crea y muestra el dialogo (formulario)
        Dialog dialog = new Dialog(this, R.style.DialogAddProducto);
        dialog.setContentView(R.layout.dialog_add_producto);

        //mete las acciones en los botones/img/texto
        ImageView btnCerrar = dialog.findViewById(R.id.btnCerrar);

        com.google.android.material.button.MaterialButton btnTomarFoto = dialog.findViewById(R.id.btnTomarFoto);
        com.google.android.material.button.MaterialButton btnSubirFoto = dialog.findViewById(R.id.btnSubirFoto);
        com.google.android.material.button.MaterialButton btnCancelar = dialog.findViewById(R.id.btnCancelar);
        com.google.android.material.button.MaterialButton btnGuardar = dialog.findViewById(R.id.btnGuardar);

        //mensajes de error
        TextInputLayout tilNombre = dialog.findViewById(R.id.tilNombre);
        TextInputLayout tilCantidad = dialog.findViewById(R.id.tilCantidad);
        TextInputLayout tilMin = dialog.findViewById(R.id.tilMin);

        //campos de texto
        TextInputEditText etNombre = dialog.findViewById(R.id.etNombre);
        TextInputEditText etCantidad = dialog.findViewById(R.id.etCantidad);
        TextInputEditText etMin = dialog.findViewById(R.id.etMin);

        CheckBox cbDeshabilitado = dialog.findViewById(R.id.cbDeshabilitado);

        imgPreviewDialog = dialog.findViewById(R.id.imgPreviewProducto);//guarda refenrencia del preview de la img


        imagenProductoUri = null; //reseta la img actual

        //para cancelar o ir para atras
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());


        btnTomarFoto.setOnClickListener(v -> {
            //primero compurebo si tiene permiso la app para usar la camara (si la tiene sigue, sino pide el permiso)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {

                //creamos una rl para la img
                Uri uri = crearUriParaFoto();
                if (uri != null) {
                    imagenProductoUri = uri;//la gaurdamos (variable global de la foto actual)
                    camaraLauncher.launch(uri); //abrimos la camara
                }

            } else {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA); //aqui no lanzamos la camra primero sino el sistema de permisos
            }
        });

        btnSubirFoto.setOnClickListener(v -> galeriaLauncher.launch("image/*")); //seleccionar cualquier tipo de img de la galeria

        btnGuardar.setOnClickListener(v -> {
            //primero limpiamos los errores anteriores
            if (tilNombre != null) tilNombre.setError(null);
            if (tilCantidad != null) tilCantidad.setError(null);
            if (tilMin != null) tilMin.setError(null);

            //segundo leemos los textos actuales introducidos
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            String cantidadTxt = etCantidad.getText() != null ? etCantidad.getText().toString().trim() : "";
            String minTxt = etMin.getText() != null ? etMin.getText().toString().trim() : "";

            boolean hayError = false;

            //validaciones de lo que el usuario a introducido
            if (nombre.isEmpty()) {
                tilNombre.setError("Introduce el nombre del producto");
                hayError = true;
            }

            if (cantidadTxt.isEmpty()) {
                tilCantidad.setError("Introduce la cantidad");
                hayError = true;
            }

            if (imagenProductoUri == null) {
                Toast.makeText(this, "Debes añadir una foto", Toast.LENGTH_SHORT).show();
                hayError = true;
            }

            if (hayError) return;

            int cantidad;
            int cantidadMinTemp = 0;

            try {
                cantidad = Integer.parseInt(cantidadTxt);
            } catch (NumberFormatException e) {
                tilCantidad.setError("La cantidad debe ser numérica");
                return;
            }

            if (!minTxt.isEmpty()) {
                try {
                    cantidadMinTemp = Integer.parseInt(minTxt);
                } catch (NumberFormatException e) {
                    tilMin.setError("La cantidad mínima debe ser numérica");
                    return;
                }
            }

            if (cantidad < 0) {
                tilCantidad.setError("La cantidad no puede ser negativa");
                return;
            }

            if (cantidadMinTemp < 0) {
                tilMin.setError("La cantidad mínima no puede ser negativa");
                return;
            }

            //creamos variables finales para usar en el hilo
            final int cantidadFinal = cantidad;
            final int cantidadMinFinal = cantidadMinTemp;
            final boolean habilitadoFinal = !cbDeshabilitado.isChecked();
            final String imgFinal = imagenProductoUri.toString();
            final String nombreFinal = nombre;

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);

                Producto nuevo = new Producto(nombreFinal, imgFinal, cantidadFinal, cantidadMinFinal, habilitadoFinal);
                db.productoDao().insert(nuevo);

                runOnUiThread(() -> { //volvemos al hilo principal para asi actualizar la interfaz
                    Toast.makeText(this, "Producto añadido correctamente", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); //cerramos el formulario

                    //recargamos la lista de productos
                    if (selectedUser != null) {
                        cargarProductos(selectedUser.getId());
                    }
                });
            }).start(); //aqui ya ejectuamos el hilo
        });

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

    //ventana donde salen la lista de todos los productos, tanto para editar, borrar como deshbilitar
    private void mostrarDialogSeleccionProducto(String titulo, String subtitulo, AccionProducto accion) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Producto> productos = db.productoDao().getAllOrdenado();

            runOnUiThread(() -> {
                if (productos == null || productos.isEmpty()) {
                    Toast.makeText(this, "No hay productos disponibles", Toast.LENGTH_SHORT).show();
                    return;
                }

                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_seleccionar_producto);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }

                TextView tvTitulo = dialog.findViewById(R.id.tvTituloSeleccion);
                TextView tvSubtitulo = dialog.findViewById(R.id.tvSubtituloSeleccion);
                ImageView btnCerrar = dialog.findViewById(R.id.btnCerrarSeleccion);
                RecyclerView rvProductos = dialog.findViewById(R.id.rvSeleccionProductos);

                tvTitulo.setText(titulo);
                tvSubtitulo.setText(subtitulo);

                rvProductos.setLayoutManager(new GridLayoutManager(this, 3));
                rvProductos.setAdapter(new SeleccionProductoAdapter(productos, producto -> {
                    dialog.dismiss();
                    accion.onProductoSeleccionado(producto);
                }));

                btnCerrar.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            });
        }).start();
    }

    private interface AccionProducto {
        void onProductoSeleccionado(Producto producto);
    }

    private void abrirSeleccionEditarProducto() {
        mostrarDialogSeleccionProducto(
                "Seleccionar Producto para Editar",
                "Haz clic en el producto que deseas modificar",
                producto -> mostrarDialogEditarProducto(producto)
        );
    }
    private void abrirSeleccionDeshabilitarProducto() {
        mostrarDialogSeleccionProductoSoloActivos(
                "Seleccionar Producto para Deshabilitar",
                "Haz clic en el producto activo que deseas deshabilitar",
                producto -> mostrarConfirmacionDeshabilitar(producto)
        );
    }
    private void mostrarDialogSeleccionProductoSoloActivos(String titulo, String subtitulo, AccionProducto accion) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            List<Producto> productos = db.productoDao().getProductosActivosOrdenados();

            runOnUiThread(() -> {
                if (productos == null || productos.isEmpty()) {
                    Toast.makeText(this, "No hay productos activos para deshabilitar", Toast.LENGTH_SHORT).show();
                    return;
                }

                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_seleccionar_producto);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }

                TextView tvTitulo = dialog.findViewById(R.id.tvTituloSeleccion);
                TextView tvSubtitulo = dialog.findViewById(R.id.tvSubtituloSeleccion);
                ImageView btnCerrar = dialog.findViewById(R.id.btnCerrarSeleccion);
                RecyclerView rvProductos = dialog.findViewById(R.id.rvSeleccionProductos);

                tvTitulo.setText(titulo);
                tvSubtitulo.setText(subtitulo);

                rvProductos.setLayoutManager(new GridLayoutManager(this, 3));
                rvProductos.setAdapter(new SeleccionProductoAdapter(productos, producto -> {
                    dialog.dismiss();
                    accion.onProductoSeleccionado(producto);
                }));

                btnCerrar.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            });
        }).start();
    }
    private void mostrarConfirmacionDeshabilitar(Producto producto) {
        String mensaje;

        if (producto.isHabilitado()) {
            mensaje = "¿Seguro que quieres deshabilitar el producto \"" + producto.getNombre() + "\"?";
        } else {
            mensaje = "El producto \"" + producto.getNombre() + "\" ya está deshabilitado.";
        }

        if (!producto.isHabilitado()) {
            new AlertDialog.Builder(this)
                    .setTitle("Producto ya deshabilitado")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Deshabilitar producto")
                .setMessage(mensaje)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Deshabilitar", (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(this);

                        producto.setHabilitado(false);
                        db.productoDao().update(producto);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Producto deshabilitado correctamente", Toast.LENGTH_SHORT).show();
                            if (selectedUser != null) {
                                cargarProductos(selectedUser.getId());
                            }
                        });
                    }).start();
                })
                .show();
    }

    private void abrirSeleccionBorrarProducto() {
        mostrarDialogSeleccionProducto(
                "Seleccionar Producto para Borrar",
                "Haz clic en el producto que deseas eliminar",
                producto -> mostrarConfirmacionBorrar(producto)
        );
    }

    private void mostrarConfirmacionBorrar(Producto producto) {
        new AlertDialog.Builder(this)
                .setTitle("Borrar producto")
                .setMessage("¿Seguro que quieres borrar el producto \"" + producto.getNombre() + "\"? Esta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar", (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(this);
                        db.productoDao().delete(producto);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Producto borrado correctamente", Toast.LENGTH_SHORT).show();
                            if (selectedUser != null) {
                                cargarProductos(selectedUser.getId());
                            }
                        });
                    }).start();
                })
                .show();
    }

    private void mostrarDialogEditarProducto(Producto producto) {
        Dialog dialog = new Dialog(this, R.style.DialogAddProducto);
        dialog.setContentView(R.layout.dialog_add_producto);

        ImageView btnCerrar = dialog.findViewById(R.id.btnCerrar);

        com.google.android.material.button.MaterialButton btnTomarFoto = dialog.findViewById(R.id.btnTomarFoto);
        com.google.android.material.button.MaterialButton btnSubirFoto = dialog.findViewById(R.id.btnSubirFoto);
        com.google.android.material.button.MaterialButton btnCancelar = dialog.findViewById(R.id.btnCancelar);
        com.google.android.material.button.MaterialButton btnGuardar = dialog.findViewById(R.id.btnGuardar);

        TextInputLayout tilNombre = dialog.findViewById(R.id.tilNombre);
        TextInputLayout tilCantidad = dialog.findViewById(R.id.tilCantidad);
        TextInputLayout tilMin = dialog.findViewById(R.id.tilMin);

        TextInputEditText etNombre = dialog.findViewById(R.id.etNombre);
        TextInputEditText etCantidad = dialog.findViewById(R.id.etCantidad);
        TextInputEditText etMin = dialog.findViewById(R.id.etMin);

        CheckBox cbDeshabilitado = dialog.findViewById(R.id.cbDeshabilitado);

        imgPreviewDialog = dialog.findViewById(R.id.imgPreviewProducto);

        //cambiamos textos de los botones para el form de edicion
        btnGuardar.setText("Guardar cambios");
        btnCancelar.setText("Cancelar");
        btnTomarFoto.setText("Tomar foto");
        btnSubirFoto.setText("Subir foto");

        //precargar datos del producto
        etNombre.setText(producto.getNombre());
        etCantidad.setText(String.valueOf(producto.getCantidad()));

        if (producto.getCantidadMin() > 0) {
            etMin.setText(String.valueOf(producto.getCantidadMin()));
        } else {
            etMin.setText("");
        }

        cbDeshabilitado.setChecked(!producto.isHabilitado());

        if (producto.getImg() != null && !producto.getImg().isEmpty()) {
            imagenProductoUri = Uri.parse(producto.getImg());
            imgPreviewDialog.setImageURI(imagenProductoUri);
            imgPreviewDialog.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imagenProductoUri = null;
            imgPreviewDialog.setImageResource(android.R.drawable.ic_menu_gallery);
            imgPreviewDialog.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnTomarFoto.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {

                Uri uri = crearUriParaFoto();
                if (uri != null) {
                    imagenProductoUri = uri;
                    camaraLauncher.launch(uri);
                }

            } else {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnSubirFoto.setOnClickListener(v -> galeriaLauncher.launch("image/*"));

        btnGuardar.setOnClickListener(v -> {
            if (tilNombre != null) tilNombre.setError(null);
            if (tilCantidad != null) tilCantidad.setError(null);
            if (tilMin != null) tilMin.setError(null);

            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            String cantidadTxt = etCantidad.getText() != null ? etCantidad.getText().toString().trim() : "";
            String minTxt = etMin.getText() != null ? etMin.getText().toString().trim() : "";

            boolean hayError = false;

            if (nombre.isEmpty()) {
                tilNombre.setError("Introduce el nombre del producto");
                hayError = true;
            }

            if (cantidadTxt.isEmpty()) {
                tilCantidad.setError("Introduce la cantidad");
                hayError = true;
            }

            //en editar la imagen no tiene por que ser nueva
            //solo da error si no existe ni la antigua ni una nueva
            if (imagenProductoUri == null) {
                Toast.makeText(this, "Debes añadir una foto", Toast.LENGTH_SHORT).show();
                hayError = true;
            }

            if (hayError) return;

            int cantidad;
            int cantidadMinTemp = 0;

            try {
                cantidad = Integer.parseInt(cantidadTxt);
            } catch (NumberFormatException e) {
                tilCantidad.setError("La cantidad debe ser numérica");
                return;
            }

            if (!minTxt.isEmpty()) {
                try {
                    cantidadMinTemp = Integer.parseInt(minTxt);
                } catch (NumberFormatException e) {
                    tilMin.setError("La cantidad mínima debe ser numérica");
                    return;
                }
            }

            if (cantidad < 0) {
                tilCantidad.setError("La cantidad no puede ser negativa");
                return;
            }

            if (cantidadMinTemp < 0) {
                tilMin.setError("La cantidad mínima no puede ser negativa");
                return;
            }

            final String nombreFinal = nombre;
            final int cantidadFinal = cantidad;
            final int cantidadMinFinal = cantidadMinTemp;
            final boolean habilitadoFinal = !cbDeshabilitado.isChecked();
            final String imgFinal = imagenProductoUri.toString();

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);

                producto.setNombre(nombreFinal);
                producto.setCantidad(cantidadFinal);
                producto.setCantidadMin(cantidadMinFinal);
                producto.setHabilitado(habilitadoFinal);
                producto.setImg(imgFinal);

                Producto existente = db.productoDao().getByNombreExacto(nombreFinal);
                if (existente != null && existente.getId() != producto.getId()) {
                    runOnUiThread(() -> tilNombre.setError("Ya existe otro producto con ese nombre"));
                    return;
                }

                db.productoDao().update(producto);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Producto modificado correctamente", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    if (selectedUser != null) {
                        cargarProductos(selectedUser.getId());
                    }
                });
            }).start();
        });

        dialog.show();
    }
}