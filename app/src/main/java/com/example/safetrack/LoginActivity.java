package com.example.safetrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.User;
import com.example.safetrack.utils.SessionManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etUsername, etPassword;
    Button btnLogin;
    TextView tvRegister;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        session    = new SessionManager(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this,
                    RegisterActivity.class));
            finish();
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");

        RetrofitClient.getService()
                .getUsers("*", "eq." + username)
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(
                            Call<List<User>> call,
                            Response<List<User>> response) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Sign In");

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            User user = response.body().get(0);

                            if (!user.password.equals(password)) {
                                Toast.makeText(LoginActivity.this,
                                        "Wrong password",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            session.saveSession(
                                    user.id,
                                    user.username,
                                    user.role);

                            if ("admin".equals(user.role)) {
                                startActivity(new Intent(
                                        LoginActivity.this,
                                        AdminDashboardActivity.class));
                            } else {
                                startActivity(new Intent(
                                        LoginActivity.this,
                                        HomeActivity.class));
                            }
                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "User not found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<User>> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Sign In");
                        Toast.makeText(LoginActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}