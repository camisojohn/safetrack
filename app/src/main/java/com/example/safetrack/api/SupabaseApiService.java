package com.example.safetrack.api;

import com.example.safetrack.models.ArchivedIncident;
import com.example.safetrack.models.Incident;
import com.example.safetrack.models.IncidentReport;
import com.example.safetrack.models.Notification;
import com.example.safetrack.models.StatusUpdate;
import com.example.safetrack.models.User;
import com.example.safetrack.models.UserBadge;
import com.example.safetrack.models.UserViolation;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApiService {

    // ===== USERS =====
    @GET("rest/v1/users")
    Call<List<User>> getUsers(
            @Query("select") String select,
            @Query("username") String username
    );

    @GET("rest/v1/users")
    Call<List<User>> getAllUsers(
            @Query("select") String select
    );

    @POST("rest/v1/users")
    Call<List<User>> insertUser(
            @Body User user
    );

    @PATCH("rest/v1/users")
    Call<List<User>> updateUser(
            @Query("id") String id,
            @Body Map<String, Object> updates
    );

    // ===== INCIDENTS =====
    @GET("rest/v1/incidents")
    Call<List<Incident>> getIncidents(
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/incidents")
    Call<List<Incident>> getIncidentsByReporter(
            @Query("select") String select,
            @Query("reporter_id") String reporterId,
            @Query("order") String order
    );

    @POST("rest/v1/incidents")
    Call<List<Incident>> insertIncident(
            @Body Incident incident
    );

    @PATCH("rest/v1/incidents")
    Call<List<Incident>> updateIncident(
            @Query("id") String id,
            @Body Incident incident
    );

    @PATCH("rest/v1/incidents")
    Call<List<Incident>> updateStatus(
            @Query("id") String id,
            @Body StatusUpdate status
    );

    @DELETE("rest/v1/incidents")
    Call<Void> deleteIncident(
            @Query("id") String id
    );

    @DELETE("rest/v1/incidents")
    Call<Void> deleteIncidentsByReporter(
            @Query("reporter_id") String reporterId
    );

    // ===== NOTIFICATIONS =====
    @POST("rest/v1/notifications")
    Call<List<Notification>> insertNotification(
            @Body Notification notification
    );

    @GET("rest/v1/notifications")
    Call<List<Notification>> getNotifications(
            @Query("select") String select,
            @Query("user_id") String userId,
            @Query("order") String order
    );

    @PATCH("rest/v1/notifications")
    Call<List<Notification>> markNotificationRead(
            @Query("user_id") String userId,
            @Body Map<String, Object> updates
    );

    // ===== INCIDENT UPVOTES =====
    @POST("rest/v1/incident_upvotes")
    Call<List<Map<String, Object>>> insertUpvote(
            @Body Map<String, Object> upvote
    );

    @GET("rest/v1/incident_upvotes")
    Call<List<Map<String, Object>>> checkUpvote(
            @Query("select") String select,
            @Query("incident_id") String incidentId,
            @Query("user_id") String userId
    );

    // ===== INCIDENT REPORTS (USER REPORTING SYSTEM) =====
    @POST("rest/v1/incident_reports")
    Call<List<IncidentReport>> insertIncidentReport(
            @Body IncidentReport report
    );

    @GET("rest/v1/incident_reports")
    Call<List<IncidentReport>> getIncidentReports(
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/incident_reports")
    Call<List<IncidentReport>> getIncidentReportsByIncident(
            @Query("select") String select,
            @Query("incident_id") String incidentId
    );

    @PATCH("rest/v1/incident_reports")
    Call<List<IncidentReport>> updateIncidentReport(
            @Query("id") String id,
            @Body Map<String, Object> updates
    );

    // ===== USER VIOLATIONS =====
    @POST("rest/v1/user_violations")
    Call<List<UserViolation>> insertViolation(
            @Body UserViolation violation
    );

    @GET("rest/v1/user_violations")
    Call<List<UserViolation>> getUserViolations(
            @Query("select") String select,
            @Query("user_id") String userId,
            @Query("order") String order
    );

    // ===== ARCHIVED INCIDENTS =====
    @POST("rest/v1/archived_incidents")
    Call<List<ArchivedIncident>> archiveIncident(
            @Body ArchivedIncident archived
    );

    @GET("rest/v1/archived_incidents")
    Call<List<ArchivedIncident>> getArchivedIncidents(
            @Query("select") String select,
            @Query("order") String order
    );

    @DELETE("rest/v1/archived_incidents")
    Call<Void> deleteArchivedIncident(
            @Query("id") String id
    );

    // ===== USER BADGES =====
    @POST("rest/v1/user_badges")
    Call<List<UserBadge>> insertBadge(
            @Body UserBadge badge
    );

    @GET("rest/v1/user_badges")
    Call<List<UserBadge>> getUserBadges(
            @Query("select") String select,
            @Query("user_id") String userId
    );

    @GET("rest/v1/incidents")
    Call<List<Incident>> searchIncidents(
            @Query("select") String select,
            @Query("title") String titleFilter,
            @Query("type") String typeFilter,
            @Query("status") String statusFilter,
            @Query("archived") String archivedFilter,
            @Query("order") String order
    );
}