package com.example.agendapersonal.model;

public class ObservacionRI {

    private String id;
    private String ownerId;

    private String registroInvestigacionId;
    private String observacion;

    private String createdAt;
    private String updatedAt;
    private int revision;
    private String deletedAt;

    public ObservacionRI() {
        this.revision = 1;
    }

    public ObservacionRI(String registroInvestigacionId, String observacion) {
        this();
        this.registroInvestigacionId = registroInvestigacionId;
        this.observacion = observacion;
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

    public String getRegistroInvestigacionId() {
        return registroInvestigacionId;
    }

    public void setRegistroInvestigacionId(String registroInvestigacionId) {
        this.registroInvestigacionId = registroInvestigacionId;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
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
