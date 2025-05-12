package com.example.myapplication.databases;

import android.app.Application;
import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class WorkoutRepository {
    private Database database;
    private MutableLiveData<WorkoutSummary> workoutSummaryLiveData = new MutableLiveData<>();

    public WorkoutRepository(Application application) {
        database = new Database(application);
        fetchWorkoutSummary();  // Fetch initial data
    }

    // Fetch workout summary from SQLite
    public void fetchWorkoutSummary() {
        new Thread(() -> {
            Cursor cursor = database.getWorkoutSummary();
            if (cursor != null && cursor.moveToFirst()) {
                int totalExercises = cursor.getInt(0);
                int totalSets = cursor.getInt(1);
                int totalVolume = cursor.getInt(2);
                cursor.close();

                WorkoutSummary summary = new WorkoutSummary(totalExercises, totalSets, totalVolume);
                workoutSummaryLiveData.postValue(summary);  // Post to LiveData
            }
        }).start();
    }

    // Expose LiveData to ViewModel
    public LiveData<WorkoutSummary> getWorkoutSummary() {
        return workoutSummaryLiveData;
    }
}

