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

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("AWESOME GAME", game.getSkin(), "title");
        table.add(title).padBottom(80).row();

        String[] menuItems = {"Load Map", "New Endless", "Stats & Upgrades", "Leaderboard", "Settings", "Exit"};

        for(String item : menuItems) {
            TextButton button = new TextButton(item, game.getSkin());
            table.add(button).width(320).padBottom(20).row();

            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    switch(item) {
                        case "Load Map" -> game.goToGame();
                        case "New Endless" -> game.goToEndlessGame();
                        case "Stats & Upgrades" -> game.goToStats();
                        case "Leaderboard" -> game.goToLeaderboard();
                        case "Settings" -> game.goToSettings();
                        case "Exit" -> Gdx.app.exit();
                    }
                }
            });
        }
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
