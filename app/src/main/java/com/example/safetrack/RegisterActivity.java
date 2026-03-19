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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText etUsername, etMobile, etPassword;
    Button btnSignUp;
    TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        etUsername = findViewById(R.id.etUsername);
        etMobile   = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp  = findViewById(R.id.btnSignUp);
        tvLogin    = findViewById(R.id.tvLogin);

        btnSignUp.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String mobile   = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || mobile.isEmpty()
                || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this,
                    "Password min 6 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignUp.setEnabled(false);
        btnSignUp.setText("Registering...");

        User user = new User(username, mobile, password);

        RetrofitClient.getService().insertUser(user)
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(
                            Call<List<User>> call,
                            Response<List<User>> response) {
                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("Sign Up");
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Registered! Please login.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(
                                    RegisterActivity.this,
                                    LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Username already taken!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<User>> call, Throwable t) {
                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("Sign Up");
                        Toast.makeText(RegisterActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}