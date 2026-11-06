package com.example.agendapersonal.model;

public class SyncDevice {

    private String deviceId;
    private String ownerId;
    private String nombre;
    private String createdAt;
    private String lastSeenAt;

    public SyncDevice() {
    }

    public SyncDevice(String deviceId, String ownerId, String nombre) {
        this.deviceId = deviceId;
        this.ownerId = ownerId;
        this.nombre = nombre;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(String lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
