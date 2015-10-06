package com.mygdx.game.interfaces;

import com.mygdx.game.entity.Entity;

/**
 * Created by Paha on 10/6/2015.
 */
public interface IStartable {
    void added(Entity owner);
    void init();
    void start();
}
