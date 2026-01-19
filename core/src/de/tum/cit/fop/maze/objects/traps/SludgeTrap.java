package de.tum.cit.fop.maze.objects.traps;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The SludgeTrap class represents a trap that temporarily reduces the player's movement speed when stepped on.
 */
public class SludgeTrap extends Trap {
    private final float mult;

    /**
     * Constructor for SludgeTrap.
     *
     * @param x    The X coordinate.
     * @param y    The Y coordinate.
     * @param mult The speed multiplier to apply.
     */
    public SludgeTrap(int x, int y, float mult) {
        super(x, y, 3, 6);
        this.mult = mult;
    }

    /**
     * Applies the speed reduction while in range.
     *
     * @param player The player object to apply the effect to.
     * @param delta  The time in seconds since the last update.
     */
    @Override
    public void update(Player player, float delta) {
        if (player.getBounds().overlaps(getBounds())) {
            player.speedEffect(mult, 0.1f, Color.SKY);
        }
    }
}
