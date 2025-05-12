package com.example.myapplication.databases;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.myapplication.ui.ExerciseInfo.DataModels.PRs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
//import org.apache.poi.sl.draw.geom.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database extends SQLiteOpenHelper {
    private static final String TAG = "Database";
    private static final String DATABASE_NAME = "workouts.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_SETS = "sets";
    private static final String TABLE_EXERCISES = "exercises";
    private static final String TABLE_DATES = "dates";
    private static final Logger log = LogManager.getLogger(Database.class);

    private LinkedList<IDExerciseHighest> exercisesToConfigure = new LinkedList<>();
    private SQLiteDatabase db;

    Context context;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();



    // Table creation queries
    private static final String CREATE_SETS_TABLE =
            "CREATE TABLE " + TABLE_SETS + " (" +
                    "setID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "weight INTEGER, " +
                    "setOrder INTEGER, " +
                    "reps INTEGER, " +
                    "exerciseID INTEGER, " +
                    "workoutID INTEGER, " +
                    "FOREIGN KEY (exerciseID) REFERENCES exercises(exerciseID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (workoutID) REFERENCES dates(workoutID) ON DELETE CASCADE);";

    private static final String CREATE_EXERCISES_TABLE =
            "CREATE TABLE " + TABLE_EXERCISES + " (" +
                    "exerciseID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Name TEXT UNIQUE, Volume INTEGER);";

    private static final String CREATE_DATES_TABLE =
            "CREATE TABLE " + TABLE_DATES + " (" +
                    "workoutID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Date TEXT);";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("Database", "Database constructor called");
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;

        // Only initialize the database when you need it
        this.db = this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EXERCISES_TABLE);
        db.execSQL(CREATE_DATES_TABLE);
        db.execSQL(CREATE_SETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATES);
        onCreate(db);
    }

    // Get total workout summary (Total Volume, Number of Sets, Number of Exercises)
    public Cursor getWorkoutSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " +
                "(SELECT COUNT(*) FROM " + TABLE_EXERCISES + ") AS exerciseCount, " +
                "(SELECT COUNT(*) FROM " + TABLE_SETS + ") AS setCount, " +
                "(SELECT SUM(weight * reps) FROM " + TABLE_SETS + ") AS totalVolume", null);
    }

    public Cursor getWorkoutTotals() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT SUM(exercises), SUM(sets), SUM(volume) FROM workouts", null);
    }

    // Get all exercises
    public Cursor getAllExercises() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EXERCISES, null);
    }

    // Get sets for a specific exercise (Chronological Order)
    public Cursor getSetsForExercise(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT s.setID, d.Date, s.weight, s.reps, s.setOrder " +
                        "FROM " + TABLE_SETS + " s " +
                        "JOIN " + TABLE_DATES + " d ON s.workoutID = d.workoutID " +
                        "WHERE s.exerciseID = ? " +
                        "ORDER BY d.Date ASC, s.setOrder ASC",
                new String[]{String.valueOf(exerciseId)});
    }

    public int getExerciseID(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT exerciseID FROM " + TABLE_EXERCISES + " WHERE Name = ?", new String[]{name});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id; // Exercise already exists, return its ID
        }
        cursor.close();
        return -1; // Exercise not found
    }

    public String getExerciseName(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Name FROM " + TABLE_EXERCISES + " WHERE exerciseID = ?",
                new String[]{String.valueOf(exerciseId)});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0); // Fixed index to 0
            cursor.close();
            return name; // ID already exists, return its name
        }
        cursor.close();
        return ""; // Exercise not found
    }

    public int getExerciseVolume(int exerciseID) {
        SQLiteDatabase db = this.getReadableDatabase();
        int volume = 0;

        Cursor cursor = db.rawQuery("SELECT Volume FROM " + TABLE_EXERCISES + " WHERE exerciseID = ?",
                new String[]{String.valueOf(exerciseID)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                volume = cursor.getInt(0);
            }
            cursor.close();
        }

        return volume;
    }
    public int getCurrentVolumeForExercise(long exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(s.weight * s.reps) AS volume " +
                "FROM " + TABLE_SETS + " s " +
                "JOIN " + TABLE_DATES + " d ON s.workoutID = d.workoutID " +
                "WHERE s.exerciseID = ? " +
                "GROUP BY s.workoutID " +
                "ORDER BY d.Date DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});
        int volume = 0;
        if (cursor.moveToFirst()) {
            volume = cursor.getInt(cursor.getColumnIndexOrThrow("volume"));
        }
        cursor.close();
        return volume;
    }
    public int getLowestWeightForExercise(long exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MIN(weight) AS minWeight FROM " + TABLE_SETS + " WHERE exerciseID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});
        int minWeight = 0;
        if (cursor.moveToFirst()) {
            minWeight = cursor.getInt(cursor.getColumnIndexOrThrow("minWeight"));
        }
        cursor.close();
        return minWeight;
    }
    public int getLowestVolumeForExercise(long exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(weight * reps) AS volume " +
                "FROM " + TABLE_SETS +
                " WHERE exerciseID = ? GROUP BY workoutID ORDER BY volume ASC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});
        int minVolume = 0;
        if (cursor.moveToFirst()) {
            minVolume = cursor.getInt(cursor.getColumnIndexOrThrow("volume"));
        }
        cursor.close();
        return minVolume;
    }

    public Cursor getLevelsForExercise(int exerciseID) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Levels WHERE exerciseID = ?", new String[]{String.valueOf(exerciseID)});
    }

    public int getWorkoutID(String date) {
        return 0;
    }

    public boolean setExists(int exerciseID, int workoutID, int weight, int reps) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT setID FROM " + TABLE_SETS +
                        " WHERE exerciseID = ? AND workoutID = ? AND weight = ? AND reps = ?",
                new String[]{String.valueOf(exerciseID), String.valueOf(workoutID),
                        String.valueOf(weight), String.valueOf(reps)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // INSERTING DATA (WITH CHECKS)
    public long addDate(String date) {
        int existingID = getWorkoutID(date);
        if (existingID != -1) {
            return existingID; // Return existing date ID instead of inserting a duplicate
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Date", date);
        return db.insert(TABLE_DATES, null, values);
    }

    public long addSet(int weight, int reps, int exerciseID, int workoutID) {
        if (setExists(exerciseID, workoutID, weight, reps)) {
            return -1; // Set already exists, do not insert
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("weight", weight);
        values.put("reps", reps);
        values.put("exerciseID", exerciseID);
        values.put("workoutID", workoutID); // Fixed from dateID to workoutID

        long result = db.insert(TABLE_SETS, null, values);

        // Update exercise volume
        if (result != -1) {
            updateExerciseVolume(exerciseID, weight * reps, true);
        }

        return result;
    }

    // BULK IMPORT FROM CSV
    public void importFromCSV(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        int successCount = 0;
        int errorCount = 0;

        // Clear previous list of exercises to configure
        exercisesToConfigure = new LinkedList<>();

        // Columns to ignore
        Set<String> ignoreColumns = new HashSet<>(Arrays.asList(
                "Duration (sec)", "RPE", "Distance (meters)", "Seconds", "Notes", "Workout Notes"
        ));

        final int BATCH_SIZE = 100;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String headerLine = reader.readLine(); // Read header

            if (headerLine == null) {
                throw new IOException("CSV is empty!");
            }

            String[] headers = headerLine.split(";");
            Map<String, Integer> columnMap = new HashMap<>();

            // Build column index map (skip ignored columns)
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].replace("\"", "").trim();
                if (!ignoreColumns.contains(header)) {
                    columnMap.put(header, i);
                }
            }

            // Verify required columns exist
            String[] requiredColumns = {"Workout #", "Date", "Exercise Name", "Set Order", "Weight (kg)", "Reps"};
            for (String column : requiredColumns) {
                if (!columnMap.containsKey(column)) {
                    throw new IOException("Required column missing: " + column);
                }
            }

            String line;
            int lineNumber = 1;
            ArrayList<Integer> processedExerciseIds = FillProcessedExercise();

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    String[] parts = line.split(";");
                    // Clean up quotes
                    for (int i = 0; i < parts.length; i++) {
                        parts[i] = parts[i].replace("\"", "").trim();
                    }

                    // Extract relevant fields based on header map
                    String workoutNumber = getSafeString(parts, columnMap.get("Workout #"), lineNumber, "Workout #");
                    String date = getSafeString(parts, columnMap.get("Date"), lineNumber, "Date");
                    String exerciseName = getSafeString(parts, columnMap.get("Exercise Name"), lineNumber, "Exercise Name");

                    // Skip if any required field is missing
                    if (workoutNumber.isEmpty() || date.isEmpty() || exerciseName.isEmpty()) {
                        Log.w(TAG, "Skipping line " + lineNumber + " due to missing required fields");
                        errorCount++;
                        continue;
                    }

                    int setOrder = parseSafeInt(parts, columnMap.get("Set Order"), lineNumber, "Set Order");
                    float weight = parseSafeFloat(parts, columnMap.get("Weight (kg)"), lineNumber, "Weight (kg)");
                    int reps = parseSafeInt(parts, columnMap.get("Reps"), lineNumber, "Reps");

                    // Skip rows with invalid numeric data
                    if (setOrder <= 0 || weight <= 0 || reps <= 0) {
                        Log.w(TAG, "Skipping line " + lineNumber + " due to invalid numeric data");
                        errorCount++;
                        continue;
                    }

                    // 1. Add/Update workout
                    long workoutID = addOrUpdateWorkout(workoutNumber, date);
                    if (workoutID == -1) {
                        Cursor c = db.query(TABLE_DATES, new String[]{"workoutID"},
                                "workoutID = ?", new String[]{workoutNumber}, null, null, null);
                        if (c != null && c.moveToFirst()) {
                            workoutID = c.getLong(0);
                            c.close();
                        } else {
                            Log.e(TAG, "Failed to get existing workout ID for: " + workoutNumber);
                            errorCount++;
                            continue;
                        }
                    }

                    // 2. Add/Update exercise
                    long exerciseID = addExercise(exerciseName);
                    if (exerciseID == -1) {
                        exerciseID = getExerciseID(exerciseName);
                        if (exerciseID == -1) {
                            Log.e(TAG, "Failed to get existing exercise ID for: " + exerciseName);
                            errorCount++;
                            continue;
                        }
                    }

                    // Add to configuration list if we haven't processed this exercise before
                    if (!processedExerciseIds.contains((int)exerciseID)) {
                        double highest = getCurrentHighestForExercise(exerciseID);
                        exercisesToConfigure.add(new IDExerciseHighest((int)exerciseID, exerciseName, highest));
                        processedExerciseIds.add((int)exerciseID);
                    }else{
                        Log.w(TAG, "Already added Exercise");
                    }

                    // 3. Add set
                    if (weight > 0 && reps > 0) {
                        addOrUpdateSet(workoutID, exerciseID, setOrder, weight, reps);
                        successCount++;
                    } else {
                        Log.w(TAG, "Skipped set with 0 weight or reps at line " + lineNumber);
                    }

                    if (successCount % BATCH_SIZE == 0) {
                        db.setTransactionSuccessful();  // Commit the transaction
                        db.endTransaction();  // End the current transaction
                        db.beginTransaction();  // Start a new transaction
                    }

                } catch (NumberFormatException e) {
                    Log.e(TAG, "Number parsing error at line " + lineNumber + ": " + e.getMessage());
                    errorCount++;
                } catch (Exception e) {
                    Log.e(TAG, "Error processing line " + lineNumber + ": " + e.getMessage());
                    errorCount++;
                }
            }

            db.setTransactionSuccessful();
            Log.i(TAG, "Import completed: " + successCount + " rows successful, " + errorCount + " errors");

            // Clean up any invalid data
            cleanUp();

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
            db.endTransaction();
        }
    }
    public LinkedList<IDExerciseHighest> getLinkedtoAdd() {
        return exercisesToConfigure;
    }

    public ArrayList<ExercisePR> getExercisePRs(long exerciseId) {
        ArrayList<ExercisePR> prsList = new ArrayList<>();
        Map<String, Double> weightMap = new HashMap<>(); // Max weight per date

        String querySets = "SELECT weight, workoutID FROM sets WHERE exerciseID = ?";
        Cursor cursorSets = null;

        try {
            cursorSets = db.rawQuery(querySets, new String[]{String.valueOf(exerciseId)});

            if (cursorSets != null && cursorSets.moveToFirst()) {
                do {
                    int weightIndex = cursorSets.getColumnIndex("weight");
                    int workoutIDIndex = cursorSets.getColumnIndex("workoutID");

                    if (weightIndex == -1 || workoutIDIndex == -1) continue;

                    double weight = cursorSets.getDouble(weightIndex);
                    int workoutID = cursorSets.getInt(workoutIDIndex);

                    // Get the workout date for this workoutID
                    String queryDate = "SELECT Date FROM dates WHERE workoutID = ?";
                    Cursor dateCursor = db.rawQuery(queryDate, new String[]{String.valueOf(workoutID)});
                    String date = null;

                    if (dateCursor != null && dateCursor.moveToFirst()) {
                        int dateIndex = dateCursor.getColumnIndex("Date");
                        if (dateIndex != -1) {
                            date = dateCursor.getString(dateIndex);
                        }
                        dateCursor.close();
                    }

                    if (date != null) {
                        // Store the highest weight for each date
                        if (!weightMap.containsKey(date) || weight > weightMap.get(date)) {
                            weightMap.put(date, weight);
                        }
                    }
                } while (cursorSets.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error in getExercisePRs", e);
        } finally {
            if (cursorSets != null) cursorSets.close();
        }

        // Sort the dates in descending order (newest first)
        List<String> sortedDates = new ArrayList<>(weightMap.keySet());
        Collections.sort(sortedDates, Collections.reverseOrder());

        for (String date : sortedDates) {
            double maxWeight = weightMap.get(date);
            prsList.add(new ExercisePR(date, (int) maxWeight)); // Convert to int if needed
        }

        return prsList;
    }

    public LinkedList<ExercisePR> getCumulativeVolumeByDate(Integer ExerciseID) {
        LinkedList<ExercisePR> volumeList = new LinkedList<>();
        int runningTotal = 0;

        // Change the ORDER BY clause to ascending (ASC) instead of descending (DESC)
        String query = "SELECT d.Date, SUM(s.weight * s.reps) as dailyVolume " +
                "FROM " + TABLE_SETS + " s " +
                "JOIN " + TABLE_DATES + " d ON s.workoutID = d.workoutID " +
                "JOIN " + TABLE_EXERCISES + " e ON s.exerciseID = e.exerciseID " +
                "WHERE e.exerciseID = ? " +
                "GROUP BY d.Date " +
                "ORDER BY d.Date ASC;";  // Change to ascending order for cumulative volume

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(ExerciseID)});

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow("Date"));
                int dailyVolume = cursor.getInt(cursor.getColumnIndexOrThrow("dailyVolume"));
                runningTotal += dailyVolume;
                volumeList.add(new ExercisePR(date, runningTotal));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return volumeList;
    }

    public String getEarliestExerciseDate(int exerciseID) {
        String earliestDate = null;
        Cursor cursor = null;

        try {
            // Query joins the sets table with the dates table to find the earliest date
            // for the specified exercise ID
            String query = "SELECT d.Date FROM " + TABLE_DATES + " d " +
                    "JOIN " + TABLE_SETS + " s ON d.workoutID = s.workoutID " +
                    "WHERE s.exerciseID = ? " +
                    "ORDER BY d.Date ASC LIMIT 1";

            cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseID)});

            if (cursor != null && cursor.moveToFirst()) {
                earliestDate = cursor.getString(0);
                Log.d(TAG, "Earliest date for exercise ID " + exerciseID + ": " + earliestDate);
            } else {
                Log.d(TAG, "No records found for exercise ID: " + exerciseID);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting earliest date for exercise ID: " + exerciseID, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return earliestDate;
    }
    private ArrayList<Integer> FillProcessedExercise(){

        ExerciseJsonManager man = new ExerciseJsonManager(context, this);
        return man.getAllExerciseIds();
    }
    private void cleanUp() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SETS + " WHERE setOrder = 0");
    }

    public String getLatestDate(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(Date) FROM " + TABLE_DATES , null);
        return cursor.getString(0);
    }

    public String getLatestExerciseDate(int exerciseID) {
        String latestDate = null;

        // Step 1: Get workoutIDs for the given exerciseID
        String workoutIdQuery = "SELECT DISTINCT workoutID FROM " + TABLE_SETS + " WHERE exerciseID = ?";
        Cursor workoutCursor = db.rawQuery(workoutIdQuery, new String[]{String.valueOf(exerciseID)});

        if (workoutCursor != null) {
            List<String> workoutIds = new ArrayList<>();

            while (workoutCursor.moveToNext()) {
                workoutIds.add(workoutCursor.getString(workoutCursor.getColumnIndexOrThrow("workoutID")));
            }
            workoutCursor.close();

            // If we found any workoutIDs, continue
            if (!workoutIds.isEmpty()) {
                // Step 2: Build a query to get the max date from those workoutIDs
                String placeholders = new String(new char[workoutIds.size()]).replace("\0", "?,").replaceAll(",$", "");
                String dateQuery = "SELECT MAX(Date) as LatestDate FROM " + TABLE_DATES +
                        " WHERE workoutID IN (" + placeholders + ")";

                Cursor dateCursor = db.rawQuery(dateQuery, workoutIds.toArray(new String[0]));

                if (dateCursor != null) {
                    if (dateCursor.moveToFirst()) {
                        latestDate = dateCursor.getString(dateCursor.getColumnIndexOrThrow("LatestDate"));
                    }
                    dateCursor.close();
                }
            }
        }

        return latestDate;
    }
    private long addOrUpdateWorkout(String workoutNumber, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the workout already exists by workoutNumber (workoutID)
        Cursor cursor = db.query(TABLE_DATES, new String[]{"workoutID"},
                "workoutID = ?", new String[]{workoutNumber},
                null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.close(); // If workout exists, do nothing
            return -1; // Return -1 to indicate no insertion
        } else {
            // If workout doesn't exist, insert it into the dates table
            ContentValues values = new ContentValues();
            values.put("workoutID", workoutNumber);
            values.put("Date", date);

            long workoutID = db.insert(TABLE_DATES, null, values);
            if (cursor != null) cursor.close();
            return workoutID;
        }
    }

    public int getExerciseIDHighest(){
        String query = "SELECT MAX(exerciseId) FROM exercises ";

        int highestId = 0;
        // Execute the query with the exercise id passed as an argument
        Cursor cursor = db.rawQuery(query,null);

        // Check if the query returns any result
        if (cursor != null && cursor.moveToFirst()) {
            highestId = cursor.getInt(0); // Get the maximum weight (first column)
            cursor.close(); // Close the cursor after use
        }
        return highestId;
    }

    private long addExercise(String exerciseName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the exercise already exists
        Cursor cursor = db.query(TABLE_EXERCISES, new String[]{"exerciseID"},
                "Name = ?", new String[]{exerciseName},
                null, null, null);

        long exerciseID = -1;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                exerciseID = cursor.getLong(0); // Exercise exists, get its ID
            } else {
                // If exercise doesn't exist, insert it into the exercises table
                ContentValues values = new ContentValues();
                values.put("Name", exerciseName);
                values.put("Volume", 0); // Initialize Volume to 0

                exerciseID = db.insert(TABLE_EXERCISES, null, values);
            }
            cursor.close();
        }

        return exerciseID;
    }

    // Implement deletion
    private void addOrUpdateSet(long workoutID, long exerciseID, int setOrder, float weight, int reps) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the set already exists (by workoutID, exerciseID, and setOrder)
        Cursor cursor = db.query(TABLE_SETS, new String[]{"setID", "weight", "reps"},
                "workoutID = ? AND exerciseID = ? AND setOrder = ?",
                new String[]{String.valueOf(workoutID), String.valueOf(exerciseID), String.valueOf(setOrder)},
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Set exists, get current values to adjust volume
                int oldWeight = cursor.getInt(1);
                int oldReps = cursor.getInt(2);

                // Decrease volume for old set
                updateExerciseVolume(exerciseID, oldWeight * oldReps, false);

                // Update the set
                ContentValues values = new ContentValues();
                values.put("weight", weight);
                values.put("reps", reps);

                db.update(TABLE_SETS, values, "workoutID = ? AND exerciseID = ? AND setOrder = ?",
                        new String[]{String.valueOf(workoutID), String.valueOf(exerciseID), String.valueOf(setOrder)});

                // Increase volume for new values
                updateExerciseVolume(exerciseID, (int)(weight * reps), true);
            } else {
                // Set doesn't exist, insert it
                ContentValues values = new ContentValues();
                values.put("workoutID", workoutID);
                values.put("exerciseID", exerciseID);
                values.put("setOrder", setOrder);
                values.put("weight", weight);
                values.put("reps", reps);

                db.insert(TABLE_SETS, null, values);

                // Add to exercise volume
                updateExerciseVolume(exerciseID, (int)(weight * reps), true);
            }
            cursor.close();
        }
    }

    public boolean deleteExercise(int exerciseID) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EXERCISES, "exerciseID = ?", new String[]{String.valueOf(exerciseID)}) > 0;
    }

    public boolean deleteSet(int setID) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First, get the set details to update exercise volume
        Cursor cursor = db.query(TABLE_SETS,
                new String[]{"exerciseID", "weight", "reps"},
                "setID = ?",
                new String[]{String.valueOf(setID)},
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int exerciseID = cursor.getInt(0);
                int weight = cursor.getInt(1);
                int reps = cursor.getInt(2);

                // Decrease exercise volume
                updateExerciseVolume(exerciseID, weight * reps, false);

                // Delete the set
                int result = db.delete(TABLE_SETS, "setID = ?", new String[]{String.valueOf(setID)});
                cursor.close();
                return result > 0;
            }
            cursor.close();
        }

        return false;
    }

    public boolean deleteWorkout(int workoutID) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First, get all sets for this workout to update exercise volumes
        Cursor cursor = db.query(TABLE_SETS,
                new String[]{"setID", "exerciseID", "weight", "reps"},
                "workoutID = ?",
                new String[]{String.valueOf(workoutID)},
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int exerciseID = cursor.getInt(1);
                int weight = cursor.getInt(2);
                int reps = cursor.getInt(3);

                // Decrease exercise volume
                updateExerciseVolume(exerciseID, weight * reps, false);
            }
            cursor.close();
        }

        // Delete the workout
        return db.delete(TABLE_DATES, "workoutID = ?", new String[]{String.valueOf(workoutID)}) > 0;
    }

    private boolean updateExerciseVolume(long exerciseID, int volumeChange, boolean increase) {
        SQLiteDatabase db = this.getWritableDatabase();

        int currentVolume = 0;
        Cursor cursor = db.query(TABLE_EXERCISES,
                new String[]{"Volume"},
                "exerciseID = ?",
                new String[]{String.valueOf(exerciseID)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            currentVolume = cursor.getInt(0);
            cursor.close();
        } else {
            if (cursor != null) cursor.close(); // Always close the cursor
            return false; // No matching row or cursor issue
        }

        // Calculate the new volume, ensuring it doesn't go below zero
        int newVolume = increase
                ? (currentVolume + volumeChange)
                : Math.max(0, currentVolume - volumeChange);

        // Update the volume in the database
        ContentValues values = new ContentValues();
        values.put("Volume", newVolume);

        int rowsAffected = db.update(TABLE_EXERCISES,
                values,
                "exerciseID = ?",
                new String[]{String.valueOf(exerciseID)});

        // db.close(); // Uncomment if you're not managing db lifecycle elsewhere

        return rowsAffected > 0;
    }

    public int getCurrentHighestForExercise(long id) {
        int currentHighest = 0; // Default value if no data is found

        // SQL query to get the maximum weight for a specific exercise
        String query = "SELECT MAX(weight) FROM sets WHERE exerciseID = ?";

        // Execute the query with the exercise id passed as an argument
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        // Check if the query returns any result
        if (cursor != null && cursor.moveToFirst()) {
            currentHighest = cursor.getInt(0); // Get the maximum weight (first column)
            cursor.close(); // Close the cursor after use
        }

        return currentHighest; // Return the highest weight found or 0
    }

    public List<PRs> getExercisesByPRDescending() {
        List<PRs> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT e.Name, MAX(s.weight) AS PR " +
                "FROM " + TABLE_SETS + " s " +
                "JOIN " + TABLE_EXERCISES + " e ON s.exerciseID = e.exerciseID " +
                "GROUP BY e.exerciseID " +
                "ORDER BY PR DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
                int pr = cursor.getInt(cursor.getColumnIndexOrThrow("PR"));
                result.add(new PRs(pr, name)); // Using PRs data structure (PR value, Exercise name)
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }
    public List<PRs> getExercisesByVolumeDescending() {
        List<PRs> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT Name, Volume FROM " + TABLE_EXERCISES + " " +
                "ORDER BY Volume DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
                int volume = cursor.getInt(cursor.getColumnIndexOrThrow("Volume"));
                result.add(new PRs(volume, name)); // Using PRs data structure (Volume, Exercise name)
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    //SafeIn puts
    private String getSafeString(String[] parts, Integer index, int lineNumber, String columnName) {
        if (index == null) {
            Log.w("CSV Import", "Missing column: " + columnName + " at line " + lineNumber);
            return "";
        }
        if (index >= parts.length) {
            Log.w("CSV Import", "Column index out of bounds for: " + columnName + " at line " + lineNumber);
            return "";
        }
        if (parts[index] == null || parts[index].trim().isEmpty()) {
            Log.w("CSV Import", "Empty value for: " + columnName + " at line " + lineNumber);
            return "";
        }
        return parts[index].trim();
    }

    private int parseSafeInt(String[] parts, Integer index, int lineNumber, String columnName) {
        try {
            if (index != null && index < parts.length && parts[index] != null && !parts[index].trim().isEmpty()) {
                return Integer.parseInt(parts[index].trim());
            } else {
                Log.w("CSV Import", "Empty or missing integer for: " + columnName + " at line " + lineNumber);
            }
        } catch (NumberFormatException e) {
            Log.w("CSV Import", "Invalid integer format for: " + columnName + " at line " + lineNumber + ", value: " + parts[index]);
        }
        return 0;
    }

    private float parseSafeFloat(String[] parts, Integer index, int lineNumber, String columnName) {
        try {
            if (index != null && index < parts.length && parts[index] != null && !parts[index].trim().isEmpty()) {
                return Float.parseFloat(parts[index].trim());
            } else {
                Log.w("CSV Import", "Empty or missing float for: " + columnName + " at line " + lineNumber);
            }
        } catch (NumberFormatException e) {
            Log.w("CSV Import", "Invalid float format for: " + columnName + " at line " + lineNumber + ", value: " + parts[index]);
        }
        return 0f;
    }
}