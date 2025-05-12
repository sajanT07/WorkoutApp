package com.example.myapplication.databases;

public class IDExerciseHighest {
    int id;
    String ExerciseName;
    double highest;
    IDExerciseHighest(int id, String ExerciseName, double highest){
        this.id = id;
        this.ExerciseName = ExerciseName;
        this.highest = highest;
    }

    public int getId(){
        return id;
    }

    public double getHighest() {
        return highest;
    }

    public String getExerciseName(){
        return ExerciseName;
    }
}
