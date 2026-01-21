package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.PlayerStats;

/**
 * The StatsScreen class displays player statistics and provides an interface for upgrading abilities.
 * Shows current levels, costs, and allows spending experience points on character improvements.
 */
public class StatsScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final SpriteBatch batch;
    private final PlayerStats playerStats;
    private Label expLabel;
    private Label healthLabel;
    private Label speedLabel;
    private Label attackLabel;
    private TextButton attackButton;
    private TextButton speedButton;
    private TextButton healthButton;

    /**
     * Constructs a new StatsScreen for viewing and upgrading player statistics.
     *
     * @param game The main MazeRunnerGame instance for screen management
     */
    public StatsScreen(MazeRunnerGame game) {
        this.game = game;
        batch = new SpriteBatch();
        var camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());

        playerStats = BeginScreen.STATS;

        Gdx.input.setInputProcessor(stage);

        createUI();
    }

    /**
     * Creates the user interface with upgrade options and current statistics display.
     * Includes labels for experience and attribute levels, upgrade buttons with dynamic costs,
     * and navigation controls.
     */
    private void createUI() {
        stage.addActor(game.menuImage);
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("UPGRADES", game.getSkin(), "title");
        table.add(title).padBottom(80).colspan(2).row();

        expLabel = new Label("EXP: " + playerStats.getExp(), game.getSkin());
        table.add(expLabel).padBottom(20).colspan(2).row();

        healthLabel = new Label("Health Lvl: " + playerStats.getHpLvl() + " (Max HP: " + playerStats.getMaxHp() + ")", game.getSkin());
        table.add(healthLabel).pad(10);

        healthButton = new TextButton("Upgrade (" + (playerStats.getHpLvl() * 2) + " EXP)", game.getSkin());
        healthButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (playerStats.upgradeHp()) {
                    updateLabels();
                    playerStats.save();
                }
            }
        });
        table.add(healthButton).pad(10).row();

        speedLabel = new Label("Speed Lvl: " + playerStats.getSpeedLvl() + " (Speed: " + String.format("%.2f", playerStats.getSpeed()) + ")", game.getSkin());
        table.add(speedLabel).pad(10);

        speedButton = new TextButton("Upgrade (" + (playerStats.getSpeedLvl() * 2) + " EXP)", game.getSkin());
        speedButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (playerStats.upgradeSpeed()) {
                    updateLabels();
                    playerStats.save();
                }
            }
        });
        table.add(speedButton).pad(10).row();

        attackLabel = new Label("Attack Lvl: " + playerStats.getAtkLvl() + " (Cooldown: " + String.format("%.2f", playerStats.getAttackCooldown()) + "s)", game.getSkin());
        table.add(attackLabel).pad(10);

        attackButton = new TextButton("Upgrade (" + (playerStats.getAtkLvl() * 2) + " EXP)", game.getSkin());
        attackButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (playerStats.upgradeAtk()) {
                    updateLabels();
                    playerStats.save();
                }
            }
        });
        table.add(attackButton).pad(10).row();

        TextButton backButton = new TextButton("Back", game.getSkin());
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.backToGameOrMenuDependingOnWhetherOrNotThePlayerEnteredTheScreenFromTheGameOrFromTheMenu();
                playerStats.save();
            }
        });
        table.add(backButton).padTop(50).colspan(2).row();
    }

    /**
     * Updates all displayed labels and buttons with current player statistics and upgrade costs.
     * Called after successful upgrades to refresh the UI.
     */
    private void updateLabels() {
        expLabel.setText("EXP: " + playerStats.getExp());
        healthLabel.setText("Health Lvl: " + playerStats.getHpLvl() + " (Max HP: " + playerStats.getMaxHp() + ")");
        healthButton.setText("Upgrade (" + (playerStats.getHpLvl() * 2) + " EXP)");
        speedLabel.setText("Speed Lvl: " + playerStats.getSpeedLvl() + " (Speed: " + String.format("%.2f", playerStats.getSpeed()) + ")");
        speedButton.setText("Upgrade (" + (playerStats.getSpeedLvl() * 2) + " EXP)");
        attackLabel.setText("Attack Lvl: " + playerStats.getAtkLvl() + " (Cooldown: " + String.format("%.2f", playerStats.getAttackCooldown()) + "s)");
        attackButton.setText("Upgrade (" + (playerStats.getAtkLvl() * 2) + " EXP)");
    }

    /**
     * Called when this screen becomes the current screen for the game.
     */
    @Override
    public void show() {
    }

    /**
     * Renders the statistics screen by clearing the display and drawing the stage.
     *
     * @param delta The time in seconds since the last render
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.025f, 0.011f, 0.082f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Handles screen resizing by updating the stage's viewport.
     *
     * @param width  The new screen width in pixels
     * @param height The new screen height in pixels
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Called when the game is paused.
     */
    @Override
    public void pause() {
    }

    /**
     * Called when the game is resumed from pause.
     */
    @Override
    public void resume() {
    }

    /**
     * Called when this screen is no longer the current screen for the game.
     */
    @Override
    public void hide() {
    }

    /**
     * Disposes of all resources used by the statistics screen.
     */
    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
    }
}