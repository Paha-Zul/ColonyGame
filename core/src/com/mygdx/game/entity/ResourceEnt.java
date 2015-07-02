package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.GH;

/**
 * Created by Paha on 2/28/2015.
 */
public class ResourceEnt extends Entity{

    public ResourceEnt(){

    }

    public ResourceEnt(Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, drawLevel);
        this.getTags().addTag("resource");
        this.name = "Resource";

        this.components.addComponent(new GraphicIdentity()).setSprite(graphicName[0], graphicName[1]);
        Interactable inter = this.addComponent(new Interactable());
        inter.setInterType("resource");
        GridComponent gridComp = this.addComponent(new GridComponent());
        gridComp.setGridType(Constants.GRIDACTIVE);
        gridComp.setGrid(ColonyGame.worldGrid);
        gridComp.setExploreRadius(-1);
        GraphicIdentity identity = this.getComponent(GraphicIdentity.class);
        if(identity != null) identity.alignment = 1;

        this.makeCollider();
    }

    @Override
    public void initLoad() {
        super.initLoad();
    }

    @Override
    public void load() {
        super.load();
        makeCollider();
    }

    private void makeCollider(){
        //Get the graphic for the collider size
        GraphicIdentity graphic = this.getComponent(GraphicIdentity.class);
        if(graphic == null || graphic.getSprite() == null) return;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        //bodyDef.active = false;

        //Set the polygon shape as a box using the sprite's width and height
        PolygonShape box = new PolygonShape();
        float width = GH.toMeters(graphic.getSprite().getWidth()), height = GH.toMeters(graphic.getSprite().getHeight());
        Vector2 center = new Vector2(graphic.getSprite().getX(), graphic.getSprite().getY() + height/2);
        box.setAsBox(width/4, height/2, center, 0);

        box.setRadius(0.5f);
        FixtureDef fixDef = new FixtureDef();
        fixDef.isSensor = true;
        fixDef.shape = box;

        Collider collider = getComponent(Collider.class);
        if(collider == null){
            collider = new Collider();
            collider.setActive(false);
            collider = this.addComponent(collider);
        }
        collider.setWorld(ColonyGame.world);
        collider.setBody(bodyDef);
        collider.setFixture(fixDef);

        box.dispose();
    }
}
