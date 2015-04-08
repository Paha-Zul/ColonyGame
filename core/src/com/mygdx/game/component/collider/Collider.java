package com.mygdx.game.component.collider;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.component.Component;
import com.mygdx.game.interfaces.IScalable;

/**
 * Created by Paha on 1/9/2015.
 */
public class Collider extends Component implements IScalable{
    public Body body;
    public Fixture fixture;

    private World world;
    private float originalRadius;

    /**
     * Uses default values to create the body and fixture. Can be changed through calls to the body and fixture.
     * @param world The Box2D world.
     * @param shape The Shape that the body should be.
     */
    public Collider(World world, Shape shape) {
        super();

        this.world = world;
        BodyDef bodyDef = new BodyDef();
        this.body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        this.fixture = body.createFixture(fixtureDef);

        this.originalRadius = this.fixture.getShape().getRadius();

        shape.dispose();
    }

    /**
     * A more advanced setup which
     * @param world The Box2D world.
     * @param bodyDef The definition of the body.
     * @param fixtureDef The definition of the fixture.
     */
    public Collider(World world, BodyDef bodyDef, FixtureDef fixtureDef){
        this.world = world;
        this.body = world.createBody(bodyDef);

        this.fixture = this.body.createFixture(fixtureDef);
        this.originalRadius = this.fixture.getShape().getRadius();
    }

    @Override
    public void start() {
        super.start();

        Vector2 ownerPos = this.owner.transform.getPosition();
        this.body.setTransform(ownerPos.x, ownerPos.y, this.owner.transform.getRotation());
        this.body.setUserData(this.owner);
        this.fixture.setUserData(this.owner);
        this.owner.registerScalable(this);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.owner.transform.setPosition(this.body.getPosition().x, this.body.getPosition().y);
    }

    @Override
    public void destroy() {
        super.destroy();

        this.body.destroyFixture(this.fixture);
        this.world.destroyBody(this.body);
    }

    @Override
    public void scale(float scale) {
        this.fixture.getShape().setRadius(this.originalRadius*scale);
    }
}
