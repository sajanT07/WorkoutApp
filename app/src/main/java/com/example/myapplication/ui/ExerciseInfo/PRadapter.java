package com.example.myapplication.ui.ExerciseInfo;

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

public class PRadapter extends RecyclerView.Adapter<PRadapter.PRViewHolder> {

    private List<PRs> prDates;
    private final Context context;

    public PRadapter(Context context, List<PRs> prDates) {
        this.context = context;
        this.prDates = prDates; // Avoid null list
    }

    @NonNull
    @Override
    public PRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pr_detail, parent, false);
        return new PRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PRViewHolder holder, int position) {
        PRs prDate = prDates.get(position);
        holder.dateTextView.setText(prDate.getDate());
        holder.prTextView.setText(String.format("%s KG", prDate.getPR())); // Ensures proper formatting
    }

    @Override
    public int getItemCount() {
        return prDates.size();
    }

    public void updateData(List<PRs> newData) {
        this.prDates = newData;
        notifyDataSetChanged();
    }
    static class PRViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView prTextView;

        public PRViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date);
            prTextView = itemView.findViewById(R.id.weight);
        }
    }
}
