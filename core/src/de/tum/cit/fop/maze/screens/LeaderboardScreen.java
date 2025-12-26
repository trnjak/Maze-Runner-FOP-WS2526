package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.PlayerStats;

public class LeaderboardScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final SpriteBatch batch;
    private final Array<PlayerStats> stats;

    public LeaderboardScreen(MazeRunnerGame game) {
        this.game = game;
        batch = new SpriteBatch();
        var camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());
        stats = new Array<>();

        Gdx.input.setInputProcessor(stage);

        loadAllPlayerStats();
        createUI();
    }

    private void loadAllPlayerStats() {
        stats.clear();
        FileHandle saveDir = Gdx.files.local("");

        if(saveDir.exists()) {
            FileHandle[] files = saveDir.list();

            for(FileHandle file : files) {
                if(file.name().startsWith("save_") && file.name().endsWith(".json")) {
                    try {
                        String filename = file.name();
                        String name = filename.substring(5, filename.length() - 5);

                        PlayerStats ps = new PlayerStats(name);
                        stats.add(ps);
                    } catch(Exception e) {
                        System.err.println("Error loading player stats from " + file.name() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.pad(30);

        Label title = new Label("LEADERBOARDS", game.getSkin(), "title");
        table.add(title).padBottom(60).colspan(7).row();

        stats.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

        Table headerTable = new Table();

        headerTable.add(new Label("Rank", game.getSkin())).width(100).pad(10, 15, 10, 15);
        headerTable.add().width(5);
        headerTable.add(new Label("Player", game.getSkin())).width(150).pad(10, 15, 10, 15);
        headerTable.add().width(5);
        headerTable.add(new Label("Score", game.getSkin())).width(100).pad(10, 15, 10, 15);
        headerTable.add().width(5);
        headerTable.add(new Label("Level", game.getSkin())).width(100).pad(10, 15, 10, 15);

        table.add(headerTable).padBottom(30).colspan(15).row();

        Table playerTable = new Table();
        ScrollPane scrollPane = new ScrollPane(playerTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        for(int i = 0; i < stats.size; i++) {
            PlayerStats player = stats.get(i);

            Table rowContainer = new Table();

            Table rowTable = new Table();
            rowContainer.add(rowTable).pad(5, 0, 5, 0);

            Label rankLabel;
            if(i == 0) {
                rankLabel = new Label((i + 1) + "", game.getSkin(), "bold");
                rankLabel.setColor(1, 0.84f, 0, 1);
            } else if(i == 1) {
                rankLabel = new Label((i + 1) + "", game.getSkin(), "bold");
                rankLabel.setColor(0.75f, 0.75f, 0.75f, 1);
            } else if(i == 2) {
                rankLabel = new Label((i + 1) + "", game.getSkin(), "bold");
                rankLabel.setColor(0.8f, 0.5f, 0.2f, 1);
            } else {
                rankLabel = new Label(String.valueOf(i + 1), game.getSkin());
            }

            rowTable.add(rankLabel).width(100).pad(8, 15, 8, 15);
            rowTable.add().width(5);
            rowTable.add(new Label(player.getName(), game.getSkin())).width(150).pad(8, 15, 8, 15);
            rowTable.add().width(5);
            rowTable.add(new Label(String.valueOf(player.getScore()), game.getSkin())).width(100).pad(8, 15, 8, 15);
            rowTable.add().width(5);
            rowTable.add(new Label(String.valueOf(player.getLevel()), game.getSkin())).width(100).pad(8, 15, 8, 15);

            playerTable.add(rowContainer).colspan(15).padBottom(4).fillX().row();
        }

        table.add(scrollPane).colspan(15).expand().fill().pad(20, 0, 30, 0).row();

        table.add().pad(20).colspan(7).row();

        TextButton back = new TextButton("Back to Menu", game.getSkin());
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.goToMenu();
            }
        });
        table.add(back).padTop(20).width(250).height(60).colspan(7).row();
    }

    @Override
    public void show() {
        loadAllPlayerStats();
        stage.clear();
        createUI();
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