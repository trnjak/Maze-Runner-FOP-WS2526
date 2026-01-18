package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.Player;




public class ChaserEnemy extends Enemy {
    private float dx = 0, dy = 0, cd = 0; //direction to x, y, and change of direction respectively

    public ChaserEnemy(int x, int y) {
        super(x, y, 10, 1);
    }

    @Override
    protected void move(float delta, Player player, GameMap map) {
        if (inRange(player)) { //
            // stop to attack player
            dx = 0;
            dy = 0;
            return;

//            Vector2 away = new Vector2(x - player.getX(), y - player.getY());
//            if (away.len2() > 0) away.nor();
//
//            dx = away.x * 0.2f; // slow retreat
//            dy = away.y * 0.2f;
        }
        //TODO: implement chasing player with a*
        if (inChaseRange(player)) {
//            Vector2 dir = new Vector2(player.getX() - x, player.getY() - y); //move towards player
//            if(dir.len2() > 0) {
//                dir.nor();
//                dx = dir.x;
//                dy = dir.y;
//            }
            Vector2 dir = new Vector2(player.getX() - x, player.getY() - y);
            if (dir.len2() > 0) {
                dir.nor();
            }
            float lookDistance = speed * delta;
            // this specifies how far ahead should i check
            float nextX = x + dir.x * lookDistance;
            float nextY = y + dir.y * lookDistance;

            Rectangle nextRec = new Rectangle(nextX, nextY, w, h);
            // i added two instances which are the w and the h to the enemy class
//            this is where enemy is going to be after moving or after
//            trying to catch up on the player
            if (map.collidesWithWall(nextRec)) {
                Vector2 tryDir = new Vector2(dir);//we are going ro use the old direction and change it a bit
//                boolean path=false;
                Vector2 bestDir = new Vector2(dir);
                boolean found = false;

                for (int angle = 15; angle <= 90; angle += 15) {
                    // try right
                    Vector2 right = new Vector2(dir).rotateDeg(angle);
                    nextX = x + right.x * lookDistance;
                    nextY = y + right.y * lookDistance;
                    nextRec.set(nextX, nextY, w, h);
                    if (!map.collidesWithWall(nextRec)) {
                        bestDir.set(right);
                        found = true;
                        break;
                    }

                    // try left
                    Vector2 left = new Vector2(dir).rotateDeg(-angle);
                    nextX = x + left.x * lookDistance;
                    nextY = y + left.y * lookDistance;
                    nextRec.set(nextX, nextY, w, h);
                    if (!map.collidesWithWall(nextRec)) {
                        bestDir.set(left);
                        found = true;
                        break;
                    }
                }

                if (found) {
                    dir.set(bestDir);
                } else {
                    dir.setZero(); // stuck in a corner
                }

//                if(!path){
//                    dir.set(0,0);
//                    rarely happens in case it blocked in a corner it can
//                    not happen because it will already try to move up to 120 degrees
//                }


            }
            dx = dir.x;
            dy = dir.y;
//            In this lines i m implelmetnit the moves and now i m trying to od the d

            float sx = dx * speed * delta;
            float sy = dy * speed * delta;



            if (dir.len2() > 0) {
                lookX = dir.x;
                lookY = dir.y;
            }


        } else {
            // wander randomly if player is outside of range
            cd += delta;
            if (cd > 2) {
                dx = (int) (Math.random() * 3) - 1; //0,1,2 w/0 the -1, -1,0,1 with, so it's left/stay/right
                dy = (int) (Math.random() * 3) - 1;
                Vector2 dir = new Vector2(dx, dy);
                if (dir.len2() > 0) {
                    dir.nor();
                }
                dx = dir.x;
                dy = dir.y;
                cd = 0;
            }
        }
        Rectangle pb = player.getBounds();
        float sx = dx * speed * delta; //step toward x
        float sy = dy * speed * delta; //step toward y


        Rectangle next = new Rectangle(x + sx, y + sy, w, h);
        // move if no collision, otherwise attempt axis aligned movement
        if (!map.collidesWithWall(next)) {
            x += sx;
            y += sy;
//            if (next.overlaps(pb)){
//                player.damage();
//            }


        } else {
            Rectangle tryX = new Rectangle(x + sx, y, w, h); //bounds for next x movement
            Rectangle tryY = new Rectangle(x, y + sy, w, h); //same for y

            if (!map.collidesWithWall(tryX)) {
                x += sx; //move along x if possible
//                the  overlop exists and the enemy can not move anymore
            }
            if (!map.collidesWithWall(tryY)) {
                y += sy; //move along y if possible
            }
//            if (next.overlaps(pb)) {
//                player.damage();  // or whatever your damage logic is
//            }
        }

        if (dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }
        Rectangle enemyBounds = new Rectangle(x, y, w, h);
        Rectangle playerBounds = player.getBounds();

        if (enemyBounds.overlaps(playerBounds)) {

            pushPlayer(player);
        }
    }

    private boolean inChaseRange(Player player) {
        return distance(player) <= 128; //chase range 128 pixels
    }
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
