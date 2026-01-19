package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.KeyBindings;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * The SettingsScreen class allows players to configure key bindings for game controls.
 */
public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final KeyBindings binds = KeyBindings.load();

    private final String[] items = {
            "Move Up",
            "Move Down",
            "Move Left",
            "Move Right",
            "Attack",
            "Sprint",
            "Back"
    };

    private final TextButton[] buttons;
    private final Label statusLabel;

    private int i = 0;
    private boolean waiting = false;

    /**
     * Constructor for SettingsScreen.
     *
     * @param game The main game instance.
     */
    public SettingsScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());
        stage.addActor(game.menuImage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        buttons = new TextButton[items.length];
        for (int idx = 0; idx < items.length; idx++) {
            String text = getButtonText(idx);
            TextButton button = new TextButton(text, game.getSkin());
            buttons[idx] = button;
            table.add(button).width(320).padBottom(20).row();
            final int index = idx;
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (index == items.length - 1) {
                        game.setScreen(new MenuScreen(game));
                    } else {
                        waiting = true;
                        i = index;
                        updateStatus();
                    }
                }
            });
        }

        statusLabel = new Label("", game.getSkin());
        table.add(statusLabel).padTop(40).row();
        updateStatus();
    }

    /**
     * Generates the text displayed on each key binding button.
     *
     * @param idx The index of the control in the items array.
     */
    private String getButtonText(int idx) {
        if (idx == items.length - 1) return items[idx];
        return items[idx] + ": " + Input.Keys.toString(getCurrentBind(idx));
    }

    /**
     * Updates all button texts.
     */
    private void updateButtons() {
        for (int idx = 0; idx < items.length; idx++) {
            buttons[idx].setText(getButtonText(idx));
        }
    }

    /**
     * Updates the status label based on current interaction state.
     */
    private void updateStatus() {
        if (waiting) {
            statusLabel.setText("Press a key (ESC to cancel)");
        } else {
            statusLabel.setText("Click to change, ESC to return");
        }
    }

    /**
     * Handles keyboard input for navigation and key binding changes.
     */
    private void handleInput() {
        if (!waiting) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                game.setScreen(new MenuScreen(game));
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                waiting = false;
                updateStatus();
                return;
            }
            for (int k = 0; k <= Input.Keys.MAX_KEYCODE; k++) {
                if (Gdx.input.isKeyJustPressed(k)) {
                    applyBind(i, k);
                    binds.save();
                    waiting = false;
                    updateButtons();
                    updateStatus();
                    break;
                }
            }
        }
    }

    /**
     * Applies a new key binding to the specified control.
     *
     * @param i   The index of the control being modified.
     * @param key The new key code to assign.
     */
    private void applyBind(int i, int key) {
        switch (i) {
            case 0 -> binds.UP = key;
            case 1 -> binds.DOWN = key;
            case 2 -> binds.LEFT = key;
            case 3 -> binds.RIGHT = key;
            case 4 -> binds.ATTACK = key;
            case 5 -> binds.SPRINT = key;
        }
    }

    /**
     * Retrieves the current key binding for a specified control.
     *
     * @param i The index of the control.
     */
    private int getCurrentBind(int i) {
        return switch (i) {
            case 0 -> binds.UP;
            case 1 -> binds.DOWN;
            case 2 -> binds.LEFT;
            case 3 -> binds.RIGHT;
            case 4 -> binds.ATTACK;
            case 5 -> binds.SPRINT;
            default -> -1;
        };
    }

    /**
     * Renders the settings screen by processing input and drawing the stage.
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.255f, 0.286f, 0.349f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        handleInput();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
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

    /**
     * Disposes of all resources used by the settings screen.
     */
    @Override
    public void dispose() {
        stage.dispose();
    }
}
