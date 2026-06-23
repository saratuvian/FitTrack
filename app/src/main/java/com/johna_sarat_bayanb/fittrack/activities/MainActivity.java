package com.johna_sarat_bayanb.fittrack.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.johna_sarat_bayanb.fittrack.R;
import com.johna_sarat_bayanb.fittrack.adapters.WorkoutAdapter;
import com.johna_sarat_bayanb.fittrack.databinding.ActivityMainBinding;
import com.johna_sarat_bayanb.fittrack.models.Workout;
import com.johna_sarat_bayanb.fittrack.services.WorkoutService;
import com.johna_sarat_bayanb.fittrack.utils.Constants;
import com.johna_sarat_bayanb.fittrack.utils.LoadingHelper;
import com.johna_sarat_bayanb.fittrack.utils.PermissionHelper;
import com.johna_sarat_bayanb.fittrack.utils.SharedPrefManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.ContextCompat;
import com.johna_sarat_bayanb.fittrack.utils.WorkoutConstants;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private boolean isWorkoutRunning = false;
    private BroadcastReceiver workoutReceiver;
    private boolean isReceiverRegistered = false;
    private List<Workout> workoutList = new ArrayList<>();
    private WorkoutAdapter adapter;
    private ExecutorService firestoreExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestoreExecutor = Executors.newSingleThreadExecutor();
        PermissionHelper.requestPermissions(this);
        setSupportActionBar(binding.toolbar);
        setupReceiver();
        setupRecycler();
        setupChart();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodaySummary();
    }

    private void setupReceiver() {

        workoutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                int reps = intent.getIntExtra(WorkoutConstants.EXTRA_REPS, 0);
                int time = intent.getIntExtra(WorkoutConstants.EXTRA_TIME, 0);

                binding.tvReps.setText("Reps: " + reps);

                int minutes = time / 60;
                int seconds = time % 60;

                binding.tvTimer.setText(String.format("Time: %02d:%02d", minutes, seconds));
            }
        };

        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(WorkoutConstants.ACTION_UPDATE);
            ContextCompat.registerReceiver(this, workoutReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            isReceiverRegistered = true;
        }
    }

    private void setupRecycler() {
        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new WorkoutAdapter(workoutList);
        binding.recyclerView.setAdapter(adapter);

        loadWorkoutsFromFirestore();
    }

    private void setupChart() {
        binding.lineChart.getDescription().setText("Weekly Progress");
    }

    private void updateChart() {

        Map<String, Integer> map = new LinkedHashMap<>();

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        java.util.Calendar cal = java.util.Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new java.util.Date());
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i);
            String day = sdf.format(cal.getTime());
            map.put(day, 0);
        }

        for (var w : workoutList) {

            String date = w.getDate();

            if (date != null && date.length() >= 10) {

                String day = date.substring(0, 10);

                if (map.containsKey(day)) {
                    map.put(day, map.get(day) + w.getReps());
                }
            }
        }

        ArrayList<Entry> entries = new ArrayList<>();

        int index = 0;
        for (Integer value : map.values()) {
            entries.add(new Entry(index++, value));
        }

        LineDataSet dataSet =
                new LineDataSet(entries, "Last 7 Days");

        LineData lineData =
                new LineData(dataSet);

        binding.lineChart.setData(lineData);
        binding.lineChart.invalidate();
    }

    private void setupListeners() {

        binding.btnStart.setOnClickListener(v -> {

            if (!isWorkoutRunning) {
                startWorkout();
            } else {
                stopWorkout();
            }
        });
    }

    private void startWorkout() {
        isWorkoutRunning = true;
        binding.btnStart.setText("Stop Workout");

        Intent intent = new Intent(this, WorkoutService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
    }

    private void stopWorkout() {
        isWorkoutRunning = false;
        binding.btnStart.setText("Start Workout");

        stopService(new Intent(this, WorkoutService.class));

        binding.tvReps.setText("Reps: 0");
        binding.tvTimer.setText("Time: 00:00");

        saveWorkoutToFirestore();
    }

    private void saveWorkoutToFirestore() {

        LoadingHelper.show(binding.loadingOverlay);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String date = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
        ).format(new java.util.Date());

        int reps = WorkoutService.finalReps;
        int duration = WorkoutService.finalDuration;

        Workout workout = new Workout(
                userId,
                date,
                "General",
                reps,
                duration
        );

        firestoreExecutor.execute(() -> {

            FirebaseFirestore.getInstance()
                    .collection("workouts")
                    .add(workout)
                    .addOnSuccessListener(doc -> runOnUiThread(() -> {

                        LoadingHelper.hide(binding.loadingOverlay);

                        loadWorkoutsFromFirestore();

                    }))
                    .addOnFailureListener(e -> runOnUiThread(() -> {

                        LoadingHelper.hide(binding.loadingOverlay);

                        new AlertDialog.Builder(this)
                                .setTitle("Error")
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }));
        });

    }

    private void loadWorkoutsFromFirestore() {

        LoadingHelper.show(binding.loadingOverlay);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestoreExecutor.execute(() -> {

            FirebaseFirestore.getInstance()
                    .collection("workouts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(query -> runOnUiThread(() -> {

                        workoutList.clear();

                        for (var doc : query) {

                            int reps = doc.getLong("reps") != null
                                    ? doc.getLong("reps").intValue()
                                    : 0;

                            int duration = doc.getLong("duration") != null
                                    ? doc.getLong("duration").intValue()
                                    : 0;

                            Workout w = new Workout(
                                    doc.getString("userId"),
                                    doc.getString("date"),
                                    doc.getString("exerciseType"),
                                    reps,
                                    duration
                            );

                            workoutList.add(w);
                        }

                        if (workoutList.isEmpty()) {
                            binding.tvNoDataFound.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.GONE);
                        } else {
                            Collections.sort(workoutList, (w1, w2) ->
                                    w2.getDate().compareTo(w1.getDate()));
                            binding.tvNoDataFound.setVisibility(View.GONE);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();

                        updateChart();

                        loadTodaySummary();

                        LoadingHelper.hide(binding.loadingOverlay);

                    }))
                    .addOnFailureListener(e -> runOnUiThread(() -> {

                        LoadingHelper.hide(binding.loadingOverlay);

                        new AlertDialog.Builder(this)
                                .setTitle("Load Failed")
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }));
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_about) {
            showAboutDialog();
            return true;

        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;

        } else if (id == R.id.menu_exit) {
            showExitDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {

        StringBuilder message = new StringBuilder();
        message.append("App: ").append(Constants.APP_NAME).append("\n\n");

        message.append("Developers:\n");
        for (int i = 0; i < Constants.NAMES.length; i++) {
            message.append(Constants.NAMES[i])
                    .append(" (")
                    .append(Constants.EMAILS[i])
                    .append(")\n");
        }

        message.append("\nSubmission: ").append(Constants.SUBMISSION_DATE);

        message.append("\n\nAndroid Version: ")
                .append(Build.VERSION.RELEASE);

        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Should I exit?")
                .setPositiveButton("Yes", (d, w) -> finishAffinity())
                .setNegativeButton("No", null)
                .show();
    }

    private void loadTodaySummary() {

        LoadingHelper.show(binding.loadingOverlay);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());

        FirebaseFirestore.getInstance()
                .collection("workouts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {

                    int totalReps = 0;
                    int totalTime = 0;

                    for (var doc : query) {

                        String date = doc.getString("date");

                        if (date != null && date.startsWith(today)) {

                            totalReps += doc.getLong("reps").intValue();
                            totalTime += doc.getLong("duration").intValue();
                        }
                    }

                    int goal = SharedPrefManager.getGoal(this);

                    binding.tvTodayReps.setText("Reps: " + totalReps);
                    int minutes = totalTime / 60;
                    int seconds = totalTime % 60;

                    binding.tvTodayTime.setText(
                            String.format("Time: %d min %02d sec", minutes, seconds)
                    );

                    binding.tvGoal.setText("Goal: " + goal + " min");

                    int progress = (int) ((totalTime / 60f) / goal * 100);
                    binding.progressGoal.setProgress(Math.min(progress, 100));

                    LoadingHelper.hide(binding.loadingOverlay);

                })
                .addOnFailureListener(e -> {

                    LoadingHelper.hide(binding.loadingOverlay);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isReceiverRegistered && workoutReceiver != null) {
            unregisterReceiver(workoutReceiver);
            isReceiverRegistered = false;
        }

        if (firestoreExecutor != null && !firestoreExecutor.isShutdown()) {
            firestoreExecutor.shutdown();
        }

    }
}