package com.mygdx.game.component;

import com.mygdx.game.helpers.Constants;
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

        stats.addStat("health", 100, 100);
        stats.addStat("food", 100, 100);
        stats.addStat("water", 100, 100);

        //Remove its animal properties and make it a resource.
        stats.getStat("health").onZero = () -> {
            Interactable interactable = this.owner.getComponent(Interactable.class);

            this.owner.removeTag(Constants.ENTITY_ANIMAL); //Remove the animal tag
            this.owner.addTag(Constants.ENTITY_RESOURCE); //Add the resource tag
            this.owner.addComponent(new Resource(this.animalRef)); //Add a Resource Component.
            if(interactable != null) interactable.changeType("resource");
            this.owner.destroyComponent(BehaviourManagerComp.class); //Destroy the BehaviourManagerComp
            this.owner.destroyComponent(Stats.class); //Destroy the Stats component.
            this.owner.destroyComponent(Animal.class); //Destroy this (Animal) Component.
        };

        this.setActive(false);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
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
    public String getStatsText() {
        return null;
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

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void setToDestroy() {
        super.setToDestroy();
    }
}
