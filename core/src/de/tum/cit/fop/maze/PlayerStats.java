package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import de.tum.cit.fop.maze.objects.Player;

/**
 * The PlayerStats class manages persistent player data including experience, upgrades, and achievements.
 * Handles save file operations, stat progression, and achievement tracking.
 *
 * @see Player
 */
public class PlayerStats {
    private static final Json json = new Json();
    private static final Array<Achievement> DEFAULT_ACHIEVEMENTS = new Array<>() {{
        add(new Achievement("first_blood", "First Blood", "Kill your first enemy", 1, "kill"));
        add(new Achievement("enemy_slayer", "Enemy Slayer", "Kill 50 enemies", 50, "kill"));
        add(new Achievement("key_collector", "Key Collector", "Collect 20 keys", 20, "collect"));
        add(new Achievement("score_master", "Score Master", "Reach 5000 total score", 5000, "score"));
        add(new Achievement("survivor", "Survivor", "Complete 10 levels", 10, "level"));
        add(new Achievement("upgrade_master", "Upgrade Master", "Reach level 10 in all upgrades", 30, "upgrade"));
    }};
    private final String SAVE_FILE;
    private String name;
    private int exp = 0, score = 0;
    private int hpLvl = 1, speedLvl = 1, atkLvl = 1, level = 1;
    private ObjectMap<String, Achievement> achievements;

    /**
     * Creates a new PlayerStats instance for the specified player name.
     * Loads existing save data or initializes new statistics.
     *
     * @param name The player's name entered on the start screen
     */
    public PlayerStats(String name) {
        this.name = name;
        SAVE_FILE = "save_" + name + ".json";
        achievements = new ObjectMap<>();
        load();
        initAchievements();
    }

    /**
     * Loads player statistics from the associated save file.
     * Falls back to default values if the file doesn't exist or is corrupted.
     */
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

                name = nameObj instanceof String ? (String) nameObj : "Player";
                score = scoreObj instanceof Number ? ((Number) scoreObj).intValue() : 0;
                level = levelObj instanceof Number ? ((Number) levelObj).intValue() : 1;
                exp = expObj instanceof Number ? ((Number) expObj).intValue() : 0;
                hpLvl = hpObj instanceof Number ? ((Number) hpObj).intValue() : 1;
                speedLvl = speedObj instanceof Number ? ((Number) speedObj).intValue() : 1;
                atkLvl = atkObj instanceof Number ? ((Number) atkObj).intValue() : 1;

                Object achievementsObj = data.get("achievements");
                if (achievementsObj instanceof ObjectMap) {
                    achievements = (ObjectMap<String, Achievement>) achievementsObj;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading stats for " + name + ": " + e.getMessage());
        }
    }

    /**
     * Initialises default achievements if no achievements are loaded from the save file.
     */
    private void initAchievements() {
        if (achievements.size == 0) {
            for (Achievement ach : DEFAULT_ACHIEVEMENTS) {
                achievements.put(ach.getId(), ach);
            }
        }
    }

    /**
     * Saves the current player statistics to the associated save file.
     */
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
            data.put("achievements", achievements);

            FileHandle file = Gdx.files.local(SAVE_FILE);
            file.writeString(json.prettyPrint(json.toJson(data)), false);
        } catch (Exception e) {
            System.err.println("Error saving stats for " + name + ": " + e.getMessage());
        }
    }

    /**
     * Updates achievements of a specific type and returns newly unlocked achievement names.
     *
     * @param type   The achievement category to update (e.g., "kill", "collect", "score")
     * @param amount The amount of progress to add to matching achievements
     * @return Array of names for achievements that were just unlocked
     */
    public Array<String> updateAchievement(String type, int amount) {
        Array<String> newlyUnlocked = new Array<>();

        for (Achievement ach : achievements.values()) {
            if (ach.getType().equals(type) && !ach.isUnlocked()) {
                boolean wasUnlocked = ach.isUnlocked();
                ach.addProgress(amount);

                if (ach.isUnlocked() && !wasUnlocked) {
                    newlyUnlocked.add(ach.getName());
                }
            }
        }

        Achievement upgradeMaster = achievements.get("upgrade_master");
        if (upgradeMaster != null && !upgradeMaster.isUnlocked()) {
            int totalUpgrades = hpLvl + speedLvl + atkLvl;
            upgradeMaster.setProgress(totalUpgrades - 3);
            if (totalUpgrades >= 33) {
                if (!upgradeMaster.isUnlocked()) {
                    upgradeMaster.setUnlocked(true);
                    newlyUnlocked.add(upgradeMaster.getName());
                }
            }
        }

        if (newlyUnlocked.size > 0) {
            save();
        }

        return newlyUnlocked;
    }

    /**
     * Gets all achievements as an array.
     *
     * @return Array containing all achievements
     */
    public Array<Achievement> getAchievements() {
        Array<Achievement> result = new Array<>();
        for (Achievement ach : achievements.values()) {
            result.add(ach);
        }
        return result;
    }

    /**
     * Gets the number of unlocked achievements.
     *
     * @return Count of achievements that have been unlocked
     */
    public int getUnlockedAchievementsCount() {
        int count = 0;
        for (Achievement ach : achievements.values()) {
            if (ach.isUnlocked()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds experience points to the player.
     *
     * @param n The amount of experience to add
     */
    public void addExp(int n) {
        exp += n;
        save();
    }

    /**
     * Adds points to the player's score.
     *
     * @param n The amount to add to the score
     */
    public void addScore(int n) {
        score += n;
        save();
    }

    /**
     * Attempts to spend experience points for upgrades.
     *
     * @param cost The experience cost of the upgrade
     * @return true if the player had enough experience and the purchase succeeded, false otherwise
     */
    public boolean spendExp(int cost) {
        if (exp >= cost) {
            exp -= cost;
            save();
            return true;
        }
        return false;
    }

    /**
     * Upgrades the player's maximum health level.
     *
     * @return true if the upgrade was successful, false if insufficient experience
     */
    public boolean upgradeHp() {
        int cost = hpLvl * 2;
        if (spendExp(cost)) {
            hpLvl++;
            return true;
        }
        return false;
    }

    /**
     * Upgrades the player's movement speed level.
     *
     * @return true if the upgrade was successful, false if insufficient experience
     */
    public boolean upgradeSpeed() {
        int cost = speedLvl * 2;
        if (spendExp(cost)) {
            speedLvl++;
            return true;
        }
        return false;
    }

    /**
     * Upgrades the player's attack speed level.
     *
     * @return true if the upgrade was successful, false if insufficient experience
     */
    public boolean upgradeAtk() {
        int cost = atkLvl * 2;
        if (spendExp(cost)) {
            atkLvl++;
            return true;
        }
        return false;
    }

    /**
     * Gets the player's current movement speed multiplier.
     *
     * @return Movement speed (base 1.0 + 2% per speed level)
     */
    public float getSpeed() {
        return 1f + (speedLvl - 1) * 0.02f;
    }

    /**
     * Gets the player's current attack cooldown.
     *
     * @return Attack cooldown in seconds (minimum 0.01s)
     */
    public float getAttackCooldown() {
        return Math.max(0.01f, 0.5f - (atkLvl - 1) * 0.005f);
    }

    /**
     * Increments the player's level by one.
     */
    public void incrementLvl() {
        level++;
        save();
    }

    /**
     * Gets the player's maximum health points.
     *
     * @return Maximum health (base 4 + health level)
     */
    public int getMaxHp() {
        return 4 + hpLvl;
    }

    /**
     * Gets the player's name.
     *
     * @return The player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the player's total score.
     *
     * @return The current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the player's current level.
     *
     * @return The current level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the player's current experience points.
     *
     * @return The current experience
     */
    public int getExp() {
        return exp;
    }

    /**
     * Gets the player's health upgrade level.
     *
     * @return Current health level
     */
    public int getHpLvl() {
        return hpLvl;
    }

    /**
     * Gets the player's speed upgrade level.
     *
     * @return Current speed level
     */
    public int getSpeedLvl() {
        return speedLvl;
    }

    /**
     * Gets the player's attack upgrade level.
     *
     * @return Current attack level
     */
    public int getAtkLvl() {
        return atkLvl;
    }
}