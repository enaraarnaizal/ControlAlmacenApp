package com.example.controlalmacen.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "albaranes")
public class Albaran {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long usuarioId;
    private long fechaMs;           // System.currentTimeMillis() - fecha de creación
    private String cif;             // CIF del albarán
    private String nombre;          // Nombre del albarán
    private double importe;         // Importe del albarán
    private boolean pagado;         // Si está pagado o no
    private Long fechaPagoMs;       // Fecha de pago (puede ser null)
    private String imagenUri;       // URI de la imagen del albarán

    public Albaran(long usuarioId, long fechaMs, String cif, String nombre, double importe,
                   boolean pagado, Long fechaPagoMs, String imagenUri) {
        this.usuarioId = usuarioId;
        this.fechaMs = fechaMs;
        this.cif = cif;
        this.nombre = nombre;
        this.importe = importe;
        this.pagado = pagado;
        this.fechaPagoMs = fechaPagoMs;
        this.imagenUri = imagenUri;
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long usuarioId) { this.usuarioId = usuarioId; }

    public long getFechaMs() { return fechaMs; }
    public void setFechaMs(long fechaMs) { this.fechaMs = fechaMs; }

    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }

    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }

    public Long getFechaPagoMs() { return fechaPagoMs; }
    public void setFechaPagoMs(Long fechaPagoMs) { this.fechaPagoMs = fechaPagoMs; }

    public String getImagenUri() { return imagenUri; }
    public void setImagenUri(String imagenUri) { this.imagenUri = imagenUri; }
}

