package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.CircleCollider;
import com.mygdx.game.component.graphic.ColonistGraphic;
import com.mygdx.game.util.Constants;

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

        this.makeCollider();
    }

    @Override
    public void initLoad() {
    }

    @Override
    public void load() {
        super.load();
        makeCollider();
    }

    private void makeCollider(){
        CircleCollider collider = getComponent(CircleCollider.class);
        if(collider == null) collider = this.addComponent(new CircleCollider());
        collider.setupBody(BodyDef.BodyType.DynamicBody, ColonyGame.world, this.getTransform().getPosition(), 1, true, true);

    }
}
