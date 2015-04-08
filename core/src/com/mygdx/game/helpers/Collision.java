package com.mygdx.game.helpers;


import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.entity.Entity;

/**
 * Created by Paha on 3/26/2015.
 */
public class Collision implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture first = contact.getFixtureA();
        Fixture other = contact.getFixtureB();

        Entity firstEntity = (Entity)first.getUserData();
        Entity otherEntity = (Entity)other.getUserData();

        EventSystem.notifyEntityEvent(firstEntity, "collide_start", first, other);
        EventSystem.notifyEntityEvent(otherEntity, "collide_start", other, first);
    }

    @Override
    public void endContact(Contact contact) {
        Fixture first = contact.getFixtureA();
        Fixture other = contact.getFixtureB();

        Entity firstEntity = (Entity)first.getUserData();
        Entity otherEntity = (Entity)other.getUserData();

        EventSystem.notifyEntityEvent(firstEntity, "collide_end", first, other);
        EventSystem.notifyEntityEvent(otherEntity, "collide_end", other, first);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
