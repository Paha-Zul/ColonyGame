package com.mygdx.game.screens;

import com.badlogic.gdx.Screen;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.Grid;

/**
 * Created by Paha on 2/19/2015.
 */
public class PreLoadingScreen implements Screen{
    private DataBuilder builder;
    private ColonyGame game;

    public PreLoadingScreen(ColonyGame game){
        this.game = game;
    }

    @Override
    public void show() {
        builder = new DataBuilder(ColonyGame.assetManager);
        builder.loadFiles();
    }

    @Override
    public void render(float delta) {
        if(builder.update()) {
            ColonyGame.worldGrid = Grid.newGridInstance("grid", Constants.GRID_WIDTH, Constants.GRID_HEIGHT, Constants.GRID_SQUARESIZE);
            this.game.setScreen(new MainMenuScreen(this.game));
        }
    }

    @Override
    public void resize(int width, int height) {

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
