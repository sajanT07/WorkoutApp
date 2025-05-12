package com.example.myapplication.ui.ExerciseList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.ExerciseList.ListDataModels.Workout;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    List<Workout> workouts;
    Context context;

    public WorkoutAdapter(Context context, List<Workout> workouts) {
        this.context = context;
        this.workouts = workouts;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.workout_layer, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.dateTextView.setText(workout.date);

        // Create and set the adapter for the inner RecyclerView
        ExerciseAdapter exerciseAdapter = new ExerciseAdapter(context, workout.exercises);
        holder.exerciseRecyclerView.setAdapter(exerciseAdapter);

        // Only set the LayoutManager once
        if (holder.exerciseRecyclerView.getLayoutManager() == null) {
            holder.exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        RecyclerView exerciseRecyclerView;

        public WorkoutViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.tv_workout_date);
            exerciseRecyclerView = itemView.findViewById(R.id.exceriseRecyclerView);
        }
    }
}

