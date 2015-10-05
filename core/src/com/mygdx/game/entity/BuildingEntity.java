package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.BoxCollider;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.managers.DataManager;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Created by Paha on 1/18/2015.
 */
public class BuildingEntity extends Entity{
    public BuildingEntity(){

    }

    public BuildingEntity(Vector2 position, float rotation, String buildingName, int drawLevel) {
        this(position, rotation, DataManager.getData(buildingName, DataBuilder.JsonBuilding.class), drawLevel);
    }

    public BuildingEntity(Vector2 position, float rotation, DataBuilder.JsonBuilding buildingRef, int drawLevel) {
        super(position, rotation, drawLevel);
        this.name = "Main Base";
        this.tags.addTags("building", "selectable", "constructing");

        this.addComponent(new GraphicIdentity()).setSprite(buildingRef.image, buildingRef.spriteSheet, buildingRef.dimensions[0], buildingRef.dimensions[1]);
        this.addComponent(new GridComponent()).setGridType(Constants.GRIDSTATIC).setGrid(ColonyGame.worldGrid).setExploreRadius(8).setAddMulti(true);
        this.addComponent(new Building()).setBuildingRef(buildingRef);
        this.addComponent(new Interactable()).setInterType("building");
        this.addComponent(new Constructable());

        this.makeCollider();
    }

    private void makeCollider(){
        float hWidth = 1;
        float hHeight = 1;

        GraphicIdentity graphic = this.getGraphicIdentity();
        if(graphic != null){
            hWidth = graphic.getSprite().getWidth()/2;
            hHeight = graphic.getSprite().getHeight()/2;
        }

        BoxCollider collider = getComponent(BoxCollider.class);
        if(collider == null) collider = this.addComponent(new BoxCollider());
        collider.setupBody(BodyDef.BodyType.StaticBody, ColonyGame.world, hWidth, hHeight, this.getTransform().getPosition(), false, false);
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
