package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.Grid;
import com.mygdx.game.util.gui.GUI;

/**
 * Created by Paha on 2/19/2015.
 * <p>The screen where the blue circle thingy spins. Loads all assets and files before displaying the main menu.</p>
 */
public class PreLoadingScreen implements Screen{
    private DataBuilder builder;
    private ColonyGame game;

    private Texture loading;
    private int rotation = 0, rotationSpeed = -4;

    public PreLoadingScreen(ColonyGame game){
        this.game = game;
    }

    @Override
    public void show() {
        //Load the assets first.
        builder = new DataBuilder(ColonyGame.assetManager);
        builder.loadAllAssets();
        this.loading = new Texture(Gdx.files.internal("img/misc/loading.png"), true);
        this.loading.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void render(float delta) {
        int midX = Gdx.graphics.getWidth()/2, midY = Gdx.graphics.getHeight()/2;

        ColonyGame.batch.setProjectionMatrix(ColonyGame.UICamera.combined);
        ColonyGame.batch.begin();
        //Draw loading circle.
        ColonyGame.batch.draw(loading, midX - 64, midY - 64, 64, 64, 128, 128, 1, 1, rotation, 0, 0, 128, 128, false, false);
        rotation += rotationSpeed;
        ColonyGame.batch.end();

        //When the assets are done being loaded, load all the config/Json files and change the screen.
        if(builder.update()) {
            builder.loadAllFiles();
            GUI.init();
            ColonyGame.worldGrid = Grid.newGridInstance("grid", Constants.GRID_WIDTH, Constants.GRID_HEIGHT, Constants.GRID_SQUARESIZE);
            this.game.setScreen(new MainMenuScreen(this.game));
        }
    }

    @Override
    public void resize(int width, int height) {
        ColonyGame.UICamera.setToOrtho(false, width, height);
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
