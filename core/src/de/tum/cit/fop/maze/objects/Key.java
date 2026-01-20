package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * The Key class represents a collectible key object that the player can pick up to unlock the exit.
 * Extends GameObj for basic object functionality and rendering.
 */
public class Key extends GameObj {
    /**
     * Constructs a new Key at the specified coordinates.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     */
    public Key(int x, int y) {
        super(x, y, 8, 5);
    }

    /**
     * Renders the key using the static object rendering method from GameObj.
     *
     * @param batch The SpriteBatch used for rendering
     */
    @Override
    public void render(SpriteBatch batch) {
        renderStaticObj(batch);
    }

    /**
     * Gets the collision bounds of the key for interaction detection.
     *
     * @return The rectangle representing the key's collision bounds
     */
    @Override
    public Rectangle getBounds() {
        return getBoundsStaticObj();
    }
}
