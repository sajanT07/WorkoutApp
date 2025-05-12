package com.example.myapplication.ui.GameActivity;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.GameManager.Item;
import com.example.myapplication.ui.GameActivity.InventoryFragment.InventoryFragment;
import com.example.myapplication.ui.GameActivity.StatsFragment.StatsFragment;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private ArrayList<Item> itemList;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Item> itemList) {
        super(fragmentActivity);
        this.itemList = itemList;
    }


    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new InventoryFragment(); // ViewModel will provide data
            case 1:
                return new StatsFragment();
            case 2:
                return new InventoryFragment();//UsableItemsFragment();
            case 3:
                return new InventoryFragment();//RoomsTravelledFragment();
            default:
                return new InventoryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}