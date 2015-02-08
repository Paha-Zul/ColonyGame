package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Screen;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.client.ClientPlayer;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.server.ServerPlayer;
import com.mygdx.game.ui.UI;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class GameScreen implements Screen{
    private ServerPlayer serverPlayer;
    private ClientPlayer clientPlayer;
    private ColonyGame game;

    public GameScreen(final ColonyGame game){

        if(ColonyGame.server)
            serverPlayer = new ServerPlayer(ColonyGame.batch, ColonyGame.renderer, this.game);
        else
            clientPlayer = new ClientPlayer();

        this.game = game;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if(ColonyGame.server)
            serverPlayer.render(delta);
        else
            clientPlayer.render(delta);
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
