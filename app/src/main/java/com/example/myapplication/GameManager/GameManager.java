package com.example.myapplication.GameManager;

import android.content.Context;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GameManager {
    private Player player;
    private Room currentRoom;
    private Point currentPosition;
    private Map<Point, Room> roomMap;

    private Context context;
    private RoomGenerator roomGenerator;
    public GameManager(Context context) {
        this.player = new Player();
        this.roomMap = new HashMap<>();
        this.currentPosition = new Point(0, 0);
        this.roomGenerator = new RoomGenerator(context);

        // Create the starting room safely
        Room startingRoom = new Room(RoomType.EMPTY, null, new Point(currentPosition), "You awaken in a dark, cold room.",50,50);


        // Make sure it's added to the map and set as the current room
        roomMap.put(new Point(currentPosition), startingRoom);
        player.currentRoom = startingRoom;
        this.currentRoom = startingRoom;
        generateBossPath(2);
    }

    private void generateBossPath(int length) {
        Point current = new Point(0, 0);
        roomMap.put(current, new Room(current)); // Starting room

        Random random = new Random();
        Set<Point> usedPoints = new HashSet<>();
        usedPoints.add(new Point(current));

        Room previousRoom = roomMap.get(current);

        for (int i = 0; i < length; i++) {
            List<Direction> availableDirs = getUnvisitedDirections(current, usedPoints);
            if (availableDirs.isEmpty()) break;

            Direction chosen = availableDirs.get(random.nextInt(availableDirs.size()));
            Point next = getNeighborPosition(current, chosen);

            Room newRoom = new Room(next);
            roomMap.put(next, newRoom);
            usedPoints.add(new Point(next));

            previousRoom.connectRoom(chosen, newRoom);
            newRoom.connectRoom(chosen.getOpposite(), previousRoom);

            previousRoom = newRoom;
            current = next;
        }

        // Mark the last room as the boss room
        previousRoom.setType(RoomType.BOSS);
        previousRoom.setDescription("You sense a great evil lurking here...");
    }

    private List<Direction> getUnvisitedDirections(Point current, Set<Point> used) {
        List<Direction> directions = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            Point next = getNeighborPosition(current, dir);
            if (!used.contains(next)) {
                directions.add(dir);
            }
        }
        return directions;
    }
    public String move(Direction direction) {
        String message = "";
        Room nextRoom = null;
        Point nextPosition = getNeighborPosition(currentPosition, direction);

        if (roomMap.get(nextPosition) != null) {
            nextRoom = roomMap.get(nextPosition);
        }

        // Exit is blocked?
        if (currentRoom.isExitBlocked(direction)) {
            message = "The exit is blocked!";
        }

        // If nextRoom is null or we are blocked, generate anyway
        if (nextRoom == null) {
            nextRoom = roomGenerator.generateRoom(nextPosition, currentRoom, direction);
            roomMap.put(nextPosition, nextRoom);

            if (message.equals("The exit is blocked!")) {
                currentRoom.blockExitBidirectional(direction);
            } else {
                currentRoom.connectRoomBidirectional(direction, nextRoom);
            }
        } else if (!message.equals("The exit is blocked!")) {
            // Existing room and not blocked — make sure they are connected
            currentRoom.connectRoomBidirectional(direction, nextRoom);
        }

        if (!message.equals("The exit is blocked!")) {
            currentPosition = nextPosition;
            currentRoom = nextRoom;
            message = "You move " + direction.toString().toLowerCase() + ".\n" + currentRoom.getDescription();
        }

        return message;
    }

    /*
    public String act(PlayerAction action) {
        return currentRoom.resolveAction(player, action);
    }

     */

    public Player getPlayer() {
        return player;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public Point getCurrentPosition() {
        return currentPosition;
    }

    public Map<Point, Room> getRoomMap() {
        return roomMap;
    }

    private Point getNeighborPosition(Point pos, Direction dir) {
        switch (dir) {
            case UP: return new Point(pos.x, pos.y - 1);
            case DOWN: return new Point(pos.x, pos.y + 1);
            case LEFT: return new Point(pos.x - 1, pos.y);
            case RIGHT: return new Point(pos.x + 1, pos.y);
            default: return pos;
        }
    }

    private Direction getOppositeDirection(Direction dir) {
        switch (dir) {
            case UP: return Direction.DOWN;
            case DOWN: return Direction.UP;
            case LEFT: return Direction.RIGHT;
            case RIGHT: return Direction.LEFT;
            default: return dir;
        }
    }
}