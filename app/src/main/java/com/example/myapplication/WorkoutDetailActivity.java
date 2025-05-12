package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutDetailActivity extends AppCompatActivity {
    private WorkoutDayDetails dataManager;
    private WorkoutAdapter adapter;
    private RecyclerView recyclerView;
    private String workoutName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_progressactivity);

        workoutName = getIntent().getStringExtra("workoutName");
        if (workoutName == null) {
            finish();
            return;
        }

        setTitle(workoutName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize data manager
        dataManager = new WorkoutDayDetails(this);
        dataManager.loadAllWorkoutSheets();

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load workout days
        /* Lamda parameter shenanigans
        WorkoutSheet sheet = dataManager.getWorkoutSheet(workoutName);
        if (sheet != null) {
            List<String> workoutDates = sheet.getWorkoutDates();

            adapter = new WorkoutAdapter(workoutDates, date -> {
                // Open workout day detail
                com.example.myapplication.ui.workoutdata.WorkoutDayDetailFragment fragment = com.example.myapplication.ui.workoutdata.WorkoutDayDetailFragment.newInstance(workoutName, date);
                fragment.show(getSupportFragmentManager(), "workout_day_detail");
            }, date -> {
                // Delete workout day

                boolean success = dataManager.deleteWorkoutDay(workoutName, date);
                if (success) {
                    refreshWorkoutDays();
                    Toast.makeText(WorkoutDetailActivity.this,
                            "Deleted workout from " + date, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WorkoutDetailActivity.this,
                            "Failed to delete workout", Toast.LENGTH_SHORT).show();
                }


            });

            recyclerView.setAdapter(adapter);
        }

         */

    }

    private void refreshWorkoutDays() {
        WorkoutSheet sheet = dataManager.getWorkoutSheet(workoutName);
        if (sheet != null) {
            List<String> workoutDates = sheet.getWorkoutDates();
            adapter.updateData(workoutDates);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
