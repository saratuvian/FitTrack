package com.johna_sarat_bayanb.fittrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.johna_sarat_bayanb.fittrack.databinding.ActivityAuthBinding;
import com.johna_sarat_bayanb.fittrack.models.User;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupListeners();

    }

    private void setupListeners() {

        binding.btnLogin.setOnClickListener(v -> loginUser());

        binding.btnRegister.setOnClickListener(v -> registerUser());
    }

    private void loginUser() {

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!validate(email, password)) return;

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    showLoading(false);

                    if (task.isSuccessful()) {
                        goToMain();
                    } else {
                        Toast.makeText(this,
                                "Login Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser() {

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!validate(email, password)) return;

        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser);
                        } else {
                            showLoading(false);
                        }

                    } else {
                        showLoading(false);
                        Toast.makeText(this,
                                "Registration Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser) {

        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail());

        db.collection("users")
                .document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(unused -> {
                    showLoading(false);
                    goToMain();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this,
                            "Firestore Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private boolean validate(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_LONG).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void goToMain() {
        startActivity(new Intent(AuthActivity.this, MainActivity.class));
        finish();
    }

}