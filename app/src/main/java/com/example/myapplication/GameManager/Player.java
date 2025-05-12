package com.example.myapplication.GameManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int health = 100;
    private int maxHealth = 100;
    private int attack = 10;
    private int defense = 5;
    private int xp = 0;

    private Item selectedItem;
    public Room currentRoom;
    private Room previousRoom;

    private List<Item> appliedItems = new ArrayList<>();
    private List<Item> inventory = new ArrayList<>();

    public Player(int health, int attack, int defense, int xp) {
        this.health = health;
        this.attack = attack;
        this.defense = defense;
        this.xp = xp;
    }



    public Player() {

    }
    public Item getSelectedItem() {
        return selectedItem;
    }
    public List<Item> getAppliedItems() {

        return appliedItems;
    }
    public void SelectItem(Item item){
        selectedItem = item;
    }

    public void gainXP(int amount) { xp += amount; }
    public void takeDamage(int amount) {
        int reduced = Math.max(0, amount - defense);
        health -= reduced;
    }
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public boolean isAlive() { return health > 0; }

    public void addItem(Item item) {
        inventory.add(item);
        //applyItemEffects(item);
    }

    public String saveInventory() {
        Gson gson = new Gson();
        return gson.toJson(appliedItems);  // Converts inventory list to JSON
    }

    public String saveAppliedItems() {
        Gson gson = new Gson();
        return gson.toJson(inventory);  // Converts inventory list to JSON
    }


    // Load inventory from a JSON string
    public void loadInventory(String inventoryJson) {
        if (inventoryJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Item>>(){}.getType();
            inventory = gson.fromJson(inventoryJson, type);
        }
    }

    public void loadAppliedItems(String inventoryJson) {
        if (inventoryJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Item>>(){}.getType();
            appliedItems = gson.fromJson(inventoryJson, type);
        }
    }
    public void applyItemEffects(Item item) {
        this.attack += item.getAttackBoost();
        this.defense += item.getDefenseBoost();
        this.maxHealth += item.getHealthBoost();
        appliedItems.add(item);
    }

    public void removeItemEffects(Item item) {
        this.attack -= item.getAttackBoost();
        this.defense -= item.getDefenseBoost();
        this.maxHealth -= item.getHealthBoost();
    }

    public Room getPreviousRoom() {
        return previousRoom;
    }

    // Method to move the player to a new room
    public void moveTo(Room newRoom) {
        this.previousRoom = this.currentRoom;  // Store the current room as previous
        this.currentRoom = newRoom;  // Move to the new room
    }
    // Getters
    public int getHealth() { return health; }
    public int getXP() { return xp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public List<Item> getInventory() { return inventory; }
    public String getInventoryDetailsAsString(List<Item> inventory) {
        StringBuilder builder = new StringBuilder();

        for (Item item : inventory) {
            builder.append("Name: ").append(item.getName()).append("\n");
            builder.append(" - Attack Boost: ").append(item.getAttackBoost()).append("\n");
            builder.append(" - Defense Boost: ").append(item.getDefenseBoost()).append("\n");
            builder.append(" - Health Boost: ").append(item.getHealthBoost()).append("\n\n");
        }

        return builder.toString();
    }
}