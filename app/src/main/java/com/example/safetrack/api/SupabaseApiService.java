package com.example.safetrack.api;

import com.example.safetrack.models.Incident;
import com.example.safetrack.models.Notification;
import com.example.safetrack.models.StatusUpdate;
import com.example.safetrack.models.User;
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
}