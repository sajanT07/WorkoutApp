package com.example.myapplication.databases;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myapplication.ui.ExerciseInfo.DataModels.PRs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExerciseJsonManager {
    private static final String TAG = "ExerciseJsonManager";
    private static final String FILE_NAME = "level_limits.json";
    private final Context context;
    private final Database database;

    // Structure to hold exercise data in memory
    private JSONObject exercisesJson;
    private ExecutorService executorService;
    public ExerciseJsonManager(Context context, Database database) {
        this.context = context;
        this.database = database;
        executorService = Executors.newSingleThreadExecutor();

        initializeJsonObject();
    }

    public JSONObject getExercisesJson() {
        return exercisesJson;
    }

    /**
     * Initialize the JSON object either from file or create a new one
     */
    private void initializeJsonObject() {
        File file = new File(context.getFilesDir(), FILE_NAME);

        if (file.exists()) {
            try {
                FileInputStream fis = context.openFileInput(FILE_NAME);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();

                exercisesJson = new JSONObject(sb.toString());
                Log.d(TAG, "Loaded existing JSON file");
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error loading JSON file: " + e.getMessage());
                createNewJsonObject();
            }
        } else {
            createNewJsonObject();
        }
    }

    /**
     * Create a new JSON object with the basic structure
     */
    private void createNewJsonObject() {
        try {
            exercisesJson = new JSONObject();
            exercisesJson.put("exercises", new JSONObject());
            exercisesJson.put("userData", new JSONObject());
            Log.d(TAG, "Created new JSON structure");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON structure: " + e.getMessage());
            exercisesJson = null;
        }
    }

    /**
     * Save the current JSON object to file
     * @return true if save was successful, false otherwise
     */
    public boolean saveToFile() {
        try {
            FileOutputStream fos = this.context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(exercisesJson.toString(2).getBytes());
            fos.close();
            Log.d(TAG, "JSON file saved successfully");
            return true;
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error saving JSON file: " + e.getMessage());
            return false;
        } finally {
        }
    }

    /**
     * Get the exercise JSON object by its ID
     * @param exerciseId the ID of the exercise
     * @return the JSON object for the exercise, or null if not found
     */
    private JSONObject getExerciseById(int exerciseId) {
        if (exercisesJson == null) return null;

        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            String exerciseKey = "exercise_" + exerciseId;

            if (exercises.has(exerciseKey)) {
                return exercises.getJSONObject(exerciseKey);
            }

            // Fallback to iteration if direct access fails
            Iterator<String> keys = exercises.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject exercise = exercises.getJSONObject(key);
                if (exercise.getInt("exerciseId") == exerciseId) {
                    return exercise;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting exercise by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if an exercise exists in the JSON
     * @param exerciseId the ID of the exercise to check
     * @return true if the exercise exists, false otherwise
     */
    public boolean exerciseExists(int exerciseId) {
        return getExerciseById(exerciseId) != null;
    }

    /**
     * Add a new exercise to the JSON
     * @param exerciseId the ID of the exercise
     * @param name the name of the exercise
     * @param weightLevelIncrementLimit weight level increment limit
     * @param volumeLevelIncrementLimit volume level increment limit
     * @param volumeLevelStart volume level start value
     * @param weightLevelStart weight level start value
     * @param currentHighest current highest value
     * @return true if the exercise was added successfully, false otherwise
     */
    public boolean addExercise(int exerciseId, String name, int weightLevelIncrementLimit,
                              int volumeLevelIncrementLimit, int volumeLevelStart,
                              int weightLevelStart, int currentHighest) {
        if (name == null || name.isEmpty()) {
            Log.e(TAG, "Cannot add exercise with empty name");
            return false;
        }

        boolean success = true;

        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            String newKey = "exercise_" + exerciseId;

            // 🔄 Check if the exercise already exists and preserve weight/volume records
            JSONObject existingExercise = exercises.has(newKey) ? exercises.getJSONObject(newKey) : null;
            JSONObject preservedWeightRecords = null;
            JSONObject preservedVolumeRecords = null;

            if (existingExercise != null) {
                if (existingExercise.has("weightRecords")) {
                    preservedWeightRecords = existingExercise.getJSONObject("weightRecords");
                }
                if (existingExercise.has("volumeRecords")) {
                    preservedVolumeRecords = existingExercise.getJSONObject("volumeRecords");
                }
            }

            // ✅ Create or overwrite the exercise JSON object
            JSONObject newExercise = new JSONObject();
            newExercise.put("exerciseId", exerciseId);
            newExercise.put("name", name);
            newExercise.put("weightLevelIncrementLimit", weightLevelIncrementLimit);
            newExercise.put("volumeLevelIncrementLimit", volumeLevelIncrementLimit);
            newExercise.put("currentHighest", currentHighest);
            newExercise.put("volumeLevelStart", volumeLevelStart);
            newExercise.put("weightLevelStart", weightLevelStart);
            newExercise.put("mpGained", 0);
            newExercise.put("lowBoundVol", volumeLevelStart);
            newExercise.put("upBoundVol", volumeLevelStart + volumeLevelIncrementLimit);
            newExercise.put("lowBoundWeight", weightLevelStart);
            newExercise.put("upBoundWeight", weightLevelStart + weightLevelIncrementLimit);
            newExercise.put("currentWeightLevel", 0);
            newExercise.put("currentVolumeLevel", 0);

            // ✅ Preserve or initialize records
            newExercise.put("weightRecords", preservedWeightRecords != null ? preservedWeightRecords : new JSONObject());
            newExercise.put("volumeRecords", preservedVolumeRecords != null ? preservedVolumeRecords : new JSONObject());

            // 🔁 Overwrite or add the new exercise entry
            exercises.put(newKey, newExercise);
            saveToFile();

            Log.d(TAG, "Added/Updated exercise " + name + " (ID: " + exerciseId + ") in JSON");

        } catch (JSONException e) {
            Log.e(TAG, "Error adding/updating exercise: " + e.getMessage());
            success = false;
        }

        return success;
    }

    /**
     * Add or update a volume record for an exercise on a specific date
     * @param exerciseId the ID of the exercise
     * @param date the date of the record
     * @param volume the volume value
     * @return true if the record was added or updated successfully, false otherwise
     */
    public boolean addVolumeRecord(int exerciseId, String date, double volume) {
        if (date == null || date.isEmpty() || volume <= 0) {
            Log.e(TAG, "Invalid date or volume value");
            return false;
        }

        try {
            // Retrieve the exercise record by ID
            JSONObject exercise = getExerciseById(exerciseId);
            if (exercise == null) {
                Log.d(TAG, "Exercise ID " + exerciseId + " not found for volume record");
                return false;
            }

            // Retrieve the existing volumeRecords
            JSONObject volumeRecords = exercise.getJSONObject("volumeRecords");

            // Add the new volume record for the given date
            volumeRecords.put(date, volume);

            // Save the updated exercise data
            return saveToFile();

        } catch (JSONException e) {
            Log.e(TAG, "Error adding volume record: " + e.getMessage());
            return false;
        }
    }
    /**
     * Add or update multiple volume records for an exercise
     * @param exerciseId the ID of the exercise
     * @param volumeRecords a LinkedList of ExercisePR objects containing volume records
     * @return true if any records were added or updated successfully, false otherwise
     */
    public boolean addVolumeRecord(int exerciseId, LinkedList<ExercisePR> volumeRecords) {
        if (volumeRecords == null || volumeRecords.isEmpty()) {
            Log.e(TAG, "Volume records list is empty or null");
            return false;
        }

        boolean success = false;

        for(int i = 0 ; i< volumeRecords.size(); i++){
            ExercisePR exercisePR = volumeRecords.get(i);
            success = addVolumeRecord(exerciseId, exercisePR.getDate(), exercisePR.getPrValue());
        }
        /*
        try {
            JSONObject exercise = getExerciseById(exerciseId);
            if (exercise == null) {
                Log.e(TAG, "Exercise ID " + exerciseId + " not found");
                return false;
            }

            JSONObject records = exercise.getJSONObject("volumeRecords");

            // First, find the highest volume across all existing dates
            double highestVolume = 0;
            Iterator<String> existingDateKeys = records.keys();
            while (existingDateKeys.hasNext()) {
                String existingDate = existingDateKeys.next();
                double existingVolume = records.getDouble(existingDate);
                if (existingVolume > highestVolume) {
                    highestVolume = existingVolume;
                }
            }

            LinkedList<ExercisePR> recordsCopy = new LinkedList<>(volumeRecords);

            // Sort records by volume value (highest first) to prioritize highest volumes
            recordsCopy.sort((a, b) -> Double.compare(b.getPrValue(), a.getPrValue()));

            while (!recordsCopy.isEmpty()) {
                ExercisePR exercisePR = recordsCopy.pop();
                String date = exercisePR.getDate();
                double newVolume = exercisePR.getPrValue();

                // Skip invalid records
                if (date == null || date.isEmpty() || newVolume <= 0) {
                    continue;
                }

                // Case 1: Record for this date already exists
                if (records.has(date)) {
                    double existingVolume = records.getDouble(date);
                    if (newVolume > existingVolume) {
                        records.put(date, newVolume);
                        Log.d(TAG, "Updated volume for " + date);
                        success = true;
                    }
                }
                // Case 2: No record for this date, but volume is higher than all previous records
                else if (newVolume > highestVolume) {
                    records.put(date, newVolume);
                    highestVolume = newVolume; // Update highest volume
                    Log.d(TAG, "Added new volume for " + date + " (new highest)");
                    success = true;
                }
                // Case 3: Volume is not higher than previous records
                else {
                    Log.d(TAG, "Skipped adding volume for " + date + " as it's not higher than highest record");
                }
            }

            if (success) {
                saveToFile();
            }

            return success;
        } catch (JSONException e) {
            Log.e(TAG, "Error updating volume records: " + e.getMessage());
            return false;
        } finally {
        }

         */
        return success;
    }

    /**
     * Update all weight and volume records from the database
     * @param db the database to get records from
     */
    public void updateAllRecords(Database db) {
        // Get the highest exercise ID from the database

        int highestId = db.getExerciseIDHighest();


        if(exercisesJson == null){startProcessingExerciseData();
        }
        // Loop through each exercise ID
        for (int i = 1; i <= highestId; i++) {
            // Get all PR records for this exercise
            ArrayList<ExercisePR> weightRecords = db.getExercisePRs(i);
            LinkedList<ExercisePR> volumeRecords = db.getCumulativeVolumeByDate(i);

            // Skip if there are no records for this exercise
            if ((weightRecords == null || weightRecords.isEmpty()) &&
                    (volumeRecords == null || volumeRecords.isEmpty())) {
                Log.d(TAG, "No records found for exercise ID " + i);
                continue;
            }

            // Process weight records
            if (weightRecords != null && !weightRecords.isEmpty()) {
                for(int j = 0 ; j< weightRecords.size(); j++){
                    ExercisePR exercisePR = volumeRecords.get(j);
                    addWeightRecord(i, exercisePR.getDate(), exercisePR.getPrValue());
                }
                Log.d(TAG, "Processed " + weightRecords.size() + " weight records for exercise ID " + i);
            }

            // Process volume records
            if (volumeRecords != null && !volumeRecords.isEmpty()) {
                for (ExercisePR record : volumeRecords) {
                    try {
                        addVolumeRecord(i, record.getDate(), record.getPrValue());
                    } catch (Exception e) {
                        Log.w(TAG, "Error adding weight record for exercise ID " + i + ": " + e.getMessage());
                    }
                }
                Log.d(TAG, "Processed " + volumeRecords.size() + " volume records for exercise ID " + i);
            }
        }

        // Save all changes to file
        saveToFile();
        Log.d(TAG, "Completed updating all exercise records");
    }

    /**
     * Add or update a weight record for an exercise on a specific date
     * @param exerciseId the ID of the exercise
     * @param date the date of the record
     * @param weight the weight value
     * @return true if the record was added or updated successfully, false otherwise
     */
    public boolean addWeightRecord(int exerciseId, String date, double weight) {
        if (date == null || date.isEmpty() || weight <= 0) {
            Log.e(TAG, "Invalid date or weight value");
            return false;
        }

        try {
            JSONObject exercise = getExerciseById(exerciseId);
            if (exercise == null) {
                Log.d(TAG, "Exercise ID " + exerciseId + " not found for weight record");
                return false;
            }

            JSONObject records = exercise.getJSONObject("weightRecords");
            double highestWeight = getHighestWeight(exerciseId); // Current max

            boolean isNewRecord = false;

            try {
                //if (weight > highestWeight) {

                    // Update the records and highest weight
                    records.put(date, weight);
                    exercise.put("highestWeight", weight);
                    isNewRecord = true;
                    Log.d(TAG, "New PR set for exercise ID " + exerciseId + " on " + date + ": " + weight);

                //} else {
                    // If the weight is not a new PR, no record is saved
                    Log.d(TAG, "Weight " + weight + " is not a PR. No record saved.");
               // }

                // If the weight was higher than the current, update the highestWeight
                if (weight > highestWeight) {
                    exercise.put("highestWeight", weight);
                    Log.d(TAG, "New highest weight for exercise ID " + exerciseId + ": " + weight);
                    isNewRecord = true; // Ensure this flag is true when highest weight is updated
                }

                // Save to file if a new record was set
                if (isNewRecord) {
                    return saveToFile(); // Assuming this persists your JSON
                } else {
                    return false;
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error adding weight record for date " + date + ": " + e.getMessage());
                return false;
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error adding weight record for date " + date + ": " + e.getMessage());
            return false;
        }
    }
    /**
     * Get the highest weight record for an exercise
     * @param exerciseId the ID of the exercise
     * @return the highest weight value, or 0 if no records found
     */
    public double getHighestWeight(int exerciseId) {
        double highestWeight = 0;

        try {
            JSONObject exercise = getExerciseById(exerciseId);
            if (exercise == null) return 0;

            JSONObject records = exercise.getJSONObject("weightRecords");
            Iterator<String> dateKeys = records.keys();

            while (dateKeys.hasNext()) {
                String date = dateKeys.next();
                double weight = records.getDouble(date);
                if (weight > highestWeight) {
                    highestWeight = weight;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting highest weight: " + e.getMessage());
        } finally {
        }

        return highestWeight;
    }

    /**
     * Process the CSV import to update JSON data for exercise records
     * @return true if any records were added or updated, false otherwise
     */
    public boolean startProcessingExerciseData() {
        // Initialize ExecutorService if needed
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }

        final boolean[] results = new boolean[1];
        // Run processExerciseDataFromCSV in the background thread using ExecutorService
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = processExerciseDataFromCSV();
                results[0] = result;
                // Use Handler to post UI updates to the main thread
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (result) {
                            Log.d(TAG, "Exercise data processing completed successfully.");
                        } else {
                            Log.e(TAG, "Failed to process exercise data.");
                        }
                    }
                });
            }
        });
        return results[0];
    }

    public boolean processExerciseDataFromCSV() {
        String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d(TAG, "Processing exercise data for date: " + dateToday);

        if (exercisesJson == null || !exercisesJson.has("exercise_1") || exercisesJson.isNull("exercise_1")) {
            Cursor exercisesCursor = null;
            boolean anyUpdates = false;

            try {
                exercisesCursor = database.getAllExercises();
                if (exercisesCursor == null || exercisesCursor.getCount() == 0) {
                    Log.w(TAG, "No exercises found in the database.");
                    return false;
                }

                while (exercisesCursor.moveToNext()) {
                    int exerciseId = exercisesCursor.getInt(exercisesCursor.getColumnIndexOrThrow("exerciseID"));
                    String exerciseName = exercisesCursor.getString(exercisesCursor.getColumnIndexOrThrow("Name"));
                    Log.d(TAG, "Processing exercise: ID=" + exerciseId + ", Name=" + exerciseName);

                    // Calculate today's highest weight
                    double highestWeight = database.getCurrentHighestForExercise(exerciseId);
                    Cursor setsCursor = null;
                    try {
                        setsCursor = database.getSetsForExercise(exerciseId);
                        if (setsCursor != null && setsCursor.moveToFirst()) {
                            do {
                                String rawSetDate = setsCursor.getString(setsCursor.getColumnIndexOrThrow("Date"));
                                double weight = setsCursor.getDouble(setsCursor.getColumnIndexOrThrow("weight"));

                                if (rawSetDate != null && rawSetDate.startsWith(dateToday) && weight > highestWeight) {
                                    highestWeight = weight;
                                    Log.d(TAG, "New highest weight for today: " + weight + " on " + rawSetDate);
                                }
                            } while (setsCursor.moveToNext());
                        } else {
                            Log.w(TAG, "No sets found for exercise ID: " + exerciseId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading sets for exercise ID: " + exerciseId, e);
                    } finally {
                        if (setsCursor != null) setsCursor.close();
                    }

                    // Volume records
                    LinkedList<ExercisePR> volumeRecords = database.getCumulativeVolumeByDate(exerciseId);
                    boolean hasVolume = volumeRecords != null && !volumeRecords.isEmpty();

                    // Only add exercise if data exists (either weight or volume)
                    if (highestWeight > 0 || hasVolume) {
                        addExercise(exerciseId, exerciseName, 7, 7, database.getCurrentVolumeForExercise(exerciseId), database.getLowestWeightForExercise(exerciseId), (int) highestWeight);
                    } else {
                        Log.d(TAG, "Skipping exercise ID: " + exerciseId + " (no data to record)");
                        continue;
                    }

                    // Add weight record if applicable
                    ArrayList<ExercisePR> weightRecords = database.getExercisePRs(exerciseId);
                    boolean hasWeight = weightRecords != null && !weightRecords.isEmpty();
                    Log.d(TAG, "Weight records retrieved: " + (weightRecords != null ? weightRecords.size() : "null"));
                    if (hasWeight) {
                        for (ExercisePR record : weightRecords) {
                            try {
                                addWeightRecord(exerciseId, record.getDate(), record.getPrValue());
                            } catch (Exception e) {
                                Log.w(TAG, "Error adding weight record for exercise ID " + exerciseId + ": " + e.getMessage());
                            }
                        }
                        Log.d(TAG, "Processed " + weightRecords.size() + " weight records for exercise ID " + exerciseId);
                    }

                    // Add volume record if available
                    if (hasVolume) {
                        boolean volumeInserted = addVolumeRecord(exerciseId, volumeRecords);
                        if (volumeInserted) {
                            Log.d(TAG, "Volume record added for exercise ID: " + exerciseId);
                            anyUpdates = true;
                        } else {
                            Log.w(TAG, "Failed to insert volume record for exercise ID: " + exerciseId);
                        }
                    }
                    if(addLastUpdated(exerciseId)){
                        Log.d(TAG, "Last updated date added for exercise ID: " + exerciseId);
                    }

                }


            } catch (Exception e) {
                Log.e(TAG, "Error processing exercise data from CSV", e);
                return false;
            } finally {
                if (exercisesCursor != null) exercisesCursor.close();
            }

            Log.d(TAG, "Finished processing. Updates made: " + anyUpdates);
            return anyUpdates;

        } else {
            // If data already exists, call your updateAllRecords method
            Log.d(TAG, "exercisesJson exists. Updating all records...");
            try {
                updateAllRecords(database);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to update all records", e);
                return false;
            }
        }
    }

    /**
     * Get all exercise data as a Map for easier handling in UI
     * @return a Map with exercise IDs as keys and exercise data as values
     */

    /*
    public boolean addLastUpdated(int exerciseID) {
        JSONObject exercise = getExerciseById(exerciseID);
        if (exercise == null) {
            Log.d(TAG, "Exercise ID " + exerciseID + " not found for weight record");
            return false;
        }

        String latestDate = database.getLatestExerciseDate(exerciseID);

        try {
            // Check if there is already a "lastUpdated" value
            if (exercise.has("lastUpdated")) {
                String currentLastUpdated = exercise.getString("lastUpdated");

                // Only update if the new latestDate is different
                if (!currentLastUpdated.equals(latestDate)) {
                    // Backup current date into "previousLastUpdated"
                    exercise.put("PreviousLastUpdated", currentLastUpdated);
                    // Set new date
                    exercise.put("LastUpdated", latestDate);
                }
            } else {
                // No "lastUpdated" exists, so we just add it
                exercise.put("lastUpdated", latestDate);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating lastUpdated date", e);
            return false;
        }

        return saveToFile();
    }

     */
    public boolean addLastUpdated(int exerciseID) {
        try {
            JSONObject exercise = getExerciseById(exerciseID);
            if (exercise == null) {
                Log.e(TAG, "Exercise ID " + exerciseID + " not found for updating lastUpdated");
                return false;
            }

            // Get the latest date using your existing database method
            String latestDate = database.getLatestExerciseDate(exerciseID);
            if (latestDate == null || latestDate.isEmpty()) {
                Log.w(TAG, "No latest exercise date found for ID: " + exerciseID);
                return false;
            }

            // Get earliest date from weightRecords
            String earliestDate = null;
            JSONObject weightRecords = exercise.optJSONObject("weightRecords");
            if (weightRecords != null && weightRecords.length() > 0) {
                Iterator<String> keys = weightRecords.keys();
                while (keys.hasNext()) {
                    String recordDate = keys.next();
                    if (recordDate != null && !recordDate.isEmpty()) {
                        if (earliestDate == null || recordDate.compareTo(earliestDate) < 0) {
                            earliestDate = recordDate;
                        }
                    }
                }
            }

            // Fallback if no valid earliest date found
            if (earliestDate == null) {
                earliestDate = latestDate;
                Log.d(TAG, "No earliest date found, defaulting to latest date: " + earliestDate);
            }

            Log.d(TAG, "Before update - Exercise: " + exerciseID +
                    ", lastUpdated: " + exercise.optString("lastUpdated", "not set") +
                    ", PreviousLastUpdated: " + exercise.optString("PreviousLastUpdated", "not set"));

            // Update logic
            if (!exercise.has("lastUpdated") && !exercise.has("PreviousLastUpdated")) {
                // First-time setting
                exercise.put("lastUpdated", latestDate);
                exercise.put("PreviousLastUpdated", earliestDate);
                Log.d(TAG, "Initial set of lastUpdated: " + latestDate + ", PreviousLastUpdated: " + earliestDate);
            } else {
                String currentLastUpdated = exercise.optString("lastUpdated");
                if (!latestDate.equals(currentLastUpdated)) {
                    // Update only if there's a change
                    exercise.put("PreviousLastUpdated", currentLastUpdated);
                    exercise.put("lastUpdated", latestDate);
                    Log.d(TAG, "Updated lastUpdated from " + currentLastUpdated + " to " + latestDate);
                } else {
                    Log.d(TAG, "No update needed. latestDate matches current lastUpdated.");
                    return true;
                }
            }

            // Save changes
            boolean saved = saveToFile();
            if (!saved) {
                Log.e(TAG, "Failed to save file after updating lastUpdated for exercise " + exerciseID);
                return false;
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error in addLastUpdated for exercise " + exerciseID, e);
            return false;
        }
    }
    public Map<Integer, Map<String, Object>> getAllExerciseData() {
        Map<Integer, Map<String, Object>> result = new HashMap<>();

        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            Iterator<String> keys = exercises.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject exercise = exercises.getJSONObject(key);

                int exerciseId = exercise.getInt("exerciseId");
                Map<String, Object> exerciseData = new HashMap<>();

                exerciseData.put("name", exercise.getString("name"));
                exerciseData.put("volumeLevelIncrementLimit", exercise.getInt("volumeLevelIncrementLimit"));
                exerciseData.put("weightLevelIncrementLimit", exercise.getInt("weightLevelIncrementLimit"));
                exerciseData.put("currentHighest", exercise.getInt("currentHighest"));
                exerciseData.put("volumeLevelStart", exercise.getInt("volumeLevelStart"));
                exerciseData.put("weightLevelStart", exercise.getInt("weightLevelStart"));

                // Process weight records
                Map<String, Double> weightRecords = new HashMap<>();
                JSONObject weightRecordsJson = exercise.getJSONObject("weightRecords");
                Iterator<String> weightDateKeys = weightRecordsJson.keys();

                while (weightDateKeys.hasNext()) {
                    String date = weightDateKeys.next();
                    weightRecords.put(date, weightRecordsJson.getDouble(date));
                }
                exerciseData.put("weightRecords", weightRecords);

                // Process volume records
                Map<String, Double> volumeRecords = new HashMap<>();
                JSONObject volumeRecordsJson = exercise.getJSONObject("volumeRecords");
                Iterator<String> volumeDateKeys = volumeRecordsJson.keys();

                while (volumeDateKeys.hasNext()) {
                    String date = volumeDateKeys.next();
                    volumeRecords.put(date, volumeRecordsJson.getDouble(date));
                }
                exerciseData.put("volumeRecords", volumeRecords);

                result.put(exerciseId, exerciseData);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting all exercise data: " + e.getMessage());
        } finally {
        }

        return result;
    }

    /**
     * Get a list of all exercise IDs
     * @return an ArrayList of exercise IDs
     */
    public ArrayList<Integer> getAllExerciseIds() {
        ArrayList<Integer> exerciseIds = new ArrayList<>();

        if (exercisesJson == null) {
            Log.e(TAG, "exercisesJson is null when trying to get all exercise IDs");
            return exerciseIds;
        }

        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            Iterator<String> keys = exercises.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject exercise = exercises.getJSONObject(key);
                int exerciseId = exercise.getInt("exerciseId");

                int weightLevelIncrementLimit = exercise.optInt("weightLevelIncrementLimit", 0);
                int volumeLevelIncrementLimit = exercise.optInt("volumeLevelIncrementLimit", 0);
                int currentHighest = exercise.optInt("currentHighest", 0);
                int volumeLevelStart = exercise.optInt("volumeLevelStart", 0);
                int weightLevelStart = exercise.optInt("weightLevelStart", 0);
                int mpGained = exercise.optInt("mpGained", 0);
                int lowBoundVol = exercise.optInt("lowBoundVol", 0);
                int upBoundVol = exercise.optInt("upBoundVol", 0);
                int lowBoundWeight = exercise.optInt("lowBoundWeight", 0);
                int upBoundWeight = exercise.optInt("upBoundWeight", 0);
                int currentWeightLevel = exercise.optInt("currentWeightLevel", 0);
                int currentVolumeLevel = exercise.optInt("currentVolumeLevel", 0);

                // Updated condition: must have meaningful data AND both increments equal 7
                /*
                boolean hasMeaningfulData = (
                        weightLevelIncrementLimit != 0 ||
                                volumeLevelIncrementLimit != 0 ||
                                currentHighest != 0 ||
                                volumeLevelStart != 0 ||
                                weightLevelStart != 0 ||
                                mpGained != 0 ||
                                lowBoundVol != 0 ||
                                upBoundVol != 0 ||
                                lowBoundWeight != 0 ||
                                upBoundWeight != 0 ||
                                currentWeightLevel != 0 ||
                                currentVolumeLevel != 0
                );

                 */

                boolean hasCorrectIncrements = weightLevelIncrementLimit == 7 && volumeLevelIncrementLimit == 7;

                if (hasCorrectIncrements) {
                    exerciseIds.add(exerciseId);
                } else {
                    Log.d(TAG, "Skipping exercise ID " + exerciseId + " - Data not meaningful or increment limits not both 7");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting all exercise IDs: " + e.getMessage());
        }

        return exerciseIds;
    }

    /**
     * Get a list of PRs for an exercise
     * @param exerciseID the ID of the exercise
     * @return a List of PRs objects
     */
    public List<PRs> getPRs(int exerciseID) {
        List<PRs> prs = new ArrayList<>();

        try {
            JSONObject exercise = getExerciseById(exerciseID);
            if (exercise == null) {
                Log.e(TAG, "Exercise not found for ID: " + exerciseID);
                return prs;
            }

            if (!exercise.has("weightRecords")) {
                Log.e(TAG, "No weight records found for exercise ID: " + exerciseID);
                return prs;
            }

            JSONObject records = exercise.getJSONObject("weightRecords");
            for (Iterator<String> it = records.keys(); it.hasNext(); ) {
                String dateStr = it.next();
                try {
                    double prValue = records.getDouble(dateStr);
                    prs.add(new PRs((int)prValue, dateStr));
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing PR value for date " + dateStr + ": " + e.getMessage());
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting exercise PRs: " + e.getMessage());
        } finally {
        }

        return prs;
    }

    /**
     * Get a list of volume records for an exercise
     * @param exerciseID the ID of the exercise
     * @return a List of ExercisePR objects
     */
    public List<ExercisePR> getVolumes(int exerciseID) {
        List<ExercisePR> volumes = new ArrayList<>();

        try {
            JSONObject exercise = getExerciseById(exerciseID);
            if (exercise == null) {
                Log.e(TAG, "Exercise not found for ID: " + exerciseID);
                return volumes;
            }

            if (!exercise.has("volumeRecords")) {
                Log.e(TAG, "No volume records found for exercise ID: " + exerciseID);
                return volumes;
            }

            JSONObject records = exercise.getJSONObject("volumeRecords");
            for (Iterator<String> it = records.keys(); it.hasNext(); ) {
                String dateStr = it.next();
                try {
                    double prValue = records.getDouble(dateStr);
                    volumes.add(new ExercisePR(dateStr, (int)prValue));
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing volume value for date " + dateStr + ": " + e.getMessage());
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting exercise volumes: " + e.getMessage());
        } finally {
        }

        return volumes;
    }
    public String getExerciseName(int exerciseId) {
        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            Iterator<String> keys = exercises.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject exercise = exercises.getJSONObject(key);
                if (exercise.getInt("exerciseId") == exerciseId) {
                    return exercise.getString("name");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting exercise name: " + e.getMessage());
        }
        return null;
    }

    public int getWeightLevelIncrementLimit(int exerciseId) {
        return getIntField(exerciseId, "weightLevelIncrementLimit");
    }

    public int getVolumeLevelIncrementLimit(int exerciseId) {
        return getIntField(exerciseId, "volumeLevelIncrementLimit");
    }

    public int getCurrentHighest(int exerciseId) {
        return getIntField(exerciseId, "currentHighest");
    }

    public int getVolumeLevelStart(int exerciseId) {
        return getIntField(exerciseId, "volumeLevelStart");
    }

    public int getWeightLevelStart(int exerciseId) {
        return getIntField(exerciseId, "weightLevelStart");
    }
    public int CurrentPR(int exerciseId) {
        if(getIntField(exerciseId, "Current_Highest") < 0){
            return 0;
        }else{
            return getIntField(exerciseId, "Current_Highest");
        }

    }

    public int getMpGained(int exerciseId) {
        return getIntField(exerciseId, "mpGained");
    }

    public int getLowBoundVol(int exerciseId) {
        return getIntField(exerciseId, "lowBoundVol");
    }

    public int getUpBoundVol(int exerciseId) {
        return getIntField(exerciseId, "upBoundVol");
    }

    public int getLowBoundWeight(int exerciseId) {
        return getIntField(exerciseId, "lowBoundWeight");
    }

    public int getUpBoundWeight(int exerciseId) {
        return getIntField(exerciseId, "upBoundWeight");
    }

    private int getIntField(int exerciseId, String fieldName) {
        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            Iterator<String> keys = exercises.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject exercise = exercises.getJSONObject(key);
                if (exercise.getInt("exerciseId") == exerciseId) {
                    return exercise.getInt(fieldName);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting field " + fieldName + ": " + e.getMessage());
        }
        return -1;
    }

    // === SETTERS ===

    public boolean setExerciseName(int exerciseId, String newName) {
        if (newName == null || newName.isEmpty()) {
            Log.e(TAG, "Cannot set empty exercise name");
            return false;
        }
        return setStringField(exerciseId, "name", newName);
    }

    public boolean setWeightLevelIncrementLimit(int exerciseId, int newLimit) {
        if (newLimit < 0) {
            Log.e(TAG, "Cannot set negative increment limit");
            return false;
        }
        return setIntField(exerciseId, "weightLevelIncrementLimit", newLimit);
    }

    public boolean setVolumeLevelIncrementLimit(int exerciseId, int newLimit) {
        if (newLimit < 0) {
            Log.e(TAG, "Cannot set negative increment limit");
            return false;
        }
        return setIntField(exerciseId, "volumeLevelIncrementLimit", newLimit);
    }

    public boolean setCurrentHighest(int exerciseId, int value) {
        return setIntField(exerciseId, "currentHighest", value);
    }

    public boolean setVolumeLevelStart(int exerciseId, int value) {
        return setIntField(exerciseId, "volumeLevelStart", value);
    }

    public boolean setWeightLevelStart(int exerciseId, int value) {
        return setIntField(exerciseId, "weightLevelStart", value);
    }

    public boolean setMpGained(int exerciseId, int value) {
        if (value < 0) {
            Log.e(TAG, "Cannot set negative MP gained");
            return false;
        }
        return setIntField(exerciseId, "mpGained", value);
    }

    public boolean setLowBoundVol(int exerciseId, int value) {
        return setIntField(exerciseId, "lowBoundVol", value);
    }

    public boolean setUpBoundVol(int exerciseId, int value) {
        return setIntField(exerciseId, "upBoundVol", value);
    }

    public boolean setLowBoundWeight(int exerciseId, int value) {
        return setIntField(exerciseId, "lowBoundWeight", value);
    }

    public boolean setUpBoundWeight(int exerciseId, int value) {
        return setIntField(exerciseId, "upBoundWeight", value);
    }


    private boolean setIntField(int exerciseId, String fieldName, int value) {
        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            Iterator<String> keys = exercises.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject exercise = exercises.getJSONObject(key);
                if (exercise.getInt("exerciseId") == exerciseId) {
                    exercise.put(fieldName, value);
                    saveToFile();
                    Log.d(TAG, "Updated " + fieldName + " for exercise ID " + exerciseId);
                    return true;
                }
            }
            Log.d(TAG, "Exercise ID " + exerciseId + " not found for update");
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "Error updating " + fieldName + ": " + e.getMessage());
            return false;
        }
    }

    private boolean setStringField(int exerciseId, String fieldName, String value) {
        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            Iterator<String> keys = exercises.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject exercise = exercises.getJSONObject(key);
                if (exercise.getInt("exerciseId") == exerciseId) {
                    exercise.put(fieldName, value);
                    saveToFile();
                    Log.d(TAG, "Updated " + fieldName + " for exercise ID " + exerciseId);
                    return true;
                }
            }
            Log.d(TAG, "Exercise ID " + exerciseId + " not found for update");
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "Error updating " + fieldName + ": " + e.getMessage());
            return false;
        }
    }
}