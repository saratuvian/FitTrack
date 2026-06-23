package com.johna_sarat_bayanb.fittrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.johna_sarat_bayanb.fittrack.databinding.ActivitySettingsBinding;
import com.johna_sarat_bayanb.fittrack.utils.AlarmHelper;
import com.johna_sarat_bayanb.fittrack.utils.LoadingHelper;
import com.johna_sarat_bayanb.fittrack.utils.SharedPrefManager;

import java.text.SimpleDateFormat;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private String selectedTimeDisplay = "08:00 AM";
    private String selectedTime24 = "08:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {

        int goal = SharedPrefManager.getGoal(this);
        boolean reminder = SharedPrefManager.isReminderEnabled(this);
        selectedTimeDisplay = SharedPrefManager.getReminderTime(this);

        binding.etGoal.setText(String.valueOf(goal));
        binding.switchReminder.setChecked(reminder);
        binding.btnTime.setText("Time: " + selectedTimeDisplay);

        String time24 = SharedPrefManager.getReminderTime(this);

        try {
            SimpleDateFormat inFormat =
                    new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());

            java.text.SimpleDateFormat outFormat =
                    new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());

            java.util.Date date = inFormat.parse(time24);

            selectedTimeDisplay = outFormat.format(date);
            selectedTime24 = time24;

            binding.btnTime.setText("Time: " + selectedTimeDisplay);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {

        binding.btnTime.setOnClickListener(v -> showTimePicker());

        binding.btnSave.setOnClickListener(v -> saveSettings());

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void showTimePicker() {

        java.util.Calendar cal = java.util.Calendar.getInstance();

        new android.app.TimePickerDialog(this,
                (view, hourOfDay, minute) -> {

                    selectedTime24 = String.format("%02d:%02d", hourOfDay, minute);

                    SimpleDateFormat sdf =
                            new SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());

                    java.util.Calendar selectedCal = java.util.Calendar.getInstance();
                    selectedCal.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCal.set(java.util.Calendar.MINUTE, minute);

                    selectedTimeDisplay = sdf.format(selectedCal.getTime());

                    binding.btnTime.setText("Time: " + selectedTimeDisplay);

                },
                cal.get(java.util.Calendar.HOUR_OF_DAY),
                cal.get(java.util.Calendar.MINUTE),
                false
        ).show();
    }

    private void saveSettings() {

        String goalStr = binding.etGoal.getText().toString().trim();

        if (TextUtils.isEmpty(goalStr)) {
            Toast.makeText(this, "Goal is Required", Toast.LENGTH_SHORT).show();
            return;
        }

        int goal = Integer.parseInt(goalStr);
        boolean reminder = binding.switchReminder.isChecked();

        LoadingHelper.show(binding.loadingOverlay);

        SharedPrefManager.saveGoal(this, goal);
        SharedPrefManager.setReminderEnabled(this, reminder);
        SharedPrefManager.saveReminderTime(this, selectedTime24);

        if (reminder) {
            AlarmHelper.scheduleReminder(this, selectedTime24);
        } else {
            AlarmHelper.cancelReminder(this);
        }

        LoadingHelper.hide(binding.loadingOverlay);

        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
    }

    private void logout() {

        LoadingHelper.show(binding.loadingOverlay);

        FirebaseAuth.getInstance().signOut();

        LoadingHelper.hide(binding.loadingOverlay);

        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}