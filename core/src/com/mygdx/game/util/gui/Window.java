package com.mygdx.game.util.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.util.GH;

/**
 * Created by Paha on 8/12/2015.
 * An abstract Window class for displaying UI elements.
 */
public abstract class Window{
    protected boolean active;
    protected int mousedState = 0;
    protected PlayerInterface playerInterface;
    protected Rectangle mainWindowRect;
    protected Rectangle dragWindowRect;

    private float xOffset, yOffset;

    public Window(PlayerInterface playerInterface){
        this.playerInterface = playerInterface;
    }

    public void update(float delta, SpriteBatch batch){
        this.mousedState = 0;

        if(this.mainWindowRect != null)
            this.recordMouseState(GUI.getState(this.mainWindowRect));
    }

    public void dragWindow(){
        if(this.mainWindowRect == null || this.dragWindowRect == null) return;

        if(GUI.getState(this.dragWindowRect) == GUI.DOWN){
            Vector2 mouse = GH.getFixedScreenMouseCoords();
            float xOffset = mouse.x - this.mainWindowRect.x;
            float yOffset = mouse.y - this.mainWindowRect.y;

        }
    }

    public abstract void resize(int width, int height);

    public boolean mousedOver(){
        return this.active && this.mousedState > 0;
    }

    protected void recordMouseState(int state){
        this.mousedState = this.mousedState > state ? this.mousedState : state;
    }

    protected void setMainWindowRect(Rectangle mainWindowRect){
        this.mainWindowRect = mainWindowRect;
        this.dragWindowRect = new Rectangle(mainWindowRect.x, mainWindowRect.y + mainWindowRect.height - mainWindowRect.height*0.05f, mainWindowRect.width, mainWindowRect.height*0.05f);
    }
}
