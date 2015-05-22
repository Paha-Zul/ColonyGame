package com.mygdx.game.component;

import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Created by Paha on 5/19/2015.
 */
public class Building extends Component implements IOwnable, IInteractable{
    @JsonIgnore
    private Colony colonyOwner;
    @JsonIgnore
    private Inventory inventory;

    public Building(){

    }

    @Override
    public void start() {
        super.start();
        this.setActive(false);
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        this.inventory = this.getComponent(Inventory.class);
    }

    @Override
    public void addedToColony(Colony colony) {
        this.colonyOwner = colony;
    }

    @Override
    @JsonIgnore
    public Colony getOwningColony() {
        return this.colonyOwner;
    }

    @Override
    @JsonIgnore
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    @JsonIgnore
    public Stats getStats() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getStatsText() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return this.owner.name;
    }

    @Override
    @JsonIgnore
    public BehaviourManagerComp getBehManager() {
        return null;
    }

    @Override
    @JsonIgnore
    public Component getComponent() {
        return this;
    }
}
