package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.mygdx.game.client.ClientPlayer;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.server.ServerPlayer;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class GameScreen implements Screen{
    ServerPlayer serverPlayer;
    ClientPlayer clientPlayer;

    public static float scaleX;
    public static float scaleY;

    public GameScreen(){
        if(ExploreGame.server)
            serverPlayer = new ServerPlayer(ExploreGame.batch, ExploreGame.renderer);
        else
            clientPlayer = new ClientPlayer();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if(ExploreGame.server)
            serverPlayer.render(delta);
        else
            clientPlayer.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("Resizing from game screen");
        //Resizes all the GUI elements of the game (hopefully!)
        ArrayList<IGUI> list = ListHolder.getGUIList();
        for(int i=0;i< list.size();i++){
            list.get(i).resize(width, height);
        }

        scaleX = 1920/width;
        scaleY = 1080/height;
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
