package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Projectile;
import com.mygdx.game.component.collider.Collider;

/**
 * Created by Paha on 4/2/2015.
 */
public class ProjectileEnt extends Entity{
    public ProjectileEnt(){

    }

    public ProjectileEnt(Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, graphicName, drawLevel);
        this.tags.addTag("projectile");

        this.addComponent(new Projectile());
        makeCollider();
    }

    @Override
    public void initLoad() {
        super.initLoad();
    }

    @Override
    public void load() {
        super.load();
    }

    private void makeCollider(){

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.bullet = true;
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
