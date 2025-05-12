package com.example.myapplication.ui.ExerciseIncEditor;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databases.Database;
import com.example.myapplication.databases.ExerciseJsonManager;
import com.example.myapplication.ui.ExerciseInfo.ExerciseInfroActivity;
import com.example.myapplication.ui.ExerciseList.ExerciseList;
import com.example.myapplication.ui.FAB.OptionBottomSheet;
import com.example.myapplication.ui.FAB.OptionItem;
import com.example.myapplication.ui.WelcomeActivity.WelcomeActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ExerciseIncManager extends AppCompatActivity {
    private static final String TAG = "ExerciseDataActivity"; // For logging
    private Database dbHelper;
    private RecyclerView exerciseRecyclerView;
    private ExerciseIncAdapater exerciseIncAdapater;
    private List<ExercisePRDate> exercisePRDateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_list_exercise_main);
        Log.d(TAG, "onCreate: Activity started");

        TextView summaryTextView = findViewById(R.id.tv_summary);
        summaryTextView.setText("Current Exercises:");

        setupFAB();
        setupRecyclerView();

        dbHelper = new Database(this);

        // Load data synchronously
        loadWorkoutData();
    }
    private void setupRecyclerView() {
        exerciseRecyclerView = findViewById(R.id.rv_exercises);
        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize with empty adapter
        exerciseIncAdapater = new ExerciseIncAdapater(this, exercisePRDateList);
        exerciseRecyclerView.setAdapter(exerciseIncAdapater);

        Context context = exerciseRecyclerView.getContext();
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        exerciseRecyclerView.setLayoutAnimation(controller);
    }

    private void setupFAB() {
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            Log.d("FAB", "FAB clicked, showing options");

            List<OptionItem> options = new ArrayList<>();

            Log.d("FAB", "Adding 'Welcome Page' option");
            options.add(new OptionItem("Welcome Page", R.drawable.ic_menu_gallery, WelcomeActivity.class));

            Log.d("FAB", "Adding 'Exercise List' option");
            options.add(new OptionItem("Exercise List", R.drawable.ic_menu_slideshow, ExerciseList.class));

            Log.d("FAB", "Adding 'Progress' option");
            options.add(new OptionItem("Progress", 0, ExerciseInfroActivity.class));

            Log.d("FAB", "Options list prepared: " + options.size() + " options available.");

            OptionBottomSheet bottomSheet = new OptionBottomSheet(options);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    private void loadWorkoutData() {
        // You could add a loading indicator here if needed
        // showLoadingIndicator();

        SQLiteDatabase db = null;
        Cursor exercises = null;

        try {
            Log.d(TAG, "loadWorkoutData: Querying database...");
            db = dbHelper.getReadableDatabase();

            exercises = db.rawQuery(
                    "SELECT exercises.exerciseID, exercises.Name AS exerciseName, MAX(dates.Date) AS LastDone " +
                            " FROM sets" +
                            " JOIN exercises ON sets.exerciseID = exercises.exerciseID " +
                            " JOIN dates ON sets.workoutID = dates.workoutID " +
                            " GROUP BY exercises.exerciseID, exercises.Name " +
                            " ORDER BY LastDone DESC", null);

            exercisePRDateList.clear();

            if (exercises != null && exercises.getCount() > 0) {
                Log.d(TAG, "loadWorkoutData: Found " + exercises.getCount() + " exercises");

                while (exercises.moveToNext()) {
                    int exerciseID = exercises.getInt(0);
                    String exerciseName = exercises.getString(1);
                    String date = exercises.isNull(2) ? "N/A" : exercises.getString(2);
                    int PR = 0;

                    Log.d(TAG, "Exercise ID: " + exerciseID + ", Name: " + exerciseName + ", Date: " + date);

                    ExerciseJsonManager man = new ExerciseJsonManager(this, dbHelper);
                    if (man.exerciseExists(exerciseID)) {
                        PR = man.getCurrentHighest(exerciseID);
                        Log.d(TAG, "Exercise " + exerciseName + " has PR: " + PR);
                    }

                    ExercisePRDate exercisePRDate = new ExercisePRDate(exerciseID, exerciseName, PR, date, this);
                    exercisePRDateList.add(exercisePRDate);
                }
            } else {
                Log.w(TAG, "loadWorkoutData: No exercise data found");
                // Consider showing an empty state view here
            }

            // Update the UI
            exerciseIncAdapater.notifyDataSetChanged();
            exerciseRecyclerView.scheduleLayoutAnimation();

            if (exercisePRDateList.isEmpty()) {
                Log.w(TAG, "loadWorkoutData: No exercises to display in adapter");
            } else {
                Log.d(TAG, "loadWorkoutData: Adapter updated with " + exercisePRDateList.size() + " items");
            }

            // hideLoadingIndicator();

        } catch (Exception e) {
            Log.e(TAG, "loadWorkoutData: Error loading workout data", e);
        } finally {
            if (exercises != null) {
                exercises.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }
}