package com.example.safetrack.models;

import com.google.gson.annotations.SerializedName;

public class ArchivedIncident {
    public String id;

    @SerializedName("original_id")
    public String originalId;

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

    @SerializedName("deleted_by")
    public String deletedBy;

    @SerializedName("deleted_at")
    public String deletedAt;

    @SerializedName("original_created_at")
    public String originalCreatedAt;

    public ArchivedIncident() {}

    public ArchivedIncident(Incident incident, String deletedBy) {
        this.originalId = incident.id;
        this.title = incident.title;
        this.type = incident.type;
        this.description = incident.description;
        this.location = incident.location;
        this.status = incident.status;
        this.upvotes = incident.upvotes;
        this.imageBase64 = incident.imageBase64;
        this.reporterId = incident.reporterId;
        this.reporterName = incident.reporterName;
        this.isAnonymous = incident.isAnonymous;
        this.deletedBy = deletedBy;
        this.originalCreatedAt = incident.createdAt;
    }
}
