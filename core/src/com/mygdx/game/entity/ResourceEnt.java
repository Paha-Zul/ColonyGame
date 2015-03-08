package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.GraphicIdentity;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.Constants;

/**
 * Created by Paha on 2/28/2015.
 */
public class ResourceEnt extends Entity{
    public ResourceEnt(Vector2 position, float rotation, Texture graphic, SpriteBatch batch, int drawLevel) {
        super(position, rotation, graphic, batch, drawLevel);
        this.addTag(Constants.ENTITY_RESOURCE);
        this.name = "Resource";

        this.addComponent(new Interactable("resource"));
        this.addComponent(new GridComponent(Constants.GRIDSTATIC, ColonyGame.worldGrid, -1));
        this.getComponent(GraphicIdentity.class).alignment = 1;

        this.makeCollider();
    }

    private void makeCollider(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        CircleShape circle = new CircleShape();
        circle.setRadius(10f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;

        //Add the Collider Component and an Interactable Component.
        this.addComponent(new Collider(ColonyGame.world, bodyDef, fixtureDef));

        circle.dispose();
    }
}
