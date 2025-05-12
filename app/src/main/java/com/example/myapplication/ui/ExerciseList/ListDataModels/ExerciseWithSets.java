package com.example.myapplication.ui.ExerciseList.ListDataModels;


import java.util.List;

public class ExerciseWithSets {
    int exerciseID;
    public String name;
    public List<Set> sets;

    public ExerciseWithSets(int exerciseID, String name, List<Set> sets) {
        this.exerciseID = exerciseID;
        this.name = name;
        this.sets = sets;
    }
}