package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameMap;

/**
 * The GameObj abstract class serves as the foundation for all game objects.
 * Provides common functionality for position, rendering, collision detection, and texture management.
 * Subclasses implement specific behaviour while inheriting core game object features.
 */
public abstract class GameObj {
    /**
     * The standard tile size in pixels used for game object dimensions and positioning.
     */
    public static final int TILE = 32;
    /**
     * Collision padding value to make collision detection more forgiving around object edges.
     * Negative value creates a slightly smaller collision area than visual representation.
     */
    protected static final float P = -5;
    protected final TextureRegion texture;
    protected float x, y;
    protected float w, h;

    /**
     * Constructs a new GameObj with specified position and texture coordinates.
     *
     * @param x     The X coordinate of the object
     * @param y     The Y coordinate of the object
     * @param tileY The row index in the GameMap texture tile sheet
     * @param tileX The column index in the GameMap texture tile sheet
     */
    public GameObj(float x, float y, int tileY, int tileX) {
        this.x = x;
        this.y = y;
        this.texture = GameMap.TEXTURE_REGION[tileY][tileX];
    }

    /**
     * Renders a static object positioned in coordinates.
     * Objects using this method are typically fixed at grid positions.
     *
     * @param batch The SpriteBatch used for rendering
     */
    protected void renderStaticObj(SpriteBatch batch) {
        batch.draw(texture, x * TILE, y * TILE, TILE, TILE);
    }

    /**
     * Renders a moving object positioned in pixel coordinates.
     * Objects using this method can move freely at sub-tile precision.
     *
     * @param batch   The SpriteBatch used for rendering
     * @param current The TextureRegion to render (allows for animated or directional sprites)
     */
    protected void renderMovingObj(SpriteBatch batch, TextureRegion current) {
        batch.draw(current, x, y, w, h);
    }

    /**
     * Abstract render method defining the visual representation of the object.
     * Must be implemented by concrete subclasses.
     *
     * @param batch The SpriteBatch used for rendering
     */
    public abstract void render(SpriteBatch batch);

    /**
     * Gets collision bounds for static objects positioned in coordinates.
     * Includes collision padding for more forgiving hit detection.
     *
     * @return Rectangle representing the object's collision bounds
     */
    protected Rectangle getBoundsStaticObj() {
        return new Rectangle(
                x * TILE - P, y * TILE - P, TILE + 2 * P, TILE + 2 * P
        );
    }

    /**
     * Gets collision bounds for moving objects positioned in pixel coordinates.
     * Includes collision padding for more forgiving hit detection.
     *
     * @return Rectangle representing the object's collision bounds
     */
    protected Rectangle getBoundsMovingObj() {
        return new Rectangle(
                x - P, y - P, w + 2 * P, h + 2 * P
        );
    }

    /**
     * Abstract method to get the collision bounds of the object.
     * Must be implemented by concrete subclasses to specify appropriate bounds.
     *
     * @return Rectangle representing the object's collision bounds
     */
    public abstract Rectangle getBounds();

    /**
     * Gets the object's X coordinate.
     *
     * @return The current X coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Sets the object's X coordinate.
     *
     * @param x The new X coordinate
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Gets the object's Y coordinate.
     *
     * @return The current Y coordinate
     */
    public float getY() {
        return y;
    }

    /**
     * Sets the object's Y coordinate.
     *
     * @param y The new Y coordinate
     */
    public void setY(float y) {
        this.y = y;
    }
}
