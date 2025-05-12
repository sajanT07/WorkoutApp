package com.example.myapplication.ui.ExerciseIncEditor;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databases.Database;
import com.example.myapplication.databases.ExerciseJsonManager;
import com.example.myapplication.ui.ExerciseInfo.ExerciseInfroActivity;
import com.example.myapplication.ui.PowerBar.PowerBar;

import java.util.List;

public class ExerciseIncAdapater extends RecyclerView.Adapter<ExerciseIncAdapater.ViewHolder> {
    private Context context;
    private List<ExercisePRDate> exerciseList;

    public ExerciseIncAdapater(Context context, List<ExercisePRDate> exerciseList) {
        this.context = context;
        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exercise_setting_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExercisePRDate exercise = exerciseList.get(position);

        ExerciseJsonManager man = new ExerciseJsonManager(context, new Database(context.getApplicationContext()));

        holder.textViewExerciseTitle.setText(exercise.getExerciseName());

        String WeightIncrement =  "Weight Increment: " + (man.getWeightLevelIncrementLimit(exercise.getExerciseID())) + "KG";
        String VolumeIncrement = "Volume Increment: " + man.getVolumeLevelIncrementLimit(exercise.getExerciseID()) + "KG";
        String WeightStart = "Start Weight: " + man.getWeightLevelStart(exercise.getExerciseID()) +"KG";
        String VolumeStart = "Start Volume: "+man.getVolumeLevelStart(exercise.getExerciseID()) + "KG";

        String MPText = "MP: " + man.getMpGained(exercise.getExerciseID());
        holder.textViewMP.setText(MPText);
        holder.textViewWeightIncrement.setText(WeightIncrement);
        holder.textViewVolumeIncrement.setText(VolumeIncrement);
        holder.textViewWeightStart.setText(WeightStart);
        holder.textViewVolumeStart.setText(VolumeStart);

        // Click listener on the whole card to open detailed exercise information
        holder.cardView.setOnClickListener(v -> {
            InputData(exercise.exerciseID);
        });
    }

    public void InputData( int exerciseID){
        ExerciseJsonManager man = new ExerciseJsonManager(context, new Database(context.getApplicationContext()));

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView label = new TextView(context);
        label.setText("Exercise: " + man.getExerciseName(exerciseID));
        layout.addView(label);

        EditText weightIncr = new EditText(context);
        weightIncr.setHint("Weight Increment");
        layout.addView(weightIncr);

        EditText volIncr = new EditText(context);
        volIncr.setHint("Volume Increment");
        layout.addView(volIncr);

        EditText volStart = new EditText(context);
        volStart.setHint("Start Volume");
        layout.addView(volStart);

        EditText weightStart = new EditText(context);
        weightStart.setHint("Start Weight");
        layout.addView(weightStart);
        Database db  = new Database(context);

        TextView HighestWeight = new TextView(context);
        String HighestWeightText = "Highest Weight: " + db.getCurrentHighestForExercise(exerciseID);
        HighestWeight.setText(HighestWeightText);
        layout.addView(HighestWeight);

        TextView HighestVolume = new TextView(context);
        String HighestVolumeText = "Highest Volume: " + db.getExerciseVolume(exerciseID);
        HighestVolume.setText(HighestVolumeText);
        layout.addView(HighestVolume);

        TextView LowestWeight = new TextView(context);
        String LowestWeightText = "Lowest Weight: " + db.getLowestWeightForExercise(exerciseID);
        LowestWeight.setText(LowestWeightText);
        layout.addView(LowestWeight);

        TextView LowestVolume = new TextView(context);
        String LowestVolumeText = "Lowest Weight: " + db.getLowestVolumeForExercise(exerciseID);
        LowestVolume.setText(LowestVolumeText);
        layout.addView(LowestVolume);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Configure " + man.getExerciseName(exerciseID))
                .setView(layout)
                .setCancelable(true)
                .setPositiveButton("Submit", (dialog, which) -> {
                    try {
                        int weightIncrement = Integer.parseInt(weightIncr.getText().toString());
                        int volumeIncrement = Integer.parseInt(volIncr.getText().toString());
                        int startVolume = Integer.parseInt(volStart.getText().toString());
                        int startWeight = Integer.parseInt(weightStart.getText().toString());

                        ExerciseJsonManager manager = new ExerciseJsonManager(context, db);
                        boolean success = manager.addExercise((int) exerciseID,
                                man.getExerciseName(exerciseID),
                                weightIncrement, volumeIncrement,
                                startVolume, startWeight,
                                db.getCurrentHighestForExercise(exerciseID));

                        if (!success) {
                            Toast.makeText(context, "Failed to configure", Toast.LENGTH_SHORT).show();
                            InputData(exerciseID);
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
                        InputData(exerciseID);
                    }
                })
                .show();
    }









    @Override
    public int getItemCount() {
        return (exerciseList != null) ? exerciseList.size() : 0;  // Prevent null pointer exception
    }

    // Helper function to animate ProgressBar

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView exerciseCard;

        public TextView textViewExerciseTitle;
        public TextView textViewMP;

        public TextView textViewWeightIncrement;
        public TextView textViewVolumeIncrement;
        public TextView textViewWeightStart;
        public TextView textViewVolumeStart;

        public GridLayout textGrid;
        public View cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            exerciseCard = itemView.findViewById(R.id.exerciseCard);
            cardView = itemView.findViewById(R.id.exerciseCard);

            textViewExerciseTitle = itemView.findViewById(R.id.textViewExerciseTitle);
            textViewMP = itemView.findViewById(R.id.textViewMP);

            textViewWeightIncrement = itemView.findViewById(R.id.WeightIncrement);
            textViewVolumeIncrement = itemView.findViewById(R.id.VolumeIncrement);
            textViewWeightStart = itemView.findViewById(R.id.WeightStart);
            textViewVolumeStart = itemView.findViewById(R.id.VolumeStart);

            textGrid = itemView.findViewById(R.id.textGrid);
        }
    }
}