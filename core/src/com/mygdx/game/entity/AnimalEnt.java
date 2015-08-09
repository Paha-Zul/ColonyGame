package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.managers.DataManager;

/**
 * Created by Paha on 2/26/2015.
 */
public class AnimalEnt extends Entity{

    public AnimalEnt(){

    }

    public AnimalEnt(DataBuilder.JsonAnimal animalRef, Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, drawLevel);
        this.getTags().addTag("animal");
        this.getTags().addTag("alive");
        this.name = "AnimalDefault";

        //Add the graphic.
        ((GraphicIdentity)this.components.addComponent(new GraphicIdentity())).setSprite(graphicName[0], graphicName[1]);
        ((Animal)this.addComponent(new Animal())).setAnimalRef(animalRef);         //Add the animal
        this.addComponent(new Stats());  //Add the stats.
        ((Interactable)this.addComponent(new Interactable())).setInterType("animal");
        ((GridComponent)this.addComponent(new GridComponent())).setGridType(Constants.GRIDACTIVE).setGrid(ColonyGame.worldGrid).setExploreRadius(-1);
        BehaviourManagerComp behManager = this.addComponent(new BehaviourManagerComp());

        this.makeCollider();

        tuneBehaviour(behManager);
    }

    public AnimalEnt(String animalName, Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        this(DataManager.getData(animalName, DataBuilder.JsonAnimal.class), position, rotation, graphicName, drawLevel);
    }

    @Override
    public void initLoad() {
        super.initLoad();
    }

    @Override
    public void load() {
        super.load();
        makeCollider();
    }

    private void makeCollider(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        //bodyDef.active = false;

        CircleShape circle = new CircleShape();
        circle.setRadius(0.5f);
        FixtureDef fixDef = new FixtureDef();
        fixDef.isSensor = true;
        fixDef.shape = circle;

        Collider collider = getComponent(Collider.class);
        if(collider == null) collider = this.addComponent(new Collider());
        collider.setWorld(ColonyGame.world);
        collider.setBody(bodyDef);
        collider.setFixture(fixDef);

        circle.dispose();
    }

    private void tuneBehaviour(BehaviourManagerComp behManager){
        BlackBoard bb = behManager.getBlackBoard();
        bb.idleDistance = 3;
        bb.baseIdleTime = 0.3f;
        bb.randomIdleTime = 1f;
    }
}
