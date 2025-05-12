package com.example.myapplication.ui.ProgressActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.MainMenus;
import com.example.myapplication.ProgressEntry;
import com.example.myapplication.R;
import com.example.myapplication.WorkoutDayDetails;
import com.example.myapplication.databinding.FragmentProgressactivityBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressActivityFragment extends Fragment {

    private FragmentProgressactivityBinding binding;
    private ProgressViewModel progressViewModel;
    private Context context;
    private Spinner spinnerExercise;
    private LineChart chartProgress = null;
    private WorkoutDayDetails workoutDayDetails;
    private final ActivityResultLauncher<Intent> csvFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        workoutDayDetails.importCSVData("",uriToFile(uri)); // Pass the selected CSV file
                    }
                }
            });
    private File uriToFile(Uri uri) {
        File tempFile = new File(context.getCacheDir(), "imported_file.csv");

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return tempFile;
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        csvFilePicker.launch(intent);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        progressViewModel = new ViewModelProvider(this).get(ProgressViewModel.class);
        binding = FragmentProgressactivityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinnerExercise = binding.spinnerExercise;

        // Initialize and observe button text
        final TextView button = binding.importButton;
        progressViewModel.getButton().observe(getViewLifecycleOwner(), button::setText);

        // Set the button click listener to open file picker
        binding.importButton.setOnClickListener(view -> openFilePicker());

        // Observe and set the spinner adapter with exercise names
        progressViewModel.getExerciseNames().observe(getViewLifecycleOwner(), exerciseNames -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, exerciseNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerExercise.setAdapter(adapter);
        });

        // Set spinner item selection listener
        spinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String exerciseName = (String) parent.getItemAtPosition(position);
                progressViewModel.loadProgressData(exerciseName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Observe progress entries and display the progress chart
        progressViewModel.getProgressEntries().observe(getViewLifecycleOwner(), this::displayProgressChart);

        return root;
    }


    private void displayProgressChart(List<ProgressEntry> progressEntries) {

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

}