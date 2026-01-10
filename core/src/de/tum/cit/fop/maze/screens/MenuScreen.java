package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * The MenuScreen class provides the main menu interface for the game.
 */
public class MenuScreen implements Screen {

    private final Stage stage;

    /**
     * Constructor for MenuScreen.
     *
     * @param game The main game class.
     */
    public MenuScreen(MazeRunnerGame game) {
        var camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Label title = new Label("AWESOME GAME", game.getSkin(), "title");
        mainTable.add(title).padBottom(80).row();
        Table gridTable = new Table();
        gridTable.defaults().pad(10);

        String[][] menuItems = {
                {"Load Map", "New Endless"},
                {"Stats/Upgrades", "Leaderboard"},
                {"Achievements", "Settings"},
                {"Exit", null}
        };

        float buttonWidth = 300f;
        float buttonHeight = 60f;
        float exitButtonWidth = buttonWidth * 2 + 10;

        for(int row = 0; row < menuItems.length; row++) {
            for(int col = 0; col < 2; col++) {
                String item = menuItems[row][col];
                if(item != null) {
                    TextButton button = new TextButton(item, game.getSkin());
                    if(row == menuItems.length - 1 && "Exit".equals(item)) {
                        gridTable.add(button)
                                .colspan(2)
                                .width(exitButtonWidth)
                                .height(buttonHeight)
                                .padBottom(20)
                                .center();
                    } else {
                        gridTable.add(button)
                                .width(buttonWidth)
                                .height(buttonHeight)
                                .padBottom(20);
                        if(col == 0) {
                            gridTable.getCell(button).padRight(10);
                        }
                    }

                    button.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            switch(item) {
                                case "Load Map" -> game.goToGame();
                                case "New Endless" -> game.goToEndlessGame();
                                case "Stats/Upgrades" -> game.goToStats();
                                case "Leaderboard" -> game.goToLeaderboard();
                                case "Achievements" -> game.goToAchievement();
                                case "Settings" -> game.goToSettings();
                                case "Exit" -> Gdx.app.exit();
                            }
                        }
                    });
                } else if(col == 0) {
                    gridTable.add().width(buttonWidth).height(0);
                }
            }
            gridTable.row();
        }

        mainTable.add(gridTable).center();
    }

    /**
     * Renders the menu screen by clearing the display and drawing the stage.
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.46f, 0.23f, 0.21f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Handles screen resizing by updating the stage's viewport.
     * @param width The new screen width.
     * @param height The new screen height.
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
     * Shows the screen and sets the input processor.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
}
