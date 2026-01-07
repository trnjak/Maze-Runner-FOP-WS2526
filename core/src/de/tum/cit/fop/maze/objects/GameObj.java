package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameMap;

/**
 * The GameObj abstract class serves as the base class for all game objects, with common properties
 * and methods for position, rendering, collision detection, and texture management.
 */
public abstract class GameObj {
    /**
     * The standard tile size in pixels used for game object dimensions.
     */
    public static final int TILE = 32;
    protected float x, y;
    protected float w, h;
    protected final TextureRegion texture;

    /**
     * Collision padding value to make collision detection more generous around object edges.
     */
    protected static final float P = -5;

    /**
     * Constructor for GameObj.
     *
     * @param x The X coordinate of the object.
     * @param y The Y coordinate of the object.
     * @param tileY The row index in the texture tile sheet.
     * @param tileX The column index in the texture tile sheet.
     */
    public GameObj(float x, float y, int tileY, int tileX) {
        this.x = x;
        this.y = y;
        this.texture = GameMap.TEXTURE_REGION[tileY][tileX];
    }

    /**
     * Renders a static object (positioned in tile coordinates) using the object's texture.
     * @param batch The SpriteBatch used for rendering.
     */
    protected void renderStaticObj(SpriteBatch batch) {
        batch.draw(texture, x * TILE, y * TILE, TILE, TILE);
    }

    /**
     * Renders a moving object (positioned in pixel coordinates) using the specified texture region.
     * @param batch The SpriteBatch used for rendering.
     * @param current The TextureRegion to render.
     */
    protected void renderMovingObj(SpriteBatch batch, TextureRegion current) {
        batch.draw(current, x, y, w, h);
    }

    /**
     * Abstract render method that must be implemented by all game objects to define their visual representation.
     * @param batch The SpriteBatch used for rendering.
     */
    public abstract void render(SpriteBatch batch);

    /**
     * Gets the collision bounds for static objects (positioned in tile coordinates) with added padding.
     */
    protected Rectangle getBoundsStaticObj() {
        return new Rectangle(
                x * TILE - P, y * TILE - P,TILE + 2 * P, TILE + 2 * P
        );
    }

    /**
     * Gets the collision bounds for moving objects (positioned in pixel coordinates) with added padding.
     */
    protected Rectangle getBoundsMovingObj() {
        return new Rectangle(
                x - P, y - P, w + 2 * P, h + 2 * P
        );
    }

    /**
     * Abstract method to get the collision bounds, which must be implemented by all game objects.
     */
    public abstract Rectangle getBounds();

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }
}
