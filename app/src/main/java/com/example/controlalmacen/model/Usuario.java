package com.example.controlalmacen.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios") //tabla real en la bse de datos
public class Usuario {

    //clave primaria
    @PrimaryKey(autoGenerate = true) //se autogenera automaticamente
    private int id;

    private String nombre;
    private String fotoUri;        // guardamos la URI/ruta de la foto
    private boolean administrador;
    private String password;
    private String email;
    private boolean habilitado;

    private String passwordCorreo;

    public Usuario(String nombre, String fotoUri, boolean administrador, String password, String email, boolean habilitado, String passwordCorreo) {
        this.nombre = nombre;
        this.fotoUri = fotoUri;
        this.administrador = administrador;
        this.password = password;
        this.email = email;
        this.habilitado = habilitado;
        this.passwordCorreo = passwordCorreo;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFotoUri() { return fotoUri; }
    public void setFotoUri(String fotoUri) { this.fotoUri = fotoUri; }

    public boolean isAdministrador() { return administrador; }
    public void setAdministrador(boolean administrador) { this.administrador = administrador; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

    public String getPasswordCorreo() {
        return passwordCorreo;
    }

    public void setPasswordCorreo(String passwordCorreo) {
        this.passwordCorreo = passwordCorreo;
    }
}