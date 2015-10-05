package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.graphic.ColonistGraphic;
import com.mygdx.game.util.Constants;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Created by Paha on 1/18/2015.
 */
public class ColonistEnt extends Entity{

    public ColonistEnt(){

    }

    public ColonistEnt(Vector2 position, float rotation, String[] graphicName, int drawLevel) {
        super(position, rotation, drawLevel);
        this.name = "Colonist";
        this.tags.addTags("humanoid", "colonist", "alive", "selectable");

        this.addComponent(new ColonistGraphic()).setSprite(graphicName[0], graphicName[1]);
        this.addComponent(new Colonist());
        this.addComponent(new GridComponent()).setGridType(Constants.GRIDACTIVE).setGrid(ColonyGame.worldGrid).setExploreRadius(3);
        this.addComponent(new Interactable()).setInterType("humanoid");
        this.addComponent(new Stats());
        this.addComponent(new Inventory());
        this.addComponent(new Equipment());
        this.addComponent(new BehaviourManagerComp());
        this.addComponent(new Effects());
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
