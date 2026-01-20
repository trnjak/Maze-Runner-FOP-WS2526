package de.tum.cit.fop.maze;

/**
 * The Achievement class represents an in-game achievement that players can unlock.
 * Tracks progress towards specific goals and automatically unlocks when targets are met.
 */
public class Achievement {
    private final String id, name, description, type;
    private final int target;
    private boolean unlocked;
    private int progress;

    /**
     * Constructs a new Achievement with the specified parameters.
     *
     * @param id          The unique identifier for the achievement
     * @param name        The display name of the achievement
     * @param description A description of the achievement requirements
     * @param target      The total progress required to unlock the achievement
     * @param type        The category or type of the achievement
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

    /**
     * Gets the achievement's unique identifier.
     *
     * @return The achievement ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the achievement's display name.
     *
     * @return The achievement name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the achievement's description.
     *
     * @return The achievement description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if the achievement has been unlocked.
     *
     * @return true if the achievement is unlocked, false otherwise
     */
    public boolean isUnlocked() {
        return unlocked;
    }

    /**
     * Sets the unlocked state of the achievement.
     *
     * @param unlocked The new unlocked state
     */
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    /**
     * Gets the current progress towards the achievement.
     *
     * @return The current progress value
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets the current progress towards the achievement.
     *
     * @param progress The new progress value
     */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * Gets the target progress required to unlock the achievement.
     *
     * @return The target progress value
     */
    public int getTarget() {
        return target;
    }

    /**
     * Gets the achievement's type/category.
     *
     * @return The achievement type
     */
    public String getType() {
        return type;
    }

    /**
     * Adds progress towards the achievement and automatically unlocks if target is reached.
     * Progress is capped at the target value upon unlocking.
     *
     * @param amount The amount of progress to add (must be positive)
     */
    public void addProgress(int amount) {
        this.progress += amount;
        if (this.progress >= this.target) {
            this.unlocked = true;
            this.progress = this.target;
        }
    }

    /**
     * Gets a formatted progress string showing current progress relative to target.
     *
     * @return String in format "current/target"
     */
    public String getProgressText() {
        return progress + "/" + target;
    }
}