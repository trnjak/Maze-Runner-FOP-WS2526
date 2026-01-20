package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

/**
 * The KeyBindings class manages customisable keyboard controls for player actions.
 * Handles persistence of key mappings to a JSON configuration file.
 */
public class KeyBindings {
    private static final String PATH = "keybinds.json";
    public int UP = Input.Keys.UP;
    public int DOWN = Input.Keys.DOWN;
    public int LEFT = Input.Keys.LEFT;
    public int RIGHT = Input.Keys.RIGHT;
    public int ATTACK = Input.Keys.SPACE;
    public int SPRINT = Input.Keys.SHIFT_LEFT;

    /**
     * Loads key bindings from the configuration file.
     * Creates a new file with default bindings if none exists.
     *
     * @return The loaded or newly created KeyBindings instance
     */
    public static KeyBindings load() {
        FileHandle file = Gdx.files.local(PATH);
        if (!file.exists()) {
            KeyBindings kb = new KeyBindings();
            kb.save();
            return kb;
        }
        Json json = new Json();
        return json.fromJson(KeyBindings.class, file);
    }

    /**
     * Saves the current key bindings to the configuration file in JSON format.
     */
    public void save() {
        Json json = new Json();
        FileHandle file = Gdx.files.local(PATH);
        file.writeString(json.prettyPrint(this), false);
    }
}
