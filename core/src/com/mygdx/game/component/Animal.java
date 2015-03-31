package com.mygdx.game.component;

import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.interfaces.IInteractable;

/**
 * Created by Paha on 2/26/2015.
 */
public class Animal extends Component implements IInteractable{
    private BehaviourManagerComp behComp;
    private Stats stats;
    private DataBuilder.JsonAnimal animalRef;

    public Animal(DataBuilder.JsonAnimal animalRef) {
        super();

        this.animalRef = animalRef;
    }

    @Override
    public void start() {
        super.start();

        this.stats = this.getComponent(Stats.class);
        this.setActive(false);

        //EventSystem.registerEvent(this.owner, "collidestart", args -> this.owner.setToDestroy());
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public Stats getStats() {
        return this.stats;
    }

    @Override
    public Skills getSkills() {
        return null;
    }

    @Override
    public String getName() {
        return this.getEntityOwner().name;
    }

    public DataBuilder.JsonAnimal getAnimalRef() {
        return animalRef;
    }

    @Override
    public BehaviourManagerComp getBehManager() {
        return null;
    }
}
