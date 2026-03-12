package com.example.controlalmacen.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.controlalmacen.model.Interaccion;
import com.example.controlalmacen.model.Usuario;
import com.example.controlalmacen.model.Producto;

@Database(entities = {Usuario.class, Producto.class,  Interaccion.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UsuarioDao usuarioDao();
    public abstract ProductoDao productoDao();
    public abstract InteraccionDao interaccionDao();


    private static final RoomDatabase.Callback roomCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    new Thread(() -> {

                        UsuarioDao usuarioDao = INSTANCE.usuarioDao();
                        ProductoDao productoDao = INSTANCE.productoDao();

                        // Usuarioss iniciales
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

                        productoDao.insert(new Producto("Tornillos M6", null, 400, 80, true));
                        productoDao.insert(new Producto("Arandelas 8mm", null, 900, 150, true));
                        productoDao.insert(new Producto("Tuercas M4", null, 600, 120, true));
                        productoDao.insert(new Producto("Tuercas M6", null, 450, 100, true));
                        productoDao.insert(new Producto("Cinta aislante negra", null, 120, 30, true));

                        productoDao.insert(new Producto("Cinta aislante roja", null, 110, 25, true));
                        productoDao.insert(new Producto("Cinta aislante azul", null, 100, 25, true));
                        productoDao.insert(new Producto("Guantes de seguridad", null, 60, 20, true));
                        productoDao.insert(new Producto("Casco de obra", null, 40, 10, true));
                        productoDao.insert(new Producto("Gafas de protección", null, 70, 15, true));

                        productoDao.insert(new Producto("Brocas 6mm", null, 90, 20, true));
                        productoDao.insert(new Producto("Brocas 8mm", null, 85, 20, true));
                        productoDao.insert(new Producto("Brocas 10mm", null, 75, 15, true));

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
