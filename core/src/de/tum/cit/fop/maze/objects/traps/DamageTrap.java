package de.tum.cit.fop.maze.objects.traps;

import de.tum.cit.fop.maze.objects.Player;

/**
 * The DamageTrap class represents a trap that damages the player.
 */
public class DamageTrap extends Trap {
    private final float interval;
    private float timer = 0;
    private boolean active = false;

    /**
     * Constructor for DamageTrap.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param interval The time interval in seconds between damage ticks while the player is on the trap.
     */
    public DamageTrap(int x, int y, float interval) {
        super(x, y, 3, 5);
        this.interval = interval;
    }

    /**
     * Damages player when in range, first damage is immediate and if player doesn't
     * step off the trap, subsequent damage happens every second.
     * @param player The player object to apply the effect to.
     * @param delta The time in seconds since the last update.
     */
    @Override
    public void update(Player player, float delta) {
        if(player.getBounds().overlaps(getBounds())) {
            if(!active) {
                player.loseLife();
                active = true;
                timer = 0f;
            }
            timer += delta;
            if(timer >= interval) {
                player.loseLife();
                timer = 0f;
            }
        } else {
            active = false;
            timer = 0f;
        }
    }
}
