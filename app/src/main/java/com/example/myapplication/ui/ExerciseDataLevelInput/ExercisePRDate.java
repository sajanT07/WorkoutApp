package com.example.myapplication.ui.ExerciseDataLevelInput;

import android.content.Context;

import com.example.myapplication.ui.PowerBar.PowerBar;

public class ExercisePRDate {
    int exerciseID;
    private String name;
    private String date;
    private Integer PR;

    // Weight-related fields
    private int weightCurrentLevel;
    private int weightCurrentLowBound;
    private int weightUpBound;
    private int weightOverflowOverLevel;

    // Volume-related fields
    private int volumeCurrentLevel;
    private int volumeCurrentLowBound;
    private int volumeUpBound;
    private int volumeOverflowOverLevel;

    private PowerBar weightPowerBar;
    private PowerBar volumePowerBar;

    public ExercisePRDate(int exerciseID, String name, Integer PR, String date, Context context) {
        this.exerciseID = exerciseID;
        this.name = name;
        this.PR = PR;
        this.date = date;

        // Initialize PowerBar instances for weight and volume
        this.weightPowerBar = new PowerBar(exerciseID, "weight",context);
        this.volumePowerBar = new PowerBar(exerciseID, "volume",context);

        // Calculate PowerBar data for both weight and volume
        calculateWeightData();
        calculateVolumeData();
    }

    public PowerBar getVolumePowerBar() {
        return volumePowerBar;
    }

    public PowerBar getWeightPowerBar() {
        return weightPowerBar;
    }

    private void calculateWeightData() {
        this.weightCurrentLevel = weightPowerBar.currentLevel;
        this.weightCurrentLowBound = weightPowerBar.currentLowBound;
        this.weightUpBound = weightPowerBar.upBound;
        this.weightOverflowOverLevel = weightPowerBar.overflowOverLevel;
    }

    private void calculateVolumeData() {
        this.volumeCurrentLevel = volumePowerBar.currentLevel;
        this.volumeCurrentLowBound = volumePowerBar.currentLowBound;
        this.volumeUpBound = volumePowerBar.upBound;
        this.volumeOverflowOverLevel = volumePowerBar.overflowOverLevel;
    }

    // Getter methods for exercise data
    public String getExerciseName() {
        return name;
    }

    public int getExerciseID() {
        return exerciseID;
    }

    public Integer getPR() {
        return PR;
    }

    public String getDate() {
        return date;
    }

    // Getter methods for weight-related PowerBar data
    public int getWeightCurrentLevel() {
        return weightCurrentLevel;
    }

    public int getWeightCurrentLowBound() {
        return weightCurrentLowBound;
    }

    public int getWeightUpBound() {
        return weightUpBound;
    }

    public int getWeightOverflowOverLevel() {
        return weightOverflowOverLevel;
    }

    // Getter methods for volume-related PowerBar data
    public int getVolumeCurrentLevel() {
        return volumeCurrentLevel;
    }

    public int getVolumeCurrentLowBound() {
        return volumeCurrentLowBound;
    }

    public int getVolumeUpBound() {
        return volumeUpBound;
    }

    public int getVolumeOverflowOverLevel() {
        return volumeOverflowOverLevel;
    }

    // Calculate the progress for weight as a percentage
    public int getWeightLevelProgress() {
        return (weightCurrentLevel - weightPowerBar.levelStart) * 100 / weightPowerBar.levelIncrementLimit;
    }

    // Calculate the progress for volume as a percentage
    public int getVolumeLevelProgress() {
        return (volumeCurrentLevel - volumePowerBar.levelStart) * 100 / volumePowerBar.levelIncrementLimit;
    }

    // Calculate the overflow progress for weight as a percentage
    public int getWeightOverflowProgress() {
        return (weightOverflowOverLevel * 100) / weightPowerBar.levelIncrementLimit;
    }

    // Calculate the overflow progress for volume as a percentage
    public int getVolumeOverflowProgress() {
        return (volumeOverflowOverLevel * 100) / volumePowerBar.levelIncrementLimit;
    }
}