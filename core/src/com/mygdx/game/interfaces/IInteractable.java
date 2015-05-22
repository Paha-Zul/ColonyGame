package com.mygdx.game.interfaces;

import com.mygdx.game.component.BehaviourManagerComp;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.Stats;

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
}
