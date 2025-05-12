package com.example.myapplication.ui.GameActivity;

import com.example.myapplication.GameManager.Room;

public class RoomDisplayInfo {
    Room room;
    int gridX, gridY; // Grid coordinates
    int size;         // Size of the cell (1, 2, or 3)

    RoomDisplayInfo(Room room, int gridX, int gridY, int size) {
        this.room = room;
        this.gridX = gridX;
        this.gridY = gridY;
        this.size = size;
    }
}
