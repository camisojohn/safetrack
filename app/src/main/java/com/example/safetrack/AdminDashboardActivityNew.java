package com.example.safetrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.ArchivedIncident;
import com.example.safetrack.models.Incident;
import com.example.safetrack.models.IncidentReport;
import com.example.safetrack.models.Notification;
import com.example.safetrack.models.StatusUpdate;
import com.example.safetrack.models.User;
import com.example.safetrack.models.UserViolation;
import com.example.safetrack.utils.SessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivityNew extends AppCompatActivity {

    RecyclerView recyclerAdmin;
    TextView tvPendingCount, tvActionCount, tvResolvedCount, tvTotalCount;
    ImageButton btnArchives, btnLogout;
    TabLayout tabLayout;
    EditText etSearch;

    List<Incident> incidentList = new ArrayList<>();
    List<Incident> filteredIncidents = new ArrayList<>();
    List<User> userList = new ArrayList<>();
    List<User> filteredUsers = new ArrayList<>();
    List<IncidentReport> reportsList = new ArrayList<>();

    IncidentAdminAdapter incidentAdapter;
    UserAdminAdapter userAdapter;
    ReportsAdminAdapter reportsAdapter;

    SessionManager session;
    int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        session = new SessionManager(this);

        recyclerAdmin = findViewById(R.id.recyclerAdmin);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvActionCount = findViewById(R.id.tvActionCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        btnArchives = findViewById(R.id.btnArchives);
        btnLogout = findViewById(R.id.btnLogout);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.etSearch);

        incidentAdapter = new IncidentAdminAdapter();
        userAdapter = new UserAdminAdapter();
        reportsAdapter = new ReportsAdminAdapter();

        recyclerAdmin.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerAdmin.setAdapter(incidentAdapter);

        btnArchives.setOnClickListener(v ->
                startActivity(new Intent(this,
                        ArchivesActivity.class)));

        btnLogout.setOnClickListener(v -> confirmLogout());

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        currentTab = tab.getPosition();
                        etSearch.setText("");

                        if (currentTab == 0) {
                            recyclerAdmin.setAdapter(incidentAdapter);
                            loadAllIncidents();
                        } else if (currentTab == 1) {
                            recyclerAdmin.setAdapter(userAdapter);
                            loadAllUsers();
                        } else {
                            recyclerAdmin.setAdapter(reportsAdapter);
                            loadUserReports();
                        }
                    }
                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}
                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
                filterData(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadAllIncidents();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    session.logout();
                    startActivity(new Intent(this,
                            LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void filterData(String query) {
        if (currentTab == 0) {
            filteredIncidents.clear();
            if (query.isEmpty()) {
                filteredIncidents.addAll(incidentList);
            } else {
                for (Incident i : incidentList) {
                    if (i.title.toLowerCase()
                            .contains(query.toLowerCase())
                            || i.type.toLowerCase()
                            .contains(query.toLowerCase())) {
                        filteredIncidents.add(i);
                    }
                }
            }
            incidentAdapter.notifyDataSetChanged();
        } else if (currentTab == 1) {
            filteredUsers.clear();
            if (query.isEmpty()) {
                filteredUsers.addAll(userList);
            } else {
                for (User u : userList) {
                    if (u.username.toLowerCase()
                            .contains(query.toLowerCase())) {
                        filteredUsers.add(u);
                    }
                }
            }
            userAdapter.notifyDataSetChanged();
        }
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
                            filteredIncidents.clear();

                            for (Incident i : response.body()) {
                                if (!i.archived) {
                                    incidentList.add(i);
                                    filteredIncidents.add(i);
                                }
                            }

                            incidentAdapter.notifyDataSetChanged();
                            updateStats();
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<Incident>> call, Throwable t) {}
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
                            filteredUsers.clear();
                            userList.addAll(response.body());
                            filteredUsers.addAll(response.body());
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<User>> call, Throwable t) {}
                });
    }

    private void loadUserReports() {
        RetrofitClient.getService()
                .getIncidentReports("*", "created_at.desc")
                .enqueue(new Callback<List<IncidentReport>>() {
                    @Override
                    public void onResponse(
                            Call<List<IncidentReport>> call,
                            Response<List<IncidentReport>> response) {
                        if (response.isSuccessful()
                                && response.body() != null) {
                            reportsList.clear();
                            reportsList.addAll(response.body());
                            reportsAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<IncidentReport>> call,
                            Throwable t) {}
                });
    }

    private void updateStats() {
        int pending = 0, action = 0, resolved = 0;
        for (Incident i : incidentList) {
            if ("Pending".equals(i.status)) pending++;
            else if ("Taking Action".equals(i.status)) action++;
            else if ("Resolved".equals(i.status)) resolved++;
        }
        tvPendingCount.setText(String.valueOf(pending));
        tvActionCount.setText(String.valueOf(action));
        tvResolvedCount.setText(String.valueOf(resolved));
        tvTotalCount.setText(String.valueOf(incidentList.size()));
    }

    private void updateStatus(Incident incident, String newStatus) {
        RetrofitClient.getService()
                .updateStatus("eq." + incident.id,
                        new StatusUpdate(newStatus))
                .enqueue(new Callback<List<Incident>>() {
                    @Override
                    public void onResponse(
                            Call<List<Incident>> call,
                            Response<List<Incident>> response) {
                        if ("Resolved".equals(newStatus)) {
                            rewardReporter(incident.reporterId,
                                    incident.title);
                        }
                        loadAllIncidents();
                    }
                    @Override
                    public void onFailure(
                            Call<List<Incident>> call, Throwable t) {}
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
                                    int newCount = u.reportCount + 1;
                                    String newLevel;
                                    if (newCount >= 15)
                                        newLevel = "Guardian";
                                    else if (newCount >= 5)
                                        newLevel = "Community Sentinel";
                                    else
                                        newLevel = "Observer";

                                    Map<String, Object> updates =
                                            new HashMap<>();
                                    updates.put("report_count", newCount);
                                    updates.put("level", newLevel);

                                    RetrofitClient.getService()
                                            .updateUser("eq." + reporterId,
                                                    updates)
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
                                            "Your report '" + incidentTitle
                                                    + "' has been resolved! " +
                                                    "+1 point awarded",
                                            "reward");

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
                    public void onFailure(
                            Call<List<User>> call, Throwable t) {}
                });
    }

    private void archiveIncident(Incident incident) {
        new AlertDialog.Builder(this)
                .setTitle("Archive Report")
                .setMessage("Move this report to archives?")
                .setPositiveButton("Archive", (d, w) -> {
                    ArchivedIncident archived =
                            new ArchivedIncident(incident,
                                    session.getUserId());

                    RetrofitClient.getService()
                            .archiveIncident(archived)
                            .enqueue(new Callback<List<ArchivedIncident>>() {
                                @Override
                                public void onResponse(
                                        Call<List<ArchivedIncident>> call,
                                        Response<List<ArchivedIncident>> response) {
                                    if (response.isSuccessful()) {
                                        Map<String, Object> updates =
                                                new HashMap<>();
                                        updates.put("archived", true);

                                        RetrofitClient.getService()
                                                .updateIncident(
                                                        "eq." + incident.id,
                                                        new Incident())
                                                .enqueue(new Callback<List<Incident>>() {
                                                    @Override
                                                    public void onResponse(
                                                            Call<List<Incident>> c,
                                                            Response<List<Incident>> r) {
                                                        Toast.makeText(
                                                                AdminDashboardActivityNew.this,
                                                                "Archived",
                                                                Toast.LENGTH_SHORT).show();
                                                        loadAllIncidents();
                                                    }
                                                    @Override
                                                    public void onFailure(
                                                            Call<List<Incident>> c,
                                                            Throwable t) {}
                                                });
                                    }
                                }
                                @Override
                                public void onFailure(
                                        Call<List<ArchivedIncident>> call,
                                        Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleUserStatus(User user) {
        String action = user.isBlocked ? "Enable" : "Disable";
        new AlertDialog.Builder(this)
                .setTitle(action + " User")
                .setMessage(action + " @" + user.username + "?")
                .setPositiveButton(action, (d, w) -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("is_blocked", !user.isBlocked);

                    RetrofitClient.getService()
                            .updateUser("eq." + user.id, updates)
                            .enqueue(new Callback<List<User>>() {
                                @Override
                                public void onResponse(
                                        Call<List<User>> call,
                                        Response<List<User>> res) {
                                    Toast.makeText(
                                            AdminDashboardActivityNew.this,
                                            "@" + user.username
                                                    + " " + action + "d",
                                            Toast.LENGTH_SHORT).show();
                                    loadAllUsers();
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

    private void issueViolation(IncidentReport report) {
        RetrofitClient.getService()
                .getUserViolations("*",
                        "eq." + report.reportedUserId,
                        "created_at.desc")
                .enqueue(new Callback<List<UserViolation>>() {
                    @Override
                    public void onResponse(
                            Call<List<UserViolation>> call,
                            Response<List<UserViolation>> response) {
                        int strikeNumber = 1;
                        if (response.isSuccessful()
                                && response.body() != null) {
                            strikeNumber = response.body().size() + 1;
                        }

                        if (strikeNumber > 3) {
                            Toast.makeText(
                                    AdminDashboardActivityNew.this,
                                    "User already has 3 strikes",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int finalStrike = strikeNumber;
                        new AlertDialog.Builder(
                                AdminDashboardActivityNew.this)
                                .setTitle("Issue Strike " + strikeNumber)
                                .setMessage("Issue strike " + strikeNumber
                                        + " to this user? " +
                                        (strikeNumber == 3
                                                ? "(Account will be disabled)"
                                                : ""))
                                .setPositiveButton("Issue", (d, w) -> {
                                    performViolation(report, finalStrike);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                    @Override
                    public void onFailure(
                            Call<List<UserViolation>> call,
                            Throwable t) {}
                });
    }

    private void performViolation(IncidentReport report,
                                   int strikeNumber) {
        UserViolation violation = new UserViolation(
                report.reportedUserId,
                report.reason,
                strikeNumber,
                session.getUserId()
        );

        RetrofitClient.getService()
                .insertViolation(violation)
                .enqueue(new Callback<List<UserViolation>>() {
                    @Override
                    public void onResponse(
                            Call<List<UserViolation>> call,
                            Response<List<UserViolation>> response) {
                        if (response.isSuccessful()) {
                            Map<String, Object> reportUpdates =
                                    new HashMap<>();
                            reportUpdates.put("violation_applied", true);
                            reportUpdates.put("is_valid", true);

                            RetrofitClient.getService()
                                    .updateIncidentReport(
                                            "eq." + report.id,
                                            reportUpdates)
                                    .enqueue(new Callback<List<IncidentReport>>() {
                                        @Override
                                        public void onResponse(
                                                Call<List<IncidentReport>> c,
                                                Response<List<IncidentReport>> r) {}
                                        @Override
                                        public void onFailure(
                                                Call<List<IncidentReport>> c,
                                                Throwable t) {}
                                    });

                            String warningText;
                            if (strikeNumber == 1) {
                                warningText = "WARNING: You received" +
                                        " your first strike for: "
                                        + report.reason;
                            } else if (strikeNumber == 2) {
                                warningText = "FINAL WARNING: " +
                                        "Strike 2 for: " + report.reason
                                        + ". One more and your account" +
                                        " will be disabled.";
                            } else {
                                warningText = "ACCOUNT DISABLED: " +
                                        "Strike 3 for: " + report.reason
                                        + ". Your account has been disabled.";

                                Map<String, Object> userUpdates =
                                        new HashMap<>();
                                userUpdates.put("is_banned", true);
                                userUpdates.put("violation_count", 3);

                                RetrofitClient.getService()
                                        .updateUser(
                                                "eq." + report.reportedUserId,
                                                userUpdates)
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
                            }

                            Notification notif = new Notification(
                                    report.reportedUserId,
                                    warningText,
                                    "warning"
                            );

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

                            Toast.makeText(
                                    AdminDashboardActivityNew.this,
                                    "Strike " + strikeNumber + " issued",
                                    Toast.LENGTH_SHORT).show();
                            loadUserReports();
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<UserViolation>> call,
                            Throwable t) {}
                });
    }

    class IncidentAdminAdapter extends
            RecyclerView.Adapter<IncidentAdminAdapter.VH> {

        String[] statuses = {"Pending", "Taking Action", "Resolved"};

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_incident_new,
                            parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Incident item = filteredIncidents.get(pos);
            h.tvTitle.setText(item.title);
            h.tvStatus.setText(item.status);
            h.tvReporter.setText("By: " + item.reporterName);
            h.tvType.setText(item.type);
            h.tvDate.setText(item.createdAt != null
                    ? item.createdAt.substring(0, 16)
                            .replace("T", " ")
                    : "");

            if ("Resolved".equals(item.status)) {
                h.tvStatus.setBackgroundColor(
                        android.graphics.Color
                                .parseColor("#4CAF50"));
            } else if ("Taking Action".equals(item.status)) {
                h.tvStatus.setBackgroundColor(
                        android.graphics.Color
                                .parseColor("#9C27B0"));
            } else {
                h.tvStatus.setBackgroundColor(
                        android.graphics.Color
                                .parseColor("#FFA500"));
            }

            if (item.imageBase64 != null
                    && !item.imageBase64.isEmpty()) {
                h.imgIncident.setVisibility(View.VISIBLE);
                byte[] bytes = android.util.Base64.decode(
                        item.imageBase64,
                        android.util.Base64.DEFAULT);
                Glide.with(AdminDashboardActivityNew.this)
                        .load(bytes)
                        .into(h.imgIncident);
            } else {
                h.imgIncident.setVisibility(View.GONE);
            }

            h.btnChangeStatus.setOnClickListener(v ->
                    new AlertDialog.Builder(
                            AdminDashboardActivityNew.this)
                            .setTitle("Update Status")
                            .setItems(statuses, (d, which) ->
                                    updateStatus(item, statuses[which]))
                            .show());

            h.btnArchive.setOnClickListener(v ->
                    archiveIncident(item));
        }

        @Override
        public int getItemCount() {
            return filteredIncidents.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView imgIncident;
            TextView tvTitle, tvStatus, tvReporter, tvType, tvDate;
            Button btnChangeStatus, btnArchive;

            VH(View v) {
                super(v);
                imgIncident = v.findViewById(R.id.imgAdminIncident);
                tvTitle = v.findViewById(R.id.tvAdminTitle);
                tvStatus = v.findViewById(R.id.tvAdminStatus);
                tvReporter = v.findViewById(R.id.tvAdminReporter);
                tvType = v.findViewById(R.id.tvAdminType);
                tvDate = v.findViewById(R.id.tvAdminDate);
                btnChangeStatus = v.findViewById(
                        R.id.btnChangeStatus);
                btnArchive = v.findViewById(R.id.btnAdminArchive);
            }
        }
    }

    class UserAdminAdapter extends
            RecyclerView.Adapter<UserAdminAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_user_new,
                            parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            User user = filteredUsers.get(pos);
            h.tvUsername.setText("@" + user.username);
            h.tvLevel.setText(user.level
                    + " | Reports: " + user.reportCount);
            h.tvRole.setText(user.role.toUpperCase());

            if (user.isBlocked) {
                h.tvStatus.setText("DISABLED");
                h.tvStatus.setBackgroundColor(
                        android.graphics.Color
                                .parseColor("#F44336"));
                h.tvStatus.setVisibility(View.VISIBLE);
            } else if (user.isBanned) {
                h.tvStatus.setText("BANNED");
                h.tvStatus.setBackgroundColor(
                        android.graphics.Color
                                .parseColor("#D32F2F"));
                h.tvStatus.setVisibility(View.VISIBLE);
            } else {
                h.tvStatus.setVisibility(View.GONE);
            }

            if ("admin".equals(user.role)) {
                h.btnToggle.setVisibility(View.GONE);
            } else {
                h.btnToggle.setText(user.isBlocked
                        ? "Enable" : "Disable");
                h.btnToggle.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                user.isBlocked
                                        ? android.graphics.Color
                                        .parseColor("#4CAF50")
                                        : android.graphics.Color
                                        .parseColor("#FF4444")
                        )
                );
                h.btnToggle.setOnClickListener(v ->
                        toggleUserStatus(user));
            }
        }

        @Override
        public int getItemCount() {
            return filteredUsers.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvUsername, tvLevel, tvRole, tvStatus;
            Button btnToggle;

            VH(View v) {
                super(v);
                tvUsername = v.findViewById(R.id.tvAdminUsername);
                tvLevel = v.findViewById(R.id.tvAdminLevel);
                tvRole = v.findViewById(R.id.tvAdminRole);
                tvStatus = v.findViewById(R.id.tvAdminUserStatus);
                btnToggle = v.findViewById(R.id.btnToggleUser);
            }
        }
    }

    class ReportsAdminAdapter extends
            RecyclerView.Adapter<ReportsAdminAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_report,
                            parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            IncidentReport report = reportsList.get(pos);

            h.tvReason.setText(report.reason);
            h.tvProof.setText(report.proofText);
            h.tvReportedUser.setText(
                    "Reported User ID: " + report.reportedUserId);
            h.tvDate.setText(report.createdAt != null
                    ? report.createdAt.substring(0, 16)
                            .replace("T", " ")
                    : "");

            if (report.violationApplied) {
                h.tvStatus.setText("VIOLATION ISSUED");
                h.tvStatus.setBackgroundColor(
                        android.graphics.Color
                                .parseColor("#4CAF50"));
                h.btnIssueViolation.setVisibility(View.GONE);
                h.btnDismiss.setVisibility(View.GONE);
            } else if (report.isValid != null && !report.isValid) {
                h.tvStatus.setText("DISMISSED");
                h.tvStatus.setBackgroundColor(
                        android.graphics.Color
                                .parseColor("#888888"));
                h.btnIssueViolation.setVisibility(View.GONE);
                h.btnDismiss.setVisibility(View.GONE);
            } else {
                h.tvStatus.setText("PENDING REVIEW");
                h.tvStatus.setBackgroundColor(
                        android.graphics.Color
                                .parseColor("#FFA500"));
                h.btnIssueViolation.setVisibility(View.VISIBLE);
                h.btnDismiss.setVisibility(View.VISIBLE);
            }

            h.btnIssueViolation.setOnClickListener(v ->
                    issueViolation(report));

            h.btnDismiss.setOnClickListener(v ->
                    dismissReport(report));
        }

        @Override
        public int getItemCount() {
            return reportsList.size();
        }

        private void dismissReport(IncidentReport report) {
            new AlertDialog.Builder(AdminDashboardActivityNew.this)
                    .setTitle("Dismiss Report")
                    .setMessage("Mark this report as invalid?")
                    .setPositiveButton("Dismiss", (d, w) -> {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("is_valid", false);

                        RetrofitClient.getService()
                                .updateIncidentReport(
                                        "eq." + report.id, updates)
                                .enqueue(new Callback<List<IncidentReport>>() {
                                    @Override
                                    public void onResponse(
                                            Call<List<IncidentReport>> call,
                                            Response<List<IncidentReport>> response) {
                                        Toast.makeText(
                                                AdminDashboardActivityNew.this,
                                                "Report dismissed",
                                                Toast.LENGTH_SHORT).show();
                                        loadUserReports();
                                    }
                                    @Override
                                    public void onFailure(
                                            Call<List<IncidentReport>> call,
                                            Throwable t) {}
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvReason, tvProof, tvReportedUser, tvDate, tvStatus;
            Button btnIssueViolation, btnDismiss;

            VH(View v) {
                super(v);
                tvReason = v.findViewById(R.id.tvReportReason);
                tvProof = v.findViewById(R.id.tvReportProof);
                tvReportedUser = v.findViewById(
                        R.id.tvReportedUser);
                tvDate = v.findViewById(R.id.tvReportDate);
                tvStatus = v.findViewById(R.id.tvReportStatus);
                btnIssueViolation = v.findViewById(
                        R.id.btnIssueViolation);
                btnDismiss = v.findViewById(R.id.btnDismissReport);
            }
        }
    }
}
