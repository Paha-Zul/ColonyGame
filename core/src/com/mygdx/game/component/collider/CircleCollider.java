package com.mygdx.game.component.collider;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.util.GH;

/**
 * Created by Paha on 8/24/2015.
 * A Circle for collisions.
 */
public class CircleCollider extends Collider{

    @Override
    public void init() {
        super.init();
    }

    /**
     * Sets up the CircleCollider.
     * @param bodyType The body type of the body (Dynamic, static). Use BodyDef.BodyType.
     * @param radius
     * @param isSensor
     */
    public void setupBody(BodyDef.BodyType bodyType, World world, Vector2 center, float radius, boolean isSensor, boolean active){
        this.setWorld(world);
        this.setActive(active);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(GH.toMeters(center.x), GH.toMeters(center.y));
        //bodyDef.active = false;

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);
        FixtureDef fixDef = new FixtureDef();
        fixDef.isSensor = isSensor;
        fixDef.shape = circle;

        this.setBody(bodyDef);
        this.setFixture(fixDef);

        circle.dispose();
    }
}
