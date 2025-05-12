package com.example.myapplication.ui.ExerciseInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.ExerciseInfo.DataModels.Set;
import com.example.myapplication.ui.ExerciseList.SetAdapter;

import java.util.List;

public class SetSingleAdapter extends RecyclerView.Adapter<SetSingleAdapter.SetSingleViewHolder> {
    List<Set> sets;
    Context context;

    public SetSingleAdapter(Context context, List<Set> sets) {
        this.context = context;
        this.sets = sets;
    }

    @NonNull
    @Override
    public SetSingleAdapter.SetSingleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sets_detail, parent, false);
        return new SetSingleAdapter.SetSingleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetSingleAdapter.SetSingleViewHolder holder, int position) {
        Set set = sets.get(position);

        String sets = set.reps + " reps";
        String weight = set.weight + " kg";
        // Set individual TextViews
        holder.setOrderTextView.setText("X");
        holder.setRepsTextView.setText(sets);
        holder.setWeightTextView.setText(weight);
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    static class SetSingleViewHolder extends RecyclerView.ViewHolder {
        TextView setOrderTextView;
        TextView setRepsTextView;
        TextView setWeightTextView;

        public SetSingleViewHolder(View itemView) {
            super(itemView);
            setOrderTextView = itemView.findViewById(R.id.x);
            setRepsTextView = itemView.findViewById(R.id.reps);
            setWeightTextView = itemView.findViewById(R.id.weight);
        }
    }
}
