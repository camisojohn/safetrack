package com.example.safetrack;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.User;
import com.example.safetrack.utils.SessionManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    TextView tvUsername, tvLevel,
            tvReportCount, tvAchievement;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        session       = new SessionManager(this);
        tvUsername    = findViewById(R.id.tvUsername);
        tvLevel       = findViewById(R.id.tvLevel);
        tvReportCount = findViewById(R.id.tvReportCount);
        tvAchievement = findViewById(R.id.tvAchievement);

        tvUsername.setText(session.getUsername());
        loadProfile();
    }

    private void loadProfile() {
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
                            String level = getLevel(
                                    user.reportCount);
                            tvReportCount.setText(
                                    String.valueOf(user.reportCount));
                            tvLevel.setText(level);
                            tvAchievement.setText(
                                    getEmoji(level));
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<User>> call,
                            Throwable t) {}
                });
    }

    private String getLevel(int count) {
        if (count >= 15) return "Guardian";
        if (count >= 5)  return "Community Sentinel";
        return "Observer";
    }

    private String getEmoji(String level) {
        switch (level) {
            case "Guardian":           return "🟢";
            case "Community Sentinel": return "🟡";
            default:                   return "🔵";
        }
    }
}