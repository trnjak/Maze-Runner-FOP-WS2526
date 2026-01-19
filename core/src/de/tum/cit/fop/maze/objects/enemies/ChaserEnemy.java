package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The ChaserEnemy class represents an enemy that chases the player when within line of sight and range.
 * If not within said line of sight and range, the enemy wanders randomly.
 */
public class ChaserEnemy extends Enemy {
    private float dx = 0, dy = 0, cd = 0;

    /**
     * Creates a new ChaserEnemy at the specified position.
     *
     * @param x The x-coordinate of the enemy's starting position
     * @param y The y-coordinate of the enemy's starting position
     */
    public ChaserEnemy(int x, int y) {
        super(x, y, 10, 1);
    }

    /**
     * Updates the enemy's movement based on the player's position and game state.
     * The enemy will chase the player when within line of sight and range,
     * using obstacle avoidance to navigate around walls. When not chasing,
     * the enemy moves randomly. If the enemy reaches the player, it pushes
     * the player away and stops moving.
     * @param delta The time elapsed since the last frame update
     * @param player The player character being chased
     * @param map The game map containing wall collision data
     */
    @Override
    protected void move(float delta, Player player, GameMap map) {
        if (inRange(player)) {
            dx = 0;
            dy = 0;
            return;
        }
        float ogX = x;
        float ogY = y;
        if (inChaseRange(player) && hasLineOfSight(player, map)) {
            Vector2 dir = new Vector2(player.getX() - x, player.getY() - y);
            if (dir.len2() > 0) {
                dir.nor();
            }
            float lookDistance = speed * delta;
            float nextX = x + dir.x * lookDistance;
            float nextY = y + dir.y * lookDistance;

            Rectangle nextRec = new Rectangle(nextX, nextY, w, h);
            if (map.collidesWithWall(nextRec)) {
                Vector2 bestDir = new Vector2(dir);
                boolean found = false;

                for (int angle = 15; angle <= 90; angle += 15) {
                    Vector2 right = new Vector2(dir).rotateDeg(angle);
                    nextX = x + right.x * lookDistance;
                    nextY = y + right.y * lookDistance;
                    nextRec.set(nextX, nextY, w, h);
                    if (!map.collidesWithWall(nextRec)) {
                        bestDir.set(right);
                        found = true;
                        break;
                    }

                    Vector2 left = new Vector2(dir).rotateDeg(-angle);
                    nextX = x + left.x * lookDistance;
                    nextY = y + left.y * lookDistance;
                    nextRec.set(nextX, nextY, w, h);
                    if (!map.collidesWithWall(nextRec)) {
                        bestDir.set(left);
                        found = true;
                        break;
                    }
                }

                if (found) {
                    dir.set(bestDir);
                } else {
                    dir.setZero();
                }
            }
            dx = dir.x;
            dy = dir.y;

            if (dir.len2() > 0) {
                lookX = dir.x;
                lookY = dir.y;
            }

        } else {
            cd += delta;
            if (cd > 2) {
                dx = (int) (Math.random() * 3) - 1;
                dy = (int) (Math.random() * 3) - 1;
                Vector2 dir = new Vector2(dx, dy);
                if (dir.len2() > 0) {
                    dir.nor();
                }
                dx = dir.x;
                dy = dir.y;
                cd = 0;
            }
        }

        float sx = dx * speed * delta;
        float sy = dy * speed * delta;

        Rectangle next = new Rectangle(x + sx, y + sy, w, h);

        if (!map.collidesWithWall(next)) {
            x += sx;
            y += sy;
        } else {
            Rectangle tryX = new Rectangle(x + sx, y, w, h);
            if (!map.collidesWithWall(tryX)) {
                x += sx;
            }
            Rectangle tryY = new Rectangle(x, y + sy, w, h);
            if (!map.collidesWithWall(tryY)) {
                y += sy;
            }
            Rectangle stuck = new Rectangle(x, y, w, h);
            if (map.collidesWithWall(stuck)) {
                x = ogX;
                y = ogY;
            }
        }

        if (dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }

        Rectangle enemyBounds = new Rectangle(x, y, w, h);
        Rectangle playerBounds = player.getBounds();

        if (enemyBounds.overlaps(playerBounds)) {
            pushPlayer(player);
        }
    }

    private boolean inChaseRange(Player player) {
        return distance(player) <= 192;
    }

    /**
     * Checks whether the enemy has a line of sight to the player or not,
     * used to ensure that the enemy won't try to chase a player from behind a wall.
     * @param player The player chased by the enemy.
     * @param map The game map.
     * @return True if the enemy can see the player, false otherwise.
     */
    private boolean hasLineOfSight(Player player, GameMap map) {
        Vector2 enemyPos = new Vector2(x + w / 2, y + h / 2);
        Vector2 playerPos = new Vector2(player.getX() + (float) TILE / 2, player.getY() + (float) TILE / 2);

        Vector2 dir = new Vector2(playerPos).sub(enemyPos);
        float len = dir.len();
        dir.nor();
        float stepSize = 4;
        int steps = (int) (len / stepSize);

        Vector2 cur = new Vector2(enemyPos);

        for(int i = 0; i < steps; i++) {
            cur.add(dir.x * stepSize, dir.y * stepSize);
            Rectangle checkRect = new Rectangle(cur.x - 2, cur.y - 2, 4, 4);
            if(map.collidesWithWall(checkRect)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Pushes the player away from the enemy when they collide.
     * The push direction is calculated as the vector from the enemy to the player,
     * normalised and scaled by a fixed strength.
     * @param player The player to push.
     */
    protected void pushPlayer(Player player) {
        Vector2 push = new Vector2(
                player.getX() - x,
                player.getY() - y
        );

        if (push.len2() > 0) {
            push.nor().scl(10f);
            player.setPosition(
                    player.getX() + push.x,
                    player.getY() + push.y
            );
        }
    }
}
