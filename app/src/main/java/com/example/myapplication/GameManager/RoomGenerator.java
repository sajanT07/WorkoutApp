package com.example.myapplication.GameManager;

import android.content.Context;
import android.graphics.Point;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class RoomGenerator {

    private static final Random random = new Random();
    int enemyRooms;
    int treasureRooms;
    int emptyRooms;
    private Context context;
    RoomGenerator(Context context){
        enemyRooms = 0;
        treasureRooms = 0;
        emptyRooms = 0;
        this.context = context;
    }
    public Room generateRoom(Point position, Room fromRoom, Direction fromDirection) {
        Room newRoom = new Room(position);

        // Randomly assign room type
        int roll = random.nextInt(100);
        if (roll < 40) {
            newRoom.setType(RoomType.ENEMY);
            newRoom.setEnemy(EnemyFactory.createRandomEnemy());
            newRoom.setDescription("A" + newRoom.getEnemy().getName() + " approaches!" + "\n" + newRoom.getEnemy().getDescription());
            enemyRooms++;
        } else if (roll < 60) {
            newRoom.setType(RoomType.TREASURE);
            newRoom.setDescription("There's a glimmer of treasure in the corner.");
            treasureRooms++;
        } else {
            newRoom.setType(RoomType.EMPTY);
            newRoom.setDescription("The room is quiet and empty.");
            emptyRooms++;
        }
        int randomWidth = (int) (Math.random() * 180) + 30;  // Random width between 30-80
        int randomHeight = (int) (Math.random() * 150) + 30; // Random height between 30-80
        newRoom.setHeight(randomHeight);
        newRoom.setWidth(randomWidth);

        // Always connect the room to its origin bidirectionally
        newRoom.connectRoomBidirectional(fromDirection.getOpposite(), fromRoom);

        // Randomly block other directions (excluding the one we came from)
        for (Direction dir : Direction.values()) {
            if (dir != fromDirection.getOpposite()) {
                if (random.nextBoolean()) {
                    newRoom.blockExitBidirectional(dir);
                }
            }
        }

        return newRoom;
    }

    private RoomType rollRoomType() {
        int roll = random.nextInt(100);
        if (roll < 50){
            enemyRooms++;
            return RoomType.ENEMY;
        }
        if (roll < 80){

            return RoomType.TREASURE;
        }
        return RoomType.EMPTY;
    }

    public int getEmptyRooms() {
        return emptyRooms;
    }
    int getEnemyRooms() {
        return enemyRooms;

    }
    int getTreasureRooms() {
        return treasureRooms;
    }
    private static String getDescriptionForType(RoomType type) {
        switch (type) {
            case ENEMY: return "You see a hostile creature!";
            case TREASURE: return "The room glimmers with hidden loot.";
            case EMPTY:
            default: return "The room is eerily silent.";
        }
    }
}