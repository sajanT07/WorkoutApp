package com.example.myapplication.ui.ExerciseList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.ui.ExerciseList.ListDataModels.Set;

import java.util.List;

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.SetViewHolder> {
    List<Set> sets;
    Context context;

    public SetAdapter(Context context, List<Set> sets) {
        this.context = context;
        this.sets = sets;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sets_detail, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        Set set = sets.get(position);

        String sets = set.reps +" reps";
        String weight = set.weight +" kg";
        // Set individual TextViews
        holder.setOrderTextView.setText("X");
        holder.setRepsTextView.setText(sets);
        holder.setWeightTextView.setText(weight);
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        TextView setOrderTextView;
        TextView setRepsTextView;
        TextView setWeightTextView;

        public SetViewHolder(View itemView) {
            super(itemView);
            setOrderTextView = itemView.findViewById(R.id.x);
            setRepsTextView = itemView.findViewById(R.id.reps);
            setWeightTextView = itemView.findViewById(R.id.weight);
        }
    }
}