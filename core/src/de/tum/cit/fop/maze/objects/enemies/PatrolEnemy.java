package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The PatrolEnemy class represents an enemy that patrols along a fixed axis (either horizontal or vertical).
 */
public class PatrolEnemy extends Enemy {
    private final boolean axis;
    private float dir = 1;

    /**
     * Creates a new PatrolEnemy at the specified position with the given patrol axis.
     *
     * @param x    The x-coordinate of the enemy's starting position
     * @param y    The y-coordinate of the enemy's starting position
     * @param axis The axis along which the enemy patrols (true for x-axis/horizontal, false for y-axis/vertical)
     */
    public PatrolEnemy(int x, int y, boolean axis) {
        super(x, y, 10, 2);
        this.axis = axis;
        this.speed = 150;
    }

    /**
     * Updates the enemy's movement based on its patrol pattern.
     * The enemy moves along its designated axis at a constant speed and
     * reverses direction when it encounters walls or collides with the player.
     * If the player enters attack range, the enemy stops moving to attack.
     *
     * @param delta  The time elapsed since the last frame update
     * @param player The player character
     * @param map    The game map
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
