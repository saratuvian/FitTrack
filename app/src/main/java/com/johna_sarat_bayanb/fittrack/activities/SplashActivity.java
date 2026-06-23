package com.johna_sarat_bayanb.fittrack.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.johna_sarat_bayanb.fittrack.R;
import com.johna_sarat_bayanb.fittrack.databinding.ActivitySplashBinding;
import com.johna_sarat_bayanb.fittrack.utils.Constants;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        setupUI();
        navigateNext();
    }

    private void setupUI() {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < Constants.NAMES.length; i++) {
            builder.append(Constants.NAMES[i])
                    .append("\n(")
                    .append(Constants.EMAILS[i])
                    .append(")")
                    .append("\n\n");
        }

        binding.tvNames.setText(builder.toString().trim());

        binding.tvDate.setText("Submission: " + Constants.SUBMISSION_DATE);
    }

    private void navigateNext() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            }

            finish();

        }, 3000);
    }
}