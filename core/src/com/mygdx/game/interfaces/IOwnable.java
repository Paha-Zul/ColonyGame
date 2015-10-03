package com.mygdx.game.interfaces;

import com.mygdx.game.component.Colony;

/**
 * Created by Paha on 5/19/2015.
 */
public interface IOwnable {
    /**
     * Called when an Entity that implements this interface gets added to a Colony.
     * @param colony The Colony the Entity was added to.
     */
    void addedToColony(Colony colony);
    Colony getOwningColony();
}
