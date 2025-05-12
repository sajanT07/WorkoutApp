package com.example.myapplication.ui.ExerciseInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.ExerciseList.ExerciseAdapter;
import com.example.myapplication.ui.ExerciseInfo.DataModels.Workout;

import java.util.List;

public class WorkoutAdapterSingle extends RecyclerView.Adapter<WorkoutAdapterSingle.WorkoutSingleViewHolder> {
    List<Workout> workouts;
    Context context;

    public WorkoutAdapterSingle(Context context, List<Workout> workouts) {
        this.context = context;
        this.workouts = workouts;
    }

    @NonNull
    @Override
    public WorkoutAdapterSingle.WorkoutSingleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.workout_layer, parent, false);
        return new WorkoutAdapterSingle.WorkoutSingleViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull WorkoutAdapterSingle.WorkoutSingleViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.dateTextView.setText(workout.date);

        // Create and set the adapter for the inner RecyclerView
        SetSingleAdapter exerciseAdapter = new SetSingleAdapter(context, workout.exercises);
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

    public void updateData(List<Workout> newData) {
        this.workouts = newData;
        notifyDataSetChanged();
    }
    static class WorkoutSingleViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        RecyclerView exerciseRecyclerView;

        public WorkoutSingleViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.tv_workout_date);
            exerciseRecyclerView = itemView.findViewById(R.id.exceriseRecyclerView);
        }
    }
}
