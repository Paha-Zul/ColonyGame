package com.mygdx.game.util.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.ui.PlayerInterface;

/**
 * Created by Paha on 8/12/2015.
 * An abstract Window class for displaying UI elements.
 */
public abstract class Window{
    protected boolean active;
    protected int mousedState = 0;
    protected PlayerInterface playerInterface;

    public Window(PlayerInterface playerInterface){
        this.playerInterface = playerInterface;
    }

    public abstract void update(float delta, SpriteBatch batch);
    public abstract void resize(int width, int height);
    public boolean mousedOver(){
        return this.active && this.mousedState > 0;
    }
}
