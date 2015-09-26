package com.mygdx.game.util.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.ui.*;
import com.sun.istack.internal.Nullable;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created by Paha on 8/13/2015.
 * A manager for the Windows that I've been creating...
 */
public class WindowManager {
    private Stack<Window> windowStack;
    private Array<Window> windowList;
    private boolean mousedOver = false;

    public WindowManager(){
        this.windowList = new Array<>();
        this.windowStack = new Stack<>();
    }

    public void update(SpriteBatch batch){
        this.mousedOver = false;

        //TODO We're going to need a way to limit dragging to one window. There's nothing stopping from the mouse passing over
        //TODO 2 windows and dragging them both. Maybe some 'startedDragging' and 'stoppedDragging' methods with a reference to the
        //TODO currently dragging windows?

        //Update the list. We the weird assignment means that if the moused over boolean is ever true this update, it stays true.
        this.windowList.forEach(window -> this.mousedOver = window.update(batch) || this.mousedOver);

        Iterator<Window> iter = this.windowStack.iterator();
        while(iter.hasNext()){
            Window window = iter.next();
            if(window.isDestroyed()) iter.remove();
            else window.update(batch);
        }
    }

    /**
     * Adds a window to the self managing list. Each window in this list is added once and depends on it's update() method to
     * activate/deactive itself. It is never removed from the list. These are for windows like the ColonyWindow that never change and only
     * are added to display one particular target (the colony).
     * @param window The Window to add.
     */
    public void addWindowToSelfManagingList(Window window){
        this.windowList.add(window);
    }

    /**
     * Adds a Window with a target to a stack but will not add a Window that is of the same type and also has the same target.
     * @param clazz The Class type to create.
     * @param target The Entity target of the window. Can be null.
     * @param pi The PlayerInterface for the Window to reference.
     * @param <T> T.
     * @return True if the Window was added and added, false otherwise.
     */
    public <T extends Window> boolean addWindowIfNotExistByTarget(Class<T> clazz, @Nullable Entity target, PlayerInterface pi){
        //TODO We want to check for class type AND target. What if I wanted a crafting window and information window open on the same target?
        //Search for any duplicate window. If found, return false.
        for(Window window : this.windowStack)
            if(window.getTarget() == target && window.getClass() == clazz)
                return false;

        //Let's create our window!
        Window window = null;
        if(clazz == ColonyWindow.class)
            window = new ColonyWindow(pi, target);
        else if(clazz == CraftingWindow.class)
            window = new CraftingWindow(pi, target);
        else if(clazz == PlacingConstructionWindow.class)
            window = new PlacingConstructionWindow(pi, target);

        //Push the window and force a resize. Many windows initially set many of their sizes by using the resize method.
        this.windowStack.push(window);
        window.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        return true;
    }

    /**
     * Removes the topmost Window of the stack.
     */
    public boolean removeTopMostWindow(){
        if(!this.windowStack.empty()) {
            this.windowStack.pop().destroy();
            return true;
        }

        return false;
    }

    public boolean isWindowStackEmpty(){
        return this.windowStack.empty();
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
