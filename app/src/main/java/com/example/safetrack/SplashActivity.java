package com.example.safetrack;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safetrack.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        ImageView imgLogo    = findViewById(R.id.imgLogo);
        TextView tvTitle     = findViewById(R.id.tvSplashTitle);
        TextView tvSub       = findViewById(R.id.tvSplashSub);
        ProgressBar progress = findViewById(R.id.splashProgress);

        // Logo animation — scale + fade
        imgLogo.setScaleX(0.3f);
        imgLogo.setScaleY(0.3f);

        ObjectAnimator logoFade = ObjectAnimator.ofFloat(
                imgLogo, View.ALPHA, 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(
                imgLogo, View.SCALE_X, 0.3f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(
                imgLogo, View.SCALE_Y, 0.3f, 1f);

        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(logoFade, logoScaleX, logoScaleY);
        logoAnim.setDuration(800);
        logoAnim.setInterpolator(
                new OvershootInterpolator(1.2f));
        logoAnim.start();

        // Title fade in
        new Handler().postDelayed(() -> {
            ObjectAnimator titleAnim = ObjectAnimator.ofFloat(
                    tvTitle, View.ALPHA, 0f, 1f);
            titleAnim.setDuration(600);
            titleAnim.start();
        }, 600);

        // Subtitle fade in
        new Handler().postDelayed(() -> {
            ObjectAnimator subAnim = ObjectAnimator.ofFloat(
                    tvSub, View.ALPHA, 0f, 1f);
            subAnim.setDuration(600);
            subAnim.start();
        }, 900);

        // Progress fade in
        new Handler().postDelayed(() -> {
            ObjectAnimator progAnim = ObjectAnimator.ofFloat(
                    progress, View.ALPHA, 0f, 1f);
            progAnim.setDuration(400);
            progAnim.start();
        }, 1200);

        // Navigate after 2.5 seconds
        new Handler().postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            if (session.isLoggedIn()) {
                if (session.isAdmin()) {
                    startActivity(new Intent(this,
                            AdminDashboardActivity.class));
                } else {
                    startActivity(new Intent(this,
                            HomeActivity.class));
                }
            } else {
                startActivity(new Intent(this,
                        LoginActivity.class));
            }
            finish();
        }, 2800);
    }
}