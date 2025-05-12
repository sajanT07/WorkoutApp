package com.example.myapplication.ui.GameActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.GameManager.Direction;
import com.example.myapplication.GameManager.GameManager;
import com.example.myapplication.GameManager.Item;
import com.example.myapplication.GameManager.Player;
import com.example.myapplication.GameManager.PlayerAction;
import com.example.myapplication.GameManager.Room;
import com.example.myapplication.GameManager.RoomType;
import com.example.myapplication.R;
import com.example.myapplication.ui.ExerciseDataLevelInput.ExerciseDataActivity;
import com.example.myapplication.ui.ExerciseList.ExerciseList;
import com.example.myapplication.ui.FAB.OptionBottomSheet;
import com.example.myapplication.ui.FAB.OptionItem;
import com.example.myapplication.ui.GameActivity.InventoryFragment.InventoryViewModel;
import com.example.myapplication.ui.ProgressActivity.ProgressActivity;
import com.example.myapplication.ui.WelcomeActivity.WelcomeActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameMainActivity extends AppCompatActivity {

    private static final int MAP_SIZE = 11;
    private static final int CENTER = MAP_SIZE / 2;
    private Map<Point, Room> discoveredRooms = new HashMap<>();
    private Point currentPosition = new Point(CENTER, CENTER);
    private DungeonView dungeonView;
    private GameManager gameManager;
    private TextView roomDescriptionText;
    private TextView actionDescriptionText;


    private TextView inventoryDescriptionText;
    InventoryViewModel viewModel ;

    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_main);

        roomDescriptionText = findViewById(R.id.roomDescriptionText);
        actionDescriptionText = findViewById(R.id.actionDescriptionText);
        //inventoryDescriptionText = findViewById(R.id.inventoryDescriptionText);
        gameManager = new GameManager(this);
        dungeonView = findViewById(R.id.dungeonView);
        dungeonView.setGameManager(gameManager);
        Player player = loadPlayerData();
        this.player = player;
        loadGameData();
       // updateMapGrid();
        //GridLayout mapGrid = findViewById(R.id.mapGrid);


        //inventoryDescriptionText.setText(player.getInventoryDetailsAsString(player.getInventory()));
        updateRoomDescription("You enter the dungeon...\n" + gameManager.getCurrentRoom().getDescription());

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPagerAdapter adapter;
        if(player.getInventory() != null && !(player.getInventory().isEmpty())){
            adapter = new ViewPagerAdapter((FragmentActivity) this, (ArrayList<Item>) player.getInventory());
        }else{
            adapter = new ViewPagerAdapter((FragmentActivity) this);
        }
        viewPager.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        viewModel.setPlayer(player);


        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Inventory");
                    break;
                case 1:
                    tab.setText("Stats");
                    break;
                case 2:
                    tab.setText("Usable Items");
                    break;
                case 3:
                    tab.setText("Rooms Travelled");
                    break;
            }
        }).attach();

        viewModel.setItems(player.getInventory());

        findViewById(R.id.buttonUp).setOnClickListener(v -> move(Direction.UP));
        findViewById(R.id.buttonDown).setOnClickListener(v -> move(Direction.DOWN));
        findViewById(R.id.buttonLeft).setOnClickListener(v -> move(Direction.LEFT));
        findViewById(R.id.buttonRight).setOnClickListener(v -> move(Direction.RIGHT));
        findViewById(R.id.ATTACK).setOnClickListener(v -> actionDescriptionText.setText(gameManager.getCurrentRoom().resolveAction(player, PlayerAction.ATTACK)));
        findViewById(R.id.RUN).setOnClickListener(v -> actionDescriptionText.setText(gameManager.getCurrentRoom().resolveAction(player, PlayerAction.RUN)));
        findViewById(R.id.SEARCH).setOnClickListener(v -> actionDescriptionText.setText(gameManager.getCurrentRoom().resolveAction(player, PlayerAction.SEARCH)));
        findViewById(R.id.USE_ITEM).setOnClickListener(v -> actionDescriptionText.setText(gameManager.getCurrentRoom().resolveAction(player, PlayerAction.USE_ITEM)));
        setActionVisibility(true);
        findViewById(R.id.Reset).setOnClickListener(v ->clearGameData());
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            // Log when the FAB is clicked
            Log.d("FAB", "FAB clicked, showing options");

            List<OptionItem> options = new ArrayList<>();
            // WelcomeActivity welcomeActivity = (WelcomeActivity) getApplicationContext();

            // Option that leads to a new activity (e.g., SettingsActivity)
            Log.d("FAB", "Adding 'Upload CSV' option");
            //options.add(new OptionItem("Upload CSV", R.drawable.ic_menu_camera, welcomeActivity::csvButton));

            // Option that triggers a method in the current activity
            Log.d("FAB", "Adding 'Welcome List' option");
            options.add(new OptionItem("Welcome Page", R.drawable.ic_menu_gallery, WelcomeActivity.class));

            // Option that triggers another method
            Log.d("FAB", "Adding 'Exercise List' option");
            options.add(new OptionItem("Exercise List", R.drawable.ic_menu_slideshow, ExerciseList.class));
            // Uncomment this line to add Progress option
            Log.d("FAB", "Adding 'Progress' option");
            options.add(new OptionItem("Progress", 0, ExerciseDataActivity.class));

            Log.d("FAB", "Adding 'Progress' option");
            options.add(new OptionItem("Progress", 0, ProgressActivity.class));

            Log.d("FAB", "Adding 'Save' option");
            options.add(new OptionItem("Save Progress", 0, this::savePlayerData));

            // Log to confirm options are prepared
            Log.d("FAB", "Options list prepared: " + options.size() + " options available.");

            // Create BottomSheetDialogFragment
            OptionBottomSheet bottomSheet = new OptionBottomSheet(options);

            // Log when the BottomSheet is shown
            Log.d("FAB", "Showing BottomSheetDialogFragment");
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }


    private void updateRoomDescription(String text) {
        roomDescriptionText.setText(text);
    }

    public void setMovementVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;

        findViewById(R.id.buttonUp).setVisibility(visibility);
        findViewById(R.id.buttonDown).setVisibility(visibility);
        findViewById(R.id.buttonLeft).setVisibility(visibility);
        findViewById(R.id.buttonRight).setVisibility(visibility);
    }
    private void setActionVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;

        findViewById(R.id.ATTACK).setVisibility(visibility);
        findViewById(R.id.SEARCH).setVisibility(visibility);
        findViewById(R.id.USE_ITEM).setVisibility(visibility);
        findViewById(R.id.RUN).setVisibility(visibility);
    }
    /*
    private void updateMapGrid() {
        GridLayout mapGrid = findViewById(R.id.mapGrid);
        mapGrid.removeAllViews();

        // Set a smaller column/row count to allow for larger cells
        int visibleGridSize = 5; // Smaller visible area to accommodate larger cells
        mapGrid.setColumnCount(visibleGridSize);
        mapGrid.setRowCount(visibleGridSize);

        int half = visibleGridSize / 2;

        // Create a map of rooms to display with their sizes
        Map<Point, RoomDisplayInfo> displayRooms = new HashMap<>();

        // First, determine which rooms are visible and their sizes
        for (int y = 0; y < visibleGridSize; y++) {
            for (int x = 0; x < visibleGridSize; x++) {
                int worldX = currentPosition.x + (x - half);
                int worldY = currentPosition.y + (y - half);
                Point worldPoint = new Point(worldX, worldY);

                if (discoveredRooms.containsKey(worldPoint)) {
                    Room room = discoveredRooms.get(worldPoint);
                    int cellSize = getCellSize(room);

                    // Only add if it fits in our grid
                    if (x + cellSize <= visibleGridSize && y + cellSize <= visibleGridSize) {
                        displayRooms.put(worldPoint, new RoomDisplayInfo(room, x, y, cellSize));
                    } else {
                        // If it doesn't fit, add as a 1x1 cell
                        displayRooms.put(worldPoint, new RoomDisplayInfo(room, x, y, 1));
                    }
                }
            }
        }

        // Add special handling for current position
        Point currentWorldPoint = new Point(currentPosition);
        if (!displayRooms.containsKey(currentWorldPoint)) {
            // Current position should be in center
            displayRooms.put(currentWorldPoint, new RoomDisplayInfo(gameManager.getCurrentRoom(), half, half, 1));
        }

        // Create a grid occupancy tracker to avoid overlaps
        boolean[][] occupied = new boolean[visibleGridSize][visibleGridSize];

        // Now add cells to the grid layout
        for (Map.Entry<Point, RoomDisplayInfo> entry : displayRooms.entrySet()) {
            RoomDisplayInfo info = entry.getValue();

            // Skip if any part of this cell is already occupied
            boolean canPlace = true;
            for (int dy = 0; dy < info.size && canPlace; dy++) {
                for (int dx = 0; dx < info.size && canPlace; dx++) {
                    if (info.gridX + dx >= visibleGridSize || info.gridY + dy >= visibleGridSize ||
                            occupied[info.gridY + dy][info.gridX + dx]) {
                        canPlace = false;
                    }
                }
            }

            if (!canPlace) {
                // If can't place with original size, try with size 1
                info.size = 1;
                if (info.gridX < visibleGridSize && info.gridY < visibleGridSize &&
                        !occupied[info.gridY][info.gridX]) {
                    canPlace = true;
                } else {
                    continue; // Skip this room if it still can't be placed
                }
            }

            // Mark cells as occupied
            for (int dy = 0; dy < info.size; dy++) {
                for (int dx = 0; dx < info.size; dx++) {
                    occupied[info.gridY + dy][info.gridX + dx] = true;
                }
            }

            // Create the cell view
            TextView cell = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();

            // Set the cell to span multiple rows/columns
            params.columnSpec = GridLayout.spec(info.gridX, info.size, 1f);
            params.rowSpec = GridLayout.spec(info.gridY, info.size, 1f);

            // Apply margins
            params.setMargins(2, 2, 2, 2);

            // Set cell properties
            cell.setLayoutParams(params);
            cell.setBackgroundColor(getCellColor(entry.getKey()));
            cell.setGravity(Gravity.CENTER);

            // Add room type text
            if (info.room != null) {
                String roomText = getRoomDisplayText(info.room);
                cell.setText(roomText);
                cell.setTextColor(Color.WHITE);
                cell.setTextSize(info.size * 5); // Bigger text for bigger cells
            }

            mapGrid.addView(cell);
        }

        // Fill any unoccupied spaces with empty cells
        for (int y = 0; y < visibleGridSize; y++) {
            for (int x = 0; x < visibleGridSize; x++) {
                if (!occupied[y][x]) {
                    View emptyCell = new View(this);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.columnSpec = GridLayout.spec(x, 1, 1f);
                    params.rowSpec = GridLayout.spec(y, 1, 1f);
                    params.setMargins(2, 2, 2, 2);
                    emptyCell.setLayoutParams(params);
                    emptyCell.setBackgroundColor(Color.TRANSPARENT);
                    mapGrid.addView(emptyCell);
                    occupied[y][x] = true;
                }
            }
        }
    }

     */
    private void updateMapGrid() {
        GridLayout mapGrid =null;// findViewById(R.id.mapGrid);
        mapGrid.removeAllViews();

        // Set grid dimensions
        int gridSize = 10;
        mapGrid.setColumnCount(gridSize*2); // Double the columns
        mapGrid.setRowCount(gridSize*2);    // Double the rows

        // Calculate base cell size
        int parentWidth = mapGrid.getWidth();
        if (parentWidth == 0) parentWidth = 800; // Default fallback
        int baseCellSize = parentWidth / (gridSize * 2);

        // Track occupied cells in our expanded grid
        boolean[][] occupied = new boolean[gridSize * 2][gridSize * 2];

        // Start with placing the current position at center
        int centerX = gridSize;
        int centerY = gridSize;

        // Create a list of rooms to place, with current room first
        List<PlacementCell> cellsToPlace = new ArrayList<>();

        // Add current room first with size 2
        Room currentRoom = gameManager.getCurrentRoom();
        if (currentRoom != null) {
            cellsToPlace.add(new PlacementCell(currentPosition, currentRoom, 2, true));
        }

        // Add other discovered rooms
        for (Map.Entry<Point, Room> entry : discoveredRooms.entrySet()) {
            Point pos = entry.getKey();
            // Skip current position as we already added it
            if (pos.equals(currentPosition)) continue;

            Room room = entry.getValue();
            int size = getRoomSize(room);
            cellsToPlace.add(new PlacementCell(pos, room, size, false));
        }

        // Place cells one by one
        for (PlacementCell cell : cellsToPlace) {
            // For current position, we place at center
            if (cell.isCurrent) {
                placeCell(mapGrid, cell, centerX, centerY, baseCellSize, occupied);
                continue;
            }

            // For other cells, find a good position
            // Calculate relative position from current
            int relX = cell.position.x - currentPosition.x;
            int relY = cell.position.y - currentPosition.y;

            // Convert to grid coordinates
            int gridX = centerX + relX * 2; // Multiply by 2 to space them out
            int gridY = centerY + relY * 2;

            // Ensure we're in bounds
            if (gridX >= 0 && gridX < gridSize * 2 - cell.size &&
                    gridY >= 0 && gridY < gridSize * 2 - cell.size) {

                // Check if it can be placed (no overlap)
                boolean canPlace = true;
                for (int dy = 0; dy < cell.size; dy++) {
                    for (int dx = 0; dx < cell.size; dx++) {
                        if (occupied[gridY + dy][gridX + dx]) {
                            canPlace = false;
                            break;
                        }
                    }
                    if (!canPlace) break;
                }

                if (canPlace) {
                    placeCell(mapGrid, cell, gridX, gridY, baseCellSize, occupied);
                }
            }
        }

        // Fill empty spaces with blank cells
        for (int y = 0; y < gridSize * 2; y++) {
            for (int x = 0; x < gridSize * 2; x++) {
                if (!occupied[y][x]) {
                    View emptyCell = new View(this);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = baseCellSize - 2;
                    params.height = baseCellSize - 2;
                    params.setMargins(1, 1, 1, 1);
                    params.columnSpec = GridLayout.spec(x);
                    params.rowSpec = GridLayout.spec(y);
                    emptyCell.setLayoutParams(params);
                    emptyCell.setBackgroundColor(Color.TRANSPARENT);
                    mapGrid.addView(emptyCell);
                }
            }
        }
    }

    // Helper method to place a cell
    private void placeCell(GridLayout mapGrid, PlacementCell cell, int gridX, int gridY,
                           int baseCellSize, boolean[][] occupied) {
        TextView view = new TextView(this);
        view.setGravity(Gravity.CENTER);

        if (cell.isCurrent) {
            view.setText("YOU");
            view.setBackgroundColor(Color.BLUE);
        } else {
            view.setText(getRoomText(cell.room));
            view.setBackgroundColor(getCellColor(cell.position));
        }

        view.setTextColor(Color.WHITE);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = baseCellSize * cell.size - 10;
        params.height = baseCellSize * cell.size - 10;
        params.setMargins(5, 5, 5, 5);
        params.columnSpec = GridLayout.spec(gridX, cell.size);
        params.rowSpec = GridLayout.spec(gridY, cell.size);

        view.setLayoutParams(params);
        mapGrid.addView(view);

        // Mark cells as occupied
        for (int dy = 0; dy < cell.size; dy++) {
            for (int dx = 0; dx < cell.size; dx++) {
                occupied[gridY + dy][gridX + dx] = true;
            }
        }
    }

    // Helper method to get room size
    private int getRoomSize(Room room) {
        if (room == null) return 1;

        switch (room.getType()) {
            case BOSS:
                return 2; // Boss rooms are larger
            case TREASURE:
                return 2; // Treasure rooms are larger
            default:
                return 1; // Regular rooms are smallest
        }
    }

    // Helper method to get room text
    private String getRoomText(Room room) {
        if (room == null) return "";

        switch (room.getType()) {
            case BOSS:
                return "BOSS";
            case TREASURE:
                return "TRE";
            case ENEMY:
                return "ENE";
            default:
                return "RM";
        }
    }

    // Class to hold placement data
    private static class PlacementCell {
        Point position;
        Room room;
        int size;
        boolean isCurrent;

        PlacementCell(Point position, Room room, int size, boolean isCurrent) {
            this.position = position;
            this.room = room;
            this.size = size;
            this.isCurrent = isCurrent;
        }
    }
    private int getCellSize(Room room) {
        if (room == null) return 1;

        switch (room.getType()) {
            case BOSS:
                return 3; // Boss rooms are largest (3x3)
            case TREASURE:
                return 2; // Treasure rooms are medium (2x2)
            case ENEMY:
                return 2; // Enemy rooms are medium (2x2)
            default:
                return 1; // Regular rooms are smallest (1x1)
        }
    }

    // Helper method to get display text for room
    private String getRoomDisplayText(Room room) {
        if (room == null) return "";

        switch (room.getType()) {
            case BOSS:
                return "BOSS";
            case TREASURE:
                return "TREASURE";
            case ENEMY:
                return "ENEMY";
            default:
                return "ROOM";
        }
    }

    public void savePlayerData() {
        Player player = this.player;
        SharedPreferences prefs = getSharedPreferences("RoguePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("health", player.getHealth());
        editor.putInt("attack", player.getAttack());
        editor.putInt("defense", player.getDefense());
        editor.putInt("xp", player.getXP());
        editor.putString("inventory", player.saveInventory());
        editor.putString("appliedItems", player.saveAppliedItems());
        editor.putInt("posX", currentPosition.x);
        editor.putInt("posY", currentPosition.y);

        // Save discovered rooms (as a serialized string or encoded format)
        StringBuilder discoveredData = new StringBuilder();
        for (Map.Entry<Point, Room> entry : discoveredRooms.entrySet()) {
            Point p = entry.getKey();
            Room r = entry.getValue();
            discoveredData.append(p.x).append(",").append(p.y).append(",").append(r.getType()).append(";");
        }
        editor.putString("discoveredRooms", discoveredData.toString());
        editor.apply();
    }
    public void clearGameData() {
        SharedPreferences prefs = getSharedPreferences("RoguePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Game data cleared!", Toast.LENGTH_SHORT).show();

        // Optionally restart activity to reset the game state visually
        recreate();
    }

    public void loadGameData() {
        SharedPreferences prefs = getSharedPreferences("RoguePrefs", MODE_PRIVATE);

        // Load position
        int x = prefs.getInt("posX", CENTER);
        int y = prefs.getInt("posY", CENTER);
        currentPosition = new Point(x, y);

        // Load discovered rooms
        String roomData = prefs.getString("discoveredRooms", "");
        if (!roomData.isEmpty()) {
            String[] entries = roomData.split(";");
            for (String entry : entries) {
                String[] parts = entry.split(",");
                if (parts.length == 3) {
                    int px = Integer.parseInt(parts[0]);
                    int py = Integer.parseInt(parts[1]);
                    RoomType type = RoomType.valueOf(parts[2]);

                    Point p = new Point(px, py);
                    Room room = new Room(p);
                    room.setType(type);  // Use your setter
                    discoveredRooms.put(p, room);
                }
            }
        }
    }

    public Player loadPlayerData() {
        SharedPreferences prefs = getSharedPreferences("RoguePrefs", MODE_PRIVATE);
        Player player = new Player(prefs.getInt("health", 100),prefs.getInt("attack", 10),prefs.getInt("defense", 5),prefs.getInt("xp", 0));
        String inventoryJson = prefs.getString("inventory", null);
        String appliedItemsJson = prefs.getString("appliedItems", null);
        player.loadAppliedItems(appliedItemsJson);
        player.loadInventory(inventoryJson);
        return player;
    }
    private int getCellColor(Point p) {
        if (p.equals(currentPosition)) return Color.BLUE;  // Player's current location
        else if (discoveredRooms.containsKey(p)) {
            Room room = discoveredRooms.get(p);
            assert room != null;
            if (room.getType() == RoomType.ENEMY) return Color.RED;
            if (room.getType() == RoomType.TREASURE) return Color.YELLOW;
            if(room.getType() == RoomType.BOSS)return Color.GREEN;
            return Color.GRAY;  // Explored
        }
        return Color.TRANSPARENT;  // Unexplored
    }
    /*
    private void move(Direction direction) {
        String result = gameManager.move(direction);
        setActionVisibility(true);
        if(!result.equals( "The exit is blocked!")){
            currentRoomChanged(direction);
            updateMapGrid();
            if (gameManager.getCurrentRoom().getType() == RoomType.BOSS) {
                //setMovementVisibility(false);
                setActionVisibility(true);
            } else if (gameManager.getCurrentRoom().getType() == RoomType.TREASURE) {
                setActionVisibility(true);
            }else if (gameManager.getCurrentRoom().getType() == RoomType.ENEMY) {
                //setMovementVisibility(false);
                setActionVisibility(true);
            }
            InventoryViewModel viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
            viewModel.setItems(player.getInventory());
        }
        updateRoomDescription(result);

    }

     */
    private void move(Direction dir) {
        String msg = gameManager.move(dir);
        roomDescriptionText.setText(msg);
        dungeonView.invalidate();
    }

    private void action(PlayerAction act) {
        Player player = gameManager.getPlayer();
        String outcome = gameManager.getCurrentRoom().resolveAction(player, act);
        roomDescriptionText.setText(outcome);
    }

    private void currentRoomChanged(Direction direction) {
        switch (direction) {
            case UP: currentPosition.y -= 1; break;
            case DOWN: currentPosition.y += 1; break;
            case LEFT: currentPosition.x -= 1; break;
            case RIGHT: currentPosition.x += 1; break;
        }
        discoveredRooms.put(new Point(currentPosition), gameManager.getCurrentRoom());
    }
}
