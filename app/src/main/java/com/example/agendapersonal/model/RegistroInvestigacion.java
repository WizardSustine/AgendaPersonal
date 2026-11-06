package com.example.agendapersonal.model;

public class RegistroInvestigacion {

    private String id;
    private String ownerId;

    private String idOriginal;
    private String datosOriginales;

    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public RegistroInvestigacion() {
        this.datosOriginales = "{}";
        this.revision = 1;
    }

    public RegistroInvestigacion(String idOriginal, String datosOriginales) {
        this();
        this.idOriginal = idOriginal;
        this.datosOriginales = datosOriginales;
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

    public String getIdOriginal() {
        return idOriginal;
    }

    public void setIdOriginal(String idOriginal) {
        this.idOriginal = idOriginal;
    }

    public String getDatosOriginales() {
        return datosOriginales;
    }

    public void setDatosOriginales(String datosOriginales) {
        this.datosOriginales = datosOriginales;
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
