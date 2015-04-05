package com.mygdx.game.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by Bbent_000 on 12/25/2014.
 * An interface that requires methods specific to GUI classes such as 'resize'.
 */
public interface IGUI {
    /**
     * Called when the application window is resized.
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */
    public void resize(int width, int height);

    /**
     * Adds this IGUI element to a list where it can be accessed when the screen is resized.
     */
    public void addToList();

    public void render(float delta, SpriteBatch batch);
}
