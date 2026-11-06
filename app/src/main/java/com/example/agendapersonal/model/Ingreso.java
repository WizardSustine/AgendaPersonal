package com.example.agendapersonal.model;

public class Ingreso {

    private String id;
    private String ownerId;

    private String fecha;
    private String descripcion;
    private String clasificacionId;
    private Double importe;
    private String estadoEspecial;

    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public Ingreso() {
        this.revision = 1;
    }

    public Ingreso(String fecha, String descripcion, Double importe) {
        this();
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.importe = importe;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getClasificacionId() {
        return clasificacionId;
    }

    public void setClasificacionId(String clasificacionId) {
        this.clasificacionId = clasificacionId;
    }

    public Double getImporte() {
        return importe;
    }

    public void setImporte(Double importe) {
        this.importe = importe;
    }

    public String getEstadoEspecial() {
        return estadoEspecial;
    }

    public void setEstadoEspecial(String estadoEspecial) {
        this.estadoEspecial = estadoEspecial;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }
}