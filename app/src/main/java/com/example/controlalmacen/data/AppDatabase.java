package com.example.controlalmacen.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.controlalmacen.model.Usuario;

@Database(entities = {Usuario.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UsuarioDao usuarioDao();

    private static final RoomDatabase.Callback roomCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    new Thread(() -> {

                        UsuarioDao dao = INSTANCE.usuarioDao();

                        dao.insert(new Usuario("Juan García", null,false,null,null,true));
                        dao.insert(new Usuario("María López", null,false,null,null,true));
                        dao.insert(new Usuario("Carlos Ruiz", null,false,null,null,true));
                        dao.insert(new Usuario("Ana Martín", null,false,null,null,true));
                        dao.insert(new Usuario("Pedro Sánchez", null,false,null,null,true));

                        dao.insert(new Usuario("Admin", null,true,"1234","admin@mail.com",true));

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