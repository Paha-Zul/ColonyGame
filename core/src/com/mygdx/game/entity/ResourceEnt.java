package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.component.Resource;
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
        gridComp.setGrid(ColonyGame.instance.worldGrid);
        gridComp.setExploreRadius(-1);

        if(identity != null) {
            identity.setSprite(graphicName[0], graphicName[1]);
            identity.setAnchor(0.5f, 0.05f);
            identity.configureSprite();
        }

        this.load(null, null);
    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.initLoad(entityMap, compMap);
    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.load(entityMap, compMap);
    }
}
