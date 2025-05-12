package com.example.myapplication.databases;

public class WorkoutSummary {
    private int totalExercises;
    private int totalSets;
    private int totalVolume;

    // Constructor
    public WorkoutSummary(int totalExercises, int totalSets, int totalVolume) {
        this.totalExercises = totalExercises;
        this.totalSets = totalSets;
        this.totalVolume = totalVolume;
    }

    // Getters
    public int getTotalExercises() {
        return totalExercises;
    }

    public int getTotalSets() {
        return totalSets;
    }

    public int getTotalVolume() {
        return totalVolume;
    }
}
