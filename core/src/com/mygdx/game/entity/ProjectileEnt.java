package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Projectile;
import com.mygdx.game.component.collider.Collider;

/**
 * Created by Paha on 4/2/2015.
 */
public class ProjectileEnt extends Entity{
    public ProjectileEnt(Vector2 position, float rotation, TextureRegion graphic, int drawLevel) {
        super(position, rotation, graphic, drawLevel);
        this.tags.addTag("projectile");

        this.addComponent(new Projectile());
        makeCollider();
    }

    private void makeCollider(){
        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f);
        Collider collider = this.addComponent(new Collider(ColonyGame.world, shape));

        collider.body.setType(BodyDef.BodyType.DynamicBody);
        collider.fixture.setSensor(true);

        collider.fixture.setFriction(0.5f);
        collider.fixture.setDensity(1f);
    }
}
