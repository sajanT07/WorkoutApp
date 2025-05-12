package com.example.myapplication.ui.MainScreen;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databases.Database;
import com.example.myapplication.ui.ExerciseDataLevelInput.ExerciseDataActivity;
import com.example.myapplication.ui.ExerciseInfo.DataModels.PRs;
import com.example.myapplication.ui.ExerciseInfo.ExerciseInfroActivity;
import com.example.myapplication.ui.ExerciseList.ExerciseList;
import com.example.myapplication.ui.FAB.OptionBottomSheet;
import com.example.myapplication.ui.FAB.OptionItem;
import com.example.myapplication.ui.GameActivity.GameMainActivity;
import com.example.myapplication.ui.WelcomeActivity.WelcomeActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainScreenActivity extends AppCompatActivity {
    Database db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreenleaderboard);

        // Initialize UI components
        TextView welcomeText = findViewById(R.id.textView);
        RecyclerView Prs = findViewById(R.id.recyclerView1);
        RecyclerView Volumes = findViewById(R.id.recyclerView2);

        // Initialize database
        try {
            db = new Database(this);
        } catch (Exception e) {
            Log.e(TAG, "Database initialization failed", e);
            Toast.makeText(this, "Failed to initialize database. Please try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            // Log when the FAB is clicked
            Log.d("FAB", "FAB clicked, showing options");

            List<OptionItem> options = new ArrayList<>();
            // Option that leads to a new activity (e.g., SettingsActivity)
            options.add(new OptionItem("Upload CSV", R.drawable.ic_menu_camera, WelcomeActivity.class));

            // Option that triggers a method in the current activity
            options.add(new OptionItem("Exercise List", R.drawable.ic_menu_gallery, ExerciseList.class));

            // Option that triggers another method
            options.add(new OptionItem("Sets", R.drawable.ic_menu_slideshow, ExerciseInfroActivity.class));

            options.add(new OptionItem("Game", R.drawable.ic_menu_slideshow, GameMainActivity.class));


            // Adding 'Progress' option
            options.add(new OptionItem("Progress", 0, ExerciseDataActivity.class));

            // Create BottomSheetDialogFragment
            OptionBottomSheet bottomSheet = new OptionBottomSheet(options);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        welcomeText.setText("Welcome");

        // Set LayoutManager for both RecyclerViews
        Prs.setLayoutManager(new LinearLayoutManager(this));
        Volumes.setLayoutManager(new LinearLayoutManager(this));

        // Fetch and display PRs data
        List<PRs> prsData = db.getExercisesByPRDescending(); // Assuming getExercisesByPRDescending() returns List<PRs>
        if (prsData != null && !prsData.isEmpty()) {
            valueAdapter prsAdapter = new valueAdapter(this, prsData);
            Prs.setAdapter(prsAdapter);
        } else {
            Log.e(TAG, "No PR data found.");
        }

        // Fetch and display Volumes data
        List<PRs> volumesData = db.getExercisesByVolumeDescending(); // Assuming getExercisesByVolumeDescending() returns List<PRs>
        if (volumesData != null && !volumesData.isEmpty()) {
            valueAdapter volumeAdapter = new valueAdapter(this, volumesData);
            Volumes.setAdapter(volumeAdapter);
        } else {
            Log.e(TAG, "No Volume data found.");
        }
    }
}
