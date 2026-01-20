package de.tum.cit.fop.maze.objects.traps;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The SludgeTrap class represents a trap that temporarily reduces the player's movement speed when stepped on.
 * Applies a speed multiplier effect and visual tint to the player upon collision.
 */
public class SludgeTrap extends Trap {
    private final float mult;

    /**
     * Constructs a new SludgeTrap at the specified coordinates with a speed reduction multiplier.
     *
     * @param x    The X coordinate
     * @param y    The Y coordinate
     * @param mult The speed multiplier to apply (e.g., 0.5 for 50% speed reduction)
     */
    public SludgeTrap(int x, int y, float mult) {
        super(x, y, 1, 0);
        this.mult = mult;
    }

    /**
     * Applies the speed reduction effect to the player when overlapping with the trap.
     * Activates a temporary speed multiplier and visual tint on the player.
     *
     * @param player The player object to apply the effect to
     * @param delta  The time in seconds since the last update
     */
    @Override
    public void update(Player player, float delta) {
        if (player.getBounds().overlaps(getBounds())) {
            player.speedEffect(mult, 0.1f, Color.SKY);
        }
    }
}
