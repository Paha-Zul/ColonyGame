package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;
import com.mygdx.game.interfaces.IInteractable;
import com.sun.istack.internal.NotNull;

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
