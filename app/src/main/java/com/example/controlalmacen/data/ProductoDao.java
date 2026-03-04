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
}
