package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.ui.MainMenuInterface;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.GUI;

/**
 * Created by Paha on 1/10/2015.
 */
public class MainMenuScreen implements Screen {
    private ColonyGame game;

    public MainMenuScreen(ColonyGame game) {
        super();

        this.game = game;
    }

    @Override
    public void show() {
        MainMenuInterface menuInterface = new MainMenuInterface(this.game.batch, this.game);
        Entity ent = new Entity(new Vector2(0,0), 0, 20, menuInterface);

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {
        Gdx.graphics.setDisplayMode(width, height, false);
        ColonyGame.camera.setToOrtho(false, width, height);
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

    @Override
    public void dispose() {

    }
}
