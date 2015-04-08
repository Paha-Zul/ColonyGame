package com.mygdx.game.component;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.EventSystem;
import com.mygdx.game.interfaces.IInteractable;

/**
 * Created by Paha on 2/26/2015.
 */
public class Animal extends Component implements IInteractable{
    private BehaviourManagerComp behComp;
    private Stats stats;
    private DataBuilder.JsonAnimal animalRef;
    private Collider collider;

    public Animal(DataBuilder.JsonAnimal animalRef) {
        super();
        this.animalRef = animalRef;
    }

    @Override
    public void start() {
        super.start();

        this.stats = this.getComponent(Stats.class);
        this.behComp = this.getComponent(BehaviourManagerComp.class);
        this.collider = this.getComponent(Collider.class);

        behComp.getBlackBoard().attackRange = 10f;

        addCircleSensor();

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

        //Add a collide_start event for getting hit by a projectile.
        EventSystem.registerEntityEvent(this.owner, "collide_start", args -> {
            Fixture mine = (Fixture) args[0];
            Fixture other = (Fixture) args[1]; //Get the other entity.

            Entity otherEnt = (Entity) other.getUserData();

            //If the other Entity is a projectile, kill both of us!
            if (!mine.isSensor() && otherEnt.hasTag(Constants.ENTITY_PROJECTILE) && this.owner.hasTag(Constants.ENTITY_ANIMAL)) {
                this.getComponent(Stats.class).getStat("health").addToCurrent(-50);
                otherEnt.setToDestroy();
            } else if (mine.isSensor() && otherEnt.hasTag(Constants.ENTITY_COLONIST)) {
                if (behComp.getBlackBoard().target != null) return;
                behComp.getBlackBoard().target = otherEnt;
                behComp.attack();
            }
        });

        this.setActive(false);
    }

    public void addCircleSensor(){
        CircleShape circle = new CircleShape();
        circle.setRadius(10f);
        Fixture sensor = collider.body.createFixture(circle, 1f);
        sensor.setSensor(true);
        sensor.setUserData(this.owner);
        circle.dispose();
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
        return this.behComp;
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
