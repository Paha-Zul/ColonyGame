package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.BehaviourManagerComp;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.Constants;

/**
 * Created by Paha on 2/26/2015.
 */
public class AnimalEnt extends Entity{
    public AnimalEnt(Vector2 position, float rotation, Texture graphic, SpriteBatch batch, int drawLevel) {
        super(position, rotation, graphic, batch, drawLevel);

        this.addComponent(new BehaviourManagerComp("animal"));
        this.addComponent(new GridComponent(Constants.GRIDACTIVE, ColonyGame.worldGrid, -1));
        this.transform.setScale(0.1f);

        this.makeCollider();

        tuneBehaviour();
    }

    private void makeCollider(){
        CircleShape shape = new CircleShape();
        shape.setRadius(6f);
        Collider collider = this.addComponent(new Collider(ColonyGame.world, shape));

        collider.body.setType(BodyDef.BodyType.DynamicBody);
        collider.fixture.setSensor(true);

        collider.fixture.setFriction(0.5f);
        collider.fixture.setDensity(1f);
    }

    private void tuneBehaviour(){
        BlackBoard bb = this.getComponent(BlackBoard.class);
        bb.idleDistance = 3;
        bb.baseIdleTime = 0.3f;
        bb.randomIdleTime = 1f;
    }
}
