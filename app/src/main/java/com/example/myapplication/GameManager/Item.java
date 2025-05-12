package com.example.myapplication.GameManager;

import java.io.Serializable;

public class Item implements Serializable{
    private String name;
    private int attackBoost;
    private int defenseBoost;
    private int healthBoost;

    public Item(String name, int atk, int def, int hp) {
        this.name = name;
        this.attackBoost = atk;
        this.defenseBoost = def;
        this.healthBoost = hp;
    }

    // Getters
    public String getName() { return name; }
    public int getAttackBoost() { return attackBoost; }
    public int getDefenseBoost() { return defenseBoost; }
    public int getHealthBoost() {
        this.healthBoost = this.healthBoost + 50;
        return healthBoost;
    }

    public String getStats() {
        return "Attack Boost: " + attackBoost + "\n" +"Defense Boost: " + defenseBoost + "\n"+"Health Boost: " + healthBoost;
    }


}
