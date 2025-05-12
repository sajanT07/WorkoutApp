package com.example.myapplication.ui.GameActivity.InventoryFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.GameManager.Item;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment {

    private InventoryViewModel inventoryViewModel;
    private RecyclerView recyclerView;
    private ItemAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(new ArrayList<>(), item ->
            inventoryViewModel.getPlayer().SelectItem(item)
        );
        recyclerView.setAdapter(adapter);

        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        inventoryViewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.updateItems(items); // You'll write this method
        });

        return view;
    }
}