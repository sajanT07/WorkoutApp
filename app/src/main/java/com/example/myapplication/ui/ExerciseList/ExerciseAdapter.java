package com.example.myapplication.ui.ExerciseList;

import android.content.Context;
import android.view.LayoutInflater;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databases.Database;
import com.example.myapplication.ui.ExerciseList.ListDataModels.ExerciseWithSets;

import java.util.List;


public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    List<ExerciseWithSets> exercises;
    Context context;

    public ExerciseAdapter(Context context, List<ExerciseWithSets> exercises) {
        this.context = context;
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_exercise_details, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseWithSets exercise = exercises.get(position);
        holder.exerciseNameTextView.setText(exercise.name);

        SetAdapter setAdapter = new SetAdapter(context, exercise.sets);
        holder.setRecyclerView.setAdapter(setAdapter);
        holder.setRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseNameTextView;
        RecyclerView setRecyclerView;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            exerciseNameTextView = itemView.findViewById(R.id.tv_exercise_title);
            setRecyclerView = itemView.findViewById(R.id.recycler_view);
        }
    }
}