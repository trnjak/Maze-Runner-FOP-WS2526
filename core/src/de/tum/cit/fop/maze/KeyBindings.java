package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

/**
 * The KeyBindings class handles key binds, as well as saving/loading them from a file.
 */
public class KeyBindings {
    public int UP = Input.Keys.UP;
    public int DOWN = Input.Keys.DOWN;
    public int LEFT = Input.Keys.LEFT;
    public int RIGHT = Input.Keys.RIGHT;
    public int ATTACK = Input.Keys.SPACE;
    public int SPRINT = Input.Keys.SHIFT_LEFT;

    private static final String PATH = "keybinds.json";

    /**
     * Saves the binds to a json file.
     */
    public void save() {
        Json json = new Json();
        FileHandle file = Gdx.files.local(PATH);
        file.writeString(json.prettyPrint(this), false);
    }

    /**
     * Loads the binds from a json file.
     */
    public static KeyBindings load() {
        FileHandle file = Gdx.files.local(PATH);
        if(!file.exists()) {
            KeyBindings kb = new KeyBindings();
            kb.save();
            return kb;
        }
        Json json = new Json();
        return json.fromJson(KeyBindings.class, file);
    }
}
