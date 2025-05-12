package com.example.myapplication.GameManager;

public class Enemy {
    private String name;
    private int health;
    private int attackPower;
    private int defensePower;
    private String description;

    public Enemy(String name, int health, int attackPower, int defensePower, String description) {
        this.name = name;
        this.health = health;
        this.attackPower = attackPower;
        this.defensePower = 0;
        this.description = description;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public int getDefensePower() {
        return defensePower;
    }

    public String getDescription() {
        return description;
    }

    public void takeDamage(int damage) {
        // Damage calculation taking defense into account
        int effectiveDamage = Math.max(damage - defensePower, 0); // Damage can't go below 0
        health -= effectiveDamage;

        // Ensure health doesn't go below 0
        if (health < 0) {
            health = 0;
        }

        System.out.println(name + " took " + effectiveDamage + " damage, remaining health: " + health);
    }

    // Method to check if the enemy is still alive
    public boolean isAlive() {
        return health > 0;
    }

    // Optional: Method to display the enemy's status (for debugging purposes)
    public void displayStatus() {
        System.out.println(name + " | Health: " + health + " | Attack Power: " + attackPower + " | Defense Power: " + defensePower);
    }
}