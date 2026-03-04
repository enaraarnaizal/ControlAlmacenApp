package com.example.controlalmacen.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "productos")
public class Producto {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private String img;
    private int cantidad;
    private int cantidadMin;
    private boolean habilitado;

    public Producto(String nombre, String img, int cantidad, int cantidadMin, boolean habilitado) {
        this.nombre = nombre;
        this.img = img;
        this.cantidad = cantidad;
        this.cantidadMin = cantidadMin;
        this.habilitado = habilitado;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public int getCantidadMin() { return cantidadMin; }
    public void setCantidadMin(int cantidadMin) { this.cantidadMin = cantidadMin; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}
