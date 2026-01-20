package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The PatrolEnemy class represents an enemy that patrols along a fixed axis (either horizontal or vertical).
 * Moves continuously along its designated axis, reversing direction upon collision with walls or the player.
 */
public class PatrolEnemy extends Enemy {
    private final boolean axis;
    private float dir = 1;

    /**
     * Constructs a new PatrolEnemy at the specified coordinates with a designated patrol axis.
     *
     * @param x    The x-coordinate of the enemy's starting position
     * @param y    The y-coordinate of the enemy's starting position
     * @param axis The axis along which the enemy patrols (true for horizontal/x-axis, false for vertical/y-axis)
     */
    public PatrolEnemy(int x, int y, boolean axis) {
        super(x, y, 10, 2);
        this.axis = axis;
        this.speed = 150;
    }

    /**
     * Updates the enemy's movement based on its patrol pattern.
     * The enemy moves along its designated axis at constant speed, reversing direction when encountering obstacles.
     * Stops moving when the player enters attack range to initiate attacks.
     *
     * @param delta  The time elapsed since the last frame update in seconds
     * @param player The player character for range detection and collision checking
     * @param map    The game map for wall collision detection
     */
    @Override
    protected void move(float delta, Player player, GameMap map) {
        float dx = 0, dy = 0;
        if (inRange(player)) {
            return;
        }
        if (axis) {
            dx = dir * speed * delta;
        } else {
            dy = dir * speed * delta;
        }

        Rectangle pb = player.getBounds();
        Rectangle next = new Rectangle(x + dx, y + dy, w, h);

        if (!map.collidesWithWall(next) && !next.overlaps(pb)) {
            x += dx;
            y += dy;
        } else {
            dir *= -1f;
        }

        if (dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }
    }
}
