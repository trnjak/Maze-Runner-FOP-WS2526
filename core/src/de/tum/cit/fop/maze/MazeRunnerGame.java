package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import de.tum.cit.fop.maze.screens.*;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import java.io.IOException;

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

    public Music menuMusic;

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
        // music : https://opengameart.org/content/fantasy-good-night
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_bg.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.2f);
        menuMusic.play();

        setScreen(new BeginScreen(this));
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
        setScreen(new LeaderboardScreen(this));
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
    public void goToStats() {
        setScreen(new StatsScreen(this));
        if(gameScreen != null) {
            gameScreen.pause();
        }
        if(menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    public void backToGameOrMenuDependingOnWhetherOrNotThePlayerEnteredTheScreenFromTheGameOrFromTheMenu() {
        if(gameScreen != null) {
            setScreen(gameScreen);
        }
        else {
            goToMenu();
        }
    }


    /**
     * Switches to the settings screen.
     */
    public void goToSettings() {
        this.setScreen(new SettingsScreen(this));
        if (gameScreen != null) {
            gameScreen.dispose();
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
                try {
                    MazeRunnerGame.this.setScreen(new GameScreen(MazeRunnerGame.this, file.path()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (menuScreen != null) {
                    menuScreen.dispose();
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
        menuMusic.stop();
    }

    public void goToEndlessGame() {
        gameScreen = new GameScreen(this);
        this.setScreen(gameScreen); // Set the current screen to GameScreen
        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
        }
        menuMusic.stop();
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
