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

    //productos habilitados excepto los ya usados (para completar hasta 10)
    @Query("SELECT * FROM productos " +
            "WHERE habilitado = 1 AND id NOT IN (:idsUsados) " +
            "ORDER BY nombre ASC " +
            "LIMIT :limit")
    List<Producto> getRellenoExcluyendo(List<Long> idsUsados, int limit);

    //Si no hay interacciones todavia ( pilla los primeros 10 por nombre)
    @Query("SELECT * FROM productos WHERE habilitado = 1 ORDER BY nombre ASC LIMIT 10")
    List<Producto> getPrimeros10();

    //el resto, todos habilitados excepto los del top (sin limite)
    @Query("SELECT * FROM productos " +
            "WHERE habilitado = 1 AND id NOT IN (:idsExcluidos) " +
            "ORDER BY nombre ASC")
    List<Producto> getRestoExcluyendoIds(List<Long> idsExcluidos);

    //si no hay top entonces el resto a partir de los primeros 10
    @Query("SELECT * FROM productos WHERE habilitado = 1 ORDER BY nombre ASC")
    List<Producto> getAllHabilitadosOrdenados();
}
