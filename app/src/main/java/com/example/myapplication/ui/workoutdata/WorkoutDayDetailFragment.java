package com.example.myapplication.ui.workoutdata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.WorkoutAdapter;
import com.example.myapplication.WorkoutDayDetails;
import com.example.myapplication.WorkoutSet;
import com.example.myapplication.WorkoutSheet;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutDayDetailFragment extends BottomSheetDialogFragment {
    private static final String ARG_WORKOUT_NAME = "workout_name";
    private static final String ARG_WORKOUT_DATE = "workout_date";

    private String workoutName;
    private String workoutDate;
    private WorkoutDayDetails dataManager;

    public static WorkoutDayDetailFragment newInstance(String workoutName, Object workoutDate) {
        WorkoutDayDetailFragment fragment = new WorkoutDayDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WORKOUT_NAME, workoutName);
        args.putString(ARG_WORKOUT_DATE, (String)workoutDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            workoutName = getArguments().getString(ARG_WORKOUT_NAME);
            workoutDate = getArguments().getString(ARG_WORKOUT_DATE);
        }

        dataManager = new WorkoutDayDetails(requireContext());
        dataManager.loadAllWorkoutSheets();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setsweighttracker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvWorkoutDate = view.findViewById(R.id.dateTextView);
        tvWorkoutDate.setText(workoutDate);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        WorkoutSheet sheet = dataManager.getWorkoutSheet(workoutName);
        if (sheet != null) {
            List<WorkoutSet> sets = sheet.getSetsForDate(workoutDate);

            // Group sets by exercise
            Map<String, List<WorkoutSet>> exerciseSets = new HashMap<>();
            for (WorkoutSet set : sets) {
                String exerciseName = set.getExerciseName();
                if (!exerciseSets.containsKey(exerciseName)) {
                    exerciseSets.put(exerciseName, new ArrayList<>());
                }
                exerciseSets.get(exerciseName).add(set);
            }

            //Check agaaiiniansiaoinas
            WorkoutAdapter adapter = new WorkoutAdapter((List<WorkoutSet>) exerciseSets);
            recyclerView.setAdapter(adapter);
        }
    }
}

