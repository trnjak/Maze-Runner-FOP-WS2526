package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.fop.maze.screens.*;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class MazeRunnerGame extends Game {
    public float WIDTH = 1024, HEIGHT = 768;

    // Screens
    private MenuScreen menuScreen;
    private GameScreen gameScreen;

    // Sprite Batch for rendering
    private SpriteBatch spriteBatch;

    // UI Skin
    private Skin skin;

    private final NativeFileChooser fileChooser;

    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin

        // Play some background music
        // Background sound
        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        backgroundMusic.setLooping(true);
        //backgroundMusic.play();

        goToMenu(); // Navigate to the menu screen
    }

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this)); // Set the current screen to MenuScreen
        if (gameScreen != null) {
            gameScreen.dispose(); // Dispose the game screen if it exists
            gameScreen = null;
        }
    }

    /**
     * Switches to the leaderboard screen.
     */
    public void goToLeaderboard() {
        //TODO: implement leaderboards
        if(gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if(menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    /**
     * Switches to the stats screen.
     */
    public void goToUpgrades() {
        setScreen(new UpgradeScreen(this));
        if(gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if(menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }


    /**
     * Switches to the settings screen.
     */
    public void goToSettings() {
        this.setScreen(new SettingsScreen(this)); // Set the current screen to MenuScreen
        if (gameScreen != null) {
            gameScreen.dispose(); // Dispose the game screen if it exists
            gameScreen = null;
        }
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        NativeFileChooserConfiguration nfconf = new NativeFileChooserConfiguration();
        nfconf.title = "Select Map File";
        nfconf.directory = Gdx.files.absolute("maps/");
        fileChooser.chooseFile(nfconf, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle file) {
                MazeRunnerGame.this.setScreen(new GameScreen(MazeRunnerGame.this, file.path())); // Set the current screen to GameScreen
                if (menuScreen != null) {
                    menuScreen.dispose(); // Dispose the menu screen if it exists
                    menuScreen = null;
                }
            }
            @Override
            public void onCancellation() {
            }
            @Override
            public void onError(Exception e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public void goToEndlessGame() {
        this.setScreen(new GameScreen(this)); // Set the current screen to GameScreen
        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
        }
    }

    /**
     * Cleans up resources when the game is disposed.
     */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose the skin
    }

    // Getter methods
    public Skin getSkin() {
        return skin;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
