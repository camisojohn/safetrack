package com.example.safetrack.models;

import com.google.gson.annotations.SerializedName;

public class Notification {
    public String id;
    public String message;

    @SerializedName("user_id")
    public String userId;

    public String type;

    @SerializedName("is_read")
    public boolean isRead;

    @SerializedName("created_at")
    public String createdAt;

    public Notification() {}

    public Notification(String userId, String message, String type) {
        this.userId  = userId;
        this.message = message;
        this.type    = type != null ? type : "info";
        this.isRead  = false;
    }

    public Notification(String userId, String message) {
        this(userId, message, "info");
    }
}