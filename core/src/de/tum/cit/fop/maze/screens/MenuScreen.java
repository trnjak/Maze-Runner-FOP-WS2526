package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * The MenuScreen class provides the main navigation interface for the game.
 * Displays a grid of menu options for starting games, accessing features, and exiting.
 */
public class MenuScreen implements Screen {

    private final Stage stage;

    /**
     * Constructs a new MenuScreen with navigation buttons and background.
     *
     * @param game The main MazeRunnerGame instance for screen management
     */
    public MenuScreen(MazeRunnerGame game) {
        var camera = new OrthographicCamera();
        stage = new Stage(new ExtendViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());
        stage.addActor(game.menuImage);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Label title = new Label("Hayley's Curse", game.getSkin(), "title");
        mainTable.add(title).padBottom(80).row();
        Table gridTable = new Table();
        gridTable.defaults().pad(10);

        String[][] menuItems = {
                {"Start Game", "Load Map"},
                {"Stats/Upgrades", "Leaderboard"},
                {"Achievements", "Settings"},
                {"Story", "Exit"}
        };

        float buttonWidth = 320f;
        float buttonHeight = 60f;

        for (String[] menuItem : menuItems) {
            for (int col = 0; col < 2; col++) {
                String item = menuItem[col];
                if (item != null) {
                    TextButton button = new TextButton(item, game.getSkin());
                    gridTable.add(button)
                            .width(buttonWidth)
                            .height(buttonHeight)
                            .padBottom(20);
                    if (col == 0) {
                        gridTable.getCell(button).padRight(10);
                    }

                    button.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            switch (item) {
                                case "Start Game" -> decide(game);
                                case "Load Map" -> game.goToGame();
                                case "Stats/Upgrades" -> game.goToStats();
                                case "Leaderboard" -> game.goToLeaderboard();
                                case "Achievements" -> game.goToAchievement();
                                case "Settings" -> game.goToSettings();
                                case "Story" -> game.goToStory();
                                case "Exit" -> Gdx.app.exit();
                            }
                        }
                    });
                } else if (col == 0) {
                    gridTable.add().width(buttonWidth).height(0);
                }
            }
            gridTable.row();
        }

        mainTable.add(gridTable).center();
    }

    /**
     * Determines the appropriate starting screen based on player progression.
     * New players are directed to the story screen, while returning players start endless mode.
     *
     * @param game The main MazeRunnerGame instance for screen transitions
     */
    private void decide(MazeRunnerGame game) {
        if (BeginScreen.STATS.getScore() == 0) {
            game.goToStory();
        } else {
            game.goToEndlessGame();
            Gdx.input.setInputProcessor(null);
        }
    }

    /**
     * Renders the menu screen by clearing the display and drawing the stage.
     *
     * @param delta The time in seconds since the last render
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.025f, 0.011f, 0.082f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
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
     * Disposes of all resources used by the menu screen.
     */
    @Override
    public void dispose() {
        stage.dispose();
    }

    /**
     * Shows the screen and sets the input processor to handle UI interactions.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
}