package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.DataBuilder;

/**
 * Created by Paha on 2/26/2015.
 */
public class AnimalEnt extends Entity{
    public AnimalEnt(DataBuilder.JsonAnimal animalRef, Vector2 position, float rotation, TextureRegion graphic, int drawLevel) {
        super(position, rotation, graphic, drawLevel);
        this.addTag(Constants.ENTITY_ANIMAL);

        this.name = "Squirrel";

        this.addComponent(new Animal(animalRef));
        this.addComponent(new Stats());
        this.addComponent(new Interactable("animal"));
        this.addComponent(new BehaviourManagerComp("animal"));
        this.addComponent(new GridComponent(Constants.GRIDACTIVE, ColonyGame.worldGrid, -1));

        this.makeCollider();

        tuneBehaviour();
    }

    private void makeCollider(){
        CircleShape shape = new CircleShape();
        shape.setRadius(0.5f);
        Collider collider = this.addComponent(new Collider(ColonyGame.world, shape));

        collider.body.setType(BodyDef.BodyType.DynamicBody);
        collider.fixture.setSensor(true);

        collider.fixture.setFriction(0.5f);
        collider.fixture.setDensity(1f);
    }

    private void tuneBehaviour(){
        BlackBoard bb = this.getComponent(BlackBoard.class);
        bb.idleDistance = 3;
        bb.baseIdleTime = 0.3f;
        bb.randomIdleTime = 1f;
    }
}
