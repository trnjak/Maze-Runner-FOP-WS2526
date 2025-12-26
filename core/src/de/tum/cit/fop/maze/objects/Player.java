package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.PlayerStats;
import de.tum.cit.fop.maze.objects.enemies.*;
import de.tum.cit.fop.maze.screens.BeginScreen;

import java.util.List;

public class Player extends GameObj {
    private int hp, keys = 0;
    private float tintTimer = 0, attackTimer = 0, effectTimer = 0;
    private float lookX, lookY;
    private float speedMult = 1.0f;
    private Color tint = Color.WHITE;

    private final PlayerStats playerStats;

    private final TextureRegion right, left;
    private TextureRegion current;

    private final Sound atkMiss = Gdx.audio.newSound(Gdx.files.internal("sounds/attack_miss.ogg")),
    atkHit = Gdx.audio.newSound(Gdx.files.internal("sounds/attack_hit.ogg")),
    takeDmg = Gdx.audio.newSound(Gdx.files.internal("sounds/take_dmg.ogg"));

    public Player(float x, float y) {
        super(x, y, 7, 0);
        this.w = TILE;
        this.h = TILE;

        playerStats = BeginScreen.STATS;
        hp = playerStats.getMaxHp();

        right = new TextureRegion(texture);
        left = new TextureRegion(texture);
        left.flip(true, false);

        current = right;
    }

    public void move(float dx, float dy, GameMap map) {
        float currentSpeed = playerStats.getBaseSpeed() * speedMult;
        if(dx != 0) {
            float newX = x + dx * currentSpeed;
            Rectangle tempBoundsX = new Rectangle(newX, y, w, h);
            if(!map.collidesWithWall(tempBoundsX)) {
                x = newX;
            }
        }
        if(dy != 0) {
            float newY = y + dy * currentSpeed;
            Rectangle tempBoundsY = new Rectangle(x, newY, w, h);
            if(!map.collidesWithWall(tempBoundsY)) {
                y = newY;
            }
        }
        if(dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }
    }

    public void update(float delta) {
        current = (lookX > 0) ? right : left;

        if(attackTimer > 0f) {
            attackTimer -= delta;
        }

        if(effectTimer > 0) {
            effectTimer -= delta;
            if(effectTimer <= 0) {
                speedMult = 1.0f;
                setTint(tint);
            }
        }

        if(!tint.equals(Color.WHITE)) {
            tintTimer += delta;
            if(tintTimer >= 0.25f) {
                tint = Color.WHITE;
                tintTimer = 0f;
            }
        }
    }

    public void loseLife(int n) {
        hp -= n;
        takeDmg.play(0.2f);
        setTint(Color.PINK);
    }

    public void loseLife() {
        loseLife(1);
    }

    public void attack(List<Enemy> e) {
        float attackCooldown = playerStats.getAttackCooldown();
        if(attackTimer > 0f) {
            return;
        }

        float range = Math.max(w, h) * 1.5f;
        float cx = x + w / 2f;
        float cy = y + h / 2f;

        boolean hit = false;
        float cosHalfAngle = (float) Math.cos(Math.toRadians(90));

        for(Enemy enemy : e) {
            if(!enemy.isAlive()) {
                continue;
            }

            Rectangle eb = enemy.getBounds();
            float ex = eb.x + eb.width / 2;
            float ey = eb.y + eb.height / 2;

            float vx = ex - cx;
            float vy = ey - cy;
            float d = (float) Math.sqrt(vx * vx + vy * vy);
            if(d > range) {
                atkMiss.play(0.2f);
                continue;
            }

            float nx = d == 0 ? 0 : vx / d;
            float ny = d == 0 ? 0 : vy / d;
            float dot = nx * lookX + ny * lookY;

            if(d == 0f || dot >= cosHalfAngle) {
                atkHit.play(0.2f);
                enemy.takeDamage(1);
                hit = true;
            }
        }

        if(hit) {
            setTint(Color.YELLOW);
        } else {
            setTint(Color.ORANGE);
        }

        attackTimer = attackCooldown;
    }

    public void speedEffect(float mult, float t, Color tint) {
        speedMult = mult;
        effectTimer = t;
        setTint(tint);
    }

    public void setTint(Color c) {
        this.tint = c;
        this.tintTimer = 0f;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHealth() {
        return playerStats.getMaxHp();
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void collectKey() {
        keys++;
    }

    public int getKeys() {
        return keys;
    }

    public void setKeys(int keys) {
        this.keys = keys;
    }

    public float getCurrentSpeed() {
        return playerStats.getBaseSpeed() * speedMult;
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setColor(tint);
        renderMovingObj(batch, current);
        batch.setColor(Color.WHITE);
    }

    @Override
    public Rectangle getBounds() {
        return getBoundsMovingObj();
    }
}
