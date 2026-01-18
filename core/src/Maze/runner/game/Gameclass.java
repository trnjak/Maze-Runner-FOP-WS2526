package Maze.runner.game;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.ArrayList;

public abstract class  Gameclass  {


//    those two variables here are used to specify the position on the maze grid
//    (0,0)  represents the bottom left of the maze
    protected float x;
    protected float y;
    protected Texture texture;
    int typeID;



    public Gameclass(float x, float y, Texture texture, float typeID)
     {
        this.x = x;
        this.y = y;
        this.texture = texture;



    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Texture getTexture() {
        return texture;
    }

    public int getTypeID() {
        return typeID;
    }

    //    the typeID specifies the typeID used in the map file(0=Wall, 1=Entry, etc)
    public abstract void update(float delta, ArrayList<Gameclass> obstacles);

//    this command is used to render the object it means
//        to draw it and to create a visual output






}
