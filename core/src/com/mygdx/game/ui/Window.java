package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.gui.GUI;

/**
 * Created by Paha on 8/12/2015.
 * An abstract Window class for displaying UI elements.
 */
public abstract class Window{
    protected boolean active=true, draggable = false;
    protected int mousedState = 0;
    protected PlayerInterface playerInterface;
    protected Rectangle mainWindowRect;
    protected Rectangle dragWindowRect;
    //The target of the window. This doesn't need to be assign for non-entity things (like colony inventory screen), but should be for things like crafting windows.
    protected Entity target;

    private float xOffset, yOffset;
    private boolean dragging = false;

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
        this.mousedState = 0;

        if(this.active) {
            ///If we have a main window, record the mouse state of the main window.
            if (this.mainWindowRect != null)
                this.recordMouseState(GUI.getState(this.mainWindowRect));

            //If we want this window to be draggable, drag it!
            if (this.draggable)
                this.dragWindow();
        }

        //Return if something was moused over or not.
        return this.mousedState > 0;
    }

    /**
     * Drags the window using the mainWindowRect and dragWindowRect. If either of these are null, the dragging will not happen.
     */
    protected void dragWindow(){
        if(this.mainWindowRect == null || this.dragWindowRect == null) return;

        //Get the state in reference to the rectangle and in general...
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

    /**
     * Resizes the window.
     * @param width The width of the game window.
     * @param height The height of the game window.
     */
    public abstract void resize(int width, int height);

    /**
     * Records the mouse state, keeping the highest of the two (either the incoming 'state' input, or the existing mouseState).
     * @param state The incoming state of the mouse from interacting with a window.
     */
    protected final void recordMouseState(int state){
        this.mousedState = this.mousedState > state ? this.mousedState : state;
    }

    /**
     * Sets the main window and dragWindowRect for use.
     * @param mainWindowRect The main window which the dragWindowRect will use for making it draggable (if enabled).
     */
    protected final void setMainWindowRect(Rectangle mainWindowRect){
        this.mainWindowRect = mainWindowRect;
        this.dragWindowRect = new Rectangle(mainWindowRect.x, mainWindowRect.y + mainWindowRect.height - mainWindowRect.height*0.05f, mainWindowRect.width, mainWindowRect.height*0.05f);
    }

    public final void setTarget(Entity target){
        this.target = target;
    }

    public final Entity getTarget(){
        return this.target;
    }
}
