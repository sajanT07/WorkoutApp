package com.example.myapplication.ui.PowerBar;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.myapplication.databases.Database;
import com.example.myapplication.databases.ExerciseJsonManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PowerBar {
    public int currentLevel;
    public int currentLowBound;
    public int exerciseID;
    public int levelStart;
    public int levelIncrementLimit;
    public int lowBound;
    public int upBound;

    public int overflowOverLevel;
    public int newOverflowOverLevel;
    public int currentHighest;
    public int newCurrentHighest;

    public int currentMp;
    public JSONObject exercisesJson;

    int levelGained;

    public String powerBarType;
    public String lastUpdated;
    public Context context;
    public Map<String, Integer> records;

    private Database database;
    private ExerciseJsonManager man;

    // Added logging tag
    private static final String LOG_TAG = "PowerBar";

    public PowerBar(int exerciseID, String powerBarType, Context context) {
        this.exerciseID = exerciseID;
        this.powerBarType = powerBarType;
        this.context = context;

        Log.d(LOG_TAG, "Initializing PowerBar - exerciseID: " + exerciseID + ", type: " + powerBarType);

        Database database = new Database(context);
        this.database = database;
        ExerciseJsonManager man = new ExerciseJsonManager(context, database);
        this.man = man;
        this.exercisesJson = man.getExercisesJson();
        database.close();

        getData();
        // Removed duplicate call to getPreviousHighest() since it's already called in getData()
        calculateChanges();

        if (currentLevel > 0) {
            // You leveled up — recalculate bounds
            currentLowBound = levelStart + (currentLevel * levelIncrementLimit);
            upBound = currentLowBound + levelIncrementLimit;

            // Calculate how far above the new level's lower bound the new highest value is
            newOverflowOverLevel = newCurrentHighest - currentLowBound;

            Log.d(LOG_TAG, "Level gained. Updated bounds: currentLowBound=" + currentLowBound +
                    ", upBound=" + upBound + ", newOverflowOverLevel=" + newOverflowOverLevel);

        } else {
            // No level gained — retain current bounds and just calculate overflow
            currentLowBound = levelStart ;// + (currentLevel * levelIncrementLimit);
            upBound = currentLowBound + levelIncrementLimit;

            newOverflowOverLevel = newCurrentHighest - currentLowBound;
            if (newOverflowOverLevel < 0) newOverflowOverLevel = 0; // no negative overflow

            Log.d(LOG_TAG, "No level gained. Bounds unchanged: currentLowBound=" + currentLowBound +
                    ", upBound=" + upBound + ", newOverflowOverLevel=" + newOverflowOverLevel);
        }

// Optional: Update previous overflowOverLevel too
        overflowOverLevel = currentHighest - currentLowBound;
        if (overflowOverLevel < 0) overflowOverLevel = 0;


        Log.d(LOG_TAG, "calculated newOverflowOverLevel: " + newOverflowOverLevel + " from newCurrentHighest: " +
                newCurrentHighest + " % currentLowBound: " + currentLowBound);

        // Log final values for debugging
        logFinalValues();
    }

    private void logFinalValues() {
        Log.d(LOG_TAG, "Final values - Level: " + currentLevel +
                ", LowBound: " + currentLowBound +
                ", UpBound: " + upBound +
                ", Curr/NewHighest: " + currentHighest + "/" + newCurrentHighest +
                ", Overflow/NewOverflow: " + overflowOverLevel + "/" + newOverflowOverLevel);
    }

    public int getLevelGained() {
        return levelGained;
    }

    public int getNewOverflowOverLevel() {
        return newOverflowOverLevel;
    }

    public int getLevelIncrementLimit() {
        return levelIncrementLimit;
    }

    private void calculateChanges() {
        if (records == null || records.isEmpty()) {
            Log.d(LOG_TAG, "calculateChanges: No records, skipping");
            return;
        }

        Log.d(LOG_TAG, "calculateChanges: newCurrentHighest=" + newCurrentHighest +
                ", levelStart=" + levelStart + ", levelIncrementLimit=" + levelIncrementLimit);

        int calculatedLevel = 0;
        if(levelIncrementLimit!= 0){
             calculatedLevel = (newCurrentHighest - levelStart) / levelIncrementLimit;
            //calculatedLevel = Math.round(0, calculatedLevel);
        }


        levelGained = calculatedLevel - currentLevel;
        if (levelGained <= 0) {
            Log.d(TAG, "No level gained");
            return;
        }

        currentLevel = calculatedLevel;
        Log.d(TAG, "Level Gained: " + levelGained + ", New Level: " + currentLevel);

        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            for (Iterator<String> it = exercises.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject exerciseObj = exercises.getJSONObject(key);
                if (exerciseObj.getInt("exerciseId") == exerciseID) {
                    int newLow = levelStart + (currentLevel * levelIncrementLimit);
                    int newUp = newLow + levelIncrementLimit;

                    Log.d(LOG_TAG, "calculateChanges: Setting new bounds - newLow=" + newLow + ", newUp=" + newUp);

                    if (powerBarType.equals("weight")) {
                        exerciseObj.put("currentWeightLevel", currentLevel);
                        exerciseObj.put("lowBoundWeight", newLow);
                        exerciseObj.put("upBoundWeight", newUp);
                    } else if (powerBarType.equals("volume")) {
                        exerciseObj.put("currentVolumeLevel", currentLevel);
                        exerciseObj.put("lowBoundVol", newLow);
                        exerciseObj.put("upBoundVol", newUp);
                    }

                    exerciseObj.put("mpGained", exerciseObj.optInt("mpGained", 0) + levelGained);
                    man.saveToFile();
                    break;
                }
            }

            JSONObject user = exercisesJson.optJSONObject("userData");
            if (user == null) {
                user = new JSONObject();
                exercisesJson.put("userData", user);
            }

            user.put("mp", user.optInt("mp", 0) + levelGained);
            currentMp = user.getInt("mp");
            man.saveToFile();

        } catch (Exception e) {
            Log.e(TAG, "Error in calculateChanges", e);
        }
    }

    private void saveExerciseJsonToStorage() {
        try {
            SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("exerciseData", exercisesJson.toString()); // Save the full structure
            editor.apply();
            Log.d("PowerBar", "Exercise data saved successfully.");
        } catch (Exception e) {
            Log.e("PowerBar", "Failed to save exercisesJson", e);
        }
    }

    private String getPreviousLastUpdated() {
        try {
            JSONObject exercises = exercisesJson.getJSONObject("exercises");
            Iterator<String> keys = exercises.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject exerciseObj = exercises.getJSONObject(key);
                if (exerciseObj.getInt("exerciseId") == exerciseID) {
                    return exerciseObj.optString("lastUpdated", ""); // Fixed case consistency
                }
            }
        } catch (JSONException e) {
            Log.e("PowerBar", "Error getting previousLastUpdated", e);
        }
        return "";
    }

    private int getPreviousHighest() {
        if (records == null || records.isEmpty() || lastUpdated == null || lastUpdated.isEmpty()) {
            Log.d(LOG_TAG, "getPreviousHighest: No valid records or lastUpdated, returning 0");
            return 0;
        }

        int previousHighest = 0;

        for (Map.Entry<String, Integer> entry : records.entrySet()) {
            String date = entry.getKey();
            int value = entry.getValue();

            Log.d("PreviousHigh", "Checking record - Date: " + date + ", Value: " + value + ", lastUpdated: " + lastUpdated);

            if (date.compareTo(lastUpdated) < 0 && value > previousHighest) {
                previousHighest = value;
            }
        }

        currentHighest = previousHighest;
        Log.d("PreviousHigh", "Final previousHighest: " + previousHighest);
        return previousHighest;
    }

    private void getData() {
        if (exercisesJson == null) {
            Log.e("JSON_ERROR", "exercisesJson is null");
            return;
        }

        try {
            JSONObject exercisesObj = exercisesJson.getJSONObject("exercises");
            Iterator<String> keys = exercisesObj.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject exerciseObj = exercisesObj.getJSONObject(key);

                if (exerciseObj.has("exerciseId") && exerciseObj.getInt("exerciseId") == exerciseID) {
                    Log.d(LOG_TAG, "getData: Found exercise with ID " + exerciseID);
                    records = new HashMap<>();
                    newCurrentHighest = 0;

                    if (powerBarType.equalsIgnoreCase("weight")) {
                        extractWeightData(exerciseObj);
                    } else if (powerBarType.equalsIgnoreCase("volume")) {
                        extractVolumeData(exerciseObj);
                    } else {
                        Log.e("PowerBar", "Invalid powerBarType: " + powerBarType);
                    }

                    // Get previous highest just once
                    currentHighest = getPreviousHighest();
                    break; // exit once target exercise is found
                }
            }
        } catch (JSONException e) {
            Log.e("PowerBar", "Error in getData", e);
        }
    }

    private void extractWeightData(JSONObject exerciseObj) throws JSONException {
        currentLevel = exerciseObj.optInt("currentWeightLevel", 0);
        levelStart = exerciseObj.optInt("weightLevelStart", 0);
        levelIncrementLimit = exerciseObj.optInt("weightLevelIncrementLimit", 5);
        //lowBound = exerciseObj.optInt("lowBoundWeight", 0);
        //upBound = exerciseObj.optInt("upBoundWeight", 5);
        currentMp = exerciseObj.optInt("mpGained", 0);

        lastUpdated = exerciseObj.optString("LastUpdated", "");
        parseRecords(exerciseObj, "weightRecords");

        Log.d(LOG_TAG, "extractWeightData: level=" + currentLevel + ", start=" + levelStart +
                ", increment=" + levelIncrementLimit + ", low=" + lowBound + ", up=" + upBound);
    }

    private void extractVolumeData(JSONObject exerciseObj) throws JSONException {
        currentLevel = exerciseObj.optInt("currentVolumeLevel", 0);
        levelStart = exerciseObj.optInt("volumeLevelStart", 0);
        levelIncrementLimit = exerciseObj.optInt("volumeLevelIncrementLimit", 5);
        //lowBound = exerciseObj.optInt("lowBoundVol", 0);
        //upBound = exerciseObj.optInt("upBoundVol", 5);
        currentMp = exerciseObj.optInt("mpGained", 0);

        // Fixed inconsistent case
        lastUpdated = exerciseObj.optString("LastUpdated", "");
        parseRecords(exerciseObj, "volumeRecords");

        Log.d(LOG_TAG, "extractVolumeData: level=" + currentLevel + ", start=" + levelStart +
                ", increment=" + levelIncrementLimit + ", low=" + lowBound + ", up=" + upBound);
    }

    private void parseRecords(JSONObject exerciseObj, String recordKey) throws JSONException {
        records = new HashMap<>();
        newCurrentHighest = 0;

        JSONObject recordObj = exerciseObj.optJSONObject(recordKey);
        if (recordObj != null) {
            Iterator<String> recordKeys = recordObj.keys();
            while (recordKeys.hasNext()) {
                String date = recordKeys.next();
                int value = recordObj.optInt(date, -1);
                if (value < 0) continue;
                records.put(date, value);
                if (value > newCurrentHighest) newCurrentHighest = value;
            }
            Log.d(LOG_TAG, "parseRecords: Found " + records.size() + " records, newCurrentHighest=" + newCurrentHighest);
        } else {
            Log.d(LOG_TAG, "parseRecords: No records found for key " + recordKey);
        }
    }
     public int getLevelsGained(){
        return levelGained;
    }
}