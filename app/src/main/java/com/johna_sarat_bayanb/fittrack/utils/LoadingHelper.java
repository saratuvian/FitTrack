package com.johna_sarat_bayanb.fittrack.utils;

import android.view.View;

public class LoadingHelper {

    public static void show(View overlay) {
        overlay.setVisibility(View.VISIBLE);
    }

    public static void hide(View overlay) {
        overlay.setVisibility(View.GONE);
    }
}
