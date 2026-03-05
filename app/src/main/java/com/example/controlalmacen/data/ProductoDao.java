package com.example.controlalmacen.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.controlalmacen.model.Producto;

import java.util.List;

@Dao
public interface ProductoDao {

    @Insert
    long insert(Producto p);

    @Update
    void update(Producto p);

    @Query("SELECT * FROM productos ORDER BY habilitado DESC, nombre ASC")
    List<Producto> getAllOrdenado();

    @Query("SELECT * FROM productos WHERE nombre LIKE :patron ORDER BY habilitado DESC, nombre ASC")
    List<Producto> buscarPorNombre(String patron);

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    Producto getById(int id);

    // 1) Relleno: productos habilitados EXCEPTO los ya usados (para completar hasta 10)
    @Query("SELECT * FROM productos " +
            "WHERE habilitado = 1 AND id NOT IN (:idsUsados) " +
            "ORDER BY nombre ASC " +
            "LIMIT :limit")
    List<Producto> getRellenoExcluyendo(List<Long> idsUsados, int limit);

    // 2) Si no hay interacciones todavía: primeros 10 por nombre
    @Query("SELECT * FROM productos WHERE habilitado = 1 ORDER BY nombre ASC LIMIT 10")
    List<Producto> getPrimeros10();

    // 3) Resto: todos habilitados EXCEPTO los del top (sin límite)
    @Query("SELECT * FROM productos " +
            "WHERE habilitado = 1 AND id NOT IN (:idsExcluidos) " +
            "ORDER BY nombre ASC")
    List<Producto> getRestoExcluyendoIds(List<Long> idsExcluidos);

    // 4) Si NO hay top (sin interacciones): resto a partir de los primeros 10
    @Query("SELECT * FROM productos WHERE habilitado = 1 ORDER BY nombre ASC")
    List<Producto> getAllHabilitadosOrdenados();
}
