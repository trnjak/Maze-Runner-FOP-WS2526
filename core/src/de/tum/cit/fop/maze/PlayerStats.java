package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class PlayerStats {
    private String name;
    private final String SAVE_FILE;
    private int exp = 0, score = 0;
    private int hpLvl = 1, speedLvl = 1, atkLvl = 1, level = 1;

    private static final Json json = new Json();

    public PlayerStats(String name) {
        this.name = name;
        SAVE_FILE = "save_" + name + ".json";
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            if (file.exists()) {
                String jsonData = file.readString();
                ObjectMap<String, Object> data = json.fromJson(ObjectMap.class, jsonData);

                Object nameObj = data.get("name");
                Object scoreObj = data.get("score");
                Object levelObj = data.get("level");
                Object expObj = data.get("exp");
                Object hpObj = data.get("hpLvl");
                Object speedObj = data.get("speedLvl");
                Object atkObj = data.get("atkLvl");

                name = nameObj instanceof String ? (String)nameObj : "Player";
                score = scoreObj instanceof Number ? ((Number)scoreObj).intValue() : 0;
                level = scoreObj instanceof Number ? ((Number) levelObj).intValue() : 1;
                exp = expObj instanceof Number ? ((Number)expObj).intValue() : 0;
                hpLvl = hpObj instanceof Number ? ((Number)hpObj).intValue() : 1;
                speedLvl = speedObj instanceof Number ? ((Number)speedObj).intValue() : 1;
                atkLvl = atkObj instanceof Number ? ((Number)atkObj).intValue() : 1;
            }
        } catch(Exception e) {
            System.err.println("Error loading stats for " + name + ": " + e.getMessage());
        }
    }

    public void save() {
        try {
            ObjectMap<String, Object> data = new ObjectMap<>();
            data.put("name", name);
            data.put("score", score);
            data.put("level", level);
            data.put("exp", exp);
            data.put("hpLvl", hpLvl);
            data.put("speedLvl", speedLvl);
            data.put("atkLvl", atkLvl);

            FileHandle file = Gdx.files.local(SAVE_FILE);
            file.writeString(json.prettyPrint(json.toJson(data)), false);
        } catch(Exception e) {
            System.err.println("Error saving stats for " + name + ": " + e.getMessage());
        }
    }

    public void addExp(int points) {
        exp += points;
        save();
    }

    public void addScore(int points) {
        score += points;
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

    public void incrementLvl() {
        level++;
        save();
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
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
            return true;
        }
        return false;
    }

    public boolean upgradeSpeed() {
        int cost = speedLvl * 2;
        if(spendExp(cost)) {
            speedLvl++;
            return true;
        }
        return false;
    }

    public boolean upgradeAtk() {
        int cost = atkLvl * 2;
        if(spendExp(cost)) {
            atkLvl++;
            return true;
        }
        return false;
    }
}