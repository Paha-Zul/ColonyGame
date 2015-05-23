package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.util.Constants;

/**
 * Created by Paha on 1/18/2015.
 */
public class ColonistEnt extends Entity{

    public ColonistEnt(){

    }

    public ColonistEnt(Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, graphicName, drawLevel);
        this.name = "Colonist";

        this.addComponent(new Colonist());
        GridComponent gridComp = this.addComponent(new GridComponent());
        gridComp.setGridType(Constants.GRIDACTIVE);
        gridComp.setGrid(ColonyGame.worldGrid);
        gridComp.setExploreRadius(3);
        Interactable inter = this.addComponent(new Interactable());
        inter.setInterType("humanoid");
        this.addComponent(new Stats());
        this.addComponent(new Inventory());
        this.addComponent(new BehaviourManagerComp());
        this.makeCollider();

        this.tags.addTag("humanoid");
        this.tags.addTag("colonist");
        this.tags.addTag("alive");

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
