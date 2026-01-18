package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
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

    private MenuScreen menuScreen;
    private GameScreen gameScreen;

    private SpriteBatch spriteBatch;
    private Skin skin;

    private final NativeFileChooser fileChooser;

    public Music menuMusic;
    public Image menuImage;

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
     * Called when the game is created. Initializes the SpriteBatch, Skin and menu music.
     *
     * Music from: <a href="https://opengameart.org/content/fantasy-good-night">LINK</a>
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_bg.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.2f);
        menuMusic.play();

        menuImage = new Image(new Texture("background.png"));
        menuImage.setSize(WIDTH, HEIGHT);
        menuImage.setPosition(0, 0);

        setScreen(new BeginScreen(this));
    }

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this));
        if (gameScreen != null) {
            gameScreen.dispose();
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
     * Switches to the achievements screen
     */
    public void goToAchievement() {
        setScreen(new AchievementScreen(this));
        if(gameScreen != null) {
            gameScreen.pause();
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

    /**
     * Switches to the story screen.
     */
    public void goToStory() {
        setScreen(new StoryScreen(this));
        if(gameScreen != null) {
            gameScreen.pause();
        }
        if(menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    /**
     * Switches back to the game or menu screen depending on whether the player entered the screen
     * from the game or from the menu.
     */
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
     * Opens the native file picker to load the game map. Upon choosing, switches to the game screen.
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
     * Switches to endless game screen.
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
     * Cleans up resources when the game is disposed.
     */
    @Override
    public void dispose() {
        getScreen().hide();
        getScreen().dispose();
        spriteBatch.dispose();
        skin.dispose();
    }

    public Skin getSkin() {
        return skin;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
