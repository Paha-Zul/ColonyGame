package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.GraphicIdentity;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.GH;

/**
 * Created by Paha on 2/28/2015.
 */
public class ResourceEnt extends Entity{

    public ResourceEnt(Vector2 position, float rotation, TextureRegion graphic, int drawLevel) {
        super(position, rotation, graphic, drawLevel);
        this.addTag(Constants.ENTITY_RESOURCE);
        this.name = "Resource";

        this.addComponent(new Interactable("resource"));
        this.addComponent(new GridComponent(Constants.GRIDSTATIC, ColonyGame.worldGrid, -1));
        GraphicIdentity identity = this.getComponent(GraphicIdentity.class);
        if(identity != null) identity.alignment = 1;

        this.makeCollider();
    }

    private void makeCollider(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.active = false;

        //Get the graphic for the collider size
        GraphicIdentity graphic = this.getComponent(GraphicIdentity.class);
        if(graphic == null) return;

        //Set the polygon shape as a box using the sprite's width and height
        PolygonShape box = new PolygonShape();
        float width = GH.toMeters(graphic.sprite.getWidth()), height = GH.toMeters(graphic.sprite.getHeight());
        Vector2 center = new Vector2(graphic.sprite.getX(), graphic.sprite.getY() + height/2);
        box.setAsBox(width/4, height/2, center, 0);

        //Do the fixture.
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 0;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;

        //Add the Collider Component and an Interactable Component.
        Collider coll = this.addComponent(new Collider(ColonyGame.world, bodyDef, fixtureDef));

        box.dispose();
    }
}
