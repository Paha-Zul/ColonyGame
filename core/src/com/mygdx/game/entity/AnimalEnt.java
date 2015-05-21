package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.util.BlackBoard;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.managers.DataManager;

/**
 * Created by Paha on 2/26/2015.
 */
public class AnimalEnt extends Entity{

    public AnimalEnt(DataBuilder.JsonAnimal animalRef, Vector2 position, float rotation, TextureRegion graphic, int drawLevel) {
        super(position, rotation, graphic, drawLevel);
        this.getTags().addTag("animal");
        this.getTags().addTag("alive");
        this.name = "AnimalDefault";

        this.addComponent(new Animal(animalRef));
        this.addComponent(new Stats());
        this.addComponent(new Interactable("animal"));
        BehaviourManagerComp behManager = this.addComponent(new BehaviourManagerComp("animal"));
        this.addComponent(new GridComponent(Constants.GRIDACTIVE, ColonyGame.worldGrid, -1));

        this.makeCollider();

        tuneBehaviour(behManager);
    }

    public AnimalEnt(String animalName, Vector2 position, float rotation, TextureRegion graphic, int drawLevel) {
        this(DataManager.getData(animalName, DataBuilder.JsonAnimal.class), position, rotation, graphic, drawLevel);
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

    private void tuneBehaviour(BehaviourManagerComp behManager){
        BlackBoard bb = behManager.getBlackBoard();
        bb.idleDistance = 3;
        bb.baseIdleTime = 0.3f;
        bb.randomIdleTime = 1f;
    }
}
