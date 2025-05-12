package com.example.myapplication.ui.ExerciseList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databases.Database;
import com.example.myapplication.ui.ExerciseDataLevelInput.ExerciseDataActivity;
import com.example.myapplication.ui.ExerciseInfo.ExerciseInfroActivity;
import com.example.myapplication.ui.ExerciseList.ListDataModels.ExerciseWithSets;
import com.example.myapplication.ui.ExerciseList.ListDataModels.Set;
import com.example.myapplication.ui.ExerciseList.ListDataModels.Workout;
import com.example.myapplication.ui.FAB.OptionBottomSheet;
import com.example.myapplication.ui.FAB.OptionItem;
import com.example.myapplication.ui.ProgressActivity.ProgressActivity;
import com.example.myapplication.ui.WelcomeActivity.WelcomeActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ExerciseList extends AppCompatActivity {

    RecyclerView workoutRecyclerView;
    WorkoutAdapter workoutAdapter;
    List<Workout> workoutList = new ArrayList<>();

    Database dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_exercise_main);

        TextView summaryTextView = findViewById(R.id.tv_summary);
        String text = "Current Exercises:";
        summaryTextView.setText(text);

        workoutRecyclerView = findViewById(R.id.rv_exercises);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            // Log when the FAB is clicked
            Log.d("FAB", "FAB clicked, showing options");

            List<OptionItem> options = new ArrayList<>();
           // WelcomeActivity welcomeActivity = (WelcomeActivity) getApplicationContext();

            // Option that leads to a new activity (e.g., SettingsActivity)
            Log.d("FAB", "Adding 'Upload CSV' option");
           // options.add(new OptionItem("Upload CSV", R.drawable.ic_menu_camera, welcomeActivity::csvButton));

            // Option that triggers a method in the current activity
            Log.d("FAB", "Adding 'Welcome List' option");
            options.add(new OptionItem("Welcome Page", R.drawable.ic_menu_gallery, WelcomeActivity.class));

            // Option that triggers another method
            Log.d("FAB", "Adding 'Sets' option");
            options.add(new OptionItem("Sets", R.drawable.ic_menu_slideshow, ExerciseInfroActivity.class));
            // Uncomment this line to add Progress option
            Log.d("FAB", "Adding 'Progress' option");
            options.add(new OptionItem("Progress", 0, ExerciseDataActivity.class));

            Log.d("FAB", "Adding 'Progress' option");
            options.add(new OptionItem("Progress", 0, ProgressActivity.class));
            // Log to confirm options are prepared
            Log.d("FAB", "Options list prepared: " + options.size() + " options available.");

            // Create BottomSheetDialogFragment
            OptionBottomSheet bottomSheet = new OptionBottomSheet(options);

            // Log when the BottomSheet is shown
            Log.d("FAB", "Showing BottomSheetDialogFragment");
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        dbHelper = new Database(getApplicationContext());

        loadWorkoutData(); // Load everything here
    }

    private void loadWorkoutData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Step 1: Get all workout dates DESCENDING
        Cursor dateCursor = db.rawQuery("SELECT * FROM dates ORDER BY Date DESC", null);

        if(dateCursor!= null && dateCursor.getCount()>0){
            while (dateCursor.moveToNext()) {
                int workoutID = dateCursor.getInt(0);
                String date = dateCursor.getString(1);

                // Step 2: For each workout, get exercises and sets
                List<ExerciseWithSets> exerciseList = new ArrayList<>();

                String exerciseQuery = "SELECT DISTINCT e.exerciseID, e.Name FROM exercises e " +
                        "JOIN sets s ON e.exerciseID = s.exerciseID " +
                        "WHERE s.workoutID = ?";
                Cursor exerciseCursor = db.rawQuery(exerciseQuery, new String[]{String.valueOf(workoutID)});

                if(exerciseCursor!= null && exerciseCursor.getCount()>0){
                    while (exerciseCursor.moveToNext()) {
                        int exerciseID = exerciseCursor.getInt(0);
                        String exerciseName = exerciseCursor.getString(1);

                        // Step 3: For each exercise, get sets
                        List<Set> setList = new ArrayList<>();

                        String setQuery = "SELECT * FROM sets WHERE workoutID = ? AND exerciseID = ? ORDER BY setOrder ASC";
                        Cursor setCursor = db.rawQuery(setQuery, new String[]{
                                String.valueOf(workoutID), String.valueOf(exerciseID)});

                        while (setCursor.moveToNext()) {
                            int setID = setCursor.getInt(0);
                            int weight = setCursor.getInt(1);
                            int reps = setCursor.getInt(3);
                            int setOrder = setCursor.getInt(2);

                            setList.add(new Set(setID, weight, reps, setOrder));
                        }
                        setCursor.close();

                        exerciseList.add(new ExerciseWithSets(exerciseID, exerciseName, setList));
                    }
                }

                exerciseCursor.close();

                // Step 4: Build Workout object
                workoutList.add(new Workout(workoutID, date, exerciseList));
            }
            dateCursor.close();

        }

        // Step 5: Pass to adapter
        if(getApplicationContext() != null){
            workoutAdapter = new WorkoutAdapter(getApplicationContext(), workoutList);
            workoutRecyclerView.setAdapter(workoutAdapter);
        }
    }
}

