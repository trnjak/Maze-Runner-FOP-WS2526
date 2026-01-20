package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.Achievement;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.PlayerStats;

/**
 * The AchievementScreen class displays player achievements and progress tracking.
 * Shows unlocked status, progress bars, and organises achievements by completion state.
 */
public class AchievementScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final SpriteBatch batch;
    private final PlayerStats playerStats;
    private Table achievementsTable;
    private Label unlockedCountLabel;
    private ScrollPane scrollPane;

    /**
     * Constructs a new AchievementScreen to display player accomplishments.
     *
     * @param game The main MazeRunnerGame instance for screen management
     */
    public AchievementScreen(MazeRunnerGame game) {
        this.game = game;
        batch = new SpriteBatch();
        var camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());

        playerStats = BeginScreen.STATS;

        Gdx.input.setInputProcessor(stage);

        createUI();
    }

    /**
     * Creates the user interface with achievement listings and navigation controls.
     * Includes a summary header, scrollable achievement table, and back button.
     */
    private void createUI() {
        stage.addActor(game.menuImage);
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Label title = new Label("ACHIEVEMENTS", game.getSkin(), "title");
        mainTable.add(title).padBottom(30).colspan(2).row();

        Table statsTable = new Table();
        statsTable.defaults().pad(5);

        unlockedCountLabel = new Label("Unlocked: " + playerStats.getUnlockedAchievementsCount() + "/" +
                playerStats.getAchievements().size, game.getSkin());
        statsTable.add(unlockedCountLabel).padBottom(15).colspan(2).row();

        mainTable.add(statsTable).padBottom(20).colspan(2).row();

        createAchievementsTable();

        scrollPane = new ScrollPane(achievementsTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        mainTable.add(scrollPane).colspan(2).expand().fill().pad(10).row();

        TextButton backButton = new TextButton("Back", game.getSkin());
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.goToMenu();
            }
        });
        mainTable.add(backButton).padTop(20).colspan(2).row();
    }

    /**
     * Creates a table displaying all achievements with their details.
     * Achievements are sorted by unlocked status, then by completion progress.
     * Each entry shows name, description, progress, and locked/unlocked status.
     */
    private void createAchievementsTable() {
        achievementsTable = new Table();
        achievementsTable.defaults().pad(10);

        var achievements = playerStats.getAchievements();
        achievements.sort((a1, a2) -> {
            if (a1.isUnlocked() && !a2.isUnlocked()) return -1;
            if (!a1.isUnlocked() && a2.isUnlocked()) return 1;

            float progress1 = (float) a1.getProgress() / a1.getTarget();
            float progress2 = (float) a2.getProgress() / a2.getTarget();
            if (Math.abs(progress2 - progress1) > 0.001f) {
                return Float.compare(progress2, progress1);
            }

            return a1.getName().compareTo(a2.getName());
        });

        achievementsTable.add(new Label("Achievement", game.getSkin())).padRight(20);
        achievementsTable.add(new Label("Description", game.getSkin())).expandX().fillX();
        achievementsTable.add(new Label("Progress", game.getSkin())).padLeft(20);
        achievementsTable.add(new Label("Status", game.getSkin())).padLeft(20);
        achievementsTable.row();

        achievementsTable.add(new Label("", game.getSkin())).height(1).colspan(4).fillX().row();

        for (Achievement a : achievements) {
            String unlockedText = a.isUnlocked() ? "UNLOCKED" : "LOCKED";

            Label nameLabel = new Label(a.getName(), game.getSkin());
            achievementsTable.add(nameLabel).padRight(20).top();

            Label descLabel = new Label(a.getDescription(), game.getSkin());
            descLabel.setWrap(true);
            achievementsTable.add(descLabel).expandX().fillX().top();

            Table progressCell = new Table();
            Label progressText = new Label(a.getProgressText(), game.getSkin());

            progressCell.add(progressText);
            achievementsTable.add(progressCell).padLeft(20).top();

            Label statusLabel = new Label(unlockedText, game.getSkin());
            statusLabel.setColor(a.isUnlocked() ? Color.CLEAR : Color.GRAY);
            achievementsTable.add(statusLabel).padLeft(20).top();

            achievementsTable.row();
            achievementsTable.add(new Label("", game.getSkin())).height(1).colspan(4).fillX().padBottom(5).row();
        }
    }

    /**
     * Updates the achievements display with current progress and unlocked counts.
     * Refreshes the table data and updates the scrollable content.
     */
    public void updateAchievements() {
        unlockedCountLabel.setText("Unlocked: " +
                playerStats.getUnlockedAchievementsCount() + "/" + playerStats.getAchievements().size);
        createAchievementsTable();
        scrollPane.setActor(achievementsTable);
    }

    /**
     * Called when this screen becomes visible.
     * Refreshes achievement data and sets the input processor.
     */
    @Override
    public void show() {
        updateAchievements();
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Renders the achievements screen by clearing the display and drawing the stage.
     *
     * @param delta The time in seconds since the last render
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
     * @param width  The new screen width in pixels
     * @param height The new screen height in pixels
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Called when the screen loses focus.
     */
    @Override
    public void pause() {
    }

    /**
     * Called when the screen regains focus.
     */
    @Override
    public void resume() {
    }

    /**
     * Called when the screen is no longer visible.
     */
    @Override
    public void hide() {
    }

    /**
     * Disposes of all resources used by the achievements screen.
     */
    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
    }
}