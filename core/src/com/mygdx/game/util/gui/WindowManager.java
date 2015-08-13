package com.mygdx.game.util.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Paha on 8/13/2015.
 * A manager for the Windows that I've been creating...
 */
public class WindowManager {
    private Array<Window> windowList;
    private boolean mousedOver = false;

    public WindowManager(){
        this.windowList = new Array<>();
    }

    public void update(SpriteBatch batch){
        this.mousedOver = false;

        //Update the list. We the weird assignment means that if the moused over boolean is ever true this update, it stays true.
        this.windowList.forEach(window -> this.mousedOver = window.update(batch) || this.mousedOver);
    }

    public void addWindow(Window window){
        this.windowList.add(window);
    }

    public boolean removeWindow(Window window){
        return this.windowList.removeValue(window, true);
    }

    public boolean isMousedOver(){
        return this.mousedOver;
    }

    public void resize(int width, int height){
        this.windowList.forEach(window -> window.resize(width, height));
    }

}
