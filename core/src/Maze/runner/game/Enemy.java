package Maze.runner.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;

public class Enemy extends Gameclass {
    private int x;
    private int y;
    private Texture texture;
    protected float w,h;

    public Enemy(int x, int y, Texture texture) {
        super(x, y,texture, 1);
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.w=texture.getWidth();
        this.h=texture.getHeight();


    }
    public void update(float delta, ArrayList<Gameclass> obstacles) {
        int dx=0;
        int dy=0;

    }
    // i added this because the player gets stuck after the collision with the enemies
    protected void pushPlayer(Player player) {
        Vector2 push = new Vector2(
                player.getX() - x,
                player.getY() - y
        );

        if (push.len2() > 0) {
            push.nor().scl(10f); // push strength (pixels)
            player.setPosition(
                    player.getX() + push.x,
                    player.getY() + push.y
            );
        }
    }

}