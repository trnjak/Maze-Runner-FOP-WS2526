package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.GameObj;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The Enemy abstract class serves as the base class for all enemy types in the game.
 * It provides common functionality for movement, attack patterns, health management, and rendering.
 */
public abstract class Enemy extends GameObj {
    protected float speed = 75, timer = 0, range = TILE * 1.1f;
    protected final float interval = 1;
    protected int hp = 3;
    protected boolean alive = true;
    protected float lookX, lookY;

    protected final TextureRegion right, left;
    protected TextureRegion current;

    /**
     * Constructor for Enemy.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param tileY The row index in the texture tile sheet.
     * @param tileX The column index in the texture tile sheet.
     */
    public Enemy(int x, int y, int tileY, int tileX) {
        super(x * TILE, y * TILE, tileY, tileX);
        this.w = TILE;
        this.h = TILE;

        right = new TextureRegion(texture);
        left = new TextureRegion(texture);
        left.flip(true, false);

        current = right;
    }

    /**
     * Abstract method that defines the enemy's movement behavior.
     * @param delta The time in seconds since the last update.
     * @param player The player object used for tracking or avoidance.
     * @param map The game map used for collision detection and navigation.
     */
    protected abstract void move(float delta, Player player, GameMap map);

    /**
     * Updates the enemy's state, including movement, attack timing, and orientation.
     * @param delta The time in seconds since the last update.
     * @param player The player object for range checking and attacks.
     * @param map The game map for collision and movement calculations.
     */
    public void update(float delta, Player player, GameMap map) {
        if(!alive) {
            return;
        }

        current = (lookX > 0) ? right : left;

        move(delta, player, map);

        if(inRange(player)) {
            if(timer <= 0) {
                player.loseLife();
                player.setTint(Color.RED);
                timer = interval;
            } else {
                timer -= delta;
            }
        } else {
            timer = 0;
        }
    }

    /**
     * Checks if the player is within the enemy's attack range.
     * @param player The player object to check distance against.
     */
    public boolean inRange(Player player) {
        return distance(player) <= range;
    }

    /**
     * Calculates the distance between the enemy and the player in pixels.
     * @param player The player object to calculate distance to.
     */
    protected float distance(Player player) {
        return (float) Math.sqrt(Math.pow((player.getX() - x), 2) + Math.pow((player.getY() - y), 2));
    }

    /**
     * Applies damage to the enemy.
     * @param dmg The amount of damage to apply.
     */
    public void takeDamage(int dmg) {
        if(!alive) {
            return;
        }
        hp -= dmg;
        if(hp <= 0) {
            alive = false;
        }
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public boolean isAlive() {
        return alive;
    }

    /**
     * Renders the enemy sprite with the current orientation.
     * @param batch The SpriteBatch used for rendering.
     */
    @Override
    public void render(SpriteBatch batch) {
        renderMovingObj(batch, current);
    }

    @Override
    public Rectangle getBounds() {
        return getBoundsMovingObj();
    }
}
