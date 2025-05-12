package com.example.myapplication.GameManager;

import java.util.Random;

public class EnemyFactory {
    private static final Random random = new Random();

    private static final String[] names = {
            "Goblin", "Troll", "Dragon", "Zombie", "Vampire", "Werewolf", "Ogre", "Skeleton", "Bandit", "Witch"
    };

    private static final String[] descriptions = {
            "A small and vicious creature.",
            "A towering, menacing beast.",
            "A mighty dragon with fiery breath.",
            "An undead creature hungry for flesh.",
            "A bloodthirsty creature of the night.",
            "A wolf-like creature cursed with lycanthropy.",
            "A hulking, brutish giant.",
            "An animated skeleton from the grave.",
            "A ruthless human bandit.",
            "A sorceress with dark powers."
    };

    // Method to generate a random enemy
    public static Enemy createRandomEnemy() {
        String name = names[random.nextInt(names.length)];
        int health = random.nextInt(50) + 50; // Health between 50 and 150
        int attackPower = random.nextInt(30) + 10; // Attack power between 10 and 40
        int defensePower = random.nextInt(20) + 5; // Defense power between 5 and 25
        String description = descriptions[random.nextInt(descriptions.length)];

        return new Enemy(name, health, attackPower, defensePower, description);
    }

    // Method to create a random set of enemies
    public static Enemy[] generateEnemies(int numberOfEnemies) {
        Enemy[] enemies = new Enemy[numberOfEnemies];
        for (int i = 0; i < numberOfEnemies; i++) {
            enemies[i] = createRandomEnemy();
        }
        return enemies;
    }
}
