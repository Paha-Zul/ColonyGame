package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.ui.LoadingInterface;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.ui.UI;

import java.util.ArrayList;

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
        WorldGen.tileSize = Constants.GRID_SIZE;
        WorldGen.freq = 20;
        WorldGen.init((long)(MathUtils.random()*Long.MAX_VALUE));
        WorldGen.numStep = 50;

        new LoadingInterface(game.batch, this.game);
    }

    @Override
    public void render(float delta) {
        if(WorldGen.generateWorld()) {
            LoadingInterface.setDone();
            game.setScreen(new GameScreen(this.game));
        }
    }

    @Override
    public void resize(int width, int height) {
        Gdx.graphics.setDisplayMode(width, height, false);
        ColonyGame.camera.setToOrtho(false, width, height);
        ColonyGame.UICamera.setToOrtho(false, width, height);

        //Resizes all the GUI elements of the game (hopefully!)
        ArrayList<UI> list = ListHolder.getGUIList();
        for(int i=0;i< list.size();i++){
            list.get(i).resize(width, height);
        }
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
