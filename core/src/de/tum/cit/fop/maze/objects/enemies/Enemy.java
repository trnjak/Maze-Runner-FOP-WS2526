package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.GameObj;
import de.tum.cit.fop.maze.objects.Player;

public abstract class Enemy extends GameObj {
    //speed in pixels per second
    //attack timer times the attack to only attack in attackInterval time
    //attack range in pixels
    protected float speed = 75, timer = 0, range = TILE * 1.1f;
    protected final float interval = 1; //attack once per second
    protected int hp = 3;
    protected boolean alive = true;
    protected float lookX, lookY;

    protected final TextureRegion right, left;
    protected TextureRegion current;

    public Enemy(int x, int y, int tileY, int tileX) {
        super(x * TILE, y * TILE, tileY, tileX);
        this.w = TILE;
        this.h = TILE;

        right = new TextureRegion(texture);
        left = new TextureRegion(texture);
        left.flip(true, false);

        current = right;
    }

    protected abstract void move(float delta, Player player, GameMap map);

    public void update(float delta, Player player, GameMap map) {
        if(!alive) {
            return;
        }

        current = (lookX > 0) ? right : left;

        move(delta, player, map);

        if(inRange(player)) {
            if(timer <= 0) {
                player.loseLife();
                player.setTint(Color.RED);
                timer = interval;
            } else {
                timer -= delta;
            }
        } else {
            timer = 0;
        }
    }

    public boolean inRange(Player player) {
        return distance(player) <= range;
    }

    protected float distance(Player player) {
        return (float) Math.sqrt(Math.pow((player.getX() - x), 2) + Math.pow((player.getY() - y), 2)); //distance between two points
    }

    public void takeDamage(int dmg) {
        if(!alive) {
            return;
        }
        hp -= dmg;
        if(hp <= 0) {
            alive = false;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public void render(SpriteBatch batch) {
        renderMovingObj(batch, current);
    }

    @Override
    public Rectangle getBounds() {
        return getBoundsMovingObj();
    }
}
