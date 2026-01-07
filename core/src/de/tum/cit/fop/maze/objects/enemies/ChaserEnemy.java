package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The ChaserEnemy class represents an enemy that chases the player when within range
 * and wanders randomly when the player is out of range.
 */
public class ChaserEnemy extends Enemy {
    private float dx = 0, dy = 0, cd = 0;

    /**
     * Constructor for ChaserEnemy.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     */
    public ChaserEnemy(int x, int y) {
        super(x, y, 10, 1);
    }

    /**
     * Moves the enemy, chasing the player when within chase range or wandering randomly otherwise.
     * TODO: IMPLEMENT SMARTER MOVEMENT
     * @param delta The time in seconds since the last update.
     * @param player The player object for chasing and collision detection.
     * @param map The game map for wall collision detection.
     */
    @Override
    protected void move(float delta, Player player, GameMap map) {
        if(inRange(player)) {
            dx = 0;
            dy = 0;
            return;
        }

        if(inChaseRange(player)) {
            Vector2 dir = new Vector2(player.getX() - x, player.getY() - y);
            if(dir.len2() > 0) {
                dir.nor();
                dx = dir.x;
                dy = dir.y;
            }
        } else {
            cd += delta;
            if(cd > 2) {
                dx = (int) (Math.random() * 3) - 1;
                dy = (int) (Math.random() * 3) - 1;
                Vector2 dir = new Vector2(dx, dy);
                if(dir.len2() > 0) {
                    dir.nor();
                }
                dx = dir.x;
                dy = dir.y;
                cd = 0;
            }
        }

        float sx = dx * speed * delta;
        float sy = dy * speed * delta;
        Rectangle pb = player.getBounds();
        Rectangle next = new Rectangle(x + sx, y + sy, w, h);

        if(!map.collidesWithWall(next) && !next.overlaps(pb)) {
            x += sx;
            y += sy;
        } else {
            Rectangle tryX = new Rectangle(x + sx, y, w, h);
            Rectangle tryY = new Rectangle(x, y + sy, w, h);

            if(!map.collidesWithWall(tryX) && !tryX.overlaps(pb)) {
                x += sx;
            }
            if(!map.collidesWithWall(tryY) && !tryY.overlaps(pb)) {
                y += sy;
            }
        }

        if(dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }
    }

    /**
     * Checks if the player is within the enemy's chase range.
     * @param player The player object to check distance against.
     */
    private boolean inChaseRange(Player player) {
        return distance(player) <= 128;
    }
}