package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * The StoryScreen class displays the game's narrative, setting, and objectives to the player.
 * Provides a scrollable story interface with navigation options.
 */
public class StoryScreen implements Screen {
    private final Stage stage;

    /**
     * Constructs a new StoryScreen with game story and navigation controls.
     *
     * @param game The main MazeRunnerGame instance for screen management
     */
    public StoryScreen(MazeRunnerGame game) {
        var camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(game.WIDTH, game.HEIGHT, camera), game.getSpriteBatch());
        stage.addActor(game.menuImage);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Label title = new Label("STORY", game.getSkin(), "title");
        mainTable.add(title).padBottom(40).row();

        String story =
                """
                        In the shadow-haunted Castle of Zoloft, Princess Hayley finds herself trapped by an evil witch.
                        \s
                        The witch, jealous of the princess's courage, has woven a powerful curse upon Hayley's castle:
                        \s
                        > GHOSTS chasing anyone that dares to cross their path
                        > GIANT SPIDERS patrolling the premises
                        > SPIKE TRAPS making the escape painful
                        \s
                        But, the cruelest trick of all: THE EXIT DOOR changes location every 30 seconds!
                        \s
                        Princess Hayley, armed with only her wits and unwavering determination, must navigate this ever-changing labyrinth, \
                        avoid the witch's minions, and find the exit before it moves again. \
                        \s
                        Time is against her. The castle itself conspires to keep her prisoner.
                        \s
                        Can you help Princess Hayley escape the witch's wrath?
                        """;

        Label storyLabel = new Label(story, game.getSkin());
        storyLabel.setWrap(true);
        storyLabel.setAlignment(Align.top | Align.left);

        ScrollPane scrollPane = new ScrollPane(storyLabel, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsVisible(true);

        mainTable.add(scrollPane)
                .width(game.WIDTH * 0.8f)
                .height(game.HEIGHT * 0.6f)
                .padBottom(30)
                .row();

        Table buttonTable = new Table();
        buttonTable.defaults().pad(10).width(320).height(60);

        TextButton backButton = new TextButton("Back", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });

        TextButton startButton = new TextButton("Start", game.getSkin(), "default");
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.input.setInputProcessor(null);
                game.goToEndlessGame();
            }
        });

        buttonTable.add(backButton).padRight(20);
        buttonTable.add(startButton);

        mainTable.add(buttonTable).padTop(20);
    }

    /**
     * Renders the story screen and updates the stage.
     *
     * @param delta The time in seconds since the last render
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
     *
     * @param width  The new screen width in pixels
     * @param height The new screen height in pixels
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes of all resources used by the story screen.
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