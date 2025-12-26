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

public class StatsScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final SpriteBatch batch;
    private final PlayerStats playerStats;
    private Label expLabel;
    private Label healthLabel;
    private Label speedLabel;
    private Label attackLabel;

    public StatsScreen(MazeRunnerGame game) {
        this.game = game;
        batch = new SpriteBatch();
        var camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());

        playerStats = BeginScreen.STATS;

        Gdx.input.setInputProcessor(stage);

        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("UPGRADES", game.getSkin(), "title");
        table.add(title).padBottom(80).colspan(2).row();

        expLabel = new Label("EXP: " + playerStats.getExp(), game.getSkin());
        table.add(expLabel).padBottom(20).colspan(2).row();

        healthLabel = new Label("Health Lvl: " + playerStats.getHpLvl() + " (Max HP: " + playerStats.getMaxHp() + ")", game.getSkin());
        table.add(healthLabel).pad(10);

        TextButton healthButton = new TextButton("Upgrade (" + (playerStats.getHpLvl() * 2) + " EXP)", game.getSkin());
        healthButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(playerStats.upgradeHp()) {
                    updateLabels();
                    playerStats.save();
                }
            }
        });
        table.add(healthButton).pad(10).row();

        speedLabel = new Label("Speed Lvl: " + playerStats.getSpeedLvl() + " (Speed: " + String.format("%.2f", playerStats.getBaseSpeed()) + ")", game.getSkin());
        table.add(speedLabel).pad(10);

        TextButton speedButton = new TextButton("Upgrade (" + (playerStats.getSpeedLvl() * 2) + " EXP)", game.getSkin());
        speedButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(playerStats.upgradeSpeed()) {
                    updateLabels();
                    playerStats.save();
                }
            }
        });
        table.add(speedButton).pad(10).row();

        attackLabel = new Label("Attack Lvl: " + playerStats.getAtkLvl() + " (Cooldown: " + String.format("%.2f", playerStats.getAttackCooldown()) + "s)", game.getSkin());
        table.add(attackLabel).pad(10);

        TextButton attackButton = new TextButton("Upgrade (" + (playerStats.getAtkLvl() * 2) + " EXP)", game.getSkin());
        attackButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(playerStats.upgradeAtk()) {
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

    private void updateLabels() {
        expLabel.setText("EXP: " + playerStats.getExp());
        healthLabel.setText("Health Lvl: " + playerStats.getHpLvl() + " (Max HP: " + playerStats.getMaxHp() + ")");
        speedLabel.setText("Speed Lvl: " + playerStats.getSpeedLvl() + " (Speed: " + String.format("%.2f", playerStats.getBaseSpeed()) + ")");
        attackLabel.setText("Attack Lvl: " + playerStats.getAtkLvl() + " (Cooldown: " + String.format("%.2f", playerStats.getAttackCooldown()) + "s)");
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.46f, 0.23f, 0.21f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
    }
}