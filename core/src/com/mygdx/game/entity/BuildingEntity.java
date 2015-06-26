package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.util.Constants;

/**
 * Created by Paha on 1/18/2015.
 */
public class BuildingEntity extends Entity{
    public BuildingEntity(){

    }

    public BuildingEntity(Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, drawLevel);
        this.name = "Main Base";
        this.tags.addTags("building");

        this.addComponent(new GraphicIdentity()).setSprite(graphicName[0], graphicName[1]);
        this.addComponent(new GridComponent()).setGridType(Constants.GRIDSTATIC).setGrid(ColonyGame.worldGrid).setExploreRadius(8).setAddMulti(true);
        this.addComponent(new Building());
        this.addComponent(new Interactable()).setInterType("building");
        this.addComponent(new Inventory());
        this.addComponent(new Constructable()).addItem("wood", 20).addItem("stone", 30);

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
        bodyDef.position.set(this.getTransform().getPosition());
        //bodyDef.active = false;

        float hWidth = 1;
        float hHeight = 1;

        GraphicIdentity graphic = this.getGraphicIdentity();
        if(graphic != null){
            hWidth = graphic.getSprite().getWidth()/2;
            hHeight = graphic.getSprite().getHeight()/2;
        }

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(hWidth, hHeight);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.restitution = 0;
        fixtureDef.friction = 0;
        fixtureDef.shape = shape;

        Collider collider = getComponent(Collider.class);
        if(collider == null) collider = this.addComponent(new Collider());
        collider.setWorld(ColonyGame.world);
        collider.setBody(bodyDef);
        collider.setFixture(fixtureDef);

        shape.dispose();
    }
}
