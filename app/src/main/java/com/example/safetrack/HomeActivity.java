package com.example.safetrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safetrack.utils.NotificationActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.safetrack.adapters.IncidentAdapter;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.Incident;
import com.example.safetrack.models.User;
import com.example.safetrack.utils.SessionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity
        implements IncidentAdapter.OnIncidentActionListener {

    RecyclerView recycler;
    FloatingActionButton fabPost;
    ImageButton btnProfile, btnLogout, btnNotification;
    TextView tvUsername, tvLevel, tvReportScore;
    List<Incident> incidentList = new ArrayList<>();
    IncidentAdapter adapter;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        session          = new SessionManager(this);
        recycler         = findViewById(R.id.recyclerIncidents);
        fabPost          = findViewById(R.id.fabPost);
        btnProfile       = findViewById(R.id.btnProfile);
        btnLogout        = findViewById(R.id.btnLogout);
        btnNotification  = findViewById(R.id.btnNotification);
        tvUsername       = findViewById(R.id.tvUsername);
        tvLevel          = findViewById(R.id.tvLevel);
        tvReportScore    = findViewById(R.id.tvReportScore);

        tvUsername.setText(session.getUsername());

        adapter = new IncidentAdapter(
                this, incidentList, this, session.getUserId());
        recycler.setLayoutManager(
                new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        fabPost.setOnClickListener(v ->
                startActivity(new Intent(this,
                        PostIncidentActivity.class)));

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this,
                        ProfileActivity.class)));

        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this,
                        NotificationActivity.class)));

        btnLogout.setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(this,
                    LoginActivity.class));
            finish();
        });

        loadUserScore();
        loadIncidents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserScore();
        loadIncidents();
    }

    private void loadUserScore() {
        RetrofitClient.getService()
                .getUsers("*", "eq." + session.getUsername())
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(
                            Call<List<User>> call,
                            Response<List<User>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {
                            User user = response.body().get(0);
                            tvLevel.setText(user.level != null
                                    ? user.level : "Observer");
                            tvReportScore.setText(
                                    String.valueOf(user.reportCount));
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<User>> call,
                            Throwable t) {}
                });
    }

    private void loadIncidents() {
        RetrofitClient.getService()
                .getIncidents("*", "upvotes.desc,created_at.desc")
                .enqueue(new Callback<List<Incident>>() {
                    @Override
                    public void onResponse(
                            Call<List<Incident>> call,
                            Response<List<Incident>> response) {
                        if (response.isSuccessful()
                                && response.body() != null) {
                            incidentList.clear();

                            List<Incident> active   = new ArrayList<>();
                            List<Incident> resolved = new ArrayList<>();

                            for (Incident i : response.body()) {
                                if ("Resolved".equals(i.status)) {
                                    resolved.add(i);
                                } else {
                                    active.add(i);
                                }
                            }

                            // Sort active by upvotes desc
                            Collections.sort(active,
                                    (a, b) -> b.upvotes - a.upvotes);

                            // Active first, resolved at bottom
                            incidentList.addAll(active);
                            incidentList.addAll(resolved);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<Incident>> call,
                            Throwable t) {
                        Toast.makeText(HomeActivity.this,
                                "Failed to load",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onUpvote(String incidentId,
                         int currentUpvotes) {
        Incident update = new Incident();
        update.upvotes  = currentUpvotes + 1;

        RetrofitClient.getService()
                .updateIncident("eq." + incidentId, update)
                .enqueue(new Callback<List<Incident>>() {
                    @Override
                    public void onResponse(
                            Call<List<Incident>> call,
                            Response<List<Incident>> response) {
                        loadIncidents();
                    }
                    @Override
                    public void onFailure(
                            Call<List<Incident>> call,
                            Throwable t) {}
                });
    }

    @Override
    public void onEdit(Incident incident) {
        Intent intent = new Intent(this,
                PostIncidentActivity.class);
        intent.putExtra("edit_id",
                incident.id);
        intent.putExtra("edit_title",
                incident.title);
        intent.putExtra("edit_type",
                incident.type);
        intent.putExtra("edit_description",
                incident.description);
        intent.putExtra("edit_location",
                incident.location);
        startActivity(intent);
    }

    @Override
    public void onDelete(String incidentId) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Report")
                .setMessage(
                        "Are you sure you want to delete this report?")
                .setPositiveButton("Delete", (d, w) -> {
                    RetrofitClient.getService()
                            .deleteIncident("eq." + incidentId)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(
                                        Call<Void> call,
                                        Response<Void> response) {
                                    Toast.makeText(
                                            HomeActivity.this,
                                            "Report deleted",
                                            Toast.LENGTH_SHORT).show();
                                    loadIncidents();
                                }
                                @Override
                                public void onFailure(
                                        Call<Void> call,
                                        Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}