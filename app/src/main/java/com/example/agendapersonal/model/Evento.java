package com.example.agendapersonal.model;

public class Evento {
    private String id;
    private String ownerId;
    private String descripcion;
    private String categoria;
    private String fecha;
    private String hora;
    private String personaId;
    private String lugar;
    private String estadoEspecial;
    private boolean alarmaActivada;
    private Integer anticipacionMinutos;
    private String serieId;
    private String observaciones;
    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public Evento() {
    }

    public Evento(String descripcion, String categoria, String fecha) {
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.fecha = fecha;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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

    public String getPersonaId() {
        return personaId;
    }

    public void setPersonaId(String personaId) {
        this.personaId = personaId;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
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

    public String getSerieId() {
        return serieId;
    }

    public void setSerieId(String serieId) {
        this.serieId = serieId;
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
