package com.example.myapplication;

import java.util.Locale;
public class WorkoutSet {
    private int workoutId;
    private String date;
    private String workoutName;
    private String exerciseName;
    private int setOrder;
    private double weight;
    private int reps;
    private double distance;
    private double seconds;

    public WorkoutSet(int workoutId, String date, String workoutName, String exerciseName,
                      int setOrder, double weight, int reps, double distance, double seconds) {
        this.workoutId = workoutId;
        this.date = date;
        this.workoutName = workoutName;
        this.exerciseName = exerciseName;
        this.setOrder = setOrder;
        this.weight = weight;
        this.reps = reps;
        this.distance = distance;
        this.seconds = seconds;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public String getDate() {
        return date;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public int getSetOrder() {
        return setOrder;
    }

    public double getWeight() {
        return weight;
    }

    public int getReps() {
        return reps;
    }

    public double getDistance() {
        return distance;
    }

    public double getSeconds() {
        return seconds;
    }

    @Override
    public String toString() {
        if (weight > 0 && reps > 0) {
            return String.format(Locale.UK, "Set %d: %.1f kg × %d reps", setOrder, weight, reps);
        } else if (distance > 0) {
            return String.format(Locale.UK, "Set %d: %.1f meters in %.1f seconds", setOrder, distance, seconds);
        } else {
            return String.format(Locale.UK, "Set %d", setOrder);
        }
    }
}