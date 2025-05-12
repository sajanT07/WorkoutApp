package com.example.myapplication.GameManager;

import android.graphics.Point;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Represents a single room in the game dungeon
 */
public class Room {
    private RoomType type;
    private Enemy enemy;
    private final Point position;
    private String description;
    private final Map<Direction, Room> neighbors;
    private final Set<Direction> blockedExits;

    private int width;
    private int height;

    // Game state flags
    private boolean playerDefeated = false;
    private boolean allowMovement = true;

    /**
     * Complete constructor for a room
     */
    public Room(RoomType type, Enemy enemy, Point position, String description,int width,int height) {
        this.type = type;
        this.enemy = enemy;
        this.position = position;
        this.description = description;
        this.neighbors = new EnumMap<>(Direction.class);
        this.blockedExits = new HashSet<>();
    }

    /**
     * Simplified constructor for a basic empty room
     */
    public Room(Point position) {
        this(RoomType.EMPTY, null, position, "A dark and silent room.",30,30);
    }

    /**
     * Connects two rooms bidirectionally
     */
    public void connectRoomBidirectional(Direction dir, Room neighbor) {
        if (neighbor == null) {
            throw new IllegalArgumentException("Cannot connect to a null room");
        }

        neighbors.put(dir, neighbor);
        blockedExits.remove(dir);

        neighbor.neighbors.put(dir.getOpposite(), this);
        neighbor.blockedExits.remove(dir.getOpposite());
    }

    /**
     * Blocks exits between two rooms bidirectionally
     */
    public void blockExitBidirectional(Direction dir) {
        blockedExits.add(dir);
        Room neighbor = neighbors.get(dir);
        if (neighbor != null) {
            neighbor.blockedExits.add(dir.getOpposite());
        }
    }

    /**
     * Randomly blocks exits except for the specified direction
     */
    public void randomlyBlockRemainingExits(Direction alreadyLinked) {
        Random rand = new Random();
        for (Direction dir : Direction.values()) {
            // Skip the starting room (0,0) or already linked direction
            if (!Objects.equals(position, new Point(0, 0)) && dir != alreadyLinked) {
                if (rand.nextBoolean()) {
                    blockExitBidirectional(dir);
                }
            }
        }
    }

    /**
     * One-way connection to another room
     */
    public void connectRoom(Direction direction, Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Cannot connect to a null room");
        }
        neighbors.put(direction, room);
    }

    /**
     * Handles player actions within the room and returns a message
     * Side effects like player defeat or movement allowance must be checked via getters
     */
    public String resolveAction(Player player, PlayerAction action) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        switch (action) {
            case ATTACK:
                return handleAttack(player);
            case RUN:
                return handleRun(player);
            case SEARCH:
                return handleSearch(player);
            case USE_ITEM:
                return handleUseItem(player);
            default:
                return "No action resolved.";
        }
    }

    /**
     * Handles attack action logic
     */
    private String handleAttack(Player player) {
        if (type != RoomType.ENEMY || enemy == null) {
            return "There's no enemy to attack!";
        }

        // Attack the enemy
        enemy.takeDamage(player.getAttack());
        player.takeDamage(enemy.getAttackPower());

        // Check if player died
        if (!player.isAlive()) {
            this.playerDefeated = true;
            this.allowMovement = false;
            return "You were defeated by the enemy!";
        }

        // Check if the enemy is defeated
        if (!enemy.isAlive()) {
            Item loot = ItemGenerator.generateRandomItem();
            player.addItem(loot);
            setRoomState(RoomType.EMPTY, null);
            player.gainXP(50);
            return "You defeated the enemy! You found: " + loot.getName();
        } else {
            return String.format("You hit the enemy! It's still standing.\nCurrent HP: %d\nCurrent Enemy HP: %d",
                    player.getHealth(), enemy.getHealth());
        }
    }

    /**
     * Handles run action logic
     */
    private String handleRun(Player player) {
        if (type != RoomType.ENEMY || enemy == null) {
            return "There's no enemy to run from!";
        }

        // 50% chance to escape successfully
        if (new Random().nextInt(100) > 50) {
            setRoomState(RoomType.EMPTY, null);
            this.allowMovement = true;
            return "You successfully ran away from the enemy!";
        } else {
            return "You tried to run, but the enemy caught up!";
        }
    }

    /**
     * Handles search action logic
     */
    private String handleSearch(Player player) {
        if (type == RoomType.TREASURE) {
            Item item = ItemGenerator.generateRandomItem();
            player.addItem(item);

            setRoomState(RoomType.EMPTY, null);
            return "You found treasure: " + item.getName();
        } else if (type == RoomType.EMPTY) {
            return "The room is empty, nothing to search for.";
        } else {
            return "You search the room but find nothing of interest.";
        }
    }

    /**
     * Handles use item action logic
     */
    private String handleUseItem(Player player) {
        Item item = player.getSelectedItem();
        if (item != null) {
            player.applyItemEffects(item);
            player.getInventory().remove(item);
            return "You used a" + item.getName() + "! added stats.";
        }
        return "No usable item found in inventory.";
    }

    /**
     * Updates room state in a consistent way
     */
    private void setRoomState(RoomType newType, Enemy newEnemy) {
        this.type = newType;
        this.enemy = newEnemy;
    }

    /**
     * Retrieves an item from player inventory by name
     */
    private Item getItemFromInventory(Player player, String itemName) {
        for (Item item : player.getInventory()) {
            if (item.getName().equals(itemName)) {
                return item;
            }
        }
        return null;
    }

    // Getters and setters with improved safety

    public boolean isExitBlocked(Direction dir) {
        return blockedExits.contains(dir);
    }

    public void setBlockedExit(Direction dir) {
        blockExitBidirectional(dir);
    }

    public Room getNeighbor(Direction direction) {
        return neighbors.get(direction);
    }

    public Map<Direction, Room> getNeighbors() {
        return Collections.unmodifiableMap(neighbors);
    }

    public Set<Direction> getBlockedExits() {
        return Collections.unmodifiableSet(blockedExits);
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
        // Keep room state consistent
        if (enemy != null) {
            this.type = RoomType.ENEMY;
        }
    }

    public Point getPosition() {
        return new Point(position.x, position.y); // Return a defensive copy
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setWidth(int w) { width = w; }
    public void setHeight(int h) { height = h; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPlayerDefeated() {
        return playerDefeated;
    }

    public boolean isMovementAllowed() {
        return allowMovement;
    }

    public void setAllowMovement(boolean allowMovement) {
        this.allowMovement = allowMovement;
    }


    /**
     * Reset player defeated state - typically called after game restart
     */
    public void resetPlayerDefeated() {
        this.playerDefeated = false;
        this.allowMovement = true;
    }
}