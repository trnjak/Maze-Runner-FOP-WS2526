package Maze.runner.game;

import Maze.runner.game.Gameclass;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;


import java.util.ArrayList;



public class Player extends Gameclass {
    private float x;
    private float y;
    private Texture texture;
    private  int lives ;
    private boolean hasKey;
    private float speed;
    private float w;
    private float h;
    private float invincibleTimer = 0f;
    private static final float INVINCIBLE_DURATION = 0.5f;


    public Player(float x, float y,  Texture texture) {
        super(x, y, texture,2);
        this.lives = 8;
        this.hasKey = false;
        this.speed=150;
        this.w=texture.getWidth();
        this.h=texture.getHeight();
//        the speed here is in pixels.

    }

    public float getW() {
        return w;
    }

    public float getH() {
        return h;
    }

    public int getLives() {
        return lives;
    }

    public boolean isHasKey() {
        return hasKey;
    }

    /**this f
     * function is used to make the chararcter able to move in a way that
     * is able to update its position when certain keys get pressed
     * and it s able to prohibit collisions too
     * @param delta
     */

    public void update(float delta, ArrayList<Gameclass> obstacles) {
        if (invincibleTimer > 0) {
            invincibleTimer -= delta;
        }
        /* we are updating the invincibleTimer in every frame that we use it */
        float dx=0;
        float dy=0;
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) dx=-1;
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) dx=1;
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) dy=1;
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) dy=-1;
//        the main problem here is that the player never moves after being
//        hit by specific enemy they freeze which makes the enemy freeze too
        float len =(float)Math.sqrt(dx*dx + dy*dy);
//        this computes the length of the movement vector
        if(len > 0) {
            dx /= len;
            dy /= len;
        }
        float newX = x + dx * speed * delta;
        float newY = y + dy * speed * delta;
        Rectangle newPlayerRect = new Rectangle(newX, newY,(float)texture.getWidth(),(float)texture.getHeight());
        for (Gameclass obstacle : obstacles) {
            if (obstacle.getTypeID() == 0) {
                Rectangle wallRect = new Rectangle(obstacle.getX(), obstacle.getY(), obstacle.getTexture().getWidth(), obstacle.getTexture().getHeight());
                float testX = x + dx * speed * delta;
                Rectangle rectX = new Rectangle(testX, y, w, h);

                for (Gameclass obs : obstacles) {
                    if (obs.getTypeID() == 0) {
                        Rectangle wall = new Rectangle(
                                obs.getX(), obs.getY(),
                                obs.getTexture().getWidth(),
                                obs.getTexture().getHeight()
                        );
                        if (!rectX.overlaps(wall)) {
                            x = testX;
                        }
                    }
                }

                float testY = y + dy * speed * delta;
                Rectangle rectY = new Rectangle(x, testY, w, h);

                for (Gameclass obs : obstacles) {
                    if (obs.getTypeID() == 0) {
                        Rectangle wall = new Rectangle(
                                obs.getX(), obs.getY(),
                                obs.getTexture().getWidth(),
                                obs.getTexture().getHeight()
                        );
                        if (!rectY.overlaps(wall)) {
                            y = testY;
                        }
                    }
                }


            }
        }
//        int these two lines here we are simply applying the movement
        x=newX;
        y=newY;
    }
    //    public  void damage() {
//        lives--;
//    }
    public  void damage(){
//        if (invincibleTimer > 0) return false; // ignore repeated hits

        lives--;
//        invincibleTimer = INVINCIBLE_DURATION;
//        return true;
    }

    /**this functioon does not bring any changes on the game itself
     * its just used to draw player at his current positions
     * @param batch
     */
    public void render(Batch batch) {

    }


    public void setPosition(float v, float v1) {
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, w, h);
    }
}

