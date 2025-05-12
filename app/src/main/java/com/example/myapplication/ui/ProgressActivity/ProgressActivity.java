package com.example.myapplication.ui.ProgressActivity;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.ProgressEntry;
import com.example.myapplication.R;
import com.example.myapplication.WorkoutDayDetails;
import com.example.myapplication.WorkoutSet;
import com.example.myapplication.WorkoutSheet;
import com.example.myapplication.databinding.FragmentProgressactivityBinding;
import com.example.myapplication.ui.StartScreen.StartScreenModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProgressActivity extends AppCompatActivity {
    private WorkoutDayDetails dataManager;
    private Spinner spinnerExercise;
    private FragmentProgressactivityBinding binding;
    private LineChart chartProgress;
    private List<String> exerciseNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_progressactivity);

        setTitle("Progress Tracker");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize data manager
        dataManager = new WorkoutDayDetails(this);
        dataManager.loadAllWorkoutSheets();

        // Set up spinner
        spinnerExercise = findViewById(R.id.spinner_exercise);
        chartProgress = findViewById(R.id.recyclerView);

        // Get all exercise names
        exerciseNames = getAllExerciseNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, exerciseNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExercise.setAdapter(adapter);

        // Set up chart when exercise is selected
        spinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String exerciseName = exerciseNames.get(position);
                displayProgressChart(exerciseName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initial chart setup if we have exercises
        if (!exerciseNames.isEmpty()) {
            displayProgressChart(exerciseNames.get(0));
        }
    }

    private List<String> getAllExerciseNames() {
        Set<String> exercises = new HashSet<>();

        // Load all workout sheets
        List<String> workoutNames = dataManager.getAvailableWorkoutNames();

        for (String workoutName : workoutNames) {
            WorkoutSheet sheet = dataManager.getWorkoutSheet(workoutName);
            if (sheet != null) {
                // Get all workout days
                Map<String, List<WorkoutSet>> workoutDays = sheet.getWorkoutDays();

                // Get all exercises from each day
                for (List<WorkoutSet> sets : workoutDays.values()) {
                    for (WorkoutSet set : sets) {
                        // Skip cardio exercises for progress tracking
                        if (!set.getExerciseName().contains("Cycling")) {
                            exercises.add(set.getExerciseName());
                        }
                    }
                }
            }
        }

        List<String> result = new ArrayList<>(exercises);
        Collections.sort(result);
        return result;
    }

    private void displayProgressChart(String exerciseName) {
        // Get progress data for the selected exercise
        List<ProgressEntry> progressEntries = dataManager.getExerciseProgress(exerciseName);

        // Prepare chart data
        List<Entry> weightEntries = new ArrayList<>();
        List<Entry> volumeEntries = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd", Locale.US);

        for (int i = 0; i < progressEntries.size(); i++) {
            ProgressEntry entry = progressEntries.get(i);

            // Format date for x-axis label
            String dateStr = "";
            try {
                Date date = inputFormat.parse(entry.getDate());
                dateStr = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                dateStr = "Unknown";
            }
            dateLabels.add(dateStr);

            // Add weight and volume data points
            weightEntries.add(new Entry(i, (float)entry.getMaxWeight()));
            volumeEntries.add(new Entry(i, (float)entry.getTotalVolume()));
        }

        // Create datasets
        LineDataSet weightDataSet = new LineDataSet(weightEntries, "Max Weight");
        weightDataSet.setColor(getResources().getColor(R.color.colorPrimary,null));
        weightDataSet.setCircleColor(getResources().getColor(R.color.colorPrimary,null));
        weightDataSet.setValueTextSize(12f);

        LineDataSet volumeDataSet = new LineDataSet(volumeEntries, "Total Volume");
        volumeDataSet.setColor(getResources().getColor(R.color.colorAccent,null));
        volumeDataSet.setCircleColor(getResources().getColor(R.color.colorAccent,null));
        volumeDataSet.setValueTextSize(12f);

        // Configure chart
        LineData lineData = new LineData(weightDataSet, volumeDataSet);
        chartProgress.setData(lineData);

        // Configure X-axis
        XAxis xAxis = chartProgress.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));

        // Additional chart styling
        chartProgress.getDescription().setEnabled(false);
        chartProgress.getLegend().setTextSize(12f);
        chartProgress.setExtraOffsets(10, 10, 10, 10);

        // Refresh chart
        chartProgress.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}