package de.tum.cit.fop.maze.objects.powerups;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The Speedup class represents a powerup that temporarily increases the player's movement speed.
 * Applies a speed boost and visual tint when collected by the player.
 */
public class Speedup extends Powerup {

    /**
     * Constructs a new Speedup powerup at the specified coordinates.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     */
    public Speedup(float x, float y) {
        super(x, y, 9, 6);
    }

    /**
     * Applies a temporary speed boost effect to the player upon collection.
     * Increases movement speed by 25% for 2 seconds with a green visual tint.
     *
     * @param player The player object to apply the effect to
     * @param delta  The time in seconds since the last update
     */
    @Override
    public void update(Player player, float delta) {
        if (player.getBounds().overlaps(getBounds())) {
            player.speedEffect(1.25f, 2f, Color.GREEN);
        }
    }

}
