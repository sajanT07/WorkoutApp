package com.example.myapplication.ui.ExerciseDataLevelInput;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.LinearGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ProgressBar;
import android.graphics.BitmapShader;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databases.ExerciseJsonManager;
import com.example.myapplication.ui.ExerciseInfo.ExerciseInfroActivity;
import com.example.myapplication.ui.PowerBar.PowerBar;

import java.util.List;

public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ViewHolder> {
    private Context context;
    private List<ExercisePRDate> exerciseList;

    public ExerciseListAdapter(Context context, List<ExercisePRDate> exerciseList) {
        this.context = context;
        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exercise_data_activity_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExercisePRDate exercise = exerciseList.get(position);

        // Set exercise name
        holder.textViewExerciseName.setText(exercise.getExerciseName());

        // Initialize PowerBar (Assume data is preloaded for better performance)
        PowerBar powerBarWeight = exercise.getWeightPowerBar();
        PowerBar powerBarVolume = exercise.getVolumePowerBar();

        // Calculate level progress as a percentage (0-100)
        int weightProgress = 0;
        int volumeProgress = 0;

        int weightLevelsGained = powerBarWeight.getLevelGained();
        int volumeLevelsGained = powerBarVolume.getLevelGained();

        String text = "Current PR: " + exercise.getPR() + " Kgs at " + exercise.getDate() + '\n' +
                "Current levels: Weight: " + powerBarWeight.currentLevel + " - Volume: " + powerBarVolume.currentLevel;
        holder.PRTextView.setText(text);

        holder.textViewWeightLowerBound.setText(String.valueOf(powerBarWeight.currentLowBound));
        holder.textViewWeightUpperBound.setText(String.valueOf(powerBarWeight.upBound));
        holder.textViewWeightCurrentValue.setText(String.valueOf(powerBarWeight.newCurrentHighest));
        holder.textViewVolumeLowerBound.setText(String.valueOf(powerBarVolume.currentLowBound));
        holder.textViewVolumeUpperBound.setText(String.valueOf(powerBarVolume.upBound));
        holder.textViewVolumeCurrentValue.setText(String.valueOf(powerBarVolume.newCurrentHighest));

        if (powerBarWeight.getLevelIncrementLimit() > 0) {
            // Calculate as a percentage of the increment limit for weight
            weightProgress = (int)((powerBarWeight.getNewOverflowOverLevel() * 100.0) / powerBarWeight.getLevelIncrementLimit());
            // Cap at 100 to ensure it doesn't exceed the ProgressBar max
            weightProgress = Math.min(weightProgress, 100);
        }

        if (powerBarVolume.getLevelIncrementLimit() > 0) {
            // Calculate as a percentage of the increment limit for volume
            volumeProgress = (int)((powerBarVolume.getNewOverflowOverLevel() * 100.0) / powerBarVolume.getLevelIncrementLimit());
            // Cap at 100 to ensure it doesn't exceed the ProgressBar max
            volumeProgress = Math.min(volumeProgress, 100);
        }

        // Log progress values for debugging
        android.util.Log.d("ExerciseListAdapter", "Exercise: " + exercise.getExerciseName() +
                " - Weight Progress: " + weightProgress +
                " - Volume Progress: " + volumeProgress);


        animateProgressBar(holder.progressBarWeight, weightLevelsGained,weightProgress, holder.textViewWeightCurrentValue);
        animateProgressBar(holder.progressBarVolume, volumeLevelsGained,volumeProgress, holder.textViewVolumeCurrentValue);

        if (weightProgress >= 95 || volumeProgress >= 95) {
            // Bounce the entire card view
            holder.cardView.setScaleX(0.95f);
            holder.cardView.setScaleY(0.95f);
            holder.cardView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(new OvershootInterpolator())  // Bounce effect
                    .start();
        }

        // Click listener on the whole card to open detailed exercise information
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExerciseInfroActivity.class);
            intent.putExtra("exerciseId", exercise.getExerciseID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (exerciseList != null) ? exerciseList.size() : 0;  // Prevent null pointer exception
    }

    // Helper function to animate ProgressBar
    private void animateProgressBar(ProgressBar progressBar, int levelsGained, int targetProgress, TextView currentValueText) {
        animateLevel(progressBar, levelsGained, targetProgress, currentValueText, 0);
    }

    private void animateLevel(ProgressBar progressBar, int totalLevels, int finalProgress, TextView currentValueText, int currentLevel) {
        if (currentLevel >= totalLevels) {
            animateFinalOverflow(progressBar, finalProgress, currentValueText);
            return;
        }

        ObjectAnimator levelAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        levelAnimator.setDuration(600);
        levelAnimator.setInterpolator(new DecelerateInterpolator());

        levelAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setProgress(0); // Reset after fill
                progressBar.postDelayed(() -> {
                    animateLevel(progressBar, totalLevels, finalProgress, currentValueText, currentLevel + 1);
                }, 100); // Optional delay between levels
            }
        });

        levelAnimator.start();
    }

    private void animateFinalOverflow(ProgressBar progressBar, int targetProgress, TextView currentValueText) {
        if (targetProgress <= 0) return;

        ObjectAnimator finalAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, targetProgress);
        finalAnimator.setDuration(600);
        finalAnimator.setInterpolator(new DecelerateInterpolator());

        finalAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (targetProgress >= 95) {
                    currentValueText.animate()
                            .scaleX(1.2f)
                            .scaleY(1.2f)
                            .setDuration(150)
                            .withEndAction(() ->
                                    currentValueText.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(150)
                                            .start()
                            ).start();
                }
            }
        });

        finalAnimator.start();
    }
    public Bitmap createPixelatedGradient(int width, int height) {
        // Create a linear gradient with solid start color and transparent end
        LinearGradient gradient = new LinearGradient(0, 0, width, 0, Color.parseColor("#FF8C00"), Color.TRANSPARENT, TileMode.CLAMP);

        // Create a Bitmap to draw the gradient onto
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Create a paint object and apply the gradient shader
        Paint paint = new Paint();
        paint.setShader(gradient);

        // Draw the gradient onto the canvas
        canvas.drawRect(new RectF(0, 0, width, height), paint);

        // Now, apply pixelation (small squares to simulate pixels)
        Bitmap pixelatedBitmap = Bitmap.createScaledBitmap(bitmap, width / 10, height / 10, false); // Scale down
        pixelatedBitmap = Bitmap.createScaledBitmap(pixelatedBitmap, width, height, false); // Scale back up

        return pixelatedBitmap;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewExerciseName;
        TextView PRTextView;
        CardView cardView;

        // Weight progress section
        ProgressBar progressBarWeight;
        TextView textViewWeightLowerBound;
        TextView textViewWeightUpperBound;
        TextView textViewWeightCurrentValue;

        // Volume progress section
        ProgressBar progressBarVolume;
        TextView textViewVolumeLowerBound;
        TextView textViewVolumeUpperBound;
        TextView textViewVolumeCurrentValue;

        // Overflow bar
        ProgressBar progressBarOverflow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Core info
            textViewExerciseName = itemView.findViewById(R.id.textViewExerciseName);
            PRTextView = itemView.findViewById(R.id.PRTextView);
            cardView = itemView.findViewById(R.id.cardView);

            // Weight bar + labels
            progressBarWeight = itemView.findViewById(R.id.progressBarWeight);
            textViewWeightLowerBound = itemView.findViewById(R.id.textViewWeightLowerBound);
            textViewWeightUpperBound = itemView.findViewById(R.id.textViewWeightUpperBound);
            textViewWeightCurrentValue = itemView.findViewById(R.id.textViewWeightCurrentValue);

            // Volume bar + labels
            progressBarVolume = itemView.findViewById(R.id.progressBarVolume);
            textViewVolumeLowerBound = itemView.findViewById(R.id.textViewVolumeLowerBound);
            textViewVolumeUpperBound = itemView.findViewById(R.id.textViewVolumeUpperBound);
            textViewVolumeCurrentValue = itemView.findViewById(R.id.textViewVolumeCurrentValue);

            // Overflow (if used)
            progressBarOverflow = itemView.findViewById(R.id.progressBarOverflow);
        }
    }

}