package com.example.myapplication.ui.MainScreen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.ExerciseInfo.DataModels.PRs;

import java.util.List;

public class valueAdapter extends RecyclerView.Adapter<valueAdapter.valueViewHolder> {

    private List<PRs> WeightName;
    private final Context context;

    public valueAdapter(Context context, List<PRs> prDates) {
        this.context = context;
        this.WeightName = prDates; // Avoid null list
    }

    @NonNull
    @Override
    public valueAdapter.valueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exerciseleaderboard, parent, false);
        return new valueAdapter.valueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull valueAdapter.valueViewHolder holder, int position) {
        PRs prDate = WeightName.get(position);
        String string = "#" + String.valueOf(position + 1);
        holder.RankTextView.setText(string);
        holder.NameTextView.setText(prDate.getDate());
        holder.prTextView.setText(String.format("%s KG", prDate.getPR())); // Ensures proper formatting
    }

    @Override
    public int getItemCount() {
        return WeightName.size();
    }

    public void updateData(List<PRs> newData) {
        this.WeightName = newData;
        notifyDataSetChanged();
    }

    static class valueViewHolder extends RecyclerView.ViewHolder {
        TextView NameTextView;
        TextView RankTextView;
        TextView prTextView;

        public valueViewHolder(@NonNull View itemView) {
            super(itemView);
            NameTextView = itemView.findViewById(R.id.Name);
            RankTextView = itemView.findViewById(R.id.rank);
            prTextView = itemView.findViewById(R.id.weight);
        }
    }
}
