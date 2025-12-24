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
        return new Rectangle(x * TILE, y * TILE, TILE, TILE);
    }

    protected Rectangle getBoundsMovingObj() {
        return new Rectangle(x, y, w, h);
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
