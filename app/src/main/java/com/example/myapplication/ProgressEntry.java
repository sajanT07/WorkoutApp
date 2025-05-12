package com.example.myapplication;

public class ProgressEntry {
    private String date;
    private String exerciseName;
    private double maxWeight;
    private double totalVolume;

    public ProgressEntry(String date, String exerciseName, double maxWeight, double totalVolume) {
        this.date = date;
        this.exerciseName = exerciseName;
        this.maxWeight = maxWeight;
        this.totalVolume = totalVolume;
    }

    public String getDate() {
        return date;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public double getTotalVolume() {
        return totalVolume;
    }
}

