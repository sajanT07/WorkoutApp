package com.example.myapplication;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {

    private List<WorkoutSet> entries;
    private Map<String, Map<String, List<WorkoutSet>>> organizedData;

    public WorkoutAdapter(List<WorkoutSet> entries) {
        this.entries = entries;
        organizeData();
    }


    private void organizeData() {
        // Organize by: Date -> Exercise -> Sets
        organizedData = new TreeMap<>();

        for (WorkoutSet entry : entries) {
            String date = entry.getDate();
            String exercise = entry.getExerciseName();

            if (!organizedData.containsKey(date)) {
                organizedData.put(date, new TreeMap<>());
            }

            if (!organizedData.get(date).containsKey(exercise)) {
                organizedData.get(date).put(exercise, new ArrayList<>());
            }

            organizedData.get(date).get(exercise).add(entry);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.setsweighttracker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Find the correct entry for this position
        String date = getDateAtPosition(position);
        String exercise = getExerciseAtPosition(position, date);

        holder.dateTextView.setText(formatDate(date));
        holder.exerciseTextView.setText(exercise);

        // Build the sets information
        StringBuilder setsInfo = new StringBuilder();
        List<WorkoutSet> sets = organizedData.get(date).get(exercise);

        for (WorkoutSet set : sets) {
            setsInfo.append("Set ").append(set.getSetOrder())
                    .append(": ").append(set.getWeight()).append("kg x ")
                    .append(set.getReps()).append(" reps");

            // Add distance/time if available
            /*
            if (set.getDistance() != null && !set.getDistance().isEmpty() && !set.getDistance().equals("0.0")) {
                setsInfo.append(" | ").append(set.getDistance()).append("m");
            }

            if (set.getSeconds() != null && !set.getSeconds().isEmpty() && !set.getSeconds().equals("0.0")) {
                setsInfo.append(" | ").append(set.getSeconds()).append("sec");
            }
            
             */

            setsInfo.append("\n");
        }

        holder.setsTextView.setText(setsInfo.toString());
    }

    private String formatDate(String dateString) {
        // Format: 2024-10-10 16:39:04 to a more readable format
        if (dateString != null && !dateString.isEmpty()) {
            String[] parts = dateString.split(" ");
            if (parts.length > 0) {
                return parts[0]; // Just return the date part
            }
        }
        return dateString;
    }

    private String getDateAtPosition(int position) {
        int currentPos = 0;
        for (String date : organizedData.keySet()) {
            Map<String, List<WorkoutSet>> exerciseMap = organizedData.get(date);
            for (String exercise : exerciseMap.keySet()) {
                if (currentPos == position) {
                    return date;
                }
                currentPos++;
            }
        }
        return "";
    }

    private String getExerciseAtPosition(int position, String date) {
        if (date.isEmpty() || !organizedData.containsKey(date)) {
            return "";
        }

        int currentPos = 0;
        for (String curDate : organizedData.keySet()) {
            Map<String, List<WorkoutSet>> exerciseMap = organizedData.get(curDate);
            for (String exercise : exerciseMap.keySet()) {
                if (currentPos == position) {
                    return exercise;
                }
                currentPos++;
            }
        }
        return "";
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (Map<String, List<WorkoutSet>> exerciseMap : organizedData.values()) {
            count += exerciseMap.size();
        }
        return count;
    }

    public void updateData(List<String> workoutDates) {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView exerciseTextView;
        TextView setsTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            exerciseTextView = itemView.findViewById(R.id.exerciseTextView);
            setsTextView = itemView.findViewById(R.id.setsTextView);
        }
    }
}