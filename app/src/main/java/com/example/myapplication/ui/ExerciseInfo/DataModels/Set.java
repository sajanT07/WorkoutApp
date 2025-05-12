package com.example.myapplication.ui.ExerciseInfo.DataModels;

public class Set {
    int setID;
    public int weight;
    public int reps;
    public int setOrder;

    public Set(int setID, int weight, int reps, int setOrder) {
        this.setID = setID;
        this.weight = weight;
        this.reps = reps;
        this.setOrder = setOrder;
    }
}