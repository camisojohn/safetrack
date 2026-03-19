package com.example.safetrack;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.Incident;
import com.example.safetrack.models.Notification;
import com.example.safetrack.models.StatusUpdate;
import com.example.safetrack.models.User;
import com.example.safetrack.utils.SessionManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidentDetailActivity extends AppCompatActivity {

    ImageView imgDetail;
    TextView tvDetailType, tvDetailTitle,
            tvDetailDescription, tvDetailLocation,
            tvDetailReporter, tvDetailTimestamp,
            tvDetailStatus;
    Button btnDetailUpvote, btnMarkResolved,
            btnMarkPending, btnDetailBack;

    SessionManager session;
    String incidentId, reporterId,
            incidentTitle, currentStatus;
    int currentUpvotes;
    boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_detail);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        session = new SessionManager(this);
        isAdmin = session.isAdmin();

        imgDetail           = findViewById(R.id.imgDetail);
        tvDetailType        = findViewById(R.id.tvDetailType);
        tvDetailTitle       = findViewById(R.id.tvDetailTitle);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailLocation    = findViewById(R.id.tvDetailLocation);
        tvDetailReporter    = findViewById(R.id.tvDetailReporter);
        tvDetailTimestamp   = findViewById(R.id.tvDetailTimestamp);
        tvDetailStatus      = findViewById(R.id.tvDetailStatus);
        btnDetailUpvote     = findViewById(R.id.btnDetailUpvote);
        btnMarkResolved     = findViewById(R.id.btnMarkResolved);
        btnMarkPending      = findViewById(R.id.btnMarkPending);
        btnDetailBack       = findViewById(R.id.btnDetailBack);

        incidentId     = getIntent().getStringExtra("id");
        incidentTitle  = getIntent().getStringExtra("title");
        reporterId     = getIntent().getStringExtra("reporter_id");
        currentStatus  = getIntent().getStringExtra("status");
        currentUpvotes = getIntent().getIntExtra("upvotes", 0);
        boolean isAnonymous = getIntent().getBooleanExtra("is_anonymous", false);
        String imageBase64  = getIntent().getStringExtra("image_base64");

        tvDetailType.setText(getIntent().getStringExtra("type"));
        tvDetailTitle.setText(incidentTitle);
        tvDetailDescription.setText(getIntent().getStringExtra("description"));
        tvDetailLocation.setText("📍 " + getIntent().getStringExtra("location"));
        tvDetailReporter.setText(
                isAnonymous ? "Anonymous"
                        : getIntent().getStringExtra("reporter_name"));
        tvDetailStatus.setText(currentStatus);

        String timestamp = getIntent().getStringExtra("created_at");
        if (timestamp != null && timestamp.length() >= 10) {
            tvDetailTimestamp.setText(timestamp.substring(0, 10));
        }

        setStatusColor(currentStatus);

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            imgDetail.setVisibility(View.VISIBLE);
            byte[] bytes = android.util.Base64.decode(
                    imageBase64, android.util.Base64.DEFAULT);
            Glide.with(this).load(bytes).into(imgDetail);
        }

        if (isAdmin) {
            btnMarkResolved.setVisibility(
                    "Resolved".equals(currentStatus) ? View.GONE : View.VISIBLE);
            btnMarkPending.setVisibility(
                    "Pending".equals(currentStatus) ? View.GONE : View.VISIBLE);
        } else {
            btnDetailUpvote.setVisibility(View.VISIBLE);
            btnDetailUpvote.setText("▲ Upvote (" + currentUpvotes + ")");
        }

        btnDetailBack.setOnClickListener(v -> finish());
        btnDetailUpvote.setOnClickListener(v -> upvoteIncident());
        btnMarkResolved.setOnClickListener(v -> updateStatus("Resolved"));
        btnMarkPending.setOnClickListener(v -> updateStatus("Pending"));
    }

    private void setStatusColor(String status) {
        if (status == null) return;
        switch (status) {
            case "Pending":
                tvDetailStatus.setBackgroundColor(Color.parseColor("#FFA500"));
                break;
            case "Resolved":
                tvDetailStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
            case "Reviewed":
                tvDetailStatus.setBackgroundColor(Color.parseColor("#2196F3"));
                break;
            case "In Progress":
                tvDetailStatus.setBackgroundColor(Color.parseColor("#9C27B0"));
                break;
        }
    }

    private void upvoteIncident() {
        Incident update = new Incident();
        update.upvotes = currentUpvotes + 1;

        RetrofitClient.getService()
                .updateIncident("eq." + incidentId, update)
                .enqueue(new Callback<List<Incident>>() {
                    @Override
                    public void onResponse(Call<List<Incident>> call,
                                           Response<List<Incident>> response) {
                        if (response.isSuccessful()) {
                            currentUpvotes++;
                            btnDetailUpvote.setText(
                                    "▲ Upvote (" + currentUpvotes + ")");
                            Toast.makeText(IncidentDetailActivity.this,
                                    "Upvoted! ✅", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Incident>> call,
                                          Throwable t) {}
                });
    }

    private void updateStatus(String newStatus) {
        RetrofitClient.getService()
                .updateStatus("eq." + incidentId, new StatusUpdate(newStatus))
                .enqueue(new Callback<List<Incident>>() {
                    @Override
                    public void onResponse(Call<List<Incident>> call,
                                           Response<List<Incident>> response) {
                        if (response.isSuccessful()) {
                            currentStatus = newStatus;
                            tvDetailStatus.setText(newStatus);
                            setStatusColor(newStatus);

                            btnMarkResolved.setVisibility(
                                    "Resolved".equals(newStatus)
                                            ? View.GONE : View.VISIBLE);
                            btnMarkPending.setVisibility(
                                    "Pending".equals(newStatus)
                                            ? View.GONE : View.VISIBLE);

                            Toast.makeText(IncidentDetailActivity.this,
                                    "Status updated to " + newStatus + " ✅",
                                    Toast.LENGTH_SHORT).show();

                            if ("Resolved".equals(newStatus)) {
                                rewardReporter();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Incident>> call,
                                          Throwable t) {}
                });
    }

    private void rewardReporter() {
        RetrofitClient.getService()
                .getAllUsers("*")
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call,
                                           Response<List<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (User u : response.body()) {
                                if (reporterId.equals(u.id)) {
                                    int newCount = u.reportCount + 1;
                                    String newLevel;
                                    if (newCount >= 15)
                                        newLevel = "Guardian";
                                    else if (newCount >= 5)
                                        newLevel = "Community Sentinel";
                                    else
                                        newLevel = "Observer";

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("report_count", newCount);
                                    updates.put("level", newLevel);

                                    RetrofitClient.getService()
                                            .updateUser("eq." + reporterId, updates)
                                            .enqueue(new Callback<List<User>>() {
                                                @Override
                                                public void onResponse(
                                                        Call<List<User>> c,
                                                        Response<List<User>> r) {}
                                                @Override
                                                public void onFailure(
                                                        Call<List<User>> c,
                                                        Throwable t) {}
                                            });

                                    Notification notif = new Notification(
                                            reporterId,
                                            "✅ Your report '"
                                                    + incidentTitle
                                                    + "' has been resolved!"
                                                    + " +1 point awarded 🏅");

                                    RetrofitClient.getService()
                                            .insertNotification(notif)
                                            .enqueue(new Callback<List<Notification>>() {
                                                @Override
                                                public void onResponse(
                                                        Call<List<Notification>> c,
                                                        Response<List<Notification>> r) {}
                                                @Override
                                                public void onFailure(
                                                        Call<List<Notification>> c,
                                                        Throwable t) {}
                                            });
                                    break;
                                }
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {}
                });
    }
}