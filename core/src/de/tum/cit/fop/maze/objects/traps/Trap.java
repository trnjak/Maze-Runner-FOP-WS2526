package de.tum.cit.fop.maze.objects.traps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.objects.GameObj;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The Trap abstract class serves as the base class for all trap objects in the game.
 * Provides common functionality for rendering, collision detection, and player interaction effects.
 */
public abstract class Trap extends GameObj {
    /**
     * Constructs a new Trap at the specified coordinates with given texture coordinates.
     *
     * @param x     The X coordinate
     * @param y     The Y coordinate
     * @param tileY The row index in the GameMap texture tile sheet
     * @param tileX The column index in the GameMap texture tile sheet
     */
    public Trap(int x, int y, int tileY, int tileX) {
        super(x, y, tileY, tileX);
    }

    /**
     * Abstract update method defining the trap's effect when triggered by the player.
     * Must be implemented by concrete trap subclasses to apply specific effects.
     *
     * @param player The player object to apply the trap effect to
     * @param delta  The time in seconds since the last update
     */
    public abstract void update(Player player, float delta);

    /**
     * Renders the trap using the static object rendering method.
     *
     * @param batch The SpriteBatch used for rendering
     */
    @Override
    public void render(SpriteBatch batch) {
        renderStaticObj(batch);
    }

    /**
     * Gets the collision bounds of the trap for interaction detection.
     *
     * @return The rectangle representing the trap's collision bounds
     */
    @Override
    public Rectangle getBounds() {
        return getBoundsStaticObj();
    }
}
