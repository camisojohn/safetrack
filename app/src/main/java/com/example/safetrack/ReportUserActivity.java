package com.example.safetrack;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.IncidentReport;
import com.example.safetrack.utils.SessionManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportUserActivity extends AppCompatActivity {

    Spinner spinnerReason;
    TextInputEditText etProof;
    Button btnSubmitReport, btnCancel;
    SessionManager session;

    String incidentId, reportedUserId;

    String[] reportReasons = {
            "False / Bogus Report",
            "Misleading Information",
            "Fake / Edited Evidence",
            "Spam / Prank",
            "Duplicate Report",
            "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_user);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Report Post");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        session = new SessionManager(this);

        incidentId = getIntent().getStringExtra("incident_id");
        reportedUserId = getIntent().getStringExtra("reported_user_id");

        spinnerReason = findViewById(R.id.spinnerReportReason);
        etProof = findViewById(R.id.etReportProof);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnCancel = findViewById(R.id.btnCancelReport);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                reportReasons
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerReason.setAdapter(adapter);

        btnSubmitReport.setOnClickListener(v -> submitReport());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void submitReport() {
        String reason = spinnerReason.getSelectedItem().toString();
        String proof = etProof.getText().toString().trim();

        if (proof.isEmpty()) {
            Toast.makeText(this,
                    "Please provide evidence or details",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReport.setEnabled(false);
        btnSubmitReport.setText("Submitting...");

        IncidentReport report = new IncidentReport(
                incidentId,
                session.getUserId(),
                reportedUserId,
                reason,
                proof
        );

        RetrofitClient.getService()
                .insertIncidentReport(report)
                .enqueue(new Callback<List<IncidentReport>>() {
                    @Override
                    public void onResponse(
                            Call<List<IncidentReport>> call,
                            Response<List<IncidentReport>> response) {
                        btnSubmitReport.setEnabled(true);
                        btnSubmitReport.setText("Submit Report");

                        if (response.isSuccessful()) {
                            updateIncidentFlagCount();
                            updateUserFlagCount();

                            Toast.makeText(
                                    ReportUserActivity.this,
                                    "Report submitted. Admin will review.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(
                                    ReportUserActivity.this,
                                    "Failed to submit report",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<IncidentReport>> call,
                            Throwable t) {
                        btnSubmitReport.setEnabled(true);
                        btnSubmitReport.setText("Submit Report");
                        Toast.makeText(
                                ReportUserActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateIncidentFlagCount() {
        RetrofitClient.getService()
                .getIncidentReportsByIncident(
                        "*", "eq." + incidentId)
                .enqueue(new Callback<List<IncidentReport>>() {
                    @Override
                    public void onResponse(
                            Call<List<IncidentReport>> call,
                            Response<List<IncidentReport>> response) {
                        if (response.isSuccessful()
                                && response.body() != null) {
                            int flagCount = response.body().size();

                            Map<String, Object> updates =
                                    new HashMap<>();
                            updates.put("flag_count", flagCount);
                            updates.put("is_flagged",
                                    flagCount >= 3);

                            RetrofitClient.getService()
                                    .updateIncident(
                                            "eq." + incidentId,
                                            new com.example.safetrack
                                                    .models.Incident())
                                    .enqueue(new Callback<List<
                                            com.example.safetrack
                                                    .models.Incident>>() {
                                        @Override
                                        public void onResponse(
                                                Call<List<
                                                        com.example.safetrack
                                                                .models.Incident>> c,
                                                Response<List<
                                                        com.example.safetrack
                                                                .models.Incident>> r) {}
                                        @Override
                                        public void onFailure(
                                                Call<List<
                                                        com.example.safetrack
                                                                .models.Incident>> c,
                                                Throwable t) {}
                                    });
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<IncidentReport>> call,
                            Throwable t) {}
                });
    }

    private void updateUserFlagCount() {
        RetrofitClient.getService()
                .getIncidentReports("*", "created_at.desc")
                .enqueue(new Callback<List<IncidentReport>>() {
                    @Override
                    public void onResponse(
                            Call<List<IncidentReport>> call,
                            Response<List<IncidentReport>> response) {
                        if (response.isSuccessful()
                                && response.body() != null) {
                            int flagCount = 0;
                            for (IncidentReport r : response.body()) {
                                if (reportedUserId.equals(
                                        r.reportedUserId)) {
                                    flagCount++;
                                }
                            }

                            Map<String, Object> updates =
                                    new HashMap<>();
                            updates.put("flag_count", flagCount);

                            RetrofitClient.getService()
                                    .updateUser(
                                            "eq." + reportedUserId,
                                            updates)
                                    .enqueue(new Callback<List<
                                            com.example.safetrack
                                                    .models.User>>() {
                                        @Override
                                        public void onResponse(
                                                Call<List<
                                                        com.example.safetrack
                                                                .models.User>> c,
                                                Response<List<
                                                        com.example.safetrack
                                                                .models.User>> r) {}
                                        @Override
                                        public void onFailure(
                                                Call<List<
                                                        com.example.safetrack
                                                                .models.User>> c,
                                                Throwable t) {}
                                    });
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<IncidentReport>> call,
                            Throwable t) {}
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
