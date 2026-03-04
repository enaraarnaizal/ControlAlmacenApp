package com.example.controlalmacen.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.controlalmacen.model.Usuario;

import java.util.List;

//clase que permite hablar con la base de datos
//luego room convierte estos metodos en consultas SQL
@Dao
public interface UsuarioDao {

    //metodo insertar
    @Insert
    long insert(Usuario u);

    //metodo actualizar
    @Update
    void update(Usuario u);

    //devuelve todos los perfiles
    @Query("SELECT * FROM usuarios ORDER BY habilitado DESC, nombre ASC")
    List<Usuario> getAllOrdenado();

    // Para búsquedas por filtro
    @Query("SELECT * FROM usuarios WHERE nombre LIKE :patron ORDER BY habilitado DESC, nombre ASC")
    List<Usuario> buscarPorNombre(String patron);

    //seleccion de perfil para el login
    @Query("SELECT * FROM usuarios WHERE habilitado = 1 ORDER BY nombre ASC")
    List<Usuario> getUsuariosActivos();

    //login/perfil
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    Usuario getById(int id);
}