package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.KeyBindings;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.PlayerStats;
import de.tum.cit.fop.maze.objects.Exit;
import de.tum.cit.fop.maze.objects.GameObj;
import de.tum.cit.fop.maze.objects.Key;
import de.tum.cit.fop.maze.objects.Player;
import de.tum.cit.fop.maze.objects.enemies.Enemy;
import de.tum.cit.fop.maze.objects.powerups.Powerup;
import de.tum.cit.fop.maze.objects.traps.Trap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * The GameScreen class is the main gameplay screen handling map loading, player interaction,
 * enemy AI, collision detection, scoring, and developer console functionality.
 * <p>Hud textures from: <a href="https://kenney.nl/assets/micro-roguelike">Micro Roguelike</a>
 * <p>Music from: <a href="https://opengameart.org/content/fantasy-good-night">Fantasy Good Night</a>
 * <p>SFX from: <a href="https://kenney.nl/assets/category:Audio">Kenney Audio</a> and
 * <a href="https://opengameart.org/content/witch-cackle">Witch Cackle</a>
 */
public class GameScreen implements Screen {
    private static final Texture TILE_SHEET = new Texture("hud_tilemap.png");
    public static final TextureRegion[][] TEXTURE_REGION = TextureRegion.split(TILE_SHEET, TILE_SHEET.getWidth() / 16, TILE_SHEET.getHeight() / 10);
    private final MazeRunnerGame game;
    private final OrthographicCamera camera, hudCam;
    private final Viewport viewport, hudVp;
    private final GameMap map;
    private final Player player;
    private final PlayerStats playerStats;
    private final KeyBindings binds = KeyBindings.load();
    private final ShapeRenderer sr = new ShapeRenderer();
    private final Map<String, Object> consoleVariables = new HashMap<>();
    private final Music gameMusic = Gdx.audio.newMusic(Gdx.files.internal("game_bg.mp3"));
    private final Sound scored = Gdx.audio.newSound(Gdx.files.internal("sounds/scored.ogg")),
            won = Gdx.audio.newSound(Gdx.files.internal("sounds/win.ogg")),
            lost = Gdx.audio.newSound(Gdx.files.internal("sounds/lose.ogg"));
    private boolean paused = false, gameOver = false, victory = false;
    private float score = 0, time = 300;
    private int maxHearts;
    private Stage pauseStage, gameOverStage, victoryStage, consoleStage, hudStage;
    private Table pauseTable, gameOverTable, victoryTable, consoleTable;
    private Label gameOverScore, victoryScore, consoleOutputLabel, scoreLabel, timeLabel;
    private TextField consoleTextField;
    private ScrollPane consoleScrollPane;
    private boolean consoleOpen = false;
    private String consoleOutput = "";
    private boolean wonPlayed = false, lostPlayed = false;

    private Label achievementLabel;
    private float achievementTimer = 0, endlessTimer = 0;

    /**
     * Constructs a new GameScreen with a specified map file.
     *
     * @param game The main MazeRunnerGame instance
     * @param path The file path to the map properties file
     * @throws IOException If the map file cannot be loaded
     */
    public GameScreen(MazeRunnerGame game, String path) throws IOException {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(game.WIDTH, game.HEIGHT, camera);

        hudCam = new OrthographicCamera();
        hudVp = new FitViewport(game.WIDTH, game.HEIGHT, hudCam);

        map = new GameMap();
        map.load(path);

        player = new Player(map.getEx() * GameObj.TILE, map.getEy() * GameObj.TILE);
        playerStats = BeginScreen.STATS;
        maxHearts = player.getMaxHealth();

        gameMusic.setLooping(true);
        gameMusic.setVolume(0.2f);
        gameMusic.play();

        initPauseMenu();
        initGameOverMenu();
        initVictoryMenu();
        initDeveloperConsole();
        initHud();
    }

    /**
     * Constructs a new GameScreen with a procedurally generated endless map.
     *
     * @param game The main MazeRunnerGame instance
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(game.WIDTH, game.HEIGHT, camera);

        hudCam = new OrthographicCamera();
        hudVp = new FitViewport(game.WIDTH, game.HEIGHT, hudCam);

        map = new GameMap();

        try {
            map.generateMap();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        player = new Player(map.getEx() * GameObj.TILE, map.getEy() * GameObj.TILE);
        playerStats = BeginScreen.STATS;
        maxHearts = player.getMaxHealth();

        gameMusic.setLooping(true);
        gameMusic.setVolume(0.2f);
        gameMusic.play();

        initPauseMenu();
        initGameOverMenu();
        initVictoryMenu();
        initDeveloperConsole();
        initHud();
    }

    /**
     * Main game loop that updates game logic, handles input, renders graphics and UI.
     *
     * @param delta The time in seconds since the last render
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.255f, 0.286f, 0.349f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        globalInput();

        if (!paused && !gameOver && !victory && !consoleOpen) {
            time -= delta;

            player.update(delta);
            playerInput(delta);

            for (Enemy e : map.getEnemies()) {
                e.update(delta, player, map);
                if (!player.isAlive()) {
                    gameOver = true;
                    updateGameOverScore();
                }
            }

            for (Trap t : map.getTraps()) {
                t.update(player, delta);
                if (!player.isAlive()) {
                    gameOver = true;
                    updateGameOverScore();
                }
            }

            for (Iterator<Key> it = map.getKeys().iterator(); it.hasNext(); ) {
                Key key = it.next();
                if (player.getBounds().overlaps(key.getBounds())) {
                    player.collectKey();
                    if (player.getKeys() == 1) {
                        Sound open = Gdx.audio.newSound(Gdx.files.internal("sounds/door_open.ogg"));
                        open.play(0.2f);
                    }
                    score += 100;
                    scored.play(0.1f);

                    Array<String> unlocked = playerStats.updateAchievement("collect", 1);
                    if (unlocked.size > 0) {
                        showAchievement(unlocked.first());
                    }

                    it.remove();
                }
            }

            for (Iterator<Powerup> it = map.getPowerups().iterator(); it.hasNext(); ) {
                Powerup p = it.next();
                if (player.getBounds().overlaps(p.getBounds())) {
                    p.update(player, delta);
                    score += 50;
                    scored.play(0.1f);
                    it.remove();
                }
            }

            Exit e = map.getExit();
            if (e != null) {
                e.update(delta);
                if (player.getBounds().overlaps(e.getBounds()) && player.getKeys() > 0) {
                    victory = true;
                    updateVictoryScore();
                }
                if (player.getKeys() > 0) {
                    e.open();
                }
            }

            if (map.isEndless()) {
                endlessTimer += delta;
                if (endlessTimer >= 30) {
                    Random r = new Random();
                    int newX, newY;
                    do {
                        newX = r.nextInt(map.getW() - 1);
                        newY = r.nextInt(map.getH() - 1);
                    } while (map.isWall(newX, newY));
                    map.getExit().setX(newX);
                    map.getExit().setY(newY);
                    Sound witch = Gdx.audio.newSound(Gdx.files.internal("sounds/witch_cackle.ogg"));
                    witch.play(0.4f);
                    endlessTimer = 0;
                }
            }

            centerCameraOnPlayer();
        }

        if (time <= 0) {
            gameOver = true;
            updateGameOverScore();
        }

        viewport.apply();
        camera.update();
        game.getSpriteBatch().setProjectionMatrix(camera.combined);

        game.getSpriteBatch().begin();
        map.render(game.getSpriteBatch());
        player.render(game.getSpriteBatch());
        for (Enemy enemy : map.getEnemies()) {
            enemy.render(game.getSpriteBatch());
        }
        game.getSpriteBatch().end();

        hudVp.apply();
        hudCam.update();
        game.getSpriteBatch().setProjectionMatrix(hudCam.combined);

        game.getSpriteBatch().begin();
        int currentLives = player.getHp();
        float heartX = 20, heartY = hudVp.getWorldHeight() - 40;
        for (int i = 0; i < maxHearts; i++) {
            if (i < currentLives) {
                game.getSpriteBatch().draw(TEXTURE_REGION[6][6], heartX + i * GameObj.TILE, heartY, GameObj.TILE, GameObj.TILE);
            } else {
                game.getSpriteBatch().draw(TEXTURE_REGION[6][4], heartX + i * GameObj.TILE, heartY, GameObj.TILE, GameObj.TILE);
            }
        }
        if (currentLives > maxHearts) {
            for (int i = maxHearts; i < currentLives; i++) {
                game.getSpriteBatch().draw(TEXTURE_REGION[6][6], heartX + i * GameObj.TILE, heartY, GameObj.TILE, GameObj.TILE);
            }
            maxHearts = currentLives;
        }
        if (player.getKeys() < 1) {
            game.getSpriteBatch().draw(TEXTURE_REGION[7][4], hudVp.getWorldWidth() - GameObj.TILE - 20, hudVp.getWorldHeight() - 40, GameObj.TILE, GameObj.TILE);
        } else {
            game.getSpriteBatch().draw(TEXTURE_REGION[5][10], hudVp.getWorldWidth() - GameObj.TILE - 20, hudVp.getWorldHeight() - 40, GameObj.TILE, GameObj.TILE);
        }
        Exit exit = map.getExit();
        if (exit != null) {
            float arrowRotation = computeArrow(player.getX(), player.getY(), exit.getX() * GameObj.TILE, exit.getY() * GameObj.TILE);
            if (arrowRotation >= 0) {
                float arrowX = 20, arrowY = game.HEIGHT - 80;
                float originX = (float) GameObj.TILE / 2, originY = (float) GameObj.TILE / 2;
                game.getSpriteBatch().draw(TEXTURE_REGION[6][3], arrowX, arrowY, originX, originY, GameObj.TILE, GameObj.TILE, 1, 1, arrowRotation);
            }
        }

        if (achievementTimer > 0) {
            achievementTimer -= delta;
            float alpha = Math.min(achievementTimer, 1.0f);
            achievementLabel.setColor(1, 1, 1, alpha);
            achievementLabel.draw(game.getSpriteBatch(), 1);
        }

        game.getSpriteBatch().end();

        scoreLabel.setText((int) score + "");
        timeLabel.setText((int) time + "");

        scoreLabel.setPosition(hudVp.getWorldWidth() / 2 - scoreLabel.getPrefWidth() / 2, hudVp.getWorldHeight() - 100);
        timeLabel.setPosition(hudVp.getWorldWidth() / 2 - timeLabel.getPrefWidth() / 2, hudVp.getWorldHeight() - 120);

        hudStage.act(delta);
        hudStage.draw();

        if (consoleOpen) {
            consoleStage.getViewport().apply();
            consoleTable.setVisible(true);
            consoleStage.act(delta);
            consoleStage.draw();
        } else if (paused && !gameOver && !victory) {
            pauseStage.getViewport().apply();
            pauseTable.setVisible(true);
            pauseStage.act(delta);
            pauseStage.draw();
        } else if (gameOver) {
            if (!lostPlayed) {
                lost.play(0.2f);
                gameMusic.stop();
                game.menuMusic.play();
                lostPlayed = true;
            }
            gameOverStage.getViewport().apply();
            gameOverTable.setVisible(true);
            gameOverStage.act(delta);
            gameOverStage.draw();
        } else if (victory) {
            if (!wonPlayed) {
                won.play(0.2f);
                gameMusic.stop();
                game.menuMusic.play();
                wonPlayed = true;
            }
            victoryStage.getViewport().apply();
            victoryTable.setVisible(true);
            victoryStage.act(delta);
            victoryStage.draw();
        } else {
            consoleTable.setVisible(false);
            pauseTable.setVisible(false);
            gameOverTable.setVisible(false);
            victoryTable.setVisible(false);
        }
    }

    /**
     * Displays an achievement notification temporarily on screen.
     *
     * @param name The name of the unlocked achievement
     */
    private void showAchievement(String name) {
        achievementLabel = new Label("Achievement Unlocked: " + name, game.getSkin(), "title");
        achievementLabel.setFontScale(0.5f);
        achievementLabel.setPosition(hudVp.getWorldWidth() / 2 - achievementLabel.getPrefWidth() / 2, 100);
        achievementTimer = 3.0f;
    }

    /**
     * Initializes the HUD elements including score and time displays.
     */
    private void initHud() {
        hudStage = new Stage(hudVp, game.getSpriteBatch());

        scoreLabel = new Label((int) score + "", game.getSkin(), "title");
        timeLabel = new Label((int) time + "", game.getSkin());

        scoreLabel.setFontScale(0.5f);

        scoreLabel.setPosition(hudVp.getWorldWidth() / 2 - scoreLabel.getPrefWidth() / 2, hudVp.getWorldHeight() - 100);
        timeLabel.setPosition(hudVp.getWorldWidth() / 2 - timeLabel.getPrefWidth() / 2, hudVp.getWorldHeight() - 120);

        hudStage.addActor(scoreLabel);
        hudStage.addActor(timeLabel);
    }

    /**
     * Initializes the pause menu UI with navigation options.
     */
    private void initPauseMenu() {
        pauseStage = new Stage(hudVp, game.getSpriteBatch());
        pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.setVisible(false);

        Label title = new Label("PAUSED", game.getSkin(), "title");
        pauseTable.add(title).padBottom(80).row();

        String[] menuItems = {"Continue", "New Game", "Load Map", "Main Menu"};
        for (String item : menuItems) {
            TextButton button = new TextButton(item, game.getSkin());
            pauseTable.add(button).width(320).padBottom(20).row();

            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    handlePauseMenuSelection(item);
                }
            });
        }

        pauseStage.addActor(pauseTable);
    }

    /**
     * Handles selection of options from the pause menu.
     *
     * @param item The selected menu item
     */
    private void handlePauseMenuSelection(String item) {
        switch (item) {
            case "Continue":
                paused = false;
                Gdx.input.setInputProcessor(null);
                game.menuMusic.stop();
                gameMusic.play();
                break;
            case "New Game":
                resetLevelScore();
                game.menuMusic.stop();
                game.goToEndlessGame();
                break;
            case "Load Map":
                resetLevelScore();
                game.menuMusic.stop();
                game.goToGame();
                break;
            case "Main Menu":
                gameMusic.stop();
                game.goToMenu();
                game.menuMusic.play();
                break;
        }
    }

    /**
     * Initializes common UI elements for game over and victory screens.
     *
     * @param title The title to display on the screen
     * @param s     The stage to add the table to
     * @param t     The table containing the UI elements
     * @param sc    The label to display the score
     */
    private void initMenuStep2(String title, Stage s, Table t, Label sc) {
        Label la = new Label(title, game.getSkin(), "title");
        t.add(la).padBottom(80).row();

        t.add(sc).padBottom(20).row();

        TextButton menuButton = new TextButton("Main Menu", game.getSkin());
        t.add(menuButton).width(320).padBottom(20).row();

        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameMusic.stop();
                game.goToMenu();
                game.menuMusic.play();
            }
        });

        s.addActor(t);
    }

    /**
     * Initializes the game over screen UI.
     */
    private void initGameOverMenu() {
        gameOverStage = new Stage(hudVp, game.getSpriteBatch());
        gameOverTable = new Table();
        gameOverTable.setFillParent(true);
        gameOverTable.setVisible(false);

        gameOverScore = new Label("", game.getSkin());
        initMenuStep2("YOU DIED!", gameOverStage, gameOverTable, gameOverScore);
    }

    /**
     * Initializes the victory screen UI with additional progression options.
     */
    private void initVictoryMenu() {
        victoryStage = new Stage(hudVp, game.getSpriteBatch());
        victoryTable = new Table();
        victoryTable.setFillParent(true);
        victoryTable.setVisible(false);

        victoryScore = new Label("", game.getSkin());
        initMenuStep2("YOU WON!", victoryStage, victoryTable, victoryScore);

        TextButton newMapButton = new TextButton("Next Level", game.getSkin());
        victoryTable.add(newMapButton).width(320).padBottom(20).row();

        newMapButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    playerStats.incrementLvl();
                    map.generateMap();
                    player.setX(map.getEx() * GameObj.TILE);
                    player.setY(map.getEy() * GameObj.TILE);
                    player.setKeys(0);

                    wonPlayed = false;
                    lostPlayed = false;

                    victory = false;
                    paused = false;
                    time = 300;
                    endlessTimer = 0;

                    game.menuMusic.stop();
                    gameMusic.play();

                    resetLevelScore();

                    maxHearts = player.getMaxHealth();
                    timeLabel.setText((int) time + "");

                    centerCameraOnPlayer();
                    Gdx.input.setInputProcessor(null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        TextButton upgradesButton = new TextButton("Upgrades", game.getSkin());
        victoryTable.add(upgradesButton).width(320).padBottom(20).row();

        upgradesButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToStats();
            }
        });
    }

    /**
     * Initializes the developer console for debugging and cheat commands.
     */
    private void initDeveloperConsole() {
        consoleStage = new Stage(hudVp, game.getSpriteBatch());

        consoleTable = new Table();
        consoleTable.setFillParent(true);
        consoleTable.setVisible(false);
        consoleTable.top().padTop(100);

        consoleOutputLabel = new Label("", game.getSkin());
        consoleOutputLabel.setWrap(true);
        consoleOutputLabel.setAlignment(Align.topLeft);

        consoleScrollPane = new ScrollPane(consoleOutputLabel, game.getSkin());
        consoleScrollPane.setFadeScrollBars(false);
        consoleScrollPane.setScrollingDisabled(true, false);
        consoleScrollPane.setForceScroll(false, true);

        consoleTable.add(consoleScrollPane).width(800).height(400).padBottom(10).row();

        consoleTextField = new TextField("", game.getSkin());
        consoleTextField.setMessageText("Enter command...");
        consoleTextField.setTextFieldListener((textField, c) -> {
            if (c == '\r' || c == '\n') {
                processConsoleCommand(textField.getText());
                textField.setText("");
            }
        });

        consoleTable.add(consoleTextField).width(800).padTop(10);

        consoleStage.addActor(consoleTable);

        initializeConsoleVariables();
    }

    /**
     * Appends text to the console output display with scroll management.
     *
     * @param text The text to append to the console output
     */
    private void appendConsoleOutput(String text) {
        consoleOutput += text;

        if (consoleOutput.length() > 5000) {
            consoleOutput = consoleOutput.substring(consoleOutput.length() - 5000);
            int firstNewline = consoleOutput.indexOf('\n');
            if (firstNewline != -1) {
                consoleOutput = consoleOutput.substring(firstNewline);
            }
        }

        consoleOutputLabel.setText(consoleOutput);

        consoleScrollPane.layout();
        consoleScrollPane.setScrollPercentY(1);
    }

    /**
     * Initializes default console variables with current game state values.
     */
    private void initializeConsoleVariables() {
        consoleVariables.put("health", player.getHp());
        consoleVariables.put("speed", player.getCurrentSpeed());
        consoleVariables.put("keys", player.getKeys());
        consoleVariables.put("score", score);
        consoleVariables.put("time", time);
        consoleVariables.put("paused", paused);

        consoleOutput = "Console. Type 'help' for list of commands.";
        consoleOutputLabel.setText(consoleOutput);
    }

    /**
     * Resets the level score to zero.
     */
    private void resetLevelScore() {
        score = 0;
        scoreLabel.setText(0 + "");
    }

    /**
     * Updates the game over screen with final calculated score and awards experience.
     */
    private void updateGameOverScore() {
        int finalScore = (int) (score + time);
        gameOverScore.setText("Final score: " + finalScore);
        awardExp();
        playerStats.addScore(finalScore);

        Array<String> unlocked = playerStats.checkScoreAchievements();
        if (unlocked.size > 0) {
            showAchievement(unlocked.first());
        }

        playerStats.save();
    }

    /**
     * Updates the victory screen with final calculated score and awards experience.
     */
    private void updateVictoryScore() {
        int finalScore = (int) (score + time);
        victoryScore.setText("Final score: " + finalScore);
        awardExp();
        playerStats.addScore(finalScore);

        Array<String> unlocked = playerStats.checkScoreAchievements();
        if (unlocked.size > 0) {
            showAchievement(unlocked.first());
        }

        Array<String> levelUnlocked = playerStats.checkLevelAchievements();
        if (levelUnlocked.size > 0) {
            showAchievement(levelUnlocked.first());
        }

        playerStats.save();
    }

    /**
     * Processes console commands entered by the user.
     *
     * @param command The console command string to process
     */
    private void processConsoleCommand(String command) {
        if (command.trim().isEmpty()) return;

        String[] parts = command.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();

        try {
            switch (cmd) {
                case "help", "man", "?":
                    showHelp();
                    break;
                case "set":
                    handleSetCommand(parts);
                    break;
                case "get":
                    handleGetCommand(parts);
                    break;
                case "give":
                    handleGiveCommand(parts);
                    break;
                case "kill":
                    handleKillCommand(parts);
                    break;
                case "heal":
                    handleHealCommand(parts);
                    break;
                case "teleport", "tp":
                    handleTeleportCommand(parts);
                    break;
                case "list", "ls":
                    handleListCommand();
                    break;
                case "clear", "cls":
                    consoleOutput = "";
                    consoleOutputLabel.setText(consoleOutput);
                    break;
                case "exit":
                    consoleOpen = false;
                    Gdx.input.setInputProcessor(null);
                    break;
                default:
                    appendConsoleOutput("\nUnknown command: " + cmd + ". Type 'help' for available commands.");
            }
        } catch (Exception e) {
            appendConsoleOutput("\nError executing command: " + e.getMessage());
        }
    }

    /**
     * Displays help text with available console commands.
     */
    private void showHelp() {
        String helpText = """
                
                Available Commands:
                
                help - Show this help message
                set [variable] [value] - Set a game variable
                get [variable] - Get a variable's value
                give [item] [amount] - Give items to player
                kill [all|enemies] - Kill enemies
                heal [amount] - Heal player
                teleport [x] [y] - Teleport player
                list - List all variables
                clear - Clear console output
                exit - Close console
                
                """;

        appendConsoleOutput(helpText);
    }

    /**
     * Handles the 'set' console command for modifying game variables.
     *
     * @param parts The command parts split by whitespace
     */
    private void handleSetCommand(String[] parts) {
        if (parts.length < 3) {
            appendConsoleOutput("\nUsage: set [variable] [value]");
            return;
        }

        String varName = parts[1];
        String valueStr = parts[2];

        try {
            try {
                int intValue = Integer.parseInt(valueStr);
                consoleVariables.put(varName, intValue);
                applyVariableChange(varName, intValue);
                appendConsoleOutput("\nSet " + varName + " = " + intValue);
            } catch (NumberFormatException e) {
                try {
                    float floatValue = Float.parseFloat(valueStr);
                    consoleVariables.put(varName, floatValue);
                    applyVariableChange(varName, floatValue);
                    appendConsoleOutput("\nSet " + varName + " = " + floatValue);
                } catch (NumberFormatException e2) {
                    if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
                        boolean boolValue = Boolean.parseBoolean(valueStr);
                        consoleVariables.put(varName, boolValue);
                        applyVariableChange(varName, boolValue);
                        appendConsoleOutput("\nSet " + varName + " = " + boolValue);
                    } else {
                        consoleVariables.put(varName, valueStr);
                        appendConsoleOutput("\nSet " + varName + " = \"" + valueStr + "\"");
                    }
                }
            }
        } catch (Exception e) {
            appendConsoleOutput("\nError setting variable: " + e.getMessage());
        }
    }

    /**
     * Handles the 'get' console command for retrieving variable values.
     *
     * @param parts The command parts split by whitespace
     */
    private void handleGetCommand(String[] parts) {
        if (parts.length < 2) {
            appendConsoleOutput("\nUsage: get [variable]");
            return;
        }

        String varName = parts[1];
        Object value = consoleVariables.get(varName);

        if (value != null) {
            appendConsoleOutput("\n" + varName + " = " + value);
        } else {
            appendConsoleOutput("\nVariable not found: " + varName);
        }
    }

    /**
     * Handles the 'give' console command for granting items to the player.
     *
     * @param parts The command parts split by whitespace
     */
    private void handleGiveCommand(String[] parts) {
        if (parts.length < 2) {
            appendConsoleOutput("\nUsage: give [item] [amount]");
            return;
        }

        String item = parts[1].toLowerCase();
        int amount = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;

        switch (item) {
            case "key", "keys":
                for (int i = 0; i < amount; i++) {
                    player.collectKey();
                }
                consoleVariables.put("keys", player.getKeys());
                appendConsoleOutput("\nGiven " + amount + " key(s) to player");
                break;
            case "health", "hp":
                if (amount > maxHearts) {
                    maxHearts = amount;
                }
                player.setHp(player.getHp() + amount);
                consoleVariables.put("health", player.getHp());
                appendConsoleOutput("\nGiven " + amount + " health to player");
                break;
            case "score":
                score += amount;
                consoleVariables.put("score", score);
                appendConsoleOutput("\nAdded " + amount + " to score");
                break;
            case "time":
                time += amount;
                consoleVariables.put("time", time);
                appendConsoleOutput("\nAdded " + amount + " seconds to timer");
                break;
            default:
                appendConsoleOutput("\nUnknown item: " + item);
        }
    }

    /**
     * Handles the 'kill' console command for eliminating enemies.
     *
     * @param parts The command parts split by whitespace
     */
    private void handleKillCommand(String[] parts) {
        if (parts.length > 1 && parts[1].equalsIgnoreCase("all")) {
            int count = 0;
            for (Enemy enemy : map.getEnemies()) {
                if (enemy.isAlive()) {
                    enemy.takeDamage(1234567890);
                    count++;
                }
            }
            for (Enemy e : map.getEnemies().stream().filter(e -> !e.isAlive()).toList()) {
                if (!e.isAlive()) {
                    map.getEnemies().remove(e);
                }
            }
            appendConsoleOutput("\nKilled " + count + " enemies");
        } else {
            player.attack(map.getEnemies());
            appendConsoleOutput("\nAttacked enemies in range");
        }
    }

    /**
     * Handles the 'heal' console command for restoring player health.
     *
     * @param parts The command parts split by whitespace
     */
    private void handleHealCommand(String[] parts) {
        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : playerStats.getMaxHp();
        player.setHp(Math.min(playerStats.getMaxHp(), player.getHp() + amount));
        consoleVariables.put("health", player.getHp());
        appendConsoleOutput("\nHealed player to " + player.getHp() + " HP");
    }

    /**
     * Handles the 'teleport' console command for moving the player to specified coordinates.
     *
     * @param parts The command parts split by whitespace
     */
    private void handleTeleportCommand(String[] parts) {
        if (parts.length < 3) {
            appendConsoleOutput("\nUsage: teleport [x] [y]");
            return;
        }

        try {
            float x = Float.parseFloat(parts[1]) * GameObj.TILE;
            float y = Float.parseFloat(parts[2]) * GameObj.TILE;

            Rectangle testRect = new Rectangle(x, y, GameObj.TILE, GameObj.TILE);
            if (!map.collidesWithWall(testRect)) {
                player.move(x - player.getX(), y - player.getY(), map);
                centerCameraOnPlayer();
                appendConsoleOutput("\nTeleported player to (" + parts[1] + ", " + parts[2] + ")");
            } else {
                appendConsoleOutput("\nCannot teleport to wall at (" + parts[1] + ", " + parts[2] + ")");
            }
        } catch (NumberFormatException e) {
            appendConsoleOutput("\nInvalid coordinates");
        }
    }

    /**
     * Handles the 'list' console command for displaying all console variables.
     */
    private void handleListCommand() {
        StringBuilder listOutput = new StringBuilder("\nGame Variables:");
        for (Map.Entry<String, Object> entry : consoleVariables.entrySet()) {
            listOutput.append("\n").append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        appendConsoleOutput(listOutput.toString());
    }

    /**
     * Applies changes from console variable modifications to the actual game state.
     *
     * @param varName The name of the variable being changed
     * @param value   The new value to apply
     */
    private void applyVariableChange(String varName, Object value) {
        switch (varName) {
            case "health":
                player.setHp((Integer) value);
                break;
            case "speed", "keys":
                break;
            case "score":
                score = ((Number) value).floatValue();
                break;
            case "time":
                time = ((Number) value).floatValue();
                break;
            case "paused":
                paused = (Boolean) value;
                if (paused) {
                    Gdx.input.setInputProcessor(pauseStage);
                } else {
                    Gdx.input.setInputProcessor(null);
                }
                break;
        }
    }

    /**
     * Awards experience points to the player based on their score and saves the updated stats.
     */
    private void awardExp() {
        int plusExp = (int) Math.ceil(score / 500);
        if (plusExp > 0) {
            playerStats.addExp(plusExp);
            playerStats.save();
        }
    }

    /**
     * Handles global input events like opening/closing the console, pausing, and camera zoom.
     */
    private void globalInput() {
        if (!gameOver && !victory && Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            consoleOpen = !consoleOpen;
            if (consoleOpen) {
                Gdx.input.setInputProcessor(consoleStage);
                consoleTextField.setFocusTraversal(true);
                consoleStage.setKeyboardFocus(consoleTextField);
            } else {
                Gdx.input.setInputProcessor(null);
            }
        }

        if (!consoleOpen && !gameOver && !victory && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            if (paused) {
                Gdx.input.setInputProcessor(pauseStage);
                gameMusic.stop();
                game.menuMusic.play();
            } else {
                Gdx.input.setInputProcessor(null);
                game.menuMusic.stop();
                gameMusic.play();
            }
        }

        if (!consoleOpen && gameOver && Gdx.input.getInputProcessor() != gameOverStage) {
            Gdx.input.setInputProcessor(gameOverStage);
        } else if (!consoleOpen && victory && Gdx.input.getInputProcessor() != victoryStage) {
            Gdx.input.setInputProcessor(victoryStage);
        }

        if (!consoleOpen && !paused && !gameOver && !victory) {
            if (Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS))
                camera.zoom = Math.max(0.5f, camera.zoom - 0.02f);
            if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) camera.zoom = Math.min(2.0f, camera.zoom + 0.02f);
        }
    }

    /**
     * Centers the camera on the player's current position.
     */
    private void centerCameraOnPlayer() {
        camera.position.set(player.getX() + GameObj.TILE / 2f, player.getY() + GameObj.TILE / 2f, 0);
        camera.update();
    }

    /**
     * Processes player input for movement and attacks based on key bindings.
     *
     * @param delta The time in seconds since the last frame
     */
    private void playerInput(float delta) {
        float baseSpeed = Gdx.input.isKeyPressed(binds.SPRINT) ? 168f : 100f;
        float speed = baseSpeed * player.getCurrentSpeed();
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(binds.LEFT)) dx -= speed * delta;
        if (Gdx.input.isKeyPressed(binds.RIGHT)) dx += speed * delta;
        if (Gdx.input.isKeyPressed(binds.UP)) dy += speed * delta;
        if (Gdx.input.isKeyPressed(binds.DOWN)) dy -= speed * delta;

        if (Gdx.input.isKeyJustPressed(binds.ATTACK)) {
            player.attack(map.getEnemies());
            int killed = 0;
            for (Enemy e : map.getEnemies().stream().filter(e -> !e.isAlive()).toList()) {
                if (!e.isAlive()) {
                    score += 100;
                    killed++;
                    scored.play(0.1f);
                    map.getEnemies().remove(e);
                }
            }

            if (killed > 0) {
                Array<String> unlocked = playerStats.updateAchievement("kill", killed);
                if (unlocked.size > 0) {
                    showAchievement(unlocked.first());
                }
            }
        }

        if (dx == 0 && dy == 0) return;

        Rectangle next = new Rectangle(player.getX() + dx, player.getY() + dy, GameObj.TILE, GameObj.TILE);
        if (!enemyOverlap(next)) {
            player.move(dx, dy, map);
        }
    }

    /**
     * Checks if a rectangle overlaps with any living enemies.
     *
     * @param rect The rectangle to check for overlap
     * @return true if the rectangle overlaps with any living enemy, false otherwise
     */
    private boolean enemyOverlap(Rectangle rect) {
        for (Enemy e : map.getEnemies()) {
            if (!e.isAlive()) continue;
            if (rect.overlaps(e.getBounds())) return true;
        }
        return false;
    }

    /**
     * Computes the rotation angle for the exit arrow indicator.
     *
     * @param px The player's X position
     * @param py The player's Y position
     * @param ex The exit's X position
     * @param ey The exit's Y position
     * @return Rotation angle in degrees, or -1 if the exit is too close
     */
    private float computeArrow(float px, float py, float ex, float ey) {
        float dx = ex - px;
        float dy = ey - py;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        if (d <= 8) return -1;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;
        return angle;
    }

    /**
     * Called when this screen becomes the current screen for the game.
     */
    @Override
    public void show() {
    }

    /**
     * Handles screen resizing by updating all viewports.
     *
     * @param width  The new screen width in pixels
     * @param height The new screen height in pixels
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        hudVp.update(width, height, true);
        if (hudStage != null) {
            hudStage.getViewport().update(width, height, true);
        }
    }

    /**
     * Called when the game is paused.
     */
    @Override
    public void pause() {
        paused = true;
    }

    /**
     * Called when the game is resumed from pause.
     */
    @Override
    public void resume() {
        paused = false;
    }

    /**
     * Called when this screen is no longer the current screen for the game.
     */
    @Override
    public void hide() {
    }

    /**
     * Disposes of all resources used by the game screen.
     */
    @Override
    public void dispose() {
        sr.dispose();
        pauseStage.dispose();
        gameOverStage.dispose();
        victoryStage.dispose();
        consoleStage.dispose();
        if (hudStage != null) {
            hudStage.dispose();
        }
    }
}