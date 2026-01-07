package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The PatrolEnemy class represents an enemy that patrols along a fixed axis (horizontal or vertical).
 */
public class PatrolEnemy extends Enemy {
    private final boolean axis;
    private float dir = 1;

    /**
     * Constructor for PatrolEnemy.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param axis True for horizontal (x-axis) patrol, false for vertical (y-axis) patrol.
     */
    public PatrolEnemy(int x, int y, boolean axis) {
        super(x, y, 10, 2);
        this.axis = axis;
        this.speed = 150;
    }

    /**
     * Moves the enemy along its patrol axis, stopping to attack the player when in range
     * and reversing direction upon collision with walls or the player.
     * @param delta The time in seconds since the last update.
     * @param player The player object for range checking and collision detection.
     * @param map The game map for wall collision detection.
     */
    @Override
    protected void move(float delta, Player player, GameMap map) {
        float dx = 0, dy = 0;
        if(inRange(player)) {
            return;
        }
        if(axis) {
            dx = dir * speed * delta;
        } else {
            dy = dir * speed * delta;
        }

        Rectangle pb = player.getBounds();
        Rectangle next = new Rectangle(x + dx, y + dy, w, h);

        if(!map.collidesWithWall(next) && !next.overlaps(pb)) {
            x += dx;
            y += dy;
        } else {
            dir *= -1f;
        }

        if(dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }
    }
}
