package de.tum.cit.fop.maze.objects.powerups;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.objects.GameObj;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The Powerup abstract class serves as the base class for all collectible powerup objects in the game.
 * Provides common functionality for rendering, collision detection, and player effect application.
 */
public abstract class Powerup extends GameObj {
    /**
     * Constructs a new Powerup at the specified coordinates with given texture coordinates.
     *
     * @param x     The X coordinate
     * @param y     The Y coordinate
     * @param tileY The row index in the GameMap texture tile sheet
     * @param tileX The column index in the GameMap texture tile sheet
     */
    public Powerup(float x, float y, int tileY, int tileX) {
        super(x, y, tileY, tileX);
    }

    /**
     * Abstract update method defining the powerup's effect when collected by the player.
     * Must be implemented by concrete powerup subclasses to apply specific beneficial effects.
     *
     * @param player The player object to apply the powerup effect to
     * @param delta  The time in seconds since the last update
     */
    public abstract void update(Player player, float delta);

    /**
     * Renders the powerup using the static object rendering method.
     *
     * @param batch The SpriteBatch used for rendering
     */
    @Override
    public void render(SpriteBatch batch) {
        renderStaticObj(batch);
    }

    /**
     * Gets the collision bounds of the powerup for collection detection.
     *
     * @return The rectangle representing the powerup's collision bounds
     */
    @Override
    public Rectangle getBounds() {
        return getBoundsStaticObj();
    }
}
