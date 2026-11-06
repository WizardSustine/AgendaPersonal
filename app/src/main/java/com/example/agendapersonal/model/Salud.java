package com.example.agendapersonal.model;

public class Salud {

    private String id;
    private String ownerId;

    private String fecha;
    private String hora;
    private String actividad;

    private String especialidadId;
    private String institucionId;

    private String prioridad;
    private String estadoEspecial;

    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public Salud() {
        this.prioridad = "MEDIA";
        this.revision = 1;
    }

    public Salud(String actividad, String fecha, String especialidadId) {
        this();
        this.actividad = actividad;
        this.fecha = fecha;
        this.especialidadId = especialidadId;
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

    public String getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(String especialidadId) {
        this.especialidadId = especialidadId;
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