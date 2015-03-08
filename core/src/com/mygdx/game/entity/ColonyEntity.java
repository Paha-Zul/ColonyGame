package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Colony;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.Constants;

/**
 * Created by Paha on 1/18/2015.
 */
public class ColonyEntity extends Entity{
    public ColonyEntity(Vector2 position, float rotation, Texture graphic, SpriteBatch batch, int drawLevel) {
        super(position, rotation, graphic, batch, drawLevel);

        this.addComponent(new GridComponent(Constants.GRIDSTATIC, ColonyGame.worldGrid, 5));
        this.addComponent(new Colony());
        this.addComponent(new Interactable("colony"));
        this.addComponent(new Inventory("all", 100, 100, 100));
        this.makeCollider(position);
    }

    private void makeCollider(Vector2 position){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((identity.sprite.getWidth()/2)/Constants.SCALE, (identity.sprite.getHeight()/2)/Constants.SCALE);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.restitution = 0;
        fixtureDef.friction = 0;
        fixtureDef.shape = shape;

        this.addComponent(new Collider(ColonyGame.world, bodyDef, fixtureDef));
        shape.dispose();
    }
}
