package de.tum.cit.fop.maze;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.objects.*;
import de.tum.cit.fop.maze.objects.enemies.*;
import de.tum.cit.fop.maze.objects.powerups.*;
import de.tum.cit.fop.maze.objects.traps.*;

import java.io.IOException;
import java.util.Iterator;

public class GameScreen implements Screen {
    private final MazeRunnerGame game;
    // two cameras and viewports because hud stays fixed and player moves
    private final OrthographicCamera camera, hudCam;
    private final Viewport viewport, hudVp;
    private final GameMap map;
    private final Player player;
    private boolean paused = false, gameOver = false, victory = false;

    private float score = 0, time = 300; // 300 seconds to finish

    private final KeyBindings binds = KeyBindings.load();

    private final ShapeRenderer sr = new ShapeRenderer();

    private Stage pauseStage, gameOverStage, victoryStage;
    private Table pauseTable, gameOverTable, victoryTable;
    private Image pauseBg, gameOverBg, victoryBg;
    private Label gameOverScore, victoryScore;

    private static final Texture TILE_SHEET = new Texture("hud_tilemap.png");
    public static final TextureRegion[][] TEXTURE_REGION = TextureRegion.split(TILE_SHEET, TILE_SHEET.getWidth() / 16, TILE_SHEET.getHeight() / 10);
    private static final TextureRegion ARROW_TEXTURE = TEXTURE_REGION[6][3];

    public GameScreen(MazeRunnerGame game, String path) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1024, 768, camera);
        camera.setToOrtho(false, 1024, 768);

        hudCam = new OrthographicCamera();
        hudVp = new ScreenViewport(hudCam);

        map = new GameMap();
        map.load(path);

        player = new Player(map.getEx() * GameObj.TILE, map.getEy() * GameObj.TILE);

        initPauseMenu();
        initGameOverMenu();
        initVictoryMenu();
    }

    public GameScreen(MazeRunnerGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1024, 768, camera);
        camera.setToOrtho(false, 1024, 768);

        hudCam = new OrthographicCamera();
        hudVp = new ScreenViewport(hudCam);

        map = new GameMap();
        try {
            map.generateMap();
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        player = new Player(map.getEx() * GameObj.TILE, map.getEy() * GameObj.TILE);

        initPauseMenu();
        initGameOverMenu();
        initVictoryMenu();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.46f, 0.23f, 0.21f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        globalInput();

        if(!paused && !gameOver && !victory) {
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

        // draw map and entities
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();
        map.render(game.getSpriteBatch());
        player.render(game.getSpriteBatch());
        for(Enemy enemy : map.getEnemies()) {
            enemy.render(game.getSpriteBatch());
        }
        game.getSpriteBatch().end();

        // draw HUD
        game.getSpriteBatch().setProjectionMatrix(hudCam.combined);
        game.getSpriteBatch().begin();
        int maxHearts = 5, currentLives = player.getHp();
        float heartX = 20, heartY = hudVp.getWorldHeight() - 40;
        for(int i = 0; i < maxHearts; i++) {
            if(i < currentLives) {
                game.getSpriteBatch().draw(TEXTURE_REGION[6][6], heartX + i * GameObj.TILE, heartY, GameObj.TILE, GameObj.TILE);
            } else {
                game.getSpriteBatch().draw(TEXTURE_REGION[6][4], heartX + i * GameObj.TILE, heartY, GameObj.TILE, GameObj.TILE);
            }
        }
        Exit exit = map.getExit();
        if(exit != null) {
            float arrowRotation = computeArrow(player.getX(), player.getY(), exit.getX() * GameObj.TILE, exit.getY() * GameObj.TILE);
            if(arrowRotation >= 0) {
                float arrowX = 20, arrowY = hudVp.getWorldHeight() - 80;
                float originX = (float) GameObj.TILE / 2, originY = (float) GameObj.TILE / 2;
                game.getSpriteBatch().draw(ARROW_TEXTURE, arrowX, arrowY, originX, originY, GameObj.TILE, GameObj.TILE, 1, 1, arrowRotation);
            }
        }
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Key: " + (player.getKeys() > 0 ? "DONE" : "NONE"), hudVp.getWorldWidth()-200, hudVp.getWorldHeight()-10);
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Score: " + (int) score, hudVp.getWorldWidth()-650, hudVp.getWorldHeight()-10);
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Time: " + (int) time, hudVp.getWorldWidth()-650, hudVp.getWorldHeight()-50);
        game.getSpriteBatch().end();

        //update & render stages
        if(paused && !gameOver && !victory) {
            pauseBg.setVisible(true);
            pauseTable.setVisible(true);
            pauseStage.act(delta);
            pauseStage.draw();
        } else if(gameOver) {
            gameOverBg.setVisible(true);
            gameOverTable.setVisible(true);
            gameOverStage.act(delta);
            gameOverStage.draw();
        } else if(victory) {
            victoryBg.setVisible(true);
            victoryTable.setVisible(true);
            victoryStage.act(delta);
            victoryStage.draw();
        } else {
            // Hide all backgrounds when no menu is active
            pauseBg.setVisible(false);
            pauseTable.setVisible(false);
            gameOverBg.setVisible(false);
            gameOverTable.setVisible(false);
            victoryBg.setVisible(false);
            victoryTable.setVisible(false);
        }
    }

    private void initMenu(Table t, Image bg) {
        bg.setColor(0, 0, 0, 0.5f);
        bg.setFillParent(true);
        bg.setVisible(false);

        t.setFillParent(true);
        t.setVisible(false);
    }

    private void initPauseMenu() {
        pauseStage = new Stage(new ScreenViewport(), game.getSpriteBatch());
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
        gameOverStage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        gameOverBg = new Image(new Texture(Gdx.files.internal("white.png")));
        gameOverTable = new Table();
        initMenu(gameOverTable, gameOverBg);
        gameOverScore = new Label("", game.getSkin());
        initMenuStep2("YOU DIED!", gameOverStage, gameOverBg, gameOverTable, gameOverScore);
    }

    private void initVictoryMenu() {
        victoryStage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        victoryBg = new Image(new Texture(Gdx.files.internal("white.png")));
        victoryTable = new Table();
        initMenu(victoryTable, victoryBg);
        victoryScore = new Label("", game.getSkin());
        initMenuStep2("YOU WON!", victoryStage, victoryBg, victoryTable, victoryScore);
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
    }

    private void updateVictoryScore() {
        victoryScore.setText("Final score: " + (int) (score + time));
    }

    private void globalInput() {
        // toggle pause
        if(!gameOver && !victory && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            if(paused) {
                Gdx.input.setInputProcessor(pauseStage);
            } else {
                Gdx.input.setInputProcessor(null);
            }
        }

        //set input processor for game over/victory screens
        if(gameOver && Gdx.input.getInputProcessor() != gameOverStage) {
            Gdx.input.setInputProcessor(gameOverStage);
        } else if(victory && Gdx.input.getInputProcessor() != victoryStage) {
            Gdx.input.setInputProcessor(victoryStage);
        }

        // camera zoom (only when not in any menu)
        if(!paused && !gameOver && !victory) {
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
        float speed = baseSpeed * player.getSpeed();
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
        camera.setToOrtho(false, width, height);
        viewport.update(width, height);
        hudVp.update(width, height, true);
        pauseStage.getViewport().update(width, height, true);
        gameOverStage.getViewport().update(width, height, true);
        victoryStage.getViewport().update(width, height, true);
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
    }
}
