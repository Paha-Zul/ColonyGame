package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.component.collider.BoxCollider;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.util.Constants;

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

        this.components.addComponent(new GraphicIdentity()).setSprite(graphicName[0], graphicName[1]);
        Interactable inter = this.addComponent(new Interactable());
        inter.setInterType("resource");

        GridComponent gridComp = this.addComponent(new GridComponent());
        gridComp.setGridType(Constants.GRIDACTIVE);
        gridComp.setGrid(ColonyGame.worldGrid);
        gridComp.setExploreRadius(-1);

        GraphicIdentity identity = this.getComponent(GraphicIdentity.class);
        if(identity != null) identity.setAnchor(0.5f, 0.05f);

        this.load();
    }

    @Override
    public void initLoad() {
        super.initLoad();
    }

    @Override
    public void load() {
        super.load();
        this.makeCollider();
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
                    new Vector2(graphic.getSprite().getX(), graphic.getSprite().getY()), false, false);
        }

    }
}
