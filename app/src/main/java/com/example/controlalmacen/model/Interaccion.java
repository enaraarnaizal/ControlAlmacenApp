package com.example.controlalmacen.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "interacciones")
public class Interaccion {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long usuarioId;
    private long productoId;
    private long fechaMs;   // System.currentTimeMillis()
    private String tipo;    // "ADD" o "REMOVE"

    public Interaccion(long usuarioId, long productoId, long fechaMs, String tipo) {
        this.usuarioId = usuarioId;
        this.productoId = productoId;
        this.fechaMs = fechaMs;
        this.tipo = tipo;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUsuarioId() { return usuarioId; }
    public long getProductoId() { return productoId; }
    public long getFechaMs() { return fechaMs; }
    public String getTipo() { return tipo; }
}
