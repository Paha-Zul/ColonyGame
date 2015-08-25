package com.mygdx.game.component.collider;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.component.graphic.GraphicIdentity;

/**
 * Created by Paha on 8/24/2015.
 * A Box for collisions.
 */
public class BoxCollider extends Collider{

    public void setupBody(BodyDef.BodyType bodyType, World world, float hWidth, float hHeight, Vector2 center, boolean isSensor, boolean active){
        this.setWorld(world);
        this.setActive(active);

        //Get the graphic for the collider size
        GraphicIdentity graphic = this.getComponent(GraphicIdentity.class);
        if(graphic == null || graphic.getSprite() == null) return;

        //Make a new body definition
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(center.x, center.y);
        //bodyDef.active = false;

        //Set the polygon shape as a box using the sprite's width and height
        PolygonShape box = new PolygonShape();
        Vector2 cent = new Vector2(center.x, center.y);
        box.setAsBox(hWidth, hHeight, new Vector2(0,0), 0);

        //Make a new fixture definition
        //box.setRadius(0.5f);
        FixtureDef fixDef = new FixtureDef();
        fixDef.isSensor = isSensor;
        fixDef.shape = box;

        this.setBody(bodyDef);
        this.setFixture(fixDef);

        box.dispose();
    }
}
