package com.example.controlalmacen.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.controlalmacen.model.Usuario;
import com.example.controlalmacen.model.Producto;

@Database(entities = {Usuario.class, Producto.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UsuarioDao usuarioDao();
    public abstract ProductoDao productoDao();

    private static final RoomDatabase.Callback roomCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    new Thread(() -> {

                        UsuarioDao usuarioDao = INSTANCE.usuarioDao();
                        ProductoDao productoDao = INSTANCE.productoDao();

                        // Usuarios iniciales
                        usuarioDao.insert(new Usuario("Juan García", null,false,null,null,true));
                        usuarioDao.insert(new Usuario("María López", null,false,null,null,true));
                        usuarioDao.insert(new Usuario("Carlos Ruiz", null,false,null,null,true));
                        usuarioDao.insert(new Usuario("Ana Martín", null,false,null,null,true));
                        usuarioDao.insert(new Usuario("Pedro Sánchez", null,false,null,null,true));

                        usuarioDao.insert(new Usuario("Admin", null,true,"1234","admin@mail.com",true));

                        // Productos iniciales
                        productoDao.insert(new Producto("Tornillos M4", null, 500, 100, true));
                        productoDao.insert(new Producto("Arandelas 10mm", null, 1200, 200, true));
                        productoDao.insert(new Producto("Cajas de cartón", null, 80, 20, true));
                        productoDao.insert(new Producto("Bridas negras 20cm", null, 300, 50, true));
                        productoDao.insert(new Producto("Lubricante industrial", null, 15, 5, true));

                    }).start();
                }
            };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "control_almacen.db"
                            )
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
