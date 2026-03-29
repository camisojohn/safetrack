package com.example.safetrack.models;

import com.google.gson.annotations.SerializedName;

public class IncidentReport {
    public String id;

    @SerializedName("incident_id")
    public String incidentId;

    @SerializedName("reporter_id")
    public String reporterId;

    @SerializedName("reported_user_id")
    public String reportedUserId;

    public String reason;

    @SerializedName("proof_text")
    public String proofText;

    @SerializedName("is_valid")
    public Boolean isValid;

    @SerializedName("admin_note")
    public String adminNote;

    @SerializedName("violation_applied")
    public boolean violationApplied;

    @SerializedName("created_at")
    public String createdAt;

    public IncidentReport() {}

    public IncidentReport(String incidentId, String reporterId,
                          String reportedUserId, String reason, String proofText) {
        this.incidentId = incidentId;
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.proofText = proofText;
        this.isValid = null;
        this.adminNote = "";
        this.violationApplied = false;
    }
}
