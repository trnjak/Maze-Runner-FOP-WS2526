package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.PlayerStats;

/**
 * The LeaderboardScreen class displays a ranked list of all players based on their scores.
 */
public class LeaderboardScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final SpriteBatch batch;
    private final Array<PlayerStats> stats;

    /**
     * Constructor for LeaderboardScreen.
     *
     * @param game The main game instance.
     */
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

    /**
     * Loads all player statistics from saved game files.
     */
    private void loadAllPlayerStats() {
        stats.clear();
        FileHandle saveDir = Gdx.files.local("");

        if (saveDir.exists()) {
            FileHandle[] files = saveDir.list();

            for (FileHandle file : files) {
                if (file.name().startsWith("save_") && file.name().endsWith(".json")) {
                    try {
                        String filename = file.name();
                        String name = filename.substring(5, filename.length() - 5);

                        PlayerStats ps = new PlayerStats(name);
                        stats.add(ps);
                    } catch (Exception e) {
                        System.err.println("Error loading player stats from " + file.name() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Creates the user interface layout for the leaderboard screen.
     */
    private void createUI() {
        stage.addActor(game.menuImage);
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.pad(30);

        Label title = new Label("LEADERBOARDS", game.getSkin(), "title");
        table.add(title).padBottom(40).colspan(1).center().row();

        stats.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

        Table contentTable = new Table();

        Table headerTable = new Table();
        headerTable.defaults().pad(10, 15, 10, 15);

        headerTable.add(new Label("Rank", game.getSkin())).width(100);
        headerTable.add().width(5);
        headerTable.add(new Label("Player", game.getSkin())).width(150);
        headerTable.add().width(5);
        headerTable.add(new Label("Score", game.getSkin())).width(100);
        headerTable.add().width(5);
        headerTable.add(new Label("Level", game.getSkin())).width(100);

        contentTable.add(headerTable).padBottom(20).row();

        Table playerTable = new Table();
        ScrollPane scrollPane = new ScrollPane(playerTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        float scrollPaneWidth = game.WIDTH - 200;
        float scrollPaneHeight = 300;

        for (int i = 0; i < stats.size; i++) {
            PlayerStats player = stats.get(i);

            Table rowTable = new Table();
            rowTable.defaults().pad(8, 15, 8, 15);

            Label rankLabel;
            if (i == 0) {
                rankLabel = new Label((i + 1) + "", game.getSkin());
                rankLabel.setColor(1, 0.84f, 0, 1);
            } else if (i == 1) {
                rankLabel = new Label((i + 1) + "", game.getSkin());
                rankLabel.setColor(0.75f, 0.75f, 0.75f, 1);
            } else if (i == 2) {
                rankLabel = new Label((i + 1) + "", game.getSkin());
                rankLabel.setColor(0.8f, 0.5f, 0.2f, 1);
            } else {
                rankLabel = new Label(String.valueOf(i + 1), game.getSkin());
            }

            rowTable.add(rankLabel).width(100);
            rowTable.add().width(5);
            rowTable.add(new Label(player.getName(), game.getSkin())).width(150);
            rowTable.add().width(5);
            rowTable.add(new Label(String.valueOf(player.getScore()), game.getSkin())).width(100);
            rowTable.add().width(5);
            rowTable.add(new Label(String.valueOf(player.getLevel()), game.getSkin())).width(100);

            playerTable.add(rowTable).padBottom(4).fillX().row();
        }

        contentTable.add(scrollPane)
                .width(scrollPaneWidth)
                .height(scrollPaneHeight)
                .pad(10, 0, 30, 0)
                .row();

        table.add(contentTable).center().expandY().row();

        TextButton back = new TextButton("Back to Menu", game.getSkin());
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.goToMenu();
            }
        });
        table.add(back).padTop(20).width(250).height(60).center().row();
    }

    /**
     * Shows the screen and refreshes the leaderboard data and UI.
     */
    @Override
    public void show() {
        loadAllPlayerStats();
        stage.clear();
        createUI();
    }

    /**
     * Renders the leaderboard screen by clearing the display and drawing the stage.
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.255f, 0.286f, 0.349f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Handles screen resizing by updating the stage's viewport.
     *
     * @param width  The new screen width.
     * @param height The new screen height.
     */
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

    /**
     * Disposes of all resources used by the leaderboard screen.
     */
    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
    }
}
