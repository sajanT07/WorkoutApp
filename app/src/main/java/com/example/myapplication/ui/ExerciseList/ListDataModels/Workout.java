package com.example.myapplication.ui.ExerciseList.ListDataModels;
import java.util.List;
public class Workout {
    int workoutID;
    public String date;
    public List<ExerciseWithSets> exercises;

    public Workout(int workoutID, String date, List<ExerciseWithSets> exercises) {
        this.workoutID = workoutID;
        this.date = date;
        this.exercises = exercises;
    }
}
