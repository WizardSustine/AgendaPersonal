package com.example.agendapersonal.model;

public class SyncChange {

    private String changeId;
    private String ownerId;
    private String deviceId;

    private String entityType;
    private String entityId;

    private int baseRevision;
    private int entityRevision;

    private String operation;
    private String payload;
    private String createdAt;

    public SyncChange() {
    }

    public SyncChange(String changeId, String ownerId, String deviceId,
                      String entityType, String entityId,
                      int baseRevision, int entityRevision,
                      String operation, String payload) {

        this.changeId = changeId;
        this.ownerId = ownerId;
        this.deviceId = deviceId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.baseRevision = baseRevision;
        this.entityRevision = entityRevision;
        this.operation = operation;
        this.payload = payload;
    }

    public String getChangeId() {
        return changeId;
    }

    public void setChangeId(String changeId) {
        this.changeId = changeId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public int getBaseRevision() {
        return baseRevision;
    }

    public void setBaseRevision(int baseRevision) {
        this.baseRevision = baseRevision;
    }

    public int getEntityRevision() {
        return entityRevision;
    }

    public void setEntityRevision(int entityRevision) {
        this.entityRevision = entityRevision;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
