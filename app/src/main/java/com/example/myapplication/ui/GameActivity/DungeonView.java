package com.example.myapplication.ui.GameActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.example.myapplication.GameManager.Direction;
import com.example.myapplication.GameManager.GameManager;
import com.example.myapplication.GameManager.Room;
import com.example.myapplication.GameManager.RoomType;

import java.util.Map;

public class DungeonView extends View {
    private GameManager gameManager;

    // Paints for room types
    private Paint emptyPaint, treasurePaint, enemyPaint, bossPaint, transparentPaint;
    private Paint borderPaint, doorPaint, hallPaint, currentDotPaint;

    private int tileSize = 100;
    private int viewWidth, viewHeight;

    public DungeonView(Context ctx) {
        super(ctx);
        init();
    }
    public DungeonView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    private void init() {
        emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setStyle(Paint.Style.FILL);
        emptyPaint.setColor(0xFFBBBBBB);

        treasurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        treasurePaint.setStyle(Paint.Style.FILL);
        treasurePaint.setColor(0xFFFFFF00);

        enemyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        enemyPaint.setStyle(Paint.Style.FILL);
        enemyPaint.setColor(0xFFFF0000);

        bossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bossPaint.setStyle(Paint.Style.FILL);
        bossPaint.setColor(0xFFFF00FF);

        transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        transparentPaint.setStyle(Paint.Style.FILL);
        transparentPaint.setColor(0x00FFFFFF); // Fully transparent

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setColor(0xFF444444);

        doorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        doorPaint.setStyle(Paint.Style.FILL);
        doorPaint.setColor(0xFFFFA500);

        hallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hallPaint.setStyle(Paint.Style.FILL);
        hallPaint.setColor(0xFF8B4513);

        currentDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentDotPaint.setStyle(Paint.Style.FILL);
        currentDotPaint.setColor(0xFF4CAF50);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
    }

    public void setGameManager(GameManager gm) {
        this.gameManager = gm;
        invalidate();
    }

    public void setTileSize(int size) {
        this.tileSize = size;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (gameManager == null) return;

        Point curPos = gameManager.getCurrentPosition();
        Room curRoom = gameManager.getCurrentRoom();
        float roomCX = curPos.x * tileSize + curRoom.getWidth() * 0.5f;
        float roomCY = curPos.y * tileSize + curRoom.getHeight() * 0.5f;
        float screenCX = viewWidth * 0.5f;
        float screenCY = viewHeight * 0.5f;

        canvas.save();
        canvas.translate(screenCX - roomCX, screenCY - roomCY);

        int ct = tileSize / 8;
        for (Map.Entry<Point, Room> e : gameManager.getRoomMap().entrySet()) {
            Point pos = e.getKey();
            Room r = e.getValue();
            int left = pos.x * tileSize;
            int top = pos.y * tileSize;
            int right = left + r.getWidth();
            int bottom = top + r.getHeight();

            Room nr = r.getNeighbors().get(Direction.RIGHT);
            if (nr != null && !r.isExitBlocked(Direction.RIGHT)) {
                Point np = nr.getPosition();
                int nLeft = np.x * tileSize;
                int cy = top + r.getHeight() / 2 - ct / 2;
                int cw = nLeft - right;
                canvas.drawRect(right, cy, right + cw, cy + ct, hallPaint);
            }

            Room nd = r.getNeighbors().get(Direction.DOWN);
            if (nd != null && !r.isExitBlocked(Direction.DOWN)) {
                Point np = nd.getPosition();
                int nTop = np.y * tileSize;
                int cx = left + r.getWidth() / 2 - ct / 2;
                int ch = nTop - bottom;
                canvas.drawRect(cx, bottom, cx + ct, bottom + ch, hallPaint);
            }
        }

        for (Map.Entry<Point, Room> e : gameManager.getRoomMap().entrySet()) {
            Point pos = e.getKey();
            Room r = e.getValue();

            int left = pos.x * tileSize;
            int top = pos.y * tileSize;
            int right = left + r.getWidth();
            int bottom = top + r.getHeight();

            int open = 0;
            for (Direction d : Direction.values())
                if (!r.isExitBlocked(d) && r.getNeighbors().containsKey(d)) open++;

            Paint fill;
            if (open == 0) {
                fill = transparentPaint;
                borderPaint = transparentPaint;
            } else {
                switch (r.getType()) {
                    case TREASURE: fill = treasurePaint; break;
                    case ENEMY: fill = enemyPaint; break;
                    case BOSS: fill = bossPaint; break;
                    case EMPTY:
                    default: fill = emptyPaint; break;
                }
            }

            canvas.drawRect(left, top, right, bottom, fill);
            canvas.drawRect(left, top, right, bottom, borderPaint);

            int ds = tileSize / 4;
            for (Direction d : Direction.values()) {
                if (!r.isExitBlocked(d) && r.getNeighbors().containsKey(d)) {
                    switch (d) {
                        case UP:
                            canvas.drawRect(left + r.getWidth() / 2 - ds / 2, top - ds / 2,
                                    left + r.getWidth() / 2 + ds / 2, top + ds / 2, doorPaint);
                            break;
                        case DOWN:
                            canvas.drawRect(left + r.getWidth() / 2 - ds / 2, bottom - ds / 2,
                                    left + r.getWidth() / 2 + ds / 2, bottom + ds / 2, doorPaint);
                            break;
                        case LEFT:
                            canvas.drawRect(left - ds / 2, top + r.getHeight() / 2 - ds / 2,
                                    left + ds / 2, top + r.getHeight() / 2 + ds / 2, doorPaint);
                            break;
                        case RIGHT:
                            canvas.drawRect(right - ds / 2, top + r.getHeight() / 2 - ds / 2,
                                    right + ds / 2, top + r.getHeight() / 2 + ds / 2, doorPaint);
                            break;
                    }
                }
            }
        }

        float currentCX = curPos.x * tileSize + curRoom.getWidth() * 0.5f;
        float currentCY = curPos.y * tileSize + curRoom.getHeight() * 0.5f;
        float radius = Math.min(curRoom.getWidth(), curRoom.getHeight()) * 0.3f;
        canvas.drawCircle(currentCX, currentCY, radius, currentDotPaint);

        canvas.restore();
    }
}