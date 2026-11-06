package com.example.agendapersonal.model;

public class MotivoEmocional {

    private String id;
    private String ownerId;

    private String nombre;
    private String ambitoId;

    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public MotivoEmocional() {
        this.revision = 1;
    }

    public MotivoEmocional(String nombre, String ambitoId) {
        this();
        this.nombre = nombre;
        this.ambitoId = ambitoId;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAmbitoId() {
        return ambitoId;
    }

    public void setAmbitoId(String ambitoId) {
        this.ambitoId = ambitoId;
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