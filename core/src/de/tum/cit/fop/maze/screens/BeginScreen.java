package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.PlayerStats;

/**
 * The BeginScreen class is the initial screen shown when the game starts.
 * Prompts the player to enter their name and initialises their player statistics.
 */
public class BeginScreen implements Screen {
    public static PlayerStats STATS;

    private final Stage stage;
    private final MazeRunnerGame game;

    /**
     * Constructs a new BeginScreen to initialise player profile and statistics.
     *
     * @param game The main MazeRunnerGame instance for screen management
     */
    public BeginScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();
        stage = new Stage(new ExtendViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        showPlayerNameDialog();
    }

    /**
     * Displays a dialogue box prompting the player to enter their name.
     * Includes options to proceed with the entered name or cancel/exit the application.
     */
    private void showPlayerNameDialog() {
        Dialog dialog = new Dialog("", game.getSkin());
        dialog.getTitleLabel().setAlignment(Align.center);

        Table table = new Table();

        Label nameLabel = new Label("Enter Player Name:", game.getSkin());
        final TextField nameField = new TextField("", game.getSkin());
        nameField.setMaxLength(20);

        table.add(nameLabel).padBottom(5).row();
        table.add(nameField).width(300).padBottom(20).row();

        dialog.getContentTable().add(table);

        TextButton open = new TextButton("Open", game.getSkin());
        TextButton cancel = new TextButton("Cancel", game.getSkin());

        cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                Gdx.app.exit();
            }
        });

        open.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    STATS = new PlayerStats(name);
                    STATS.save();
                } else {
                    STATS = new PlayerStats("Player");
                    STATS.save();
                }
                dialog.hide();
                game.goToMenu();
            }
        });

        dialog.getButtonTable().defaults().width(150).pad(5);
        dialog.getButtonTable().add(cancel);
        dialog.getButtonTable().add(open);

        dialog.show(stage);

        dialog.setPosition(
                stage.getWidth() / 2 - dialog.getWidth() / 2,
                stage.getHeight() / 2 - dialog.getHeight() / 2
        );

        stage.setKeyboardFocus(nameField);
        nameField.setCursorPosition(nameField.getText().length());
    }

    /**
     * Renders the begin screen by clearing the display and drawing the stage.
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
     * Disposes of all resources used by the begin screen.
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