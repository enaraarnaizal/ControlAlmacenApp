package com.example.controlalmacen.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.controlalmacen.model.Albaran;
import com.example.controlalmacen.model.Interaccion;
import com.example.controlalmacen.model.Usuario;
import com.example.controlalmacen.model.Producto;

@Database(entities = {Usuario.class, Producto.class,  Interaccion.class, Albaran.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UsuarioDao usuarioDao();
    public abstract ProductoDao productoDao();
    public abstract InteraccionDao interaccionDao();
    public abstract AlbaranDao albaranDao();


    private static final RoomDatabase.Callback roomCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    new Thread(() -> {

                        UsuarioDao usuarioDao = INSTANCE.usuarioDao();
                        ProductoDao productoDao = INSTANCE.productoDao();
                        AlbaranDao albaranDao = INSTANCE.albaranDao();

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

                        // Albaranes de ejemplo (diferentes fechas)
                        long ahora = System.currentTimeMillis();
                        long unDia = 24 * 60 * 60 * 1000L;
                        String imagenDefault = "android.resource://com.example.controlalmacen/drawable/ic_albaran_default";

                        // Albaranes del mes actual
                        albaranDao.insert(new Albaran(1, ahora, "B12345678", "Tornillos SA", 1250.50, true, ahora - (1 * unDia), imagenDefault));
                        albaranDao.insert(new Albaran(1, ahora - (2 * unDia), "A98765432", "Construcción ABC SL", 3500.00, false, null, imagenDefault));
                        albaranDao.insert(new Albaran(2, ahora - (5 * unDia), "C11223344", "Ferretería López", 850.75, true, ahora - (3 * unDia), imagenDefault));
                        albaranDao.insert(new Albaran(1, ahora - (7 * unDia), "B55667788", "Materiales García", 2100.00, true, ahora - (6 * unDia), imagenDefault));

                        // Albaranes de semanas anteriores
                        albaranDao.insert(new Albaran(3, ahora - (10 * unDia), "A33445566", "Suministros Pérez", 1500.25, false, null, imagenDefault));
                        albaranDao.insert(new Albaran(2, ahora - (15 * unDia), "B77889900", "Herramientas Pro", 950.00, true, ahora - (12 * unDia), imagenDefault));
                        albaranDao.insert(new Albaran(1, ahora - (20 * unDia), "C22334455", "Distribuciones Norte", 4200.50, false, null, imagenDefault));
                        albaranDao.insert(new Albaran(1, ahora - (25 * unDia), "A66778899", "Lubricantes Industrial", 675.30, true, ahora - (24 * unDia), imagenDefault));

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
                            .fallbackToDestructiveMigration()
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
