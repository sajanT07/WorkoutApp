package com.example.myapplication.databases;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class WorkoutViewModel extends AndroidViewModel {
    private WorkoutRepository repository;
    private LiveData<WorkoutSummary> workoutSummary;

    public WorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        workoutSummary = repository.getWorkoutSummary();
    }

    public LiveData<WorkoutSummary> getWorkoutSummary() {
        return workoutSummary;
    }

    // Refresh data when needed
    public void refreshData() {
        repository.fetchWorkoutSummary();
    }
}