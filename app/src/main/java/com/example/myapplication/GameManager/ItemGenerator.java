package com.example.myapplication.GameManager;

import java.util.Random;

public class ItemGenerator {
    // List of item names for random generation
    private static final String[] itemNames = {
            "Healing Potion", "Mana Potion", "Sword of Strength", "Shield of Resilience",
            "Fireball Scroll", "Dragon's Claw", "Knight's Armor", "Elixir of Power"
    };

    // Method to generate a random item
    public static Item generateRandomItem() {
        Random random = new Random();

        // Select a random item name
        String name = itemNames[random.nextInt(itemNames.length)];

        // Randomly assign attack, defense, and health boosts
        int attackBoost = random.nextInt(10) + 1;   // Random boost between 1 and 10
        int defenseBoost = random.nextInt(10) + 1;  // Random boost between 1 and 10
        int healthBoost = random.nextInt(30) + 1;   // Random boost between 1 and 30

        // Create and return a new item with random attributes
        return new Item(name, attackBoost, defenseBoost, healthBoost);
    }

    // Optionally, you can add a method to generate a specific set of items if needed
    public static Item generateSpecialItem() {
        return new Item("Legendary Sword", 15, 5, 0);
    }
}