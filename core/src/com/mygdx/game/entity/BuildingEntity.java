package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Building;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.util.Constants;

/**
 * Created by Paha on 1/18/2015.
 */
public class BuildingEntity extends Entity{
    public BuildingEntity(){

    }

    public BuildingEntity(Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, graphicName, drawLevel);
        this.name = "Main Base";
        this.tags.addTag("building");

        GridComponent gridComp = this.addComponent(new GridComponent());
        gridComp.setGridType(Constants.GRIDSTATIC);
        gridComp.setGrid(ColonyGame.worldGrid);
        gridComp.setExploreRadius(8);
        this.addComponent(new Building());
        Interactable inter = this.addComponent(new Interactable());
        inter.setInterType("building");
        this.addComponent(new Inventory());
        this.makeCollider();
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
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(this.components.transform.getPosition());
        //bodyDef.active = false;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 1);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.restitution = 0;
        fixtureDef.friction = 0;
        fixtureDef.shape = shape;

        Collider collider = getComponent(Collider.class);
        if(collider == null) collider = this.addComponent(new Collider());
        collider.setWorld(ColonyGame.world);
        collider.setBody(bodyDef);
        collider.setFixture(fixtureDef);
        collider.setActive(false);

        shape.dispose();
    }
}
