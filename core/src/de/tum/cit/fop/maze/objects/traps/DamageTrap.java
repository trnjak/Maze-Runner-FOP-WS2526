package de.tum.cit.fop.maze.objects.traps;

import de.tum.cit.fop.maze.objects.Player;

/**
 * The DamageTrap class represents a trap that inflicts damage to the player at regular intervals.
 * First damage occurs immediately upon contact, with subsequent damage at configured intervals if the player remains.
 */
public class DamageTrap extends Trap {
    private final float interval;
    private float timer = 0;
    private boolean active = false;

    /**
     * Constructs a new DamageTrap at the specified coordinates with a damage interval.
     *
     * @param x        The X coordinate
     * @param y        The Y coordinate
     * @param interval The time interval in seconds between damage ticks while the player remains on the trap
     */
    public DamageTrap(int x, int y, float interval) {
        super(x, y, 3, 5);
        this.interval = interval;
    }

    /**
     * Updates the trap's damage application to the player.
     * First damage occurs immediately upon collision, with subsequent damage at regular intervals.
     *
     * @param player The player object to apply damage to
     * @param delta  The time in seconds since the last update
     */
    @Override
    public void update(Player player, float delta) {
        if (player.getBounds().overlaps(getBounds())) {
            if (!active) {
                player.loseLife();
                active = true;
                timer = 0f;
            }
            timer += delta;
            if (timer >= interval) {
                player.loseLife();
                timer = 0f;
            }
        } else {
            active = false;
            timer = 0f;
        }
    }
}
