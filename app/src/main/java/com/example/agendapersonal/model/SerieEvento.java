package com.example.agendapersonal.model;

public class SerieEvento {

    private String id;
    private String ownerId;

    private String frecuencia;
    private String configuracionPersonalizada;
    private String fechaFin;

    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public SerieEvento() {
        this.revision = 1;
    }

    public SerieEvento(String frecuencia) {
        this();
        this.frecuencia = frecuencia;
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

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getConfiguracionPersonalizada() {
        return configuracionPersonalizada;
    }

    public void setConfiguracionPersonalizada(String configuracionPersonalizada) {
        this.configuracionPersonalizada = configuracionPersonalizada;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
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