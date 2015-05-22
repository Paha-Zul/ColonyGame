package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.mygdx.game.util.GH;

/**
 * Created by Paha on 1/18/2015.
 */
public class BuildingEntity extends Entity{
    public BuildingEntity(Vector2 position, float rotation, TextureRegion graphic, int drawLevel) {
        super(position, rotation, graphic, drawLevel);
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
        this.makeCollider(position);
    }

    private void makeCollider(Vector2 position){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(GH.toMeters(getGraphicIdentity().sprite.getWidth()/2), GH.toMeters(getGraphicIdentity().sprite.getHeight()/2));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.restitution = 0;
        fixtureDef.friction = 0;
        fixtureDef.shape = shape;

        this.addComponent(new Collider(ColonyGame.world, bodyDef, fixtureDef));
        shape.dispose();
    }
}
