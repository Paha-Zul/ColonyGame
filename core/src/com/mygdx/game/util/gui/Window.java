package com.mygdx.game.util.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.util.GH;

/**
 * Created by Paha on 8/12/2015.
 * An abstract Window class for displaying UI elements.
 */
public abstract class Window{
    protected boolean active, draggable = false;
    protected int mousedState = 0;
    protected PlayerInterface playerInterface;
    protected Rectangle mainWindowRect;
    protected Rectangle dragWindowRect;

    private float xOffset, yOffset;
    private boolean dragging = false;

    public Window(PlayerInterface playerInterface){
        this.playerInterface = playerInterface;
    }

    public void update(float delta, SpriteBatch batch){
        this.mousedState = 0;

        if(this.mainWindowRect != null)
            this.recordMouseState(GUI.getState(this.mainWindowRect));

        if(this.draggable)
            this.dragWindow(batch);
    }

    public void dragWindow(SpriteBatch batch){
        if(this.mainWindowRect == null || this.dragWindowRect == null) return;

        GUI.Texture(new TextureRegion(this.playerInterface.blueSquare), batch, this.dragWindowRect);
        int rectState = GUI.getState(this.dragWindowRect);
        int genState = GUI.getState();

        //If we are just down on the drag rectangle, start dragging!
        if(rectState == GUI.JUSTDOWN){
            Vector2 mouse = GH.getFixedScreenMouseCoords();
            this.xOffset = mouse.x - this.mainWindowRect.x;
            this.yOffset = mouse.y - this.mainWindowRect.y;
            this.dragging = true;

        //If we are dragging and haven't let up yet.. drag!
        }else if(this.dragging && genState != GUI.JUSTUP && genState != GUI.UP){
            Vector2 mouse = GH.getFixedScreenMouseCoords();
            this.mainWindowRect.setPosition(mouse.x - xOffset, mouse.y - yOffset);
            this.dragWindowRect = new Rectangle(mainWindowRect.x, mainWindowRect.y + mainWindowRect.height - mainWindowRect.height*0.05f, mainWindowRect.width, mainWindowRect.height*0.05f);

        //If we have let up, stop dragging.
        }else if(genState == GUI.UP || genState == GUI.JUSTUP){
            this.dragging = false;
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
