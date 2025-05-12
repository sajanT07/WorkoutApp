package com.example.myapplication.ui.GameActivity.InventoryFragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.GameManager.Item;
import com.example.myapplication.GameManager.Player;

import java.util.ArrayList;
import java.util.List;

public class InventoryViewModel extends ViewModel {

    private final MutableLiveData<List<Item>> items = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Player> player = new MutableLiveData<>();


    private Player playerObj;
    public InventoryViewModel() {

    }

    public LiveData<Player> getPlayerMute() {
        return player;
    }

    public Player getPlayer() {
        return playerObj;
    }
    public void setPlayer(Player player) {
        this.playerObj = player;
        this.player.setValue(player);
    }

    public LiveData<List<Item>> getItems() {
        return items;
    }

    public void addItem(Item item) {
        List<Item> currentItems = items.getValue();
        if (currentItems != null) {
            currentItems.add(item);
            items.setValue(currentItems); // Triggers observers
        }
    }

    public void setItems(List<Item> newItems) {
        items.setValue(newItems);
    }


    public void removeItem(Item item) {
        List<Item> current = new ArrayList<>(items.getValue());
        current.remove(item);
        items.setValue(current);
    }
}