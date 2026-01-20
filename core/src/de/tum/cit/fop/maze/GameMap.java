package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.objects.Exit;
import de.tum.cit.fop.maze.objects.GameObj;
import de.tum.cit.fop.maze.objects.Key;
import de.tum.cit.fop.maze.objects.enemies.ChaserEnemy;
import de.tum.cit.fop.maze.objects.enemies.Enemy;
import de.tum.cit.fop.maze.objects.enemies.PatrolEnemy;
import de.tum.cit.fop.maze.objects.powerups.Hpup;
import de.tum.cit.fop.maze.objects.powerups.Powerup;
import de.tum.cit.fop.maze.objects.powerups.Speedup;
import de.tum.cit.fop.maze.objects.traps.DamageTrap;
import de.tum.cit.fop.maze.objects.traps.SludgeTrap;
import de.tum.cit.fop.maze.objects.traps.Trap;
import de.tum.cit.fop.maze.screens.BeginScreen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * The GameMap class manages the game world including terrain, objects, and procedural generation.
 * Handles map loading, rendering, collision detection, and endless mode generation.
 * <p>Map textures from: <a href="https://kenney.nl/assets/tiny-dungeon">Tiny Dungeon by Kenney</a>
 */
public class GameMap {
    private static final Texture TILE_SHEET = new Texture("main_tilemap.png");
    public static final TextureRegion[][] TEXTURE_REGION = TextureRegion.split(TILE_SHEET, TILE_SHEET.getWidth() / 12, TILE_SHEET.getHeight() / 11);
    private final Map<String, Integer> tiles = new HashMap<>();
    private final TextureRegion wall = TEXTURE_REGION[3][4];
    private final TextureRegion[] floor = {TEXTURE_REGION[4][0], TEXTURE_REGION[4][1]};
    private final TextureRegion entry = TEXTURE_REGION[4][0];
    private final List<Trap> traps = new ArrayList<>();
    private final List<Key> keys = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Powerup> powerups = new ArrayList<>();
    private final PlayerStats playerStats = BeginScreen.STATS;
    private int w, h;
    private int ex, ey;
    private Exit exit;
    private boolean isEndless = false;

    /**
     * Loads a map from a properties file and initialises all game objects.
     * Clears existing map data and parses tile values to populate the map.
     *
     * @param path The path to the map properties file
     * @throws RuntimeException If the map file cannot be loaded
     */
    public void load(String path) {
        tiles.clear();
        traps.clear();
        keys.clear();
        enemies.clear();
        powerups.clear();
        exit = null;

        Properties p = new Properties();
        try (BufferedReader r = new BufferedReader(Gdx.files.internal(path).reader())) {
            p.load(r);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load map.", e);
        }

        int maxX = 0, maxY = 0;
        for (String key : p.stringPropertyNames()) {
            int val = Integer.parseInt(p.getProperty(key).trim());
            tiles.put(key, val);

            String[] split = key.split(",");
            int x = Integer.parseInt(split[0]);
            int y = Integer.parseInt(split[1]);

            if (x > maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }

            switch (val) {
                case 1 -> {
                    ex = x;
                    ey = y;
                }
                case 2 -> exit = new Exit(x, y);
                case 3 -> traps.add(Math.round(Math.random()) == 0 ?
                        new SludgeTrap(x, y, 0.33f) : new DamageTrap(x, y, 1f));
                case 4 -> enemies.add(Math.round(Math.random()) == 0 ?
                        new ChaserEnemy(x, y) : new PatrolEnemy(x, y, Math.round(Math.random()) == 0));
                case 5 -> keys.add(new Key(x, y));
                case 6 -> powerups.add(Math.round(Math.random()) == 0 ?
                        new Hpup(x, y) : new Speedup(x, y));
            }
        }
        w = maxX + 1;
        h = maxY + 1;
    }

    /**
     * Renders the entire game map including terrain, objects, and entities.
     *
     * @param batch The SpriteBatch used for drawing
     */
    public void render(SpriteBatch batch) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Integer v = tiles.get(x + "," + y);
                TextureRegion t;
                if (v == null) {
                    t = floor[1];
                } else {
                    t = switch (v) {
                        case 0 -> wall;
                        case 1 -> entry;
                        default -> floor[1];
                    };
                }
                batch.draw(t, x * GameObj.TILE, y * GameObj.TILE, GameObj.TILE, GameObj.TILE);
            }
        }
        traps.forEach(trap -> trap.render(batch));
        keys.forEach(key -> key.render(batch));
        enemies.forEach(enemy -> enemy.render(batch));
        powerups.forEach(powerup -> powerup.render(batch));
        exit.render(batch);
    }

    /**
     * Gets the tile value at specified coordinates.
     * Coordinates outside map boundaries are treated as walls.
     *
     * @param x The x-coordinate of the tile
     * @param y The y-coordinate of the tile
     * @return The tile value or 0 for out-of-bounds coordinates
     */
    public Integer getTile(int x, int y) {
        if (x < 0 || x >= w || y < 0 || y >= h) {
            return 0;
        }
        return tiles.get(x + "," + y);
    }

    /**
     * Checks if the tile at given coordinates is a wall.
     *
     * @param x The x-coordinate of the tile
     * @param y The y-coordinate of the tile
     * @return true if the tile is a wall, false otherwise
     */
    public boolean isWall(int x, int y) {
        Integer v = getTile(x, y);
        return v != null && v == 0;
    }

    /**
     * Determines if a rectangle collides with any wall in the map.
     * Uses a slightly smaller hitbox for more forgiving collision detection.
     *
     * @param r The rectangle to check for collision
     * @return true if the rectangle overlaps any wall, false otherwise
     */
    public boolean collidesWithWall(Rectangle r) {
        int t = GameObj.TILE;

        float margin = 0.1f * t;
        Rectangle hitbox = new Rectangle(
                r.x + margin,
                r.y + margin,
                r.width - 2 * margin,
                r.height - 2 * margin
        );

        int minTX = (int) Math.floor(hitbox.x / t);
        int maxTX = (int) Math.ceil((hitbox.x + hitbox.width) / t);
        int minTY = (int) Math.floor(hitbox.y / t);
        int maxTY = (int) Math.ceil((hitbox.y + hitbox.height) / t);

        if (minTX < 0 || maxTX >= w || minTY < 0 || maxTY >= h) {
            return true;
        }

        for (int x = minTX; x <= Math.min(w - 1, maxTX); x++) {
            for (int y = minTY; y <= Math.min(h - 1, maxTY); y++) {
                if (this.isWall(x, y)) {
                    Rectangle wallRect = new Rectangle(x * t, y * t, t, t);
                    if (hitbox.overlaps(wallRect)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Generates a procedural maze for endless mode using depth-first search and room placement.
     * Map size and object density scale with player level, up to maximum limits.
     *
     * @throws IOException If the generated map file cannot be written
     */
    public void generateMap() throws IOException {
        int lvl = playerStats.getLevel();
        int n = Math.min(20 + lvl, 40);

        if (n % 2 == 0) n++;

        isEndless = true;

        long seed = lvl * 1000L + playerStats.getScore();
        Random r = new Random(seed);

        FileHandle file = Gdx.files.local("maps/endless.properties");
        if (file.exists()) {
            file.delete();
        }

        int[][] grid = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = 0;
            }
        }

        int startX = 1;
        int startY = 1;

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});
        grid[startX][startY] = 7;
        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0];
            int y = current[1];

            List<int[]> neighbours = new ArrayList<>();
            for (int[] dir : dirs) {
                int nx = x + dir[0] * 2;
                int ny = y + dir[1] * 2;

                if (nx > 0 && nx < n - 1 && ny > 0 && ny < n - 1 && grid[nx][ny] == 0) {
                    neighbours.add(new int[]{nx, ny, dir[0], dir[1]});
                }
            }

            if (!neighbours.isEmpty()) {
                int[] next = neighbours.get(r.nextInt(neighbours.size()));
                int nx = next[0];
                int ny = next[1];
                int dx = next[2];
                int dy = next[3];

                grid[x + dx][y + dy] = 7;
                grid[nx][ny] = 7;

                stack.push(new int[]{nx, ny});
            } else {
                stack.pop();
            }
        }

        int roomCount = Math.min(2 + lvl / 5, 5);
        for (int room = 0; room < roomCount; room++) {

            int roomSize = 3 + r.nextInt(3);
            int roomX = 2 + r.nextInt(n - roomSize - 4);
            int roomY = 2 + r.nextInt(n - roomSize - 4);

            for (int i = 0; i < roomSize; i++) {
                for (int j = 0; j < roomSize; j++) {
                    int x = roomX + i;
                    int y = roomY + j;
                    if (x < n && y < n) {
                        grid[x][y] = 7;
                    }
                }
            }

            int entrances = 1 + r.nextInt(2);
            for (int e = 0; e < entrances; e++) {
                int side = r.nextInt(4);
                int connX, connY;

                switch (side) {
                    case 0:
                        connX = roomX + r.nextInt(roomSize);
                        connY = roomY + roomSize;
                        if (connY < n - 1) {
                            grid[connX][connY] = 7;
                            if (connY + 1 < n) grid[connX][connY + 1] = 7;
                        }
                        break;
                    case 1:
                        connX = roomX + r.nextInt(roomSize);
                        connY = roomY - 1;
                        if (connY > 0) {
                            grid[connX][connY] = 7;
                            grid[connX][connY - 1] = 7;
                        }
                        break;
                    case 2:
                        connX = roomX + roomSize;
                        connY = roomY + r.nextInt(roomSize);
                        if (connX < n - 1) {
                            grid[connX][connY] = 7;
                            if (connX + 1 < n) grid[connX + 1][connY] = 7;
                        }
                        break;
                    case 3:
                        connX = roomX - 1;
                        connY = roomY + r.nextInt(roomSize);
                        if (connX > 0) {
                            grid[connX][connY] = 7;
                            grid[connX - 1][connY] = 7;
                        }
                        break;
                }
            }
        }

        List<int[]> walkableCells = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 7) {
                    walkableCells.add(new int[]{i, j});
                }
            }
        }

        int exitX, exitY;
        int attempts = 0;
        do {
            int[] exitCell = walkableCells.get(r.nextInt(walkableCells.size()));
            exitX = exitCell[0];
            exitY = exitCell[1];
            attempts++;

            int distance = Math.abs(exitX - startX) + Math.abs(exitY - startY);
            int minDistance = n / 3;

            if (distance >= minDistance || attempts > 50) {
                break;
            }
        } while (true);

        grid[startX][startY] = 1;
        grid[exitX][exitY] = 2;

        List<int[]> roomCells = new ArrayList<>();
        List<int[]> corridorCells = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 7 && !(i == startX && j == startY) && !(i == exitX && j == exitY)) {
                    int neighborCount = 0;
                    for (int[] dir : dirs) {
                        int nx = i + dir[0];
                        int ny = j + dir[1];
                        if (nx >= 0 && nx < n && ny >= 0 && ny < n && grid[nx][ny] == 7) {
                            neighborCount++;
                        }
                    }

                    if (neighborCount >= 3) {
                        roomCells.add(new int[]{i, j});
                    } else {
                        corridorCells.add(new int[]{i, j});
                    }
                }
            }
        }

        int maxWalkable = walkableCells.size();
        int trapCount = Math.min(3 + (int) (lvl * 0.5), maxWalkable / 10);
        int enemyCount = Math.min(2 + (int) (lvl * 0.7), maxWalkable / 8);
        int powerCount = Math.min(2 + (int) (lvl * 0.4), maxWalkable / 12);

        Collections.shuffle(corridorCells, r);
        int placedTraps = 0;
        for (int i = 0; i < corridorCells.size() && placedTraps < trapCount; i++) {
            int[] cell = corridorCells.get(i);
            if (grid[cell[0]][cell[1]] == 7) {
                grid[cell[0]][cell[1]] = 3;
                placedTraps++;
                corridorCells.remove(i);
                i--;
            }
        }

        int enemiesInRooms = Math.min(enemyCount * 2 / 3, roomCells.size());
        Collections.shuffle(roomCells, r);
        int enemiesPlaced = 0;
        for (int i = 0; i < roomCells.size() && enemiesPlaced < enemiesInRooms; i++) {
            int[] cell = roomCells.get(i);
            if (grid[cell[0]][cell[1]] == 7) {
                grid[cell[0]][cell[1]] = 4;
                enemiesPlaced++;
                roomCells.remove(i);
                i--;
            }
        }

        Collections.shuffle(corridorCells, r);
        for (int i = 0; i < corridorCells.size() && enemiesPlaced < enemyCount; i++) {
            int[] cell = corridorCells.get(i);
            if (grid[cell[0]][cell[1]] == 7) {
                grid[cell[0]][cell[1]] = 4;
                enemiesPlaced++;
                corridorCells.remove(i);
                i--;
            }
        }

        List<int[]> allAvailable = new ArrayList<>();
        allAvailable.addAll(roomCells);
        allAvailable.addAll(corridorCells);

        Collections.shuffle(allAvailable, r);
        if (!allAvailable.isEmpty()) {
            int bestIndex = -1;
            int bestDistance = Integer.MAX_VALUE;

            for (int i = 0; i < Math.min(allAvailable.size(), 20); i++) {
                int[] cell = allAvailable.get(i);
                int distToStart = Math.abs(cell[0] - startX) + Math.abs(cell[1] - startY);
                int distToExit = Math.abs(cell[0] - exitX) + Math.abs(cell[1] - exitY);
                int diff = Math.abs(distToStart - distToExit);

                if (diff < bestDistance) {
                    bestDistance = diff;
                    bestIndex = i;
                }
            }

            if (bestIndex != -1) {
                int[] cell = allAvailable.get(bestIndex);
                grid[cell[0]][cell[1]] = 5;
                allAvailable.remove(bestIndex);
            }
        }

        int powerupsInRooms = Math.min(powerCount * 3 / 4, roomCells.size());
        Collections.shuffle(roomCells, r);
        int placedPowers = 0;
        for (int i = 0; i < roomCells.size() && placedPowers < powerupsInRooms; i++) {
            int[] cell = roomCells.get(i);
            if (grid[cell[0]][cell[1]] == 7) {
                grid[cell[0]][cell[1]] = 6;
                placedPowers++;
                roomCells.remove(i);
                i--;
            }
        }

        Collections.shuffle(corridorCells, r);
        for (int i = 0; i < corridorCells.size() && placedPowers < powerCount; i++) {
            int[] cell = corridorCells.get(i);
            if (grid[cell[0]][cell[1]] == 7) {
                grid[cell[0]][cell[1]] = 6;
                placedPowers++;
                corridorCells.remove(i);
                i--;
            }
        }

        StringBuilder sb = new StringBuilder();
        BufferedWriter bw = new BufferedWriter(file.writer(false));

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int val = grid[i][j];
                if (val == 7) {
                    continue;
                }
                sb.append(i).append(",").append(j).append("=").append(val).append("\n");
            }
        }

        bw.write(sb.toString());
        bw.close();

        load("maps/endless.properties");
    }

    /**
     * Gets the map width in tiles.
     *
     * @return The width of the map
     */
    public int getW() {
        return w;
    }

    /**
     * Gets the map height in tiles.
     *
     * @return The height of the map
     */
    public int getH() {
        return h;
    }

    /**
     * Checks if the current map is in endless mode.
     *
     * @return true if the map is procedurally generated for endless mode
     */
    public boolean isEndless() {
        return isEndless;
    }

    /**
     * Gets the entry point x-coordinate.
     *
     * @return The x-coordinate of the map entry
     */
    public int getEx() {
        return ex;
    }

    /**
     * Gets the entry point y-coordinate.
     *
     * @return The y-coordinate of the map entry
     */
    public int getEy() {
        return ey;
    }

    /**
     * Gets the exit object for the map.
     *
     * @return The Exit object
     */
    public Exit getExit() {
        return exit;
    }

    /**
     * Gets all traps in the map.
     *
     * @return List of Trap objects
     */
    public List<Trap> getTraps() {
        return traps;
    }

    /**
     * Gets all keys in the map.
     *
     * @return List of Key objects
     */
    public List<Key> getKeys() {
        return keys;
    }

    /**
     * Gets all enemies in the map.
     *
     * @return List of Enemy objects
     */
    public List<Enemy> getEnemies() {
        return enemies;
    }

    /**
     * Gets all powerups in the map.
     *
     * @return List of Powerup objects
     */
    public List<Powerup> getPowerups() {
        return powerups;
    }

    /**
     * Gets the map width in tiles.
     *
     * @return The width of the map
     */
    public int getWidth() {
        return w;
    }

    /**
     * Gets the map height in tiles.
     *
     * @return The height of the map
     */
    public int getHeight() {
        return h;
    }
}