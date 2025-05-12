package com.example.myapplication.databases;

public class IdExercise {
    long id;
    String ExerciseName;

    IdExercise(long id, String ExerciseName){
        this.id = id;
        this.ExerciseName = ExerciseName;
    }

    public long getId(){
        return id;
    }

    public String getExerciseName(){
        return ExerciseName;
    }
}
