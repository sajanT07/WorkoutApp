package com.example.myapplication.ui.ExerciseInfo.DataModels;

public class ExId {
    int ExId;
    String ExerciseName;

    public ExId(int exId, String exerciseName) {
        this.ExId = exId;
        this.ExerciseName = exerciseName;
    }

    public int getExId() {
        return ExId;
    }

    public String getExerciseName() {
        return ExerciseName;
    }
}
