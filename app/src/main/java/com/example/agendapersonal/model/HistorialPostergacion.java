package com.example.agendapersonal.model;

public class HistorialPostergacion {

    private String id;
    private String ownerId;

    private String tipoEntidad;
    private String entidadId;
    private String fechaOriginal;
    private String nuevaFecha;
    private String fechaPostergacion;

    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public HistorialPostergacion() {
        this.revision = 1;
    }

    public HistorialPostergacion(String tipoEntidad, String entidadId,
                                 String fechaOriginal) {
        this();
        this.tipoEntidad = tipoEntidad;
        this.entidadId = entidadId;
        this.fechaOriginal = fechaOriginal;
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

    public String getTipoEntidad() {
        return tipoEntidad;
    }

    public void setTipoEntidad(String tipoEntidad) {
        this.tipoEntidad = tipoEntidad;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(String entidadId) {
        this.entidadId = entidadId;
    }

    public String getFechaOriginal() {
        return fechaOriginal;
    }

    public void setFechaOriginal(String fechaOriginal) {
        this.fechaOriginal = fechaOriginal;
    }

    public String getNuevaFecha() {
        return nuevaFecha;
    }

    public void setNuevaFecha(String nuevaFecha) {
        this.nuevaFecha = nuevaFecha;
    }

    public String getFechaPostergacion() {
        return fechaPostergacion;
    }

    public void setFechaPostergacion(String fechaPostergacion) {
        this.fechaPostergacion = fechaPostergacion;
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
