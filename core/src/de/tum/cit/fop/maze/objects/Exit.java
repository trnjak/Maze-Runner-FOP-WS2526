package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.GameMap;

/**
 * The Exit class represents the level's exit point, which becomes accessible when a key is collected.
 * Features an animated opening sequence that plays when unlocked.
 */
public class Exit extends GameObj {
    private Animation<TextureRegion> openAnimation;
    private float time = 0;
    private boolean isOpen = false;

    /**
     * Constructs a new Exit at the specified coordinates.
     * Initialises with closed state and loads opening animation frames.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     */
    public Exit(int x, int y) {
        super(x, y, 3, 9);
        loadOpenAnimation();
    }

    /**
     * Opens the exit, enabling passage for the player and starting the opening animation.
     * Must be called when the player collects a key.
     */
    public void open() {
        isOpen = true;
        time = 0;
    }

    /**
     * Updates the exit's animation state when open.
     *
     * @param delta The time in seconds since the last update
     */
    public void update(float delta) {
        if (isOpen) {
            time += delta;
        }
    }

    /**
     * Loads the animation frames for the exit's opening sequence.
     * Uses three frames from the game's texture sheet to create a visual transition.
     */
    private void loadOpenAnimation() {
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        walkFrames.add(GameMap.TEXTURE_REGION[1][9]);
        walkFrames.add(GameMap.TEXTURE_REGION[2][9]);
        walkFrames.add(GameMap.TEXTURE_REGION[3][9]);

        openAnimation = new Animation<>(1, walkFrames);
    }

    /**
     * Renders the exit, displaying either the static closed texture or the current animation frame.
     *
     * @param batch The SpriteBatch used for rendering
     */
    @Override
    public void render(SpriteBatch batch) {
        if (isOpen) {
            TextureRegion currentFrame = openAnimation.getKeyFrame(time, false);
            batch.draw(currentFrame, x * TILE, y * TILE, TILE, TILE);
        } else {
            renderStaticObj(batch);
        }
    }

    /**
     * Gets the collision bounds of the exit for interaction detection.
     *
     * @return The rectangle representing the exit's collision bounds
     */
    @Override
    public Rectangle getBounds() {
        return getBoundsStaticObj();
    }
}
