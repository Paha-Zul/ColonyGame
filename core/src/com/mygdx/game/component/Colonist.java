package com.mygdx.game.component;

import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.interfaces.IInteractable;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colonist extends Component implements IInteractable{
    private Colony colony;
    private Inventory inventory;
    private Stats stats;
    private BehaviourManagerComp manager;
    private String firstName, lastName;

    public Colonist() {
        super();
        this.setActive(false);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void start() {
        super.start();

        this.inventory = this.getComponent(Inventory.class);
        this.stats = this.getComponent(Stats.class);
        this.manager = this.getComponent(BehaviourManagerComp.class);
        this.getComponent(BlackBoard.class).moveSpeed = 200f;

        stats.addStat("health", 100, 100);
        stats.addStat("food", 100, 100);
        stats.addStat("water", 100, 100);
        stats.addStat("energy", 100, 100);

        stats.addTimer(new RepeatingTimer(5f, () -> stats.getStat("food").addToCurrent(-1))); //Subtract food every 5 seconds
        stats.addTimer(new RepeatingTimer(10f, () -> stats.getStat("water").addToCurrent(-1))); //Subtract water every 10 seconds.
        //If food or water is 0, subtract health.
        stats.addTimer(new RepeatingTimer(10f, () -> {
            if (stats.getStat("food").getCurrVal() <= 0 || stats.getStat("water").getCurrVal() <= 0)
                stats.getStat("health").addToCurrent(-1);
        }));
    }

    public Colony getColony() {
        return colony;
    }

    public void setColony(Colony colony) {
        this.colony = colony;
    }

    public void setName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public Skills getSkills() {
        return null;
    }

    @Override
    public String getName() {
        return this.firstName;
    }

    public Inventory getInventory(){
        return this.inventory;
    }

    public Stats getStats(){
        return this.stats;
    }

    @Override
    public BehaviourManagerComp getBehManager() {
        return this.manager;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
