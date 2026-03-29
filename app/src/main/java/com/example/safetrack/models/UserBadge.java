package com.example.safetrack.models;

import com.google.gson.annotations.SerializedName;

public class UserBadge {
    public String id;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("badge_name")
    public String badgeName;

    @SerializedName("earned_at")
    public String earnedAt;

    public UserBadge() {}

    public UserBadge(String userId, String badgeName) {
        this.userId = userId;
        this.badgeName = badgeName;
    }

    public static String getBadgeEmoji(String badgeName) {
        if (badgeName == null) return "";
        switch (badgeName) {
            case "first_report": return "🌟";
            case "vigilant_5": return "👁️";
            case "sentinel": return "🛡️";
            case "guardian": return "🏅";
            case "elite_reporter": return "💎";
            case "community_hero": return "🦸";
            default: return "⭐";
        }
    }

    public static String getBadgeTitle(String badgeName) {
        if (badgeName == null) return "";
        switch (badgeName) {
            case "first_report": return "First Report";
            case "vigilant_5": return "Vigilant Observer";
            case "sentinel": return "Community Sentinel";
            case "guardian": return "Guardian";
            case "elite_reporter": return "Elite Reporter";
            case "community_hero": return "Community Hero";
            default: return badgeName;
        }
    }
}
