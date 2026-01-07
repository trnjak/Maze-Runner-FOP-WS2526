package de.tum.cit.fop.maze.objects.traps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.objects.*;

/**
 * The Trap abstract class serves as the base class for all trap objects in the game.
 * It provides common functionality for rendering and collision detection while requiring
 * specific update behavior to be implemented by each trap type.
 */
public abstract class Trap extends GameObj {
    /**
     * Constructor for Trap.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param tileY The row index in the texture tile sheet.
     * @param tileX The column index in the texture tile sheet.
     */
    public Trap(int x, int y, int tileY, int tileX) {
        super(x, y, tileY, tileX);
    }

    /**
     * Abstract update method that defines the trap's effect when triggered by the player.
     * @param player The player object to apply the trap effect to.
     * @param delta The time in seconds since the last update.
     */
    public abstract void update(Player player, float delta);

    /**
     * Renders the trap using the static object rendering method.
     * @param batch The SpriteBatch used for rendering.
     */
    @Override
    public void render(SpriteBatch batch) {
        renderStaticObj(batch);
    }
    @Override
    public Rectangle getBounds() {
        return getBoundsStaticObj();
    }
}
