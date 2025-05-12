package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkoutDayDetails {
    private static final String TAG = "WorkoutDataManager";
    private static final String BASE_DIR = "workout_data";
    private final Context context;
    private Map<String, WorkoutSheet> workoutSheets;

    public WorkoutDayDetails(Context context) {
        this.context = context;
        this.workoutSheets = new HashMap<>();
        initializeDirectories();
    }

    private void initializeDirectories() {
        File baseDir = new File(context.getFilesDir(), BASE_DIR);
        if (!baseDir.exists()) {
            if (baseDir.mkdirs()) {
                Log.d(TAG, "Created base directory");
            } else {
                Log.e(TAG, "Failed to create base directory");
            }
        }
    }

    public boolean importCSVData(String csvFilePath, File file) {
        try {


            FileReader fileReader = new FileReader(file);

            if(!csvFilePath.equals("")){
                File csvFile = new File(csvFilePath);
                fileReader = new FileReader(csvFile);
            }

            CSVParser csvParser = CSVParser.parse(fileReader, CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withFirstRecordAsHeader());

            for (CSVRecord record : csvParser) {
                String workoutId = record.get("Workout #");
                String date = record.get("Date");
                String workoutName = record.get("Workout Name");
                String exerciseName = record.get("Exercise Name");
                String setOrder = record.get("Set Order");
                String weight = record.get("Weight (kg)");
                String reps = record.get("Reps");
                String distance = record.get("Distance (meters)");
                String seconds = record.get("Seconds");

                // Create a workout set
                WorkoutSet set = new WorkoutSet(
                        Integer.parseInt(workoutId),
                        date,
                        workoutName,
                        exerciseName,
                        Integer.parseInt(setOrder),
                        weight.isEmpty() ? 0 : Double.parseDouble(weight),
                        reps.isEmpty() ? 0 : Integer.parseInt(reps),
                        distance.isEmpty() ? 0 : Double.parseDouble(distance),
                        seconds.isEmpty() ? 0 : Double.parseDouble(seconds)
                );

                // Add to appropriate sheet
                getOrCreateWorkoutSheet(workoutName).addSet(set);
            }

            // Save all workout sheets
            saveAllWorkoutSheets();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error importing CSV data", e);
            return false;
        }
    }

    private WorkoutSheet getOrCreateWorkoutSheet(String workoutName) {
        if (!workoutSheets.containsKey(workoutName)) {
            WorkoutSheet sheet = new WorkoutSheet(workoutName);
            // Try to load existing data
            File sheetFile = new File(context.getFilesDir(), BASE_DIR + "/" + workoutName + ".json");
            if (sheetFile.exists()) {
                sheet.loadFromFile(sheetFile);
            }
            workoutSheets.put(workoutName, sheet);
        }
        return workoutSheets.get(workoutName);
    }

    public void saveAllWorkoutSheets() {
        for (WorkoutSheet sheet : workoutSheets.values()) {
            File sheetFile = new File(context.getFilesDir(), BASE_DIR + "/" + sheet.getWorkoutName() + ".json");
            sheet.saveToFile(sheetFile);
        }
    }

    public List<String> getAvailableWorkoutNames() {
        return new ArrayList<>(workoutSheets.keySet());
    }

    public WorkoutSheet getWorkoutSheet(String workoutName) {
        return workoutSheets.get(workoutName);
    }

    public boolean deleteWorkoutDay(String workoutName, Object workoutDate) {
        WorkoutSheet sheet = workoutSheets.get(workoutName);
        if (sheet != null) {
            boolean result = sheet.deleteWorkoutDay((String) workoutDate);
            if (result) {
                File sheetFile = new File(context.getFilesDir(), BASE_DIR + "/" + workoutName + ".json");
                sheet.saveToFile(sheetFile);
            }
            return result;
        }
        return false;
    }

    public boolean deleteWorkoutSheet(String workoutName) {
        if (workoutSheets.containsKey(workoutName)) {
            workoutSheets.remove(workoutName);
            File sheetFile = new File(context.getFilesDir(), BASE_DIR + "/" + workoutName + ".json");
            return sheetFile.delete();
        }
        return false;
    }

    public List<String> loadAllWorkoutSheets() {
        workoutSheets.clear();
        File baseDir = new File(context.getFilesDir(), BASE_DIR);
        File[] files = baseDir.listFiles((dir, name) -> name.endsWith(".json"));

        List<String> loadedSheets = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String workoutName = file.getName().replace(".json", "");
                WorkoutSheet sheet = new WorkoutSheet(workoutName);
                sheet.loadFromFile(file);
                workoutSheets.put(workoutName, sheet);
                loadedSheets.add(workoutName);
            }
        }

        return loadedSheets;
    }

    // Get progress metrics for an exercise
    public List<ProgressEntry> getExerciseProgress(String exerciseName) {
        List<ProgressEntry> progressEntries = new ArrayList<>();

        for (WorkoutSheet sheet : workoutSheets.values()) {
            progressEntries.addAll(sheet.getExerciseProgress(exerciseName));
        }

        // Sort by date
        progressEntries.sort((o1, o2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date date1 = sdf.parse(o1.getDate());
                Date date2 = sdf.parse(o2.getDate());
                assert date1 != null;
                return date1.compareTo(date2);
            } catch (ParseException e) {
                return 0;
            }
        });

        return progressEntries;
    }
}
