package com.example.myapplication.ui.FAB;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class OptionBottomSheet extends BottomSheetDialogFragment {
    private List<OptionItem> options;

    // Required empty constructor
    public OptionBottomSheet() {}

    public OptionBottomSheet(List<OptionItem> options) {
        this.options = options;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_options, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.options_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new OptionAdapter(options, getContext(), this));

        return view;
    }
}
