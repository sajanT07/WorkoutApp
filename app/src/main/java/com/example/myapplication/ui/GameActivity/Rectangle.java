package com.example.myapplication.ui.GameActivity;

public class Rectangle {
    int x, y, width, height;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean intersects(Rectangle other) {
        return x < other.x + other.width && x + width > other.x &&
                y < other.y + other.height && y + height > other.y;
    }
}