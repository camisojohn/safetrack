package com.example.safetrack.models;

import com.google.gson.annotations.SerializedName;

public class User {
    public String id;
    public String username;
    public String mobile;
    public String password;
    public String role;
    public String level;

    @SerializedName("report_count")
    public int reportCount;

    @SerializedName("is_blocked")
    public boolean isBlocked;

    @SerializedName("created_at")
    public String createdAt;

    public User() {}

    public User(String username, String mobile,
                String password) {
        this.username    = username;
        this.mobile      = mobile;
        this.password    = password;
        this.role        = "user";
        this.reportCount = 0;
        this.level       = "Observer";
        this.isBlocked   = false;
    }
}