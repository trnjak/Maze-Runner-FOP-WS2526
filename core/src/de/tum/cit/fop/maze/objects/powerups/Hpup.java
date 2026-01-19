package de.tum.cit.fop.maze.objects.powerups;

import de.tum.cit.fop.maze.objects.Player;

/**
 * The Hpup class represents a powerup that restores one health point to the player.
 */
public class Hpup extends Powerup {
    /**
     * Constructor for Hpup.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     */
    public Hpup(float x, float y) {
        super(x, y, 9, 7);
    }

    /**
     * Updates the powerup's effect on the player when collected, restores one health point if below maximum health.
     *
     * @param player The player object to apply the effect to.
     * @param delta  The time in seconds since the last update.
     */
    @Override
    public void update(Player player, float delta) {
        if (player.getBounds().overlaps(getBounds()) && player.getHp() + 1 <= player.getMaxHealth()) {
            player.setHp(player.getHp() + 1);
        }
    }
}
