package com.example.agendapersonal.model;

public class Estudio {

    private String id;
    private String ownerId;
    private String fechaEntrega;
    private String hora;
    private String actividad;
    private String materiaId;
    private String institucionId;
    private String prioridad;
    private String estadoEspecial;
    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public Estudio() {
    }

    public Estudio(String actividad, String fechaEntrega, String materiaId,
                   String institucionId) {
        this.actividad = actividad;
        this.fechaEntrega = fechaEntrega;
        this.materiaId = materiaId;
        this.institucionId = institucionId;
        this.prioridad = "MEDIA";
        this.revision = 1;
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

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(String materiaId) {
        this.materiaId = materiaId;
    }

    public String getInstitucionId() {
        return institucionId;
    }

    public void setInstitucionId(String institucionId) {
        this.institucionId = institucionId;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
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
