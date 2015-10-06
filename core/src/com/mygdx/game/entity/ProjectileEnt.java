package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.Projectile;
import com.mygdx.game.component.collider.CircleCollider;
import com.mygdx.game.component.graphic.GraphicIdentity;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Created by Paha on 4/2/2015.
 */
public class ProjectileEnt extends Entity{
    public ProjectileEnt(){

    }

    public ProjectileEnt(Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, drawLevel);
        this.tags.addTag("projectile");

        this.components.addComponent(new GraphicIdentity()).setSprite(graphicName[0], graphicName[1]);
        this.addComponent(new Projectile());
        makeCollider();
    }

    private void makeCollider(){
        CircleCollider collider = getComponent(CircleCollider.class);
        if(collider == null) collider = this.addComponent(new CircleCollider());
        collider.setupBody(BodyDef.BodyType.DynamicBody, ColonyGame.instance.world, this.getTransform().getPosition(), 1, true, true);
    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.initLoad(entityMap, compMap);
        this.makeCollider();
    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.load(entityMap, compMap);
    }
}
