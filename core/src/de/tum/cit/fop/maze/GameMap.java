package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.objects.*;
import de.tum.cit.fop.maze.objects.enemies.*;
import de.tum.cit.fop.maze.objects.powerups.*;
import de.tum.cit.fop.maze.objects.traps.*;
import de.tum.cit.fop.maze.screens.BeginScreen;

import java.io.*;
import java.util.*;

/**
 * The GameMap class represents the game world containing tiles, traps, enemies, powerups, keys, and the exit.
 * It handles map loading, rendering, collision detection, and procedural generation for endless mode.
 *
 * Map textures from: <a href="https://kenney.nl/assets/tiny-dungeon">LINK</a>
 */
public class GameMap {
    private static final Texture TILE_SHEET = new Texture("main_tilemap.png");
    public static final TextureRegion[][] TEXTURE_REGION = TextureRegion.split(TILE_SHEET, TILE_SHEET.getWidth()/12, TILE_SHEET.getHeight()/11);

    private int w, h;
    private final Map<String, Integer> tiles = new HashMap<>();
    private int ex, ey;
    private final TextureRegion wall = TEXTURE_REGION[3][4],
            floor[] = {TEXTURE_REGION[4][0], TEXTURE_REGION[4][1]},
            entry = TEXTURE_REGION[4][0];
    private final List<Trap> traps = new ArrayList<>();
    private final List<Key> keys = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Powerup> powerups = new ArrayList<>();
    private Exit exit;

    private boolean isEndless = false;

    private final PlayerStats playerStats = BeginScreen.STATS;

    /**
     * Loads a map from a properties file at the specified path.
     * Clears existing map data and parses tile values to populate the map with walls, entry point, exit,
     * traps, enemies, keys, and powerups.
     * @param path The path to the map properties file.
     */
    public void load(String path) {
        tiles.clear();
        traps.clear();
        keys.clear();
        enemies.clear();
        powerups.clear();
        exit = null;

        Properties p = new Properties();
        try(BufferedReader r = new BufferedReader(Gdx.files.internal(path).reader())) {
            p.load(r);
        } catch(IOException e) {
            throw new RuntimeException("Failed to load map.", e);
        }

        int maxX = 0, maxY = 0;
        for(String key : p.stringPropertyNames()) {
            int val = Integer.parseInt(p.getProperty(key).trim());
            tiles.put(key, val);

            String[] split = key.split(",");
            int x = Integer.parseInt(split[0]);
            int y = Integer.parseInt(split[1]);

            if(x > maxX) {
                maxX = x;
            }
            if(y > maxY) {
                maxY = y;
            }

            switch(val) {
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
     * Renders the entire map including tiles, traps, keys, enemies, powerups, and the exit.
     * @param batch The SpriteBatch used for drawing.
     */
    public void render(SpriteBatch batch) {
        for(int y = 0; y < h; y++) {
            for(int x = 0; x < w; x++) {
                Integer v = tiles.get(x + "," + y);
                TextureRegion t;
                if(v == null) {
                    t = floor[1];
                } else {
                    t = switch(v) {
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
     * Retrieves the tile value at the specified coordinates.
     * Coordinates outside the map boundaries are treated as walls.
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     */
    public Integer getTile(int x, int y) {
        if(x < 0 || x >= w || y < 0 || y >= h) {
            return 0;
        }
        return tiles.get(x + "," + y);
    }

    /**
     * Checks if the tile at the given coordinates is a wall.
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     */
    public boolean isWall(int x, int y) {
        Integer v = getTile(x, y);
        return v != null && v == 0;
    }

    /**
     * Determines if a given rectangle collides with any wall in the map.
     * @param r The rectangle to check for collision.
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

        if(minTX < 0 || maxTX >= w || minTY < 0 || maxTY >= h) {
            return true;
        }

        for(int x = minTX; x <= Math.min(w - 1, maxTX); x++) {
            for(int y = minTY; y <= Math.min(h - 1, maxTY); y++) {
                if(this.isWall(x, y)) {
                    Rectangle wallRect = new Rectangle(x * t, y * t, t, t);
                    if(hitbox.overlaps(wallRect)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Generates a procedural map for endless mode based on the player's current level.
     * The map size and number of objects increase with level, up to a maximum.
     * Saves the generated map to a local file and loads it.
     */
    public void generateMap() throws IOException {
        int level = playerStats.getLevel();
        int n = Math.min((25 * level / 2), 20), key = 0, trap = 0, enemy = 0, power = 0;
        isEndless = true;

        FileHandle file = Gdx.files.local("maps/endless.properties");
        if(file.exists()) {
            file.delete();
        }

        StringBuilder sb = new StringBuilder(10000);
        BufferedWriter bw = new BufferedWriter(file.writer(false));

        for(int i = 0; i < n; i++) {
            sb.append(i).append(",0=0\n");
            sb.append("0,").append(i).append("=0\n");
            sb.append((n - 1)).append(",").append(i).append("=0\n");
            sb.append(i).append(",").append(n - 1).append("=0\n");
        }

        sb.append("1,1=1\n");
        sb.append((n - 2)).append(",").append(n - 2).append("=2\n");

        for(int i = 2; i < n - 2; i++) {
            for(int j = 2; j < n - 2; j++) {
                int what = (int) (Math.random() * 7);
                switch(what) {
                    case 3:
                        if(trap < 5) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                            trap++;
                        }
                        break;
                    case 4:
                        if(enemy < 5 && (i > 3 && j > 3)) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                            enemy++;
                        }
                        break;
                    case 5:
                        if(key < 3) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                            key++;
                        }
                        break;
                    case 6:
                        if(power < 5) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                            power++;
                        }
                        break;
                    default:
                        if(Math.round(Math.random()) > 0 && !(what == 1 || what == 2)) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                        }
                        break;
                }
            }
        }
        bw.write(sb.toString());
        bw.close();

        load("maps/endless.properties");
    }

    public boolean isEndless() {
        return isEndless;
    }

    public int getEx() {
        return ex;
    }

    public int getEy() {
        return ey;
    }

    public Exit getExit() {
        return exit;
    }

    public List<Trap> getTraps() {
        return traps;
    }

    public List<Key> getKeys() {
        return keys;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Powerup> getPowerups() {
        return powerups;
    }
}