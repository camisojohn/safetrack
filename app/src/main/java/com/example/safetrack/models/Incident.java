package com.example.safetrack.models;

import com.google.gson.annotations.SerializedName;

public class Incident {
    public String id;
    public String title;
    public String type;
    public String description;
    public String location;
    public String status;
    public int upvotes;

    @SerializedName("image_base64")
    public String imageBase64;

    @SerializedName("reporter_id")
    public String reporterId;

    @SerializedName("reporter_name")
    public String reporterName;

    @SerializedName("is_anonymous")
    public boolean isAnonymous;

    @SerializedName("created_at")
    public String createdAt;

    public Incident() {}

    public Incident(String title, String type,
                    String description, String location,
                    String imageBase64, String reporterId,
                    String reporterName, boolean isAnonymous) {
        this.title        = title;
        this.type         = type;
        this.description  = description;
        this.location     = location;
        this.imageBase64  = imageBase64 != null ? imageBase64 : "";
        this.reporterId   = reporterId;
        this.reporterName = reporterName;
        this.isAnonymous  = isAnonymous;
        this.status       = "Pending";
        this.upvotes      = 0;
    }
}