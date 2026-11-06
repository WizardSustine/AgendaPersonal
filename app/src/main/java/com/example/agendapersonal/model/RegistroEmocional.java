package com.example.agendapersonal.model;

public class RegistroEmocional {

    private String id;
    private String ownerId;

    private String fechaHora;
    private String valoracion;
    private String motivoId;
    private String accionEventoAsociado;
    private String observaciones;

    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public RegistroEmocional() {
        this.revision = 1;
    }

    public RegistroEmocional(String fechaHora, String valoracion,
                             String motivoId) {
        this();
        this.fechaHora = fechaHora;
        this.valoracion = valoracion;
        this.motivoId = motivoId;
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

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getValoracion() {
        return valoracion;
    }

    public void setValoracion(String valoracion) {
        this.valoracion = valoracion;
    }

    public String getMotivoId() {
        return motivoId;
    }

    public void setMotivoId(String motivoId) {
        this.motivoId = motivoId;
    }

    public String getAccionEventoAsociado() {
        return accionEventoAsociado;
    }

    public void setAccionEventoAsociado(String accionEventoAsociado) {
        this.accionEventoAsociado = accionEventoAsociado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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