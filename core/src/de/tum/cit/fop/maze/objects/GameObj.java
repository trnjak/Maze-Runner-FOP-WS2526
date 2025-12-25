package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameMap;

public abstract class GameObj {
    public static final int TILE = 32;
    protected float x, y;
    protected float w, h;
    protected final TextureRegion texture;

    protected static final float P = -5; //padding for a more generous collision detection

    public GameObj(float x, float y, int tileY, int tileX) {
        this.x = x;
        this.y = y;
        this.texture = GameMap.TEXTURE_REGION[tileY][tileX];
    }

    protected void renderStaticObj(SpriteBatch batch) {
        batch.draw(texture, x * TILE, y * TILE, TILE, TILE);
    }

    protected void renderMovingObj(SpriteBatch batch, TextureRegion current) {
        batch.draw(current, x, y, w, h);
    }

    public abstract void render(SpriteBatch batch);

    protected Rectangle getBoundsStaticObj() {
        return new Rectangle(
                x * TILE - P, y * TILE - P,TILE + 2 * P, TILE + 2 * P
        );
    }

    protected Rectangle getBoundsMovingObj() {
        return new Rectangle(
                x - P, y - P, w + 2 * P, h + 2 * P
        );
    }

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
