package com.example.myapplication;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkoutSheet {
    private static final String TAG = "WorkoutSheet";

    private String workoutName;
    private Map<String, List<WorkoutSet>> workoutDays; // Date -> List of sets

    public WorkoutSheet(String workoutName) {
        this.workoutName = workoutName;
        this.workoutDays = new HashMap<>();
    }

    public void addSet(WorkoutSet set) {
        if (!workoutDays.containsKey(set.getDate())) {
            workoutDays.put(set.getDate(), new ArrayList<>());
        }
        workoutDays.get(set.getDate()).add(set);
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public Map<String, List<WorkoutSet>> getWorkoutDays() {
        return workoutDays;
    }

    public List<String> getWorkoutDates() {
        List<String> dates = new ArrayList<>(workoutDays.keySet());
        // Sort dates
        dates.sort((o1, o2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date date1 = sdf.parse(o1);
                Date date2 = sdf.parse(o2);
                return date2.compareTo(date1); // Newest first
            } catch (ParseException e) {
                return 0;
            }
        });
        return dates;
    }

    public List<WorkoutSet> getSetsForDate(String date) {
        return workoutDays.getOrDefault(date, new ArrayList<>());
    }

    public boolean deleteWorkoutDay(String date) {
        return workoutDays.remove(date) != null;
    }

    public boolean saveToFile(File file) {
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(TAG, "Failed to create file: " + file.getAbsolutePath());
                    return false;
                }
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(file);
            SheetData data = new SheetData(workoutName, workoutDays);
            gson.toJson(data, writer);
            writer.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving workout sheet", e);
            return false;
        }
    }

    public boolean loadFromFile(File file) {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(file);
            Type type = new TypeToken<SheetData>(){}.getType();
            SheetData data = gson.fromJson(reader, type);
            reader.close();

            if (data != null) {
                this.workoutName = data.workoutName;
                this.workoutDays = data.workoutDays;
                return true;
            }
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Error loading workout sheet", e);
            return false;
        }
    }

    public List<ProgressEntry> getExerciseProgress(String exerciseName) {
        List<ProgressEntry> progressEntries = new ArrayList<>();

        for (Map.Entry<String, List<WorkoutSet>> entry : workoutDays.entrySet()) {
            String date = entry.getKey();
            List<WorkoutSet> sets = entry.getValue();

            // Filter sets by exercise name
            List<WorkoutSet> exerciseSets = sets.stream()
                    .filter(set -> set.getExerciseName().equals(exerciseName))
                    .collect(Collectors.toList());

            if (!exerciseSets.isEmpty()) {
                // Get max weight and volume for this exercise on this day
                double maxWeight = 0;
                double totalVolume = 0;

                for (WorkoutSet set : exerciseSets) {
                    if (set.getWeight() > maxWeight) {
                        maxWeight = set.getWeight();
                    }
                    totalVolume += set.getWeight() * set.getReps();
                }

                progressEntries.add(new ProgressEntry(date, exerciseName, maxWeight, totalVolume));
            }
        }

        return progressEntries;
    }

    // Helper class for GSON serialization
    private static class SheetData {
        private String workoutName;
        private Map<String, List<WorkoutSet>> workoutDays;

        public SheetData(String workoutName, Map<String, List<WorkoutSet>> workoutDays) {
            this.workoutName = workoutName;
            this.workoutDays = workoutDays;
        }
    }
}