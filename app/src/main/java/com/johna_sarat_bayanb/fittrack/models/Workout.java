package com.johna_sarat_bayanb.fittrack.models;

public class Workout {

    private String userId;
    private String date;
    private String exerciseType;
    private int reps;
    private int duration;

    public Workout() {
    }

    public Workout(String userId, String date, String exerciseType, int reps, int duration) {
        this.userId = userId;
        this.date = date;
        this.exerciseType = exerciseType;
        this.reps = reps;
        this.duration = duration;
    }

    public String getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public int getReps() {
        return reps;
    }

    public int getDuration() {
        return duration;
    }
}
