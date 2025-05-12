package com.example.myapplication.ui.GameActivity;

import android.content.Context;
import android.graphics.Point;

import com.example.myapplication.GameManager.Room;

import java.util.HashMap;
import java.util.Map;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class VariableSizeGridView extends View {

    private Map<Point, Room> roomMap = new HashMap<>();
    private Map<Room, Rect> layoutMap = new HashMap<>();
    private Paint paint;
    private int baseUnitSize = 50; // dp

    public VariableSizeGridView(Context context) {
        super(context);
        init();
    }

    public VariableSizeGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void setRoomData(Map<Point, Room> data) {
        this.roomMap = data;
        calculateLayout();
        invalidate(); // Request redraw
    }

    private void calculateLayout() {
        layoutMap.clear();

        for (Map.Entry<Point, Room> entry : roomMap.entrySet()) {
            Point p = entry.getKey();
            Room room = entry.getValue();

            // Calculate room size based on sizeCategory
            int width = (int)(Math.random()*30 + 5);
            int height = (int)(Math.random()*30 + 5);

            // Create rectangle for this room
            Rect rect = new Rect(
                    p.x * baseUnitSize,
                    p.y * baseUnitSize,
                    p.x * baseUnitSize + width * baseUnitSize,
                    p.y * baseUnitSize + height * baseUnitSize
            );

            // Store in layout map
            layoutMap.put(room, rect);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawColor(Color.LTGRAY);

        // Draw rooms
        for (Map.Entry<Room, Rect> entry : layoutMap.entrySet()) {
            Room room = entry.getKey();
            Rect rect = entry.getValue();

            // Fill room
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawRect(rect, paint);

            // Draw border
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            canvas.drawRect(rect, paint);

            // Draw room name
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setTextSize(20);
            //canvas.drawText(room.name, rect.left + 10, rect.top + 30, paint);
        }
    }
}