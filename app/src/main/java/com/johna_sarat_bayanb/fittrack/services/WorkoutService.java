package com.johna_sarat_bayanb.fittrack.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.johna_sarat_bayanb.fittrack.utils.NotificationHelper;
import com.johna_sarat_bayanb.fittrack.utils.WorkoutConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int reps = 0;
    private long startTime = 0;
    private float lastMagnitude = 0;
    private static final float THRESHOLD = 6f;
    private long lastRepTime = 0;
    public static int finalReps = 0;
    public static int finalDuration = 0;
    private ExecutorService sensorExecutor;

    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            sendUpdate();
            updateHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper.createChannel(this);

        sensorExecutor = Executors.newSingleThreadExecutor();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        reps = 0;
        startTime = SystemClock.elapsedRealtime();
        finalReps = 0;
        finalDuration = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1,
                    NotificationHelper.getNotification(this, "Workout in progress"),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
        } else {
            startForeground(1,
                    NotificationHelper.getNotification(this, "Workout in progress"));
        }

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        updateHandler.post(updateRunnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        updateHandler.removeCallbacks(updateRunnable);

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        if (sensorExecutor != null && !sensorExecutor.isShutdown()) {
            sensorExecutor.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (sensorExecutor == null) return;

        final float x = event.values[0];
        final float y = event.values[1];
        final float z = event.values[2];

        sensorExecutor.execute(() -> {

            float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = Math.abs(magnitude - lastMagnitude);

            lastMagnitude = magnitude;

            long now = System.currentTimeMillis();

            if (delta > THRESHOLD && (now - lastRepTime > 500)) {
                reps++;
                lastRepTime = now;
            }

            sendUpdate();
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void sendUpdate() {

        long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
        int seconds = (int) (elapsedMillis / 1000);

        finalReps = reps;
        finalDuration = seconds;

        Intent intent = new Intent(WorkoutConstants.ACTION_UPDATE);
        intent.putExtra(WorkoutConstants.EXTRA_REPS, reps);
        intent.putExtra(WorkoutConstants.EXTRA_TIME, seconds);
        intent.setPackage(getPackageName());

        sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}
