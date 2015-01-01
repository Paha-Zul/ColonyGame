package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.LoadingInterface;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.WorldGen;

/**
 * Created by Bbent_000 on 12/29/2014.
 * The Screen responsible for loading the game and displaying the progress of loading.
 */
public class LoadingScreen implements Screen {
    private ColonyGame game;

    public LoadingScreen(final ColonyGame game){
        this.game = game;
    }

    @Override
    public void show() {
        WorldGen.tileSize = 25;
        WorldGen.freq = 20;
        WorldGen.init((long)(MathUtils.random()*Long.MAX_VALUE));
        WorldGen.numStep = 50;

        LoadingInterface inter = new LoadingInterface(game.batch);
        Entity ent = new Entity(new Vector2(0,0), 0, 15, inter);
    }

    @Override
    public void render(float delta) {
        if(WorldGen.generateTerrain()) {
            LoadingInterface.setDone();
            game.setScreen(new GameScreen(this.game));
        }
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
