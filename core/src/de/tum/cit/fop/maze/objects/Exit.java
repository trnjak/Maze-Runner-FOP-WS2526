package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.GameMap;

/**
 * The Exit class represents the level's exit point, accessible only when a key is collected.
 */
public class Exit extends GameObj {
    private Animation<TextureRegion> openAnimation;
    private float time = 0;
    private boolean isOpen = false;

    /**
     * Constructor for Exit.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     */
    public Exit(int x, int y) {
        super(x, y, 3, 9);
        loadOpenAnimation();
    }

    /**
     * Opens the exit, making it passable and starting the opening animation sequence.
     */
    public void open() {
        isOpen = true;
        time = 0;
    }

    /**
     * Updates the exit's animation state when it is open.
     * @param delta The time in seconds since the last update.
     */
    public void update(float delta) {
        if(isOpen) {
            time += delta;
        }
    }

    /**
     * Loads the animation frames for the exit's opening sequence from the game's texture sheet.
     */
    private void loadOpenAnimation() {
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        walkFrames.add(GameMap.TEXTURE_REGION[1][9]);
        walkFrames.add(GameMap.TEXTURE_REGION[2][9]);
        walkFrames.add(GameMap.TEXTURE_REGION[3][9]);

        openAnimation = new Animation<>(1, walkFrames);
    }

    /**
     * Renders the exit, displaying either the static closed texture or the current frame of the opening animation.
     * @param batch The SpriteBatch used for rendering.
     */
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
