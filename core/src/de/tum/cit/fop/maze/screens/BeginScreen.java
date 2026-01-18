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
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.PlayerStats;

/**
 * The BeginScreen class is the initial screen shown when the game starts,
 * prompting the player to enter their name and initializing their player statistics.
 */
public class BeginScreen implements Screen {
    public static PlayerStats STATS;

    private final Stage stage;
    private final MazeRunnerGame game;

    /**
     * Constructor for BeginScreen.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public BeginScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        showPlayerNameDialog();
    }

    /**
     * Displays a dialog box prompting the player to enter their name, with options to proceed or cancel.
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
                if(!name.isEmpty()) {
                    STATS = new PlayerStats(name);
                    STATS.save();
                }
                else {
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
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.255f, 0.286f, 0.349f, 1);
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
     * Disposes of all resources used by the begin screen.
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