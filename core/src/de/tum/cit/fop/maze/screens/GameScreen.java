package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.*;
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
    // two cameras and viewports because hud stays fixed and player moves
    private final OrthographicCamera camera, hudCam;
    private final Viewport viewport, hudVp;
    private final GameMap map;
    private final Player player;
    private boolean paused = false, gameOver = false, victory = false;

    private float score = 0, time = 300; // 300 seconds to finish
    private int maxHearts;

    private final KeyBindings binds = KeyBindings.load();

    private final ShapeRenderer sr = new ShapeRenderer();

    private Stage pauseStage, gameOverStage, victoryStage, consoleStage;
    private Table pauseTable, gameOverTable, victoryTable, consoleTable;
    private Image pauseBg, gameOverBg, victoryBg, consoleBg;
    private Label gameOverScore, victoryScore, consoleOutputLabel;
    private TextField consoleTextField;
    private ScrollPane consoleScrollPane;

    private static final Texture TILE_SHEET = new Texture("hud_tilemap.png");
    public static final TextureRegion[][] TEXTURE_REGION = TextureRegion.split(TILE_SHEET, TILE_SHEET.getWidth() / 16, TILE_SHEET.getHeight() / 10);
    private static final TextureRegion ARROW_TEXTURE = TEXTURE_REGION[6][3];

    private boolean consoleOpen = false;
    private String consoleOutput = "";
    private final Map<String, Object> consoleVariables = new HashMap<>();

    public GameScreen(MazeRunnerGame game, String path) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(game.WIDTH, game.HEIGHT, camera);

        hudCam = new OrthographicCamera();
        hudVp = new FitViewport(game.WIDTH, game.HEIGHT, hudCam);

        map = new GameMap();
        map.load(path);

        player = new Player(map.getEx() * GameObj.TILE, map.getEy() * GameObj.TILE);
        maxHearts = player.getMaxHealth();

        initPauseMenu();
        initGameOverMenu();
        initVictoryMenu();
        initDeveloperConsole();
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
        maxHearts = player.getMaxHealth();

        initPauseMenu();
        initGameOverMenu();
        initVictoryMenu();
        initDeveloperConsole();
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
                    score += 100;
                    it.remove();
                }
            }

            // check powerups
            for(Iterator<Powerup> it = map.getPowerups().iterator(); it.hasNext(); ) {
                Powerup p = it.next();
                if(player.getBounds().overlaps(p.getBounds())) {
                    p.update(player, delta);
                    score += 50;
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
        Exit exit = map.getExit();
        if(exit != null) {
            float arrowRotation = computeArrow(player.getX(), player.getY(), exit.getX() * GameObj.TILE, exit.getY() * GameObj.TILE);
            if(arrowRotation >= 0) {
                float arrowX = 20, arrowY = game.HEIGHT - 80;
                float originX = (float) GameObj.TILE / 2, originY = (float) GameObj.TILE / 2;
                game.getSpriteBatch().draw(ARROW_TEXTURE, arrowX, arrowY, originX, originY, GameObj.TILE, GameObj.TILE, 1, 1, arrowRotation);
            }
        }
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Key: " + (player.getKeys() > 0 ? "DONE" : "NONE"), hudVp.getWorldWidth()-200, hudVp.getWorldHeight()-10);
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Score: " + (int) score, hudVp.getWorldWidth()-650, hudVp.getWorldHeight()-10);
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Time: " + (int) time, hudVp.getWorldWidth()-650, hudVp.getWorldHeight()-50);
        game.getSpriteBatch().end();

        if(consoleOpen) {
            consoleStage.getViewport().apply();
            consoleBg.setVisible(true);
            consoleTable.setVisible(true);
            consoleStage.act(delta);
            consoleStage.draw();
        } else if(paused && !gameOver && !victory) {
            pauseStage.getViewport().apply();
            pauseBg.setVisible(true);
            pauseTable.setVisible(true);
            pauseStage.act(delta);
            pauseStage.draw();
        } else if(gameOver) {
            gameOverStage.getViewport().apply();
            gameOverBg.setVisible(true);
            gameOverTable.setVisible(true);
            gameOverStage.act(delta);
            gameOverStage.draw();
        } else if(victory) {
            victoryStage.getViewport().apply();
            victoryBg.setVisible(true);
            victoryTable.setVisible(true);
            victoryStage.act(delta);
            victoryStage.draw();
        } else {
            consoleBg.setVisible(false);
            consoleTable.setVisible(false);
            pauseBg.setVisible(false);
            pauseTable.setVisible(false);
            gameOverBg.setVisible(false);
            gameOverTable.setVisible(false);
            victoryBg.setVisible(false);
            victoryTable.setVisible(false);
        }
    }

    private void initMenu(Table t, Image bg) {
        bg.setColor(0, 0, 0, 0.7f);
        bg.setFillParent(true);
        bg.setVisible(false);

        t.setFillParent(true);
        t.setVisible(false);
    }

    private void initPauseMenu() {
        pauseStage = new Stage(hudVp, game.getSpriteBatch());
        pauseBg = new Image(new Texture(Gdx.files.internal("white.png")));
        pauseTable = new Table();
        initMenu(pauseTable, pauseBg);

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

        pauseStage.addActor(pauseBg);
        pauseStage.addActor(pauseTable);
    }

    private void initMenuStep2(String title, Stage s, Image bg, Table t, Label sc) {
        Label la = new Label(title, game.getSkin(), "title");
        t.add(la).padBottom(80).row();

        t.add(sc).padBottom(20).row();

        TextButton menuButton = new TextButton("Main Menu", game.getSkin());
        t.add(menuButton).width(320).padBottom(20).row();

        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MenuScreen(game));
            }
        });

        if(map.isEndless()) {
            TextButton newMapButton = new TextButton("Next Endless", game.getSkin());
            t.add(newMapButton).width(320).padBottom(20).row();

            newMapButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new GameScreen(game));
                }
            });
        }

        s.addActor(bg);
        s.addActor(t);
    }

    private void initGameOverMenu() {
        gameOverStage = new Stage(hudVp, game.getSpriteBatch());
        gameOverBg = new Image(new Texture(Gdx.files.internal("white.png")));
        gameOverTable = new Table();
        initMenu(gameOverTable, gameOverBg);
        gameOverScore = new Label("", game.getSkin());
        initMenuStep2("YOU DIED!", gameOverStage, gameOverBg, gameOverTable, gameOverScore);
    }

    private void initVictoryMenu() {
        victoryStage = new Stage(hudVp, game.getSpriteBatch());
        victoryBg = new Image(new Texture(Gdx.files.internal("white.png")));
        victoryTable = new Table();
        initMenu(victoryTable, victoryBg);
        victoryScore = new Label("", game.getSkin());
        initMenuStep2("YOU WON!", victoryStage, victoryBg, victoryTable, victoryScore);
    }

    private void initDeveloperConsole() {
        consoleStage = new Stage(hudVp, game.getSpriteBatch());
        consoleBg = new Image(new Texture(Gdx.files.internal("white.png")));
        consoleBg.setColor(0, 0, 0, 0.7f);
        consoleBg.setFillParent(true);
        consoleBg.setVisible(false);

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

        consoleStage.addActor(consoleBg);
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

    private void handlePauseMenuSelection(String item) {
        switch(item) {
            case "Continue":
                paused = false;
                Gdx.input.setInputProcessor(null);
                break;
            case "New Map":
                game.goToGame();
                break;
            case "New Endless":
                game.goToEndlessGame();
                break;
            case "Main Menu":
                game.goToMenu();
                break;
        }
    }

    private void updateGameOverScore() {
        gameOverScore.setText("Final score: " + (int) (score + time));
        awardExp();
    }

    private void updateVictoryScore() {
        victoryScore.setText("Final score: " + (int) (score + time));
        awardExp();
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
                case "teleport":
                    handleTeleportCommand(parts);
                    break;
                case "list":
                    handleListCommand();
                    break;
                case "clear":
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
            } catch (NumberFormatException e) {
                try {
                    float floatValue = Float.parseFloat(valueStr);
                    consoleVariables.put(varName, floatValue);
                    applyVariableChange(varName, floatValue);
                    appendConsoleOutput("\nSet " + varName + " = " + floatValue);
                } catch (NumberFormatException e2) {
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
        } catch (Exception e) {
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
                player.move(x - player.getX(), y - player.getY());
                centerCameraOnPlayer();
                appendConsoleOutput("\nTeleported player to (" + parts[1] + ", " + parts[2] + ")");
            } else {
                appendConsoleOutput("\nCannot teleport to wall at (" + parts[1] + ", " + parts[2] + ")");
            }
        } catch (NumberFormatException e) {
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
        int plusExp = (int)(score / 500);
        if (plusExp > 0) {
            PlayerStats.getInstance().addExp(plusExp);
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
            } else {
                Gdx.input.setInputProcessor(null);
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
                    map.getEnemies().remove(e);
                }
            }
        }

        if(dx == 0 && dy == 0) return;

        // collision
        Rectangle next = new Rectangle(player.getX() + dx, player.getY() + dy, GameObj.TILE, GameObj.TILE);
        if(!map.collidesWithWall(next) && !enemyOverlap(next)) player.move(dx, dy);
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
    }
}