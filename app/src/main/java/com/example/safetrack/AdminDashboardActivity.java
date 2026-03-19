package com.example.safetrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.Incident;
import com.example.safetrack.models.Notification;
import com.example.safetrack.models.StatusUpdate;
import com.example.safetrack.models.User;
import com.example.safetrack.utils.SessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    RecyclerView recyclerAdmin;
    TextView tvPendingCount, tvResolvedCount, tvTotalCount;
    Button btnAdminLogout;
    TabLayout tabLayout;
    List<Incident> incidentList = new ArrayList<>();
    List<User> userList = new ArrayList<>();
    IncidentAdminAdapter incidentAdapter;
    UserAdminAdapter userAdapter;
    SessionManager session;
    boolean showingReports = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        session         = new SessionManager(this);
        recyclerAdmin   = findViewById(R.id.recyclerAdmin);
        tvPendingCount  = findViewById(R.id.tvPendingCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
        tvTotalCount    = findViewById(R.id.tvTotalCount);
        btnAdminLogout  = findViewById(R.id.btnAdminLogout);
        tabLayout       = findViewById(R.id.tabLayout);

        incidentAdapter = new IncidentAdminAdapter();
        userAdapter     = new UserAdminAdapter();

        recyclerAdmin.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerAdmin.setAdapter(incidentAdapter);

        btnAdminLogout.setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(this,
                    LoginActivity.class));
            finish();
        });

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(
                            TabLayout.Tab tab) {
                        if (tab.getPosition() == 0) {
                            showingReports = true;
                            recyclerAdmin.setAdapter(
                                    incidentAdapter);
                            loadAllIncidents();
                        } else {
                            showingReports = false;
                            recyclerAdmin.setAdapter(
                                    userAdapter);
                            loadAllUsers();
                        }
                    }
                    @Override
                    public void onTabUnselected(
                            TabLayout.Tab tab) {}
                    @Override
                    public void onTabReselected(
                            TabLayout.Tab tab) {}
                });

        loadAllIncidents();
    }

    private void loadAllIncidents() {
        RetrofitClient.getService()
                .getIncidents("*", "created_at.desc")
                .enqueue(new Callback<List<Incident>>() {
                    @Override
                    public void onResponse(
                            Call<List<Incident>> call,
                            Response<List<Incident>> response) {
                        if (response.isSuccessful()
                                && response.body() != null) {
                            incidentList.clear();
                            incidentList.addAll(
                                    response.body());
                            incidentAdapter
                                    .notifyDataSetChanged();
                            updateStats();
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<Incident>> call,
                            Throwable t) {
                        Toast.makeText(
                                AdminDashboardActivity.this,
                                "Failed to load",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAllUsers() {
        RetrofitClient.getService()
                .getAllUsers("*")
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(
                            Call<List<User>> call,
                            Response<List<User>> response) {
                        if (response.isSuccessful()
                                && response.body() != null) {
                            userList.clear();
                            userList.addAll(response.body());
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<User>> call,
                            Throwable t) {}
                });
    }

    private void updateStats() {
        int pending = 0, resolved = 0;
        for (Incident i : incidentList) {
            if ("Pending".equals(i.status))  pending++;
            if ("Resolved".equals(i.status)) resolved++;
        }
        tvPendingCount.setText(String.valueOf(pending));
        tvResolvedCount.setText(String.valueOf(resolved));
        tvTotalCount.setText(
                String.valueOf(incidentList.size()));
    }

    private void updateStatus(Incident incident,
                              String newStatus) {
        RetrofitClient.getService()
                .updateStatus("eq." + incident.id,
                        new StatusUpdate(newStatus))
                .enqueue(new Callback<List<Incident>>() {
                    @Override
                    public void onResponse(
                            Call<List<Incident>> call,
                            Response<List<Incident>> response) {
                        if ("Resolved".equals(newStatus)) {
                            rewardReporter(
                                    incident.reporterId,
                                    incident.title);
                        }
                        loadAllIncidents();
                    }
                    @Override
                    public void onFailure(
                            Call<List<Incident>> call,
                            Throwable t) {}
                });
    }

    private void rewardReporter(String reporterId,
                                String incidentTitle) {
        RetrofitClient.getService()
                .getAllUsers("*")
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(
                            Call<List<User>> call,
                            Response<List<User>> response) {
                        if (response.isSuccessful()
                                && response.body() != null) {
                            for (User u : response.body()) {
                                if (reporterId.equals(u.id)) {
                                    int newCount =
                                            u.reportCount + 1;
                                    String newLevel;
                                    if (newCount >= 15)
                                        newLevel = "Guardian";
                                    else if (newCount >= 5)
                                        newLevel =
                                                "Community Sentinel";
                                    else
                                        newLevel = "Observer";

                                    // Update user score
                                    Map<String, Object> updates =
                                            new HashMap<>();
                                    updates.put(
                                            "report_count",
                                            newCount);
                                    updates.put(
                                            "level",
                                            newLevel);

                                    // ✅ Fixed Callback syntax
                                    RetrofitClient.getService()
                                            .updateUser(
                                                    "eq." + reporterId,
                                                    updates)
                                            .enqueue(
                                                    new Callback<List<User>>() {
                                                        @Override
                                                        public void onResponse(
                                                                Call<List<User>> c,
                                                                Response<List<User>> r) {}
                                                        @Override
                                                        public void onFailure(
                                                                Call<List<User>> c,
                                                                Throwable t) {}
                                                    });

                                    // ✅ Fixed Callback syntax
                                    Notification notif =
                                            new Notification(
                                                    reporterId,
                                                    "✅ Your report '"
                                                            + incidentTitle
                                                            + "' has been resolved!"
                                                            + " +1 point awarded 🏅");

                                    RetrofitClient.getService()
                                            .insertNotification(notif)
                                            .enqueue(
                                                    new Callback<List<Notification>>() {
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
                    public void onFailure(
                            Call<List<User>> call,
                            Throwable t) {}
                });
    }

    private void deleteIncident(String id) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Report")
                .setMessage("Delete this incident report?")
                .setPositiveButton("Delete", (d, w) -> {
                    RetrofitClient.getService()
                            .deleteIncident("eq." + id)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(
                                        Call<Void> call,
                                        Response<Void> response) {
                                    Toast.makeText(
                                                    AdminDashboardActivity.this,
                                                    "Report deleted",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    loadAllIncidents();
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

    private void blockUser(User user) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Block User")
                .setMessage("Block @" + user.username
                        + "? Their posts will be removed.")
                .setPositiveButton("Block", (d, w) -> {
                    Map<String, Object> updates =
                            new HashMap<>();
                    updates.put("is_blocked", true);

                    RetrofitClient.getService()
                            .updateUser(
                                    "eq." + user.id, updates)
                            .enqueue(
                                    new Callback<List<User>>() {
                                        @Override
                                        public void onResponse(
                                                Call<List<User>> call,
                                                Response<List<User>> res) {
                                            RetrofitClient
                                                    .getService()
                                                    .deleteIncidentsByReporter(
                                                            "eq." + user.id)
                                                    .enqueue(
                                                            new Callback<Void>() {
                                                                @Override
                                                                public void onResponse(
                                                                        Call<Void> c,
                                                                        Response<Void> r) {
                                                                    Toast.makeText(
                                                                                    AdminDashboardActivity.this,
                                                                                    "@" + user.username
                                                                                            + " blocked and"
                                                                                            + " posts removed",
                                                                                    Toast.LENGTH_SHORT)
                                                                            .show();
                                                                    loadAllUsers();
                                                                    loadAllIncidents();
                                                                }
                                                                @Override
                                                                public void onFailure(
                                                                        Call<Void> c,
                                                                        Throwable t) {}
                                                            });
                                        }
                                        @Override
                                        public void onFailure(
                                                Call<List<User>> call,
                                                Throwable t) {}
                                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ===== INCIDENT ADAPTER =====
    class IncidentAdminAdapter extends
            RecyclerView.Adapter<IncidentAdminAdapter.VH> {

        String[] statuses = {"Pending", "Resolved"};

        @Override
        public VH onCreateViewHolder(
                ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_incident,
                            parent, false);
            return new VH(v);
        }





        @Override
        public void onBindViewHolder(VH h, int pos) {
            Incident item = incidentList.get(pos);
            h.tvTitle.setText(item.title);
            h.tvStatus.setText(item.status);
            h.tvReporter.setText("By: " + item.reporterName);
            h.tvType.setText(item.type);

            if ("Resolved".equals(item.status)) {
                h.tvStatus.setTextColor(
                        android.graphics.Color
                                .parseColor("#4CAF50"));
            } else {
                h.tvStatus.setTextColor(
                        android.graphics.Color
                                .parseColor("#FFA500"));
            }

            h.btnChangeStatus.setOnClickListener(v ->
                    new android.app.AlertDialog.Builder(
                            AdminDashboardActivity.this)
                            .setTitle("Update Status")
                            .setItems(statuses, (d, which) ->
                                    updateStatus(item,
                                            statuses[which]))
                            .show());

            h.btnDelete.setOnClickListener(v ->
                    deleteIncident(item.id));
        }

        @Override
        public int getItemCount() {
            return incidentList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvStatus,
                    tvReporter, tvType;
            Button btnChangeStatus, btnDelete;

            VH(View v) {
                super(v);
                tvTitle         = v.findViewById(
                        R.id.tvAdminTitle);
                tvStatus        = v.findViewById(
                        R.id.tvAdminStatus);
                tvReporter      = v.findViewById(
                        R.id.tvAdminReporter);
                tvType          = v.findViewById(
                        R.id.tvAdminType);
                btnChangeStatus = v.findViewById(
                        R.id.btnChangeStatus);
                btnDelete       = v.findViewById(
                        R.id.btnAdminDelete);
            }
        }
    }

    // ===== USER ADAPTER =====
    class UserAdminAdapter extends
            RecyclerView.Adapter<UserAdminAdapter.VH> {

        @Override
        public VH onCreateViewHolder(
                ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_user,
                            parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            User user = userList.get(pos);
            h.tvUsername.setText("@" + user.username);
            h.tvLevel.setText(user.level
                    + " | Reports: " + user.reportCount);
            h.tvRole.setText(user.role.toUpperCase());

            if (user.isBlocked) {
                h.btnBlock.setText("Blocked");
                h.btnBlock.setEnabled(false);
                h.btnBlock.getBackground().setTint(
                        android.graphics.Color
                                .parseColor("#888888"));
            } else if ("admin".equals(user.role)) {
                h.btnBlock.setVisibility(View.GONE);
            } else {
                h.btnBlock.setText("Block");
                h.btnBlock.setEnabled(true);
                h.btnBlock.setOnClickListener(v ->
                        blockUser(user));
            }
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvUsername, tvLevel, tvRole;
            Button btnBlock;

            VH(View v) {
                super(v);
                tvUsername = v.findViewById(
                        R.id.tvAdminUsername);
                tvLevel    = v.findViewById(
                        R.id.tvAdminLevel);
                tvRole     = v.findViewById(
                        R.id.tvAdminRole);
                btnBlock   = v.findViewById(
                        R.id.btnBlockUser);
            }
        }
    }
}