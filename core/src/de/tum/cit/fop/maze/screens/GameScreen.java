package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.*;
import de.tum.cit.fop.maze.objects.*;
import de.tum.cit.fop.maze.objects.enemies.*;
import de.tum.cit.fop.maze.objects.powerups.*;
import de.tum.cit.fop.maze.objects.traps.*;

import java.io.IOException;
import java.util.*;

public class GameScreen implements Screen {
    private final MazeRunnerGame game;
    private final OrthographicCamera camera, hudCam;
    private final Viewport viewport, hudVp;
    private final GameMap map;
    private final Player player;
    private final PlayerStats playerStats;
    private boolean paused = false, gameOver = false, victory = false;

    private float score = 0, time = 300; // 300 seconds to finish
    private int maxHearts;

    private final KeyBindings binds = KeyBindings.load();

    private final ShapeRenderer sr = new ShapeRenderer();

    private Stage pauseStage, gameOverStage, victoryStage, consoleStage, hudStage;
    private Table pauseTable, gameOverTable, victoryTable, consoleTable;
    private Label gameOverScore, victoryScore, consoleOutputLabel, scoreLabel, timeLabel;
    private TextField consoleTextField;
    private ScrollPane consoleScrollPane;

    // sheet : https://kenney.nl/assets/micro-roguelike
    private static final Texture TILE_SHEET = new Texture("hud_tilemap.png");
    public static final TextureRegion[][] TEXTURE_REGION = TextureRegion.split(TILE_SHEET, TILE_SHEET.getWidth() / 16, TILE_SHEET.getHeight() / 10);

    private boolean consoleOpen = false;
    private String consoleOutput = "";
    private final Map<String, Object> consoleVariables = new HashMap<>();

    // music : https://opengameart.org/content/random-battle
    private final Music gameMusic = Gdx.audio.newMusic(Gdx.files.internal("game_bg.mp3"));
    // sound : https://kenney.nl/assets/category:Audio
    private final Sound scored = Gdx.audio.newSound(Gdx.files.internal("sounds/scored.ogg")),
            won = Gdx.audio.newSound(Gdx.files.internal("sounds/win.ogg")),
            lost = Gdx.audio.newSound(Gdx.files.internal("sounds/lose.ogg"));
    private boolean wonPlayed = false, lostPlayed = false;

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

    public GameScreen(MazeRunnerGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(game.WIDTH, game.HEIGHT, camera);

        hudCam = new OrthographicCamera();
        hudVp = new FitViewport(game.WIDTH, game.HEIGHT, hudCam);

        map = new GameMap();
        try {
            map.generateMap();
        } catch(IOException e) {
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.46f, 0.23f, 0.21f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        globalInput();

        if(!paused && !gameOver && !victory && !consoleOpen) {
            time -= delta;

            player.update(delta);
            playerInput(delta);

            // update enemies
            for(Enemy e : map.getEnemies()) {
                e.update(delta, player, map);
                if(!player.isAlive()) {
                    gameOver = true;
                    updateGameOverScore();
                }
            }

            // update traps
            for(Trap t : map.getTraps()) {
                t.update(player, delta);
                if(!player.isAlive()) {
                    gameOver = true;
                    updateGameOverScore();
                }
            }

            // check keys
            for(Iterator<Key> it = map.getKeys().iterator(); it.hasNext(); ) {
                Key key = it.next();
                if(player.getBounds().overlaps(key.getBounds())) {
                    player.collectKey();
                    if(player.getKeys() == 1) {
                        Sound open = Gdx.audio.newSound(Gdx.files.internal("sounds/door_open.ogg"));
                        open.play(0.2f);
                    }
                    score += 100;
                    scored.play(0.1f);
                    it.remove();
                }
            }

            // check powerups
            for(Iterator<Powerup> it = map.getPowerups().iterator(); it.hasNext(); ) {
                Powerup p = it.next();
                if(player.getBounds().overlaps(p.getBounds())) {
                    p.update(player, delta);
                    score += 50;
                    scored.play(0.1f);
                    it.remove();
                }
            }

            // check exit
            Exit e = map.getExit();
            if(e != null) {
                e.update(delta);
                if(player.getBounds().overlaps(e.getBounds()) && player.getKeys() > 0) {
                    victory = true;
                    updateVictoryScore();
                }
                if(player.getKeys() > 0) {
                    e.open();
                }
            }

            centerCameraOnPlayer();
        }

        if(time <= 0) {
            gameOver = true;
            updateGameOverScore();
        }

        viewport.apply();
        camera.update();
        game.getSpriteBatch().setProjectionMatrix(camera.combined);

        // draw map and entities
        game.getSpriteBatch().begin();
        map.render(game.getSpriteBatch());
        player.render(game.getSpriteBatch());
        for(Enemy enemy : map.getEnemies()) {
            enemy.render(game.getSpriteBatch());
        }
        game.getSpriteBatch().end();

        hudVp.apply();
        hudCam.update();
        game.getSpriteBatch().setProjectionMatrix(hudCam.combined);

        game.getSpriteBatch().begin();
        int currentLives = player.getHp();
        float heartX = 20, heartY = hudVp.getWorldHeight() - 40;
        for(int i = 0; i < maxHearts; i++) {
            if(i < currentLives) {
                game.getSpriteBatch().draw(TEXTURE_REGION[6][6], heartX + i * GameObj.TILE, heartY, GameObj.TILE, GameObj.TILE);
            } else {
                game.getSpriteBatch().draw(TEXTURE_REGION[6][4], heartX + i * GameObj.TILE, heartY, GameObj.TILE, GameObj.TILE);
            }
        }
        if(currentLives > maxHearts) {
            for(int i = maxHearts; i < currentLives; i++) {
                game.getSpriteBatch().draw(TEXTURE_REGION[6][6], heartX + i * GameObj.TILE, heartY, GameObj.TILE, GameObj.TILE);
            }
            maxHearts = currentLives;
        }
        if(player.getKeys() < 1) {
            game.getSpriteBatch().draw(TEXTURE_REGION[7][4], hudVp.getWorldWidth() - GameObj.TILE - 20, hudVp.getWorldHeight() - 40, GameObj.TILE, GameObj.TILE);
        } else {
            game.getSpriteBatch().draw(TEXTURE_REGION[5][10], hudVp.getWorldWidth() - GameObj.TILE - 20, hudVp.getWorldHeight() - 40, GameObj.TILE, GameObj.TILE);
        }
        Exit exit = map.getExit();
        if(exit != null) {
            float arrowRotation = computeArrow(player.getX(), player.getY(), exit.getX() * GameObj.TILE, exit.getY() * GameObj.TILE);
            if(arrowRotation >= 0) {
                float arrowX = 20, arrowY = game.HEIGHT - 80;
                float originX = (float) GameObj.TILE / 2, originY = (float) GameObj.TILE / 2;
                game.getSpriteBatch().draw(TEXTURE_REGION[6][3], arrowX, arrowY, originX, originY, GameObj.TILE, GameObj.TILE, 1, 1, arrowRotation);
            }
        }
        game.getSpriteBatch().end();

        scoreLabel.setText((int) score + "");
        timeLabel.setText((int) time + "");

        scoreLabel.setPosition(hudVp.getWorldWidth() / 2 - scoreLabel.getPrefWidth() / 2, hudVp.getWorldHeight() - 100);
        timeLabel.setPosition(hudVp.getWorldWidth() / 2 - timeLabel.getPrefWidth() / 2, hudVp.getWorldHeight() - 120);

        hudStage.act(delta);
        hudStage.draw();

        if(consoleOpen) {
            consoleStage.getViewport().apply();
            consoleTable.setVisible(true);
            consoleStage.act(delta);
            consoleStage.draw();
        } else if(paused && !gameOver && !victory) {
            pauseStage.getViewport().apply();
            pauseTable.setVisible(true);
            pauseStage.act(delta);
            pauseStage.draw();
        } else if(gameOver) {
            if(!lostPlayed) {
                lost.play(0.2f);
                gameMusic.stop();
                game.menuMusic.play();
                lostPlayed = true;
            }
            gameOverStage.getViewport().apply();
            gameOverTable.setVisible(true);
            gameOverStage.act(delta);
            gameOverStage.draw();
        } else if(victory) {
            if(!wonPlayed) {
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

    private void initPauseMenu() {
        pauseStage = new Stage(hudVp, game.getSpriteBatch());
        pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.setVisible(false);

        Label title = new Label("PAUSED", game.getSkin(), "title");
        pauseTable.add(title).padBottom(80).row();

        String[] menuItems = {"Continue", "New Map", "New Endless", "Main Menu"};
        for(String item : menuItems) {
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

    private void handlePauseMenuSelection(String item) {
        switch(item) {
            case "Continue":
                paused = false;
                Gdx.input.setInputProcessor(null);
                game.menuMusic.stop();
                gameMusic.play();
                break;
            case "New Map":
                resetLevelScore();
                game.menuMusic.stop();
                game.goToGame();
                break;
            case "New Endless":
                resetLevelScore();
                game.menuMusic.stop();
                game.goToEndlessGame();
                break;
            case "Main Menu":
                gameMusic.stop();
                game.goToMenu();
                game.menuMusic.play();
                break;
        }
    }

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

    private void initGameOverMenu() {
        gameOverStage = new Stage(hudVp, game.getSpriteBatch());
        gameOverTable = new Table();
        gameOverTable.setFillParent(true);
        gameOverTable.setVisible(false);

        gameOverScore = new Label("", game.getSkin());
        initMenuStep2("YOU DIED!", gameOverStage, gameOverTable, gameOverScore);
    }

    private void initVictoryMenu() {
        victoryStage = new Stage(hudVp, game.getSpriteBatch());
        victoryTable = new Table();
        victoryTable.setFillParent(true);
        victoryTable.setVisible(false);

        victoryScore = new Label("", game.getSkin());
        initMenuStep2("YOU WON!", victoryStage, victoryTable, victoryScore);

        TextButton newMapButton = new TextButton("Next Endless", game.getSkin());
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

                    game.menuMusic.stop();
                    gameMusic.play();

                    resetLevelScore();

                    maxHearts = player.getMaxHealth();
                    timeLabel.setText((int) time + "");

                    centerCameraOnPlayer();
                    Gdx.input.setInputProcessor(null);
                } catch(IOException e) {
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
            if(c == '\r' || c == '\n') {
                processConsoleCommand(textField.getText());
                textField.setText("");
            }
        });

        consoleTable.add(consoleTextField).width(800).padTop(10);

        consoleStage.addActor(consoleTable);

        initializeConsoleVariables();
    }

    private void appendConsoleOutput(String text) {
        consoleOutput += text;

        if(consoleOutput.length() > 5000) {
            consoleOutput = consoleOutput.substring(consoleOutput.length() - 5000);
            int firstNewline = consoleOutput.indexOf('\n');
            if(firstNewline != -1) {
                consoleOutput = consoleOutput.substring(firstNewline);
            }
        }

        consoleOutputLabel.setText(consoleOutput);

        consoleScrollPane.layout();
        consoleScrollPane.setScrollPercentY(1);
    }

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

    private void resetLevelScore() {
        score = 0;
        scoreLabel.setText(0 + "");
    }

    private void updateGameOverScore() {
        int finalScore = (int) (score + time);
        gameOverScore.setText("Final score: " + finalScore);
        awardExp();
        playerStats.addScore(finalScore);
        playerStats.save();
    }

    private void updateVictoryScore() {
        int finalScore = (int) (score + time);
        victoryScore.setText("Final score: " + finalScore);
        awardExp();
        playerStats.addScore(finalScore);
        playerStats.save();
    }

    private void processConsoleCommand(String command) {
        if(command.trim().isEmpty()) return;

        String[] parts = command.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();

        try {
            switch(cmd) {
                case "help":
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
        } catch(Exception e) {
            appendConsoleOutput("\nError executing command: " + e.getMessage());
        }
    }

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

    private void handleSetCommand(String[] parts) {
        if(parts.length < 3) {
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
            } catch(NumberFormatException e) {
                try {
                    float floatValue = Float.parseFloat(valueStr);
                    consoleVariables.put(varName, floatValue);
                    applyVariableChange(varName, floatValue);
                    appendConsoleOutput("\nSet " + varName + " = " + floatValue);
                } catch(NumberFormatException e2) {
                    if(valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
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
        } catch(Exception e) {
            appendConsoleOutput("\nError setting variable: " + e.getMessage());
        }
    }

    private void handleGetCommand(String[] parts) {
        if(parts.length < 2) {
            appendConsoleOutput("\nUsage: get [variable]");
            return;
        }

        String varName = parts[1];
        Object value = consoleVariables.get(varName);

        if(value != null) {
            appendConsoleOutput("\n" + varName + " = " + value);
        } else {
            appendConsoleOutput("\nVariable not found: " + varName);
        }
    }

    private void handleGiveCommand(String[] parts) {
        if(parts.length < 2) {
            appendConsoleOutput("\nUsage: give [item] [amount]");
            return;
        }

        String item = parts[1].toLowerCase();
        int amount = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;

        switch(item) {
            case "key", "keys":
                for(int i = 0; i < amount; i++) {
                    player.collectKey();
                }
                consoleVariables.put("keys", player.getKeys());
                appendConsoleOutput("\nGiven " + amount + " key(s) to player");
                break;
            case "health", "hp":
                if(amount > maxHearts) {
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

    private void handleKillCommand(String[] parts) {
        if(parts.length > 1 && parts[1].equalsIgnoreCase("all")) {
            int count = 0;
            for(Enemy enemy : map.getEnemies()) {
                if(enemy.isAlive()) {
                    enemy.takeDamage(1234567890);
                    count++;
                }
            }
            for(Enemy e : map.getEnemies().stream().filter(e -> !e.isAlive()).toList()) {
                if(!e.isAlive()) {
                    map.getEnemies().remove(e);
                }
            }
            appendConsoleOutput("\nKilled " + count + " enemies");
        } else {
            player.attack(map.getEnemies());
            appendConsoleOutput("\nAttacked enemies in range");
        }
    }

    private void handleHealCommand(String[] parts) {
        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 5;
        player.setHp(Math.min(5, player.getHp() + amount));
        consoleVariables.put("health", player.getHp());
        appendConsoleOutput("\nHealed player to " + player.getHp() + " HP");
    }

    private void handleTeleportCommand(String[] parts) {
        if(parts.length < 3) {
            appendConsoleOutput("\nUsage: teleport [x] [y]");
            return;
        }

        try {
            float x = Float.parseFloat(parts[1]) * GameObj.TILE;
            float y = Float.parseFloat(parts[2]) * GameObj.TILE;

            Rectangle testRect = new Rectangle(x, y, GameObj.TILE, GameObj.TILE);
            if(!map.collidesWithWall(testRect)) {
                player.move(x - player.getX(), y - player.getY(), map);
                centerCameraOnPlayer();
                appendConsoleOutput("\nTeleported player to (" + parts[1] + ", " + parts[2] + ")");
            } else {
                appendConsoleOutput("\nCannot teleport to wall at (" + parts[1] + ", " + parts[2] + ")");
            }
        } catch(NumberFormatException e) {
            appendConsoleOutput("\nInvalid coordinates");
        }
    }

    private void handleListCommand() {
        StringBuilder listOutput = new StringBuilder("\nGame Variables:");
        for(Map.Entry<String, Object> entry : consoleVariables.entrySet()) {
            listOutput.append("\n").append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        appendConsoleOutput(listOutput.toString());
    }

    private void applyVariableChange(String varName, Object value) {
        switch(varName) {
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
                if(paused) {
                    Gdx.input.setInputProcessor(pauseStage);
                } else {
                    Gdx.input.setInputProcessor(null);
                }
                break;
        }
    }

    private void awardExp() {
        int plusExp = (int) (score / 500);
        if(plusExp > 0) {
            playerStats.addExp(plusExp);
            playerStats.save();
        }
    }

    private void globalInput() {
        if(!gameOver && !victory && Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            consoleOpen = !consoleOpen;
            if(consoleOpen) {
                Gdx.input.setInputProcessor(consoleStage);
                consoleTextField.setFocusTraversal(true);
                consoleStage.setKeyboardFocus(consoleTextField);
            } else {
                Gdx.input.setInputProcessor(null);
            }
        }

        if(!consoleOpen && !gameOver && !victory && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            if(paused) {
                Gdx.input.setInputProcessor(pauseStage);
                gameMusic.stop();
                game.menuMusic.play();
            } else {
                Gdx.input.setInputProcessor(null);
                game.menuMusic.stop();
                gameMusic.play();
            }
        }

        if(!consoleOpen && gameOver && Gdx.input.getInputProcessor() != gameOverStage) {
            Gdx.input.setInputProcessor(gameOverStage);
        } else if(!consoleOpen && victory && Gdx.input.getInputProcessor() != victoryStage) {
            Gdx.input.setInputProcessor(victoryStage);
        }

        if(!consoleOpen && !paused && !gameOver && !victory) {
            if(Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS))
                camera.zoom = Math.max(0.5f, camera.zoom - 0.02f);
            if(Gdx.input.isKeyPressed(Input.Keys.MINUS)) camera.zoom = Math.min(2.0f, camera.zoom + 0.02f);
        }
    }

    private void centerCameraOnPlayer() {
        camera.position.set(player.getX() + GameObj.TILE / 2f, player.getY() + GameObj.TILE / 2f, 0);
        camera.update();
    }

    private void playerInput(float delta) {
        float baseSpeed = Gdx.input.isKeyPressed(binds.SPRINT) ? 168f : 100f;
        float speed = baseSpeed * player.getCurrentSpeed();
        float dx = 0, dy = 0;
        if(Gdx.input.isKeyPressed(binds.LEFT)) dx -= speed * delta;
        if(Gdx.input.isKeyPressed(binds.RIGHT)) dx += speed * delta;
        if(Gdx.input.isKeyPressed(binds.UP)) dy += speed * delta;
        if(Gdx.input.isKeyPressed(binds.DOWN)) dy -= speed * delta;

        // attack
        if(Gdx.input.isKeyJustPressed(binds.ATTACK)) {
            player.attack(map.getEnemies());
            for(Enemy e : map.getEnemies().stream().filter(e -> !e.isAlive()).toList()) {
                if(!e.isAlive()) {
                    score += 100;
                    scored.play(0.1f);
                    map.getEnemies().remove(e);
                }
            }
        }

        if(dx == 0 && dy == 0) return;

        // collision
        Rectangle next = new Rectangle(player.getX() + dx, player.getY() + dy, GameObj.TILE, GameObj.TILE);
        if(!enemyOverlap(next)) {
            player.move(dx, dy, map);
        }
    }

    private boolean enemyOverlap(Rectangle rect) {
        for(Enemy e : map.getEnemies()) {
            if(!e.isAlive()) continue;
            if(rect.overlaps(e.getBounds())) return true;
        }
        return false;
    }

    private float computeArrow(float px, float py, float ex, float ey) {
        float dx = ex - px;
        float dy = ey - py;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        if(d <= 8) return -1;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if(angle < 0) angle += 360;
        return angle;
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        hudVp.update(width, height, true);
        if(hudStage != null) {
            hudStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        sr.dispose();
        pauseStage.dispose();
        gameOverStage.dispose();
        victoryStage.dispose();
        consoleStage.dispose();
        if(hudStage != null) {
            hudStage.dispose();
        }
    }
}