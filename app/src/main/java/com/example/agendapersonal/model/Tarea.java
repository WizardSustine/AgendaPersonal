package com.example.agendapersonal.model;

public class Tarea {

    private String id;
    private String ownerId;
    private String descripcion;
    private String fechaInicio;
    private String horaInicio;
    private String fechaVencimiento;
    private String horaVencimiento;
    private String categoria;
    private String prioridad;
    private String estadoEspecial;
    private boolean alarmaActivada;
    private Integer anticipacionMinutos;
    private String observaciones;
    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public Tarea() {
    }

    public Tarea(String descripcion, String fechaVencimiento,
                 String categoria) {
        this.descripcion = descripcion;
        this.fechaVencimiento = fechaVencimiento;
        this.categoria = categoria;
        this.prioridad = "MEDIA";
        this.alarmaActivada = false;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getHoraVencimiento() {
        return horaVencimiento;
    }

    public void setHoraVencimiento(String horaVencimiento) {
        this.horaVencimiento = horaVencimiento;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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

    public boolean isAlarmaActivada() {
        return alarmaActivada;
    }

    public void setAlarmaActivada(boolean alarmaActivada) {
        this.alarmaActivada = alarmaActivada;
    }

    public Integer getAnticipacionMinutos() {
        return anticipacionMinutos;
    }

    public void setAnticipacionMinutos(Integer anticipacionMinutos) {
        this.anticipacionMinutos = anticipacionMinutos;
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
