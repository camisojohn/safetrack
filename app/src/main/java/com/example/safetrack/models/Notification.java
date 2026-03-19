package com.example.safetrack.models;

import com.google.gson.annotations.SerializedName;

public class Notification {
    public String id;
    public String message;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("is_read")
    public boolean isRead;

    @SerializedName("created_at")
    public String createdAt;

    public Notification() {}

    public Notification(String userId, String message) {
        this.userId  = userId;
        this.message = message;
        this.isRead  = false;
    }
}