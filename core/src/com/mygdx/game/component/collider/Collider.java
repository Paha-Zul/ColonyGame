package com.mygdx.game.component.collider;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IScalable;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.Tags;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Paha on 1/9/2015.
 */
public class Collider extends Component implements IScalable{
    @JsonIgnore
    public Body body;
    @JsonIgnore
    public Fixture fixture;
    @JsonIgnore
    private World world;
    @JsonProperty
    private float originalRadius;

    public Collider(){

    }

    /**
     * Uses default values to create the body and fixture. Can be changed through calls to the body and fixture.
     * @param world The Box2D saveContainer.
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
     * @param world The Box2D saveContainer.
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
    public void init() {
        super.init();
    }

    @Override
    public void start() {
        super.start();
        load();
    }

    @Override
    public void save() {

    }

    @Override
    public void initLoad() {
        super.initLoad();

    }

    @Override
    public void load() {
        //TODO Under Construction! Apparently changing active state locks everything up.

        Vector2 ownerPos = owner.getTransform().getPosition();
        if(this.body == null) return;

        this.body.setTransform(ownerPos.x, ownerPos.y, this.owner.getTransform().getRotation());

        ColliderInfo bodyInfo = new ColliderInfo(owner);
        ColliderInfo fixtureInfo = new ColliderInfo(owner);
        fixtureInfo.tags.addTag(Constants.COLLIDER_CLICKABLE);
        fixtureInfo.tags.addTag("entity");
        this.body.setUserData(bodyInfo);
        this.fixture.setUserData(fixtureInfo);

        this.body.setActive(true);


        //this.owner.getComponents().registerScalable(this);
        //this.body.setActive(false);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.owner.getTransform().setPosition(this.body.getPosition().x, this.body.getPosition().y);
    }

    @JsonIgnore
    public void setWorld(World world){
        this.world = world;
    }

    @JsonIgnore
    public void setBody(BodyDef bodyDef){
        if(this.body != null) world.destroyBody(this.body);

        this.body = world.createBody(bodyDef);
        ColliderInfo bodyInfo = new ColliderInfo(owner);
        this.body.setUserData(bodyInfo);
        this.body.setTransform(this.owner.getTransform().getPosition(), 0);
    }

    @JsonIgnore
    public void setFixture(FixtureDef def){
        if(this.fixture != null) this.body.destroyFixture(this.fixture);
        this.fixture = this.body.createFixture(def);
        ColliderInfo fixtureInfo = new ColliderInfo(owner);
        fixtureInfo.tags.addTag(Constants.COLLIDER_CLICKABLE);
        fixtureInfo.tags.addTag("entity");
        this.fixture.setUserData(fixtureInfo);
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);

        this.body.destroyFixture(this.fixture);
        this.world.destroyBody(this.body);
    }

    @Override
    public void scale(float scale) {
        this.fixture.getShape().setRadius(this.originalRadius*scale);
    }

    public static class ColliderInfo{
        public String name;
        public Tags tags = new Tags("collider");
        public Entity owner;

        public ColliderInfo(Entity owner){
            this.owner = owner;
        }
    }
}
