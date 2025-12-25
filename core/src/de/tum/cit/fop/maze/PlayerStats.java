package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class PlayerStats {
    private static final String SAVE_FILE = "player_stats.json";

    private int exp = 0, hpLvl = 1, speedLvl = 1, atkLvl = 1;

    private static PlayerStats ps;
    private static final Json json = new Json();

    private PlayerStats() {
        load();
    }

    public static PlayerStats getInstance() {
        if(ps == null) {
            ps = new PlayerStats();
        }
        return ps;
    }

    @SuppressWarnings("unchecked")
    private void load() {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            if(file.exists()) {
                String jsonData = file.readString();
                ObjectMap<String, Object> data = json.fromJson(ObjectMap.class, jsonData);

                Object expObj = data.get("exp");
                Object hpObj = data.get("hpLvl");
                Object speedObj = data.get("speedLvl");
                Object atkObj = data.get("atkLvl");

                exp = expObj instanceof Number ? ((Number)expObj).intValue() : 0;
                hpLvl = hpObj instanceof Number ? ((Number)hpObj).intValue() : 1;
                speedLvl = speedObj instanceof Number ? ((Number)speedObj).intValue() : 1;
                atkLvl = atkObj instanceof Number ? ((Number)atkObj).intValue() : 1;
            } else {
                save();
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
            resetToDefaults();
        }
    }

    public void save() {
        try {
            ObjectMap<String, Integer> data = new ObjectMap<>();
            data.put("exp", exp);
            data.put("hpLvl", hpLvl);
            data.put("speedLvl", speedLvl);
            data.put("atkLvl", atkLvl);

            FileHandle file = Gdx.files.local(SAVE_FILE);
            file.writeString(json.prettyPrint(json.toJson(data)), false);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void addExp(int points) {
        exp += points;
        save();
    }

    public boolean spendExp(int cost) {
        if(exp >= cost) {
            exp -= cost;
            save();
            return true;
        }
        return false;
    }

    public int getExp() {
        return exp;
    }

    public int getHpLvl() {
        return hpLvl;
    }

    public int getSpeedLvl() {
        return speedLvl;
    }

    public int getAtkLvl() {
        return atkLvl;
    }

    public int getMaxHp() {
        return 4 + hpLvl;
    }

    public float getBaseSpeed() {
        return 1f + (speedLvl - 1) * 0.02f;
    }

    public float getAttackCooldown() {
        return Math.max(0.01f, 0.5f - (atkLvl - 1) * 0.02f);
    }

    public boolean upgradeHp() {
        int cost = hpLvl * 2;
        if(spendExp(cost)) {
            hpLvl++;
            save();
            return true;
        }
        return false;
    }

    public boolean upgradeSpeed() {
        int cost = speedLvl * 2;
        if(spendExp(cost)) {
            speedLvl++;
            save();
            return true;
        }
        return false;
    }

    public boolean upgradeAtk() {
        int cost = atkLvl * 2;
        if(spendExp(cost)) {
            atkLvl++;
            save();
            return true;
        }
        return false;
    }

    public void resetToDefaults() {
        exp = 0;
        hpLvl = 1;
        speedLvl = 1;
        atkLvl = 1;
        save();
    }
}