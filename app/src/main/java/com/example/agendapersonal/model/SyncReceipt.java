package com.example.agendapersonal.model;

public class SyncReceipt {

    private String ownerId;
    private String deviceId;
    private String changeId;
    private String receivedAt;

    public SyncReceipt() {
    }

    public SyncReceipt(String ownerId, String deviceId, String changeId) {
        this.ownerId = ownerId;
        this.deviceId = deviceId;
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

    public String getChangeId() {
        return changeId;
    }

    public void setChangeId(String changeId) {
        this.changeId = changeId;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }
}
