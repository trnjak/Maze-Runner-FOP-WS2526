package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.PlayerStats;
import de.tum.cit.fop.maze.objects.enemies.Enemy;
import de.tum.cit.fop.maze.screens.BeginScreen;

import java.util.List;
import java.util.Random;

/**
 * The Player class represents the main character in the game.
 * It handles player movement, combat, health management, and visual effects.
 * <p>Sounds from: <a href="https://opengameart.org/content/female-rpg-voice-starter-pack">Female RPG Voice Starter Pack</a>
 *
 * @see GameObj
 * @see PlayerStats
 * @see Enemy
 */
public class Player extends GameObj {
    private final PlayerStats playerStats;
    private final TextureRegion right, left;
    private final Sound[]
            atkHit = {
            Gdx.audio.newSound(Gdx.files.internal("sounds/attack1.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/attack2.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/attack3.ogg"))
    },
            takeDmg = {
                    Gdx.audio.newSound(Gdx.files.internal("sounds/damaged1.ogg")),
                    Gdx.audio.newSound(Gdx.files.internal("sounds/damaged2.ogg")),
                    Gdx.audio.newSound(Gdx.files.internal("sounds/damaged3.ogg"))
            };
    private int hp, keys = 0;
    private float tintTimer = 0, attackTimer = 0, effectTimer = 0;
    private float lookX, lookY;
    private float speedMult = 1.0f;
    private Color tint = Color.WHITE;
    private TextureRegion current;

    /**
     * Constructs a new Player at the specified position.
     *
     * @param x The initial x-coordinate of the player
     * @param y The initial y-coordinate of the player
     */
    public Player(float x, float y) {
        super(x, y, 8, 3);
        this.w = TILE;
        this.h = TILE;

        playerStats = BeginScreen.STATS;
        hp = playerStats.getMaxHp();

        right = new TextureRegion(texture);
        left = new TextureRegion(texture);
        left.flip(true, false);

        current = right;
    }

    /**
     * Moves the player in the specified direction while checking for wall collisions.
     *
     * @param dx  The horizontal movement direction (-1 for left, 1 for right, 0 for no movement)
     * @param dy  The vertical movement direction (-1 for down, 1 for up, 0 for no movement)
     * @param map The game map used for collision detection
     */
    public void move(float dx, float dy, GameMap map) {
        float currentSpeed = playerStats.getSpeed() * speedMult;
        if (dx != 0) {
            float newX = x + dx * currentSpeed;
            Rectangle tempBoundsX = new Rectangle(newX, y, w, h);
            if (!map.collidesWithWall(tempBoundsX)) {
                x = newX;
            }
        }
        if (dy != 0) {
            float newY = y + dy * currentSpeed;
            Rectangle tempBoundsY = new Rectangle(x, newY, w, h);
            if (!map.collidesWithWall(tempBoundsY)) {
                y = newY;
            }
        }
        if (dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }
    }

    /**
     * Updates the player's state each frame.
     *
     * @param delta The time elapsed since the last frame in seconds
     */
    public void update(float delta) {
        current = (lookX > 0) ? right : left;

        if (attackTimer > 0f) {
            attackTimer -= delta;
        }

        if (effectTimer > 0) {
            effectTimer -= delta;
            if (effectTimer <= 0) {
                speedMult = 1.0f;
                setTint(tint);
            }
        }

        if (!tint.equals(Color.WHITE)) {
            tintTimer += delta;
            if (tintTimer >= 0.25f) {
                tint = Color.WHITE;
                tintTimer = 0f;
            }
        }
    }

    /**
     * Reduces the player's health by the specified amount and plays damage sound.
     *
     * @param n The amount of health to lose
     */
    public void loseLife(int n) {
        hp -= n;
        Random r = new Random();
        takeDmg[r.nextInt(3)].play(0.8f);
        setTint(Color.PINK);
    }

    /**
     * Reduces the player's health by 1 and plays damage sound.
     */
    public void loseLife() {
        loseLife(1);
    }

    /**
     * Performs an attack on enemies within range and facing direction.
     * Plays attack sounds and applies visual feedback based on hit success.
     *
     * @param e The list of enemies to check for attack hits
     */
    public void attack(List<Enemy> e) {
        float attackCooldown = playerStats.getAttackCooldown();
        if (attackTimer > 0f) {
            return;
        }

        float range = Math.max(w, h) * 1.5f;
        float cx = x + w / 2f;
        float cy = y + h / 2f;

        boolean hit = false;
        float cosHalfAngle = (float) Math.cos(Math.toRadians(90));

        for (Enemy enemy : e) {
            if (!enemy.isAlive()) {
                continue;
            }

            Rectangle eb = enemy.getBounds();
            float ex = eb.x + eb.width / 2;
            float ey = eb.y + eb.height / 2;

            float vx = ex - cx;
            float vy = ey - cy;
            float d = (float) Math.sqrt(vx * vx + vy * vy);
            if (d > range) {
                continue;
            }

            float nx = d == 0 ? 0 : vx / d;
            float ny = d == 0 ? 0 : vy / d;
            float dot = nx * lookX + ny * lookY;

            if (d == 0f || dot >= cosHalfAngle) {
                enemy.takeDamage(1);
                hit = true;
            }
        }

        Random r = new Random();
        if (hit) {
            atkHit[r.nextInt(3)].play(0.8f);
            setTint(Color.YELLOW);
        } else {
            atkHit[r.nextInt(3)].play(0.2f);
            setTint(Color.ORANGE);
        }

        attackTimer = attackCooldown;
    }

    /**
     * Applies a temporary speed effect to the player.
     *
     * @param mult The speed multiplier to apply
     * @param t    The duration of the effect in seconds
     * @param tint The colour tint to apply during the effect
     */
    public void speedEffect(float mult, float t, Color tint) {
        speedMult = mult;
        effectTimer = t;
        setTint(tint);
    }

    /**
     * Sets the visual tint color of the player.
     *
     * @param c The color to tint the player with
     */
    public void setTint(Color c) {
        this.tint = c;
        this.tintTimer = 0f;
    }

    /**
     * Gets the player's current health points.
     *
     * @return The current health points
     */
    public int getHp() {
        return hp;
    }

    /**
     * Sets the player's health points.
     *
     * @param hp The new health points value
     */
    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * Gets the player's maximum health from player statistics.
     *
     * @return The maximum health points
     */
    public int getMaxHealth() {
        return playerStats.getMaxHp();
    }

    /**
     * Checks if the player is alive.
     *
     * @return true if the player's health is greater than 0, false otherwise
     */
    public boolean isAlive() {
        return hp > 0;
    }

    /**
     * Increments the player's key count by 1.
     */
    public void collectKey() {
        keys++;
    }

    /**
     * Gets the number of keys the player has collected.
     *
     * @return The number of keys collected
     */
    public int getKeys() {
        return keys;
    }

    /**
     * Sets the number of keys the player has.
     *
     * @param keys The new key count
     */
    public void setKeys(int keys) {
        this.keys = keys;
    }

    /**
     * Gets the player's current movement speed including active effects.
     *
     * @return The current movement speed
     */
    public float getCurrentSpeed() {
        return playerStats.getSpeed() * speedMult;
    }

    /**
     * Renders the player on the screen with current tint and texture.
     *
     * @param batch The SpriteBatch used for rendering
     */
    @Override
    public void render(SpriteBatch batch) {
        batch.setColor(tint);
        renderMovingObj(batch, current);
        batch.setColor(Color.WHITE);
    }

    /**
     * Gets the player's collision bounds.
     *
     * @return The rectangle representing the player's bounds
     */
    @Override
    public Rectangle getBounds() {
        return getBoundsMovingObj();
    }

    /**
     * Sets the player's position.
     *
     * @param x The new x-coordinate
     * @param y The new y-coordinate
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
