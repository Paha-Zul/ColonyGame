package com.mygdx.game.interfaces;

import com.mygdx.game.component.BehaviourManagerComp;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.Skills;
import com.mygdx.game.component.Stats;

/**
 * Created by Paha on 3/6/2015.
 */
public interface IInteractable {
    public Inventory getInventory();
    public Stats getStats();
    public Skills getSkills();
    public String getName();
    public BehaviourManagerComp getBehManager();
}
