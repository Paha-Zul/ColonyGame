package com.mygdx.game.component;

import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;

/**
 * Created by Paha on 5/19/2015.
 */
public class Building extends Component implements IOwnable, IInteractable{
    private Colony colonyOwner;
    private Inventory inventory;

    @Override
    public void start() {
        super.start();
        this.setActive(false);

        this.inventory = this.getComponent(Inventory.class);
    }

    @Override
    public void addedToColony(Colony colony) {
        this.colonyOwner = colony;
    }

    @Override
    public Colony getOwningColony() {
        return this.colonyOwner;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public Stats getStats() {
        return null;
    }

    @Override
    public String getStatsText() {
        return null;
    }

    @Override
    public Skills getSkills() {
        return null;
    }

    @Override
    public String getName() {
        return ListHolder.getIdToEntityMap().get(ownerID).name;
    }

    @Override
    public BehaviourManagerComp getBehManager() {
        return null;
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
