package com.johna_sarat_bayanb.fittrack;

import androidx.multidex.MultiDexApplication;

import com.johna_sarat_bayanb.fittrack.utils.NotificationHelper;

public class FitTrackApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper.createChannel(this);
    }
}
