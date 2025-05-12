package com.example.myapplication.ui.ExerciseInfo.DataModels;
import com.example.myapplication.ui.ExerciseList.ListDataModels.ExerciseWithSets;

import java.util.List;

public class Workout {
    int workoutID;
    public String date;
    public List<Set> exercises;

    public Workout(int workoutID, String date, List<Set> exercises) {
        this.workoutID = workoutID;
        this.date = date;
        this.exercises = exercises;
    }
}
