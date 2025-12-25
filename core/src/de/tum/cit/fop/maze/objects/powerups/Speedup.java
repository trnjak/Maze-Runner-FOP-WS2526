package de.tum.cit.fop.maze.objects.powerups;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.objects.Player;

public class Speedup extends Powerup {

    public Speedup(float x, float y) {
        super(x, y, 9, 6);
    }

    @Override
    public void update(Player player, float delta) {
        if(player.getBounds().overlaps(getBounds())) {
            player.speedEffect(1.25f, 2f, Color.GREEN); // 1.25x speed for 2s
        }
    }

}
