package com.mygdx.game.component.collider;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IScalable;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.Tags;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Created by Paha on 1/9/2015.
 */
public abstract class Collider extends Component implements IScalable{
    public static final int CIRCLE = 0, SQUARE_CUSTOM = 1, SQUARE_FITGRAPHIC = 2;

    @JsonIgnore
    protected Body body;
    @JsonIgnore
    protected Fixture fixture;
    @JsonIgnore
    protected World world;
    @JsonProperty
    protected float originalRadius=-1;
    @JsonProperty
    protected int colliderType;

    public Collider(){

    }

    @Override
    public void save() {

    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.initLoad(entityMap, compMap);
    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        //TODO Under Construction! Apparently changing active state locks everything up.

        if(this.body == null) return;

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
    public void init() {
        super.init();
    }

    @Override
    public void start() {
        super.start();
        load(null, null);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.owner.getTransform().setPosition(this.body.getPosition().x, this.body.getPosition().y);
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);

        this.body.destroyFixture(this.fixture);
        this.world.destroyBody(this.body);
    }

    public void setWorld(World world){
        this.world = world;
    }

    public void setFixture(FixtureDef def){
        if(this.fixture != null) this.body.destroyFixture(this.fixture);
        this.fixture = this.body.createFixture(def);
        ColliderInfo fixtureInfo = new ColliderInfo(owner);
        fixtureInfo.tags.addTag(Constants.COLLIDER_CLICKABLE);
        fixtureInfo.tags.addTag("entity");
        this.fixture.setUserData(fixtureInfo);
    }

    public void setBodyPosition(Vector2 position, float angle){
        this.setBodyPosition(position.x, position.y, angle);
    }

    public void setBodyPosition(float x, float y, float angle){
        this.body.setTransform(x, y, angle);
    }

    public Body getBody(){
        return this.body;
    }

    public void setBody(BodyDef bodyDef){
        if(this.body != null) world.destroyBody(this.body);

        this.body = world.createBody(bodyDef);
        ColliderInfo bodyInfo = new ColliderInfo(owner);
        this.body.setUserData(bodyInfo);
    }

    public Fixture getMainFixture(){
        return this.fixture;
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
