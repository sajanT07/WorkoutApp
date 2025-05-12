package com.example.myapplication.ui.WelcomeActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.databases.Database;
import com.example.myapplication.databases.ExerciseJsonManager;
import com.example.myapplication.databases.IDExerciseHighest;
import com.example.myapplication.ui.ExerciseDataLevelInput.ExerciseDataActivity;
import com.example.myapplication.ui.ExerciseIncEditor.ExerciseIncManager;
import com.example.myapplication.ui.ExerciseInfo.ExerciseInfroActivity;
import com.example.myapplication.ui.FAB.OptionBottomSheet;
import com.example.myapplication.ui.FAB.OptionItem;
import com.example.myapplication.ui.FAB.OptionAdapter;
import com.example.myapplication.ui.MainScreen.MainScreenActivity;
import com.example.myapplication.ui.ExerciseList.ExerciseList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    private Database db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private LinkedList<IDExerciseHighest> exercisesToConfigure;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        TextView welcomeText = findViewById(R.id.welcomeText);
        Button importCsvButton = findViewById(R.id.importCsvButton);
        Button viewExercisesButton = findViewById(R.id.viewExercisesButton);
        Button editExerciseValuesButton = findViewById(R.id.editExerciseValuesButton);
        FloatingActionButton fab = findViewById(R.id.fab);

        context = this;
        db = new Database(this);

        ExerciseJsonManager manager = new ExerciseJsonManager(context, db);
        JSONObject jsonObject = manager.getExercisesJson().optJSONObject("userData");
        String welcomeText2 = "Current MP : " + jsonObject.optInt("mp", 0);
        welcomeText.setText(welcomeText2);

        importCsvButton.setOnClickListener(v -> csvButton());
        viewExercisesButton.setOnClickListener(v -> startActivity(new Intent(this, ExerciseInfroActivity.class)));
        editExerciseValuesButton.setOnClickListener(v -> startActivity(new Intent(this, ExerciseIncManager.class)));
        fab.setOnClickListener(view -> {
            List<OptionItem> options = new ArrayList<>();
            options.add(new OptionItem("Upload CSV", R.drawable.ic_menu_camera, this::csvButton));
            options.add(new OptionItem("Exercise List", R.drawable.ic_menu_gallery, ExerciseList.class));
            options.add(new OptionItem("Sets", R.drawable.ic_menu_slideshow, ExerciseInfroActivity.class));
            options.add(new OptionItem("Progress", 0, ExerciseDataActivity.class));
            options.add(new OptionItem("Main Screen", R.drawable.ic_menu_camera, MainScreenActivity.class));

            OptionBottomSheet bottomSheet = new OptionBottomSheet(options);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    public void csvButton() {
        openFilePicker();
    }

    private void openFilePicker() {
        checkAndRequestAllFilesPermission();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(Intent.createChooser(intent, "Select CSV File"));
    }

    private void checkAndRequestAllFilesPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            new AlertDialog.Builder(this)
                    .setMessage("The app requires access to external storage to import CSV files. Please grant permission.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedFileUri = result.getData().getData();
                    processSelectedFile(selectedFileUri);
                }
            });

    private void processSelectedFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            AlertDialog loadingDialog = new AlertDialog.Builder(this)
                    .setTitle("Importing CSV")
                    .setMessage("Please wait...")
                    .setCancelable(false)
                    .create();
            loadingDialog.show();

            executorService.execute(() -> {
                try {
                    db.importFromCSV(inputStream);
                    ExerciseJsonManager manager = new ExerciseJsonManager(getApplicationContext(), db);
                    boolean processed = manager.startProcessingExerciseData();
                    exercisesToConfigure = db.getLinkedtoAdd();

                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        if (processed && exercisesToConfigure != null && !exercisesToConfigure.isEmpty()) {
                            Toast.makeText(this, "CSV Imported Successfully! Configure exercises.", Toast.LENGTH_SHORT).show();
                            showInputDialog(0);
                        } else {
                            //startExerciseDataActivity();
                            Toast.makeText(this, "CSV Imported and processed successfully.", Toast.LENGTH_SHORT).show();

                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Failed to import/process CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Failed to Import CSV. File not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInputDialog(int index) {
        if (exercisesToConfigure == null || index >= exercisesToConfigure.size()) {
            Toast.makeText(this, "All exercises configured successfully!", Toast.LENGTH_SHORT).show();
            startExerciseDataActivity();
            return;
        }

        IDExerciseHighest exercise = exercisesToConfigure.get(index);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView label = new TextView(this);
        label.setText("Exercise: " + exercise.getExerciseName());
        layout.addView(label);

        EditText weightIncr = new EditText(this);
        weightIncr.setHint("Weight Increment");
        layout.addView(weightIncr);

        EditText volIncr = new EditText(this);
        volIncr.setHint("Volume Increment");
        layout.addView(volIncr);

        EditText volStart = new EditText(this);
        volStart.setHint("Start Volume");
        layout.addView(volStart);

        EditText weightStart = new EditText(this);
        weightStart.setHint("Start Weight");
        layout.addView(weightStart);

        TextView HighestWeight = new TextView(this);
        String HighestWeightText = "Highest Weight: " + db.getCurrentHighestForExercise(exercise.getId());
        HighestWeight.setText(HighestWeightText);
        layout.addView(HighestWeight);

        TextView HighestVolume = new TextView(this);
        String HighestVolumeText = "Highest Volume: " + db.getExerciseVolume(exercise.getId());
        HighestVolume.setText(HighestVolumeText);
        layout.addView(HighestVolume);

        TextView LowestWeight = new TextView(this);
        String LowestWeightText = "Lowest Weight: " + db.getLowestWeightForExercise(exercise.getId());
        LowestWeight.setText(LowestWeightText);
        layout.addView(LowestWeight);

        TextView LowestVolume = new TextView(this);
        String LowestVolumeText = "Lowest Weight: " + db.getLowestVolumeForExercise(exercise.getId());
        LowestVolume.setText(LowestVolumeText);
        layout.addView(LowestVolume);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configure " + exercise.getExerciseName())
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("Submit", (dialog, which) -> {
                    try {
                        int weightIncrement = Integer.parseInt(weightIncr.getText().toString());
                        int volumeIncrement = Integer.parseInt(volIncr.getText().toString());
                        int startVolume = Integer.parseInt(volStart.getText().toString());
                        int startWeight = Integer.parseInt(weightStart.getText().toString());

                        ExerciseJsonManager manager = new ExerciseJsonManager(this, db);
                        boolean success = manager.addExercise((int) exercise.getId(),
                                exercise.getExerciseName(),
                                weightIncrement, volumeIncrement,
                                startVolume, startWeight,
                                db.getCurrentHighestForExercise(exercise.getId()));

                        if (success) {
                            Toast.makeText(this, "Configured successfully", Toast.LENGTH_SHORT).show();
                            showInputDialog(index + 1);
                        } else {
                            Toast.makeText(this, "Failed to configure", Toast.LENGTH_SHORT).show();
                            showInputDialog(index);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                        showInputDialog(index);
                    }
                })
                .setNegativeButton("Skip", (dialog, which) -> {
                    showInputDialog(index + 1);
                })
                .show();
    }

    private void startExerciseDataActivity() {

        Intent intent = new Intent(WelcomeActivity.this, ExerciseDataActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


}