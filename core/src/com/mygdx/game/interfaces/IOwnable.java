package com.mygdx.game.interfaces;

import com.mygdx.game.component.Colony;

/**
 * Created by Paha on 5/19/2015.
 */
public interface IOwnable {
    void addedToColony(Colony colony);
    Colony getOwningColony();
}
