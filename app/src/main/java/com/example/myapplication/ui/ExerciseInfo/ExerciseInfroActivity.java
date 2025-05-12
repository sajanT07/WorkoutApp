package com.example.myapplication.ui.ExerciseInfo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databases.Database;
import com.example.myapplication.databases.ExerciseJsonManager;
import com.example.myapplication.ui.ExerciseDataLevelInput.ExerciseDataActivity;
import com.example.myapplication.ui.ExerciseInfo.DataModels.PRs;
import com.example.myapplication.ui.ExerciseInfo.DataModels.Set;
import com.example.myapplication.ui.ExerciseInfo.DataModels.Workout;
import com.example.myapplication.ui.ExerciseList.ExerciseList;
import com.example.myapplication.ui.FAB.OptionBottomSheet;
import com.example.myapplication.ui.FAB.OptionItem;
import com.example.myapplication.ui.ProgressActivity.ProgressActivity;
import com.example.myapplication.ui.WelcomeActivity.WelcomeActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseInfroActivity extends AppCompatActivity {
    private RecyclerView workoutRecyclerView;
    private WorkoutAdapterSingle workoutAdapterSingle;
    private PRadapter pRadapter;

    private ExerciseJsonManager manager;
    private List<Workout> workoutList = new ArrayList<>();
    private List<PRs> exercisePRDates = new ArrayList<>();

    private Database dbHelper;
    private int exerciseId;
    private boolean currentState = false; // false: Sets, true: PRs
    private boolean locked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_infroexercise);

        dbHelper = new Database(this);
        manager = new ExerciseJsonManager(this, dbHelper); // Initialize manager here

        workoutRecyclerView = findViewById(R.id.rv_exercises);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutRecyclerView.setHasFixedSize(true);

        TextView summaryTextView = findViewById(R.id.tv_summary);
        Button btnPR = findViewById(R.id.btn_pr);
        Button btnSets = findViewById(R.id.btn_sets);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            // Log when the FAB is clicked
            Log.d("FAB", "FAB clicked, showing options");

            List<OptionItem> options = new ArrayList<>();
           // WelcomeActivity welcomeActivity = (WelcomeActivity) getApplicationContext();

            // Option that leads to a new activity (e.g., SettingsActivity)
            Log.d("FAB", "Adding 'Upload CSV' option");
            //options.add(new OptionItem("Upload CSV", R.drawable.ic_menu_camera, welcomeActivity::csvButton));

            // Option that triggers a method in the current activity
            Log.d("FAB", "Adding 'Welcome List' option");
            options.add(new OptionItem("Welcome Page", R.drawable.ic_menu_gallery, WelcomeActivity.class));

            // Option that triggers another method
            Log.d("FAB", "Adding 'Exercise List' option");
            options.add(new OptionItem("Exercise List", R.drawable.ic_menu_slideshow, ExerciseList.class));
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



        // Center the buttons in the layout
        centerButtons(btnPR, btnSets);

        // Set up button listeners
        btnPR.setOnClickListener(v -> {
            currentState = true;
            updateExerciseData(exerciseId);
        });

        btnSets.setOnClickListener(v -> {
            currentState = false;
            updateExerciseData(exerciseId);
        });

        Intent intent = getIntent();
        if (intent.hasExtra("exerciseId")) {
            exerciseId = intent.getIntExtra("exerciseId", 0);
            summaryTextView.setText("Current Exercise:");
            locked = true;
            setupExerciseSpinner(exerciseId, true);
        } else {
            summaryTextView.setText("All Exercises");
            setupExerciseSpinner(-1, false);
        }
    }

    private void centerButtons(Button btnPR, Button btnSets) {
        // Find the parent layout of the buttons - assuming they're in a LinearLayout
        View parentView = (View) btnPR.getParent();

        if (parentView instanceof LinearLayout) {
            LinearLayout parentLayout = (LinearLayout) parentView;
            parentLayout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

            // Set layout parameters for the buttons to center them
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            params.weight = 1.0f;

            btnPR.setLayoutParams(params);
            btnSets.setLayoutParams(params);
        } else {
            // If they're not in a LinearLayout, we need to modify the layout XML
            // For now, let's at least center the text in the buttons
            btnPR.setGravity(android.view.Gravity.CENTER);
            btnSets.setGravity(android.view.Gravity.CENTER);
        }
    }

    private void setupExerciseSpinner(int selectedExerciseId, boolean lockSpinner) {
        Spinner spinner = findViewById(R.id.spinner_filter);
        List<String> exerciseNames = new ArrayList<>();
        final List<Integer> exerciseIds = new ArrayList<>();

        try (Cursor cursor = dbHelper.getAllExercises()) {
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String name = cursor.getString(1);

                    // If locked and this is not the selected exercise, skip
                    if (lockSpinner && selectedExerciseId != id && selectedExerciseId != -1) {
                        continue;
                    }

                    exerciseNames.add(name);
                    exerciseIds.add(id);
                }
            } else {
                exerciseNames.add("No exercises found");
                exerciseIds.add(-1);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Disable spinner if locked
        spinner.setEnabled(!lockSpinner);
        spinner.setClickable(!lockSpinner);

        // Set selection for the initially selected exercise
        if (selectedExerciseId != -1) {
            for (int i = 0; i < exerciseIds.size(); i++) {
                if (exerciseIds.get(i) == selectedExerciseId) {
                    spinner.setSelection(i);
                    exerciseId = selectedExerciseId; // Update the current exerciseId
                    break;
                }
            }
        } else if (!exerciseIds.isEmpty()) {
            // If no specific exercise was selected, use the first one
            exerciseId = exerciseIds.get(0);
        }

        // Set listener for spinner selections
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!exerciseIds.isEmpty() && position < exerciseIds.size()) {
                    exerciseId = exerciseIds.get(position);
                    updateExerciseData(exerciseId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Load initial data
        updateExerciseData(exerciseId);
    }

    // Centralized method to update all necessary data based on selected exercise ID
    private void updateExerciseData(int exId) {
        if (!currentState) {
            loadSets(exId);
        } else {
            loadPRs(exId);
        }

        // Update UI to reflect current state
        Button btnPR = findViewById(R.id.btn_pr);
        Button btnSets = findViewById(R.id.btn_sets);

        if (currentState) {
            btnPR.setVisibility(View.GONE);
            btnSets.setVisibility(View.VISIBLE);
        } else {
            btnPR.setVisibility(View.VISIBLE);
            btnSets.setVisibility(View.GONE);
        }
    }

    private void loadSets(int exId) {
        List<Workout> groupedSets = new ArrayList<>();
        Map<String, List<Set>> dateToSetsMap = new HashMap<>();

        try (Cursor cursor = dbHelper.getSetsForExercise(exId)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int setId = cursor.getInt(0);
                    String date = cursor.getString(1);
                    double weight = cursor.getDouble(2);
                    int reps = cursor.getInt(3);
                    int setOrder = cursor.getInt(4);

                    // Add to map for grouping
                    if (!dateToSetsMap.containsKey(date)) {
                        dateToSetsMap.put(date, new ArrayList<>());
                    }
                    dateToSetsMap.get(date).add(new Set(setId, (int) weight, reps, setOrder));

                } while (cursor.moveToNext());
            }
        }
        Log.d("loadSets", "Unique workout dates: " + dateToSetsMap.keySet());
        // Create workouts from the grouped sets
        for (Map.Entry<String, List<Set>> entry : dateToSetsMap.entrySet()) {
            String date = entry.getKey();
            List<Set> sets = entry.getValue();
            int workoutID = dbHelper.getWorkoutID(date);

            // Sort sets by order if needed
            Collections.sort(sets, Comparator.comparingInt(s -> s.setOrder));

            Log.d("WorkoutDebug", "Date: " + date + ", Sets count: " + sets.size());
            groupedSets.add(new Workout(workoutID, date, sets));


        }

        // Sort workouts by date if needed (most recent first)
        Collections.sort(groupedSets, (w1, w2) -> w2.date.compareTo(w1.date));

        if (workoutAdapterSingle == null) {
            workoutAdapterSingle = new WorkoutAdapterSingle(this, groupedSets);
            workoutRecyclerView.setAdapter(workoutAdapterSingle);
        } else {
            workoutAdapterSingle.updateData(groupedSets);
            if (!(workoutRecyclerView.getAdapter() instanceof WorkoutAdapterSingle)) {
                workoutRecyclerView.setAdapter(workoutAdapterSingle);
            }
        }
    }

    private void loadPRs(int exId) {
        List<PRs> newPRDates = manager.getPRs(exId);
        if (newPRDates == null) {
            newPRDates = Collections.emptyList();
        }

        if (pRadapter == null) {
            pRadapter = new PRadapter(this, newPRDates);
            workoutRecyclerView.setAdapter(pRadapter);
        } else {
            pRadapter.updateData(newPRDates);
            if (!(workoutRecyclerView.getAdapter() instanceof PRadapter)) {
                workoutRecyclerView.setAdapter(pRadapter);
            }
        }
    }
}