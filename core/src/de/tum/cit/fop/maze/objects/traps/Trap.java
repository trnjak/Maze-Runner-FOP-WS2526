package de.tum.cit.fop.maze.objects.traps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.objects.*;

public abstract class Trap extends GameObj {
    public Trap(int x, int y, int tileY, int tileX) {
        super(x, y, tileY, tileX);
    }

    public abstract void update(Player player, float delta);

    @Override
    public void render(SpriteBatch batch) {
        renderStaticObj(batch);
    }
    @Override
    public Rectangle getBounds() {
        return getBoundsStaticObj();
    }
}
