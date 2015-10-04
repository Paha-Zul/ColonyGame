package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.component.Resource;
import com.mygdx.game.component.collider.BoxCollider;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.util.Constants;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Created by Paha on 2/28/2015.
 */
public class ResourceEnt extends Entity{

    public ResourceEnt(){

    }

    public ResourceEnt(Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, drawLevel);
        this.getTags().addTags("resource", "selectable");
        this.name = "Resource";

        GraphicIdentity identity = this.addComponent(new GraphicIdentity());
        this.addComponent(new Resource());
        GridComponent gridComp = this.addComponent(new GridComponent());
        Interactable inter = this.addComponent(new Interactable());

        inter.setInterType("resource");

        gridComp.setGridType(Constants.GRIDACTIVE);
        gridComp.setGrid(ColonyGame.worldGrid);
        gridComp.setExploreRadius(-1);

        if(identity != null) {
            identity.setSprite(graphicName[0], graphicName[1]);
            identity.setAnchor(0.5f, 0.05f);
            identity.configureSprite();
        }

        this.load(null, null);
    }

    private void makeCollider(){
        //Get the graphic for the collider size
        GraphicIdentity graphic = this.getComponent(GraphicIdentity.class);
        if(graphic == null || graphic.getSprite() == null) return;

        //Try to get the collider. If null, make a new one!
        BoxCollider collider = getComponent(BoxCollider.class);
        if(collider == null){
            collider = new BoxCollider();
            collider.setActive(false);
            collider = this.addComponent(collider);
            collider.setupBody(BodyDef.BodyType.StaticBody, ColonyGame.world, graphic.getSprite().getWidth()/4, graphic.getSprite().getHeight()/2,
                    new Vector2(graphic.getSprite().getX() + graphic.getSprite().getWidth()/2, graphic.getSprite().getY() + graphic.getSprite().getHeight()/2), false, false);
        }

    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.initLoad(entityMap, compMap);
    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.load(entityMap, compMap);
        this.makeCollider();
    }
}
