package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.entity.Entity;

/**
 * Created by Paha on 8/12/2015.
 * An abstract Window class for displaying UI elements.
 */
public abstract class Window{
    protected boolean active=true, draggable = false;
    protected PlayerInterface playerInterface;
    //The target of the window. This doesn't need to be assign for non-entity things (like colony inventory screen), but should be for things like crafting windows.
    protected Entity target;

    public Window(PlayerInterface playerInterface){
        this.playerInterface = playerInterface;
    }

    public Window(PlayerInterface playerInterface, Entity target){
        this.playerInterface = playerInterface;
        this.target = target;
    }

    /**
     * Updates the window.
     * @param batch The SpriteBatch to draw with.
     * @return True if the window was moused over. This can be used to stop any further input checks on other windows. False otherwise.
     */
    public boolean update(SpriteBatch batch){
        return false;
    }

    /**
     * Resizes the window.
     * @param width The width of the game window.
     * @param height The height of the game window.
     */
    public abstract void resize(int width, int height);

    public void destroy(){

    }

    public final void setTarget(Entity target){
        this.target = target;
    }

    public final Entity getTarget(){
        return this.target;
    }
}
