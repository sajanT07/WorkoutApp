package com.example.myapplication.ui.FAB;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.OptionViewHolder> {
    private List<OptionItem> options;
    private Context context;
    private BottomSheetDialogFragment bottomSheetDialogFragment;

    public OptionAdapter(List<OptionItem> options, Context context, BottomSheetDialogFragment bottomSheetDialogFragment) {
        this.options = options;
        this.context = context;
        this.bottomSheetDialogFragment = bottomSheetDialogFragment;
    }

    @Override
    public OptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_option, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OptionViewHolder holder, int position) {
        OptionItem option = options.get(position);
        holder.label.setText(option.getLabel());
        holder.icon.setImageResource(option.getIconRes());

        holder.itemView.setOnClickListener(v -> {
            if (option.isMethodOption()) {
                // If it's a method, execute the action
                option.getMethodAction().run();
            } else if (option.isActivityOption()) {
                // If it's an activity, navigate to the activity
                Intent intent = new Intent(context, option.getActivityClass());
                context.startActivity(intent);
            }
            bottomSheetDialogFragment.dismiss(); // Optionally dismiss the bottom sheet after selection
        });
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public static class OptionViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        ImageView icon;

        public OptionViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.option_text);
            icon = itemView.findViewById(R.id.option_icon);
        }
    }
}