package com.mygdx.game.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.gui.GUI;

/**
 * Created by Paha on 1/18/2015.
 */
public interface IDisplayable {
    void display(Rectangle rect, SpriteBatch batch, String name, GUI.GUIStyle style);
    void display(float x, float y, float width, float height, SpriteBatch batch, String name, GUI.GUIStyle style);
}
