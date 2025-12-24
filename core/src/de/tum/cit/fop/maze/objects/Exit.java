package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.GameMap;

public class Exit extends GameObj {
    private Animation<TextureRegion> openAnimation;
    private float time = 0;
    private boolean isOpen = false;

    public Exit(int x, int y) {
        super(x, y, 3, 9);
        loadOpenAnimation();
    }

    public void open() {
        isOpen = true;
        time = 0; // Reset animation
    }

    public void update(float delta) {
        if(isOpen) {
            time += delta;
        }
    }

    private void loadOpenAnimation() {
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        walkFrames.add(GameMap.TEXTURE_REGION[1][9]);
        walkFrames.add(GameMap.TEXTURE_REGION[2][9]);
        walkFrames.add(GameMap.TEXTURE_REGION[3][9]);

        openAnimation = new Animation<>(1, walkFrames);
    }

    @Override
    public void render(SpriteBatch batch) {
        if(isOpen) {
            TextureRegion currentFrame = openAnimation.getKeyFrame(time, false);
            batch.draw(currentFrame, x * TILE, y * TILE, TILE, TILE);
        } else {
            renderStaticObj(batch);
        }
    }
    @Override
    public Rectangle getBounds() {
        return getBoundsStaticObj();
    }
}
