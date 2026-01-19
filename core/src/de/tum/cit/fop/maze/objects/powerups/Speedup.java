package de.tum.cit.fop.maze.objects.powerups;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The Speedup class represents a powerup that temporarily increases the player's movement speed.
 */
public class Speedup extends Powerup {

    /**
     * Constructor for Speedup.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     */
    public Speedup(float x, float y) {
        super(x, y, 9, 6);
    }

    /**
     * Applies the temporary speed effect on the player if he collects it.
     *
     * @param player The player object to apply the effect to.
     * @param delta  The time in seconds since the last update.
     */
    @Override
    public void update(Player player, float delta) {
        if (player.getBounds().overlaps(getBounds())) {
            player.speedEffect(1.25f, 2f, Color.GREEN);
        }
    }

}
