package com.mygdx.game.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by Paha on 1/18/2015.
 */
public interface IDisplayable {
    void display(Rectangle rect, SpriteBatch batch, String name);

}
