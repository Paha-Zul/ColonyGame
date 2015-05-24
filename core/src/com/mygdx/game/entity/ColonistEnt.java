package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.component.graphic.ColonistGraphic;
import com.mygdx.game.util.Constants;

/**
 * Created by Paha on 1/18/2015.
 */
public class ColonistEnt extends Entity{

    public ColonistEnt(){

    }

    public ColonistEnt(Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, drawLevel);
        this.name = "Colonist";
        this.tags.addTag("humanoid");
        this.tags.addTag("colonist");
        this.tags.addTag("alive");

        ((ColonistGraphic)this.components.addComponent(new ColonistGraphic())).setSprite(graphicName[0], graphicName[1]);
        this.addComponent(new Colonist());
        ((GridComponent)this.addComponent(new GridComponent())).setGridType(Constants.GRIDACTIVE).setGrid(ColonyGame.worldGrid).setExploreRadius(3);
        ((Interactable)this.addComponent(new Interactable())).setInterType("humanoid");
        this.addComponent(new Stats());
        this.addComponent(new Inventory());
        this.addComponent(new BehaviourManagerComp());

        this.makeCollider();
    }

    @Override
    public void initLoad() {
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
}
