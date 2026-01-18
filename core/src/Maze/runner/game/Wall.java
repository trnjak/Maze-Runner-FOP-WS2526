package Maze.runner.game;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class Wall extends Gameclass{
    private int x;
    private int y;
    private Texture texture;
    public Wall(int x, int y, Texture texture) {
        super(x,y,texture,0);
        this.x = x;
        this.y = y;
        this.texture = texture;
    }
    public void update(float delta, ArrayList<Gameclass> obstacles) {

    }


}
