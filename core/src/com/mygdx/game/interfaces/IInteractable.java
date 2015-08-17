package com.mygdx.game.interfaces;

import com.mygdx.game.component.*;

/**
 * Created by Paha on 3/6/2015.
 */
public interface IInteractable {
    Inventory getInventory();
    Stats getStats();
    String getStatsText();
    String getName();
    BehaviourManagerComp getBehManager();
    Component getComponent();
    Constructable getConstructable();
    Craftable getCraftable();
    Building getBuilding();
    Enterable getEnterable();

}
