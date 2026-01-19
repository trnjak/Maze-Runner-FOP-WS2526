package de.tum.cit.fop.maze;

/**
 * The Achievement class represents an in-game achievement that players can unlock.
 * It tracks progress towards a specific target and automatically unlocks when the target is reached.
 */
public class Achievement {
    private String id, name, description, type;
    private boolean unlocked;
    private int progress, target;

    /**
     * Constructor for achievement.
     *
     * @param id          The identifier for the achievement.
     * @param name        The display name of the achievement.
     * @param description A description of the achievement requirements.
     * @param target      The total progress required to unlock the achievement.
     * @param type        The category or type of the achievement.
     */
    public Achievement(String id, String name, String description, int target, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.target = target;
        this.type = type;
        this.unlocked = false;
        this.progress = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Adds the specified amount to the current progress.
     * Automatically unlocks the achievement if the target is reached or exceeded.
     * Progress is capped at the target value upon unlocking.
     *
     * @param amount The amount to add to the current progress (must be positive).
     */
    public void addProgress(int amount) {
        this.progress += amount;
        if (this.progress >= this.target) {
            this.unlocked = true;
            this.progress = this.target;
        }
    }

    /**
     * Returns a formatted string representing the current progress towards the target.
     */
    public String getProgressText() {
        return progress + "/" + target;
    }
}