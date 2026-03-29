package com.example.safetrack.models;

import com.google.gson.annotations.SerializedName;

public class UserViolation {
    public String id;

    @SerializedName("user_id")
    public String userId;

    public String reason;

    @SerializedName("strike_number")
    public int strikeNumber;

    @SerializedName("given_by")
    public String givenBy;

    @SerializedName("created_at")
    public String createdAt;

    public UserViolation() {}

    public UserViolation(String userId, String reason, int strikeNumber, String givenBy) {
        this.userId = userId;
        this.reason = reason;
        this.strikeNumber = strikeNumber;
        this.givenBy = givenBy;
    }
}
