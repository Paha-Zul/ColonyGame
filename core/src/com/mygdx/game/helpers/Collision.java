package com.mygdx.game.helpers;


import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.mygdx.game.entity.Entity;

/**
 * Created by Paha on 3/26/2015.
 */
public class Collision implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Entity first = (Entity)contact.getFixtureA().getBody().getUserData();
        Entity other = (Entity)contact.getFixtureB().getBody().getUserData();

        EventSystem.notifyEntityEvent(first, "collidestart", other);
        EventSystem.notifyEntityEvent(other, "collidestart", first);
    }

    @Override
    public void endContact(Contact contact) {
        Entity first = (Entity)contact.getFixtureA().getBody().getUserData();
        Entity other = (Entity)contact.getFixtureB().getBody().getUserData();

        EventSystem.notifyEntityEvent(first, "collideend", other);
        EventSystem.notifyEntityEvent(other, "collideend", first);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
