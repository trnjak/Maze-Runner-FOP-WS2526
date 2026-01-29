package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.fop.maze.screens.*;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import java.io.IOException;

/**
 * The MazeRunnerGame class serves as the main entry point and screen manager for the game.
 * It handles screen transitions, manages global resources, and coordinates the overall game flow.
 */
public class MazeRunnerGame extends Game {
    private final NativeFileChooser fileChooser;
    public float WIDTH = 1280, HEIGHT = 720;
    public Music menuMusic;
    public Image menuImage;
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private SpriteBatch spriteBatch;
    private Skin skin;

    /**
     * Constructs a new MazeRunnerGame instance with the specified file chooser.
     *
     * @param fileChooser The native file chooser implementation for map selection
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
    }

    /**
     * Initializes the game resources including SpriteBatch, UI skin, and background music.
     * Sets the initial screen to the BeginScreen.
     * <p>Music from: <a href="https://opengameart.org/content/mysterious-ambience-song21">Mysterious Ambience Song21</a>
     * <p>Background image from: <a href="https://opengameart.org/content/castle-in-the-dark">Castle in the Dark</a></p>
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_bg.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.2f);
        menuMusic.play();

        menuImage = new Image(new Texture("background.gif"));
        menuImage.setFillParent(true);
        menuImage.setPosition(0, 0);

        setScreen(new BeginScreen(this));
    }

    /**
     * Navigates to the main menu screen and disposes the current game screen.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this));
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }

    /**
     * Navigates to the leaderboard screen and disposes other screens.
     */
    public void goToLeaderboard() {
        setScreen(new LeaderboardScreen(this));
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    /**
     * Navigates to the achievements screen and pauses the current game if active.
     */
    public void goToAchievement() {
        setScreen(new AchievementScreen(this));
        if (gameScreen != null) {
            gameScreen.pause();
        }
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    /**
     * Navigates to the statistics screen and pauses the current game if active.
     */
    public void goToStats() {
        setScreen(new StatsScreen(this));
        if (gameScreen != null) {
            gameScreen.pause();
        }
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    /**
     * Navigates to the story screen and pauses the current game if active.
     */
    public void goToStory() {
        setScreen(new StoryScreen(this));
        if (gameScreen != null) {
            gameScreen.pause();
        }
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    /**
     * Returns to the previous screen based on navigation context.
     * Returns to the game screen if currently in-game, otherwise returns to the menu.
     */
    public void back() {
        if (gameScreen != null) {
            setScreen(gameScreen);
        } else {
            goToMenu();
        }
    }

    /**
     * Navigates to the settings screen and disposes the current game screen.
     */
    public void goToSettings() {
        this.setScreen(new SettingsScreen(this));
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }

    /**
     * Opens a file chooser to select a custom map file and starts a new game with it.
     * Stops menu music upon successful map selection.
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

    /**
     * Starts a new endless mode game session.
     * Stops menu music and disposes the menu screen.
     */
    public void goToEndlessGame() {
        gameScreen = new GameScreen(this);
        this.setScreen(gameScreen);
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
        menuMusic.stop();
    }

    /**
     * Disposes all game resources when the application closes.
     */
    @Override
    public void dispose() {
        getScreen().hide();
        getScreen().dispose();
        spriteBatch.dispose();
        skin.dispose();
    }

    /**
     * Gets the UI skin used throughout the game.
     *
     * @return The current Skin instance
     */
    public Skin getSkin() {
        return skin;
    }

    /**
     * Gets the main SpriteBatch for rendering.
     *
     * @return The global SpriteBatch instance
     */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
