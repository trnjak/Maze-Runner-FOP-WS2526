package de.tum.cit.fop.maze.objects.powerups;

import de.tum.cit.fop.maze.objects.Player;

/**
 * The Hpup class represents a health-restoring powerup that heals the player by one health point.
 * Heals only if the player is below maximum health capacity.
 */
public class Hpup extends Powerup {
    /**
     * Constructs a new Hpup powerup at the specified coordinates.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     */
    public Hpup(float x, float y) {
        super(x, y, 9, 7);
    }

    /**
     * Updates the powerup's effect on the player when collected.
     * Restores one health point if the player is below maximum health capacity.
     *
     * @param player The player object to apply the healing effect to
     * @param delta  The time in seconds since the last update
     */
    @Override
    public void update(Player player, float delta) {
        if (player.getBounds().overlaps(getBounds()) && player.getHp() + 1 <= player.getMaxHealth()) {
            player.setHp(player.getHp() + 1);
        }
    }
}
