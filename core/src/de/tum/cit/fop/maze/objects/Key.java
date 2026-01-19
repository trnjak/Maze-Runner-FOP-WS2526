package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * The Key class represents a collectible key object that the player can pick up to unlock the exit.
 */
public class Key extends GameObj {
    /**
     * Constructor for Key. Initializes the key at the specified tile coordinates.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     */
    public Key(int x, int y) {
        super(x, y, 8, 5);
    }

    /**
     * Renders the key using the static object rendering method from GameObj.
     *
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
