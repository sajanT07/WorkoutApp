package com.example.myapplication.ui.ProgressActivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.ProgressEntry;
import com.example.myapplication.WorkoutDayDetails;
import com.example.myapplication.WorkoutSet;
import com.example.myapplication.WorkoutSheet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProgressViewModel extends ViewModel {
    private final MutableLiveData<List<String>> exerciseNames = new MutableLiveData<>();
    private final MutableLiveData<List<ProgressEntry>> progressEntries = new MutableLiveData<>();
    private final MutableLiveData<String> buttonText = new MutableLiveData<>("Import");
    private final WorkoutDayDetails dataManager;

    public ProgressViewModel() {
        // Ensure dataManager is initialized correctly
        dataManager = new WorkoutDayDetails(null); // Initialize as per your requirement
        loadExerciseNames();
    }

    public LiveData<List<String>> getExerciseNames() {
        return exerciseNames;
    }

    public LiveData<List<ProgressEntry>> getProgressEntries() {
        return progressEntries;
    }

    // Use setValue() or postValue() based on the threading context
    private void loadExerciseNames() {
        Set<String> exercises = new HashSet<>();
        List<String> workoutNames = dataManager.getAvailableWorkoutNames();

        for (String workoutName : workoutNames) {
            WorkoutSheet sheet = dataManager.getWorkoutSheet(workoutName);
            if (sheet != null) {
                Map<String, List<WorkoutSet>> workoutDays = sheet.getWorkoutDays();
                for (List<WorkoutSet> sets : workoutDays.values()) {
                    for (WorkoutSet set : sets) {
                        if (!set.getExerciseName().contains("Cycling")) {
                            exercises.add(set.getExerciseName());
                        }
                    }
                }
            }
        }

        List<String> sortedExercises = new ArrayList<>(exercises);
        sortedExercises.sort(String::compareTo);
        exerciseNames.setValue(sortedExercises); // Using setValue() if on the main thread
    }

    public void loadProgressData(String exerciseName) {
        List<ProgressEntry> progress = dataManager.getExerciseProgress(exerciseName);
        progressEntries.setValue(progress); // Use setValue() or postValue() based on the thread
    }

    public LiveData<String> getButton() {
        return buttonText; // Return the existing instance of MutableLiveData
    }
}

