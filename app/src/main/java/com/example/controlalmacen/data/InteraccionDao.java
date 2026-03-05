package com.example.controlalmacen.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.controlalmacen.model.Interaccion;
import com.example.controlalmacen.model.Producto;

import java.util.List;

@Dao
public interface InteraccionDao {

    @Insert
    long insert(Interaccion i);

    //Devuelve los PRODUCTOS más usados recientemente (10) por usuario (por fecha)
    @Query(
            "SELECT p.* FROM productos p " +
                    "JOIN (" +
                    "   SELECT productoId, MAX(fechaMs) AS lastFecha " +
                    "   FROM interacciones " +
                    "   WHERE usuarioId = :usuarioId " +
                    "   GROUP BY productoId " +
                    "   ORDER BY lastFecha DESC " +
                    "   LIMIT 10" +
                    ") t ON p.id = t.productoId " +
                    "WHERE p.habilitado = 1 " +
                    "ORDER BY t.lastFecha DESC"
    )
    List<Producto> getTop10Recientes(long usuarioId);


    @Query(
            "SELECT productoId FROM (" +
                    "   SELECT productoId, MAX(fechaMs) AS lastFecha " +
                    "   FROM interacciones " +
                    "   WHERE usuarioId = :usuarioId " +
                    "   GROUP BY productoId " +
                    "   ORDER BY lastFecha DESC " +
                    "   LIMIT 10" +
                    ")"
    )
    List<Long> getTop10Ids(long usuarioId);
}