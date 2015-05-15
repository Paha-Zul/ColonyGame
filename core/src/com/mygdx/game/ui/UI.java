package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.interfaces.IDestroyable;
import com.mygdx.game.interfaces.IGUI;

/**
 * Created by Paha on 1/13/2015.
 */
public abstract class UI implements IGUI, IDestroyable{
    protected boolean destroyed = false;
    protected SpriteBatch batch;

    public UI(SpriteBatch batch){
        this.batch = batch;

        this.addToList();
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
    }

    @Override
    public void addToList() {
        ListHolder.addGUI(this);
    }

    @Override
    public void destroy() {
        this.destroyed = true;
    }

    @Override
    public final boolean isDestroyed() {
        return destroyed;
    }
}
