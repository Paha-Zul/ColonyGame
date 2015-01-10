package com.mygdx.game.component.ui;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.helpers.GUI;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.screens.LoadingScreen;

/**
 * Created by Paha on 1/10/2015.
 */
public class MainMenuInterface extends Component implements IGUI{
    public static Texture mainMenuTexture = new Texture("img/Space2.png");
    public static Music music = Gdx.audio.newMusic(Gdx.files.internal("music/Karkarakacrrot.ogg"));

    private SpriteBatch batch;
    private ColonyGame game;

    private Rectangle startRect;
    private Rectangle quitRect;

    public MainMenuInterface(SpriteBatch batch, ColonyGame game) {
        super();

        this.batch = batch;
        this.game = game;
    }

    @Override
    public void start() {
        super.start();

        this.addToList();
        this.startRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25, 200, 50);
        this.quitRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25 - 100, 200, 50);
        music.play();
        music.setLooping(true);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.batch.draw(mainMenuTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if(GUI.Button(startRect, null, "Start", this.batch)){
            this.game.setScreen(new LoadingScreen(this.game));
            this.owner.destroy();
        }

        if(GUI.Button(quitRect, null, "Quit", this.batch)){
            Gdx.app.exit();
        }

    }

    @Override
    public void destroy() {
        super.destroy();

        music.stop();
        music.dispose();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void addToList() {
        ListHolder.addInterface(this);
    }


}
