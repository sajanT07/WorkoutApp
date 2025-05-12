package com.example.myapplication.ui.GameActivity.StatsFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.GameManager.Player;
import com.example.myapplication.R;
import com.example.myapplication.ui.GameActivity.InventoryFragment.InventoryViewModel;

public class StatsFragment extends Fragment {

    private TextView healthText;
    private TextView attackText;
    private TextView defenseText;
    private TextView appliedItems;
    private TextView levelText;

    private InventoryViewModel playerViewModel;

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_stats, container, false);

        healthText = view.findViewById(R.id.healthText);
        attackText = view.findViewById(R.id.attackText);
        defenseText = view.findViewById(R.id.defenseText);
        appliedItems = view.findViewById(R.id.appliedItems);


        playerViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        playerViewModel.getPlayerMute().observe(getViewLifecycleOwner(), this::updateStatsUI);

        return view;
    }

    private void updateStatsUI(Player player) {
        healthText.setText("Health: " + player.getHealth());
        attackText.setText("Attack: " + player.getAttack());
        defenseText.setText("Defense: " + player.getDefense());
        appliedItems.setText("Applied Items: " + player.getInventoryDetailsAsString(player.getAppliedItems()));
    }
}
