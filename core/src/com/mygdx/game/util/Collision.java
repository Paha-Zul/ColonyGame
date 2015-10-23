package com.mygdx.game.util;


import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.managers.MessageEventSystem;

/**
 * Created by Paha on 3/26/2015.
 */
public class Collision implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture first = contact.getFixtureA();
        Fixture other = contact.getFixtureB();

        Entity firstEntity = ((Collider.ColliderInfo)first.getUserData()).owner;
        Entity otherEntity = ((Collider.ColliderInfo)other.getUserData()).owner;

        MessageEventSystem.notifyEntityEvent(firstEntity, "collide_start", first, other);
        MessageEventSystem.notifyEntityEvent(otherEntity, "collide_start", other, first);
    }

    @Override
    public void endContact(Contact contact) {
        Fixture first = contact.getFixtureA();
        Fixture other = contact.getFixtureB();

        Entity firstEntity = ((Collider.ColliderInfo)first.getUserData()).owner;
        Entity otherEntity = ((Collider.ColliderInfo)other.getUserData()).owner;

        MessageEventSystem.notifyEntityEvent(firstEntity, "collide_end", first, other);
        MessageEventSystem.notifyEntityEvent(otherEntity, "collide_end", other, first);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
