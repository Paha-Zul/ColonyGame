package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.util.Constants;

/**
 * Created by Paha on 1/18/2015.
 */
public class ColonistEnt extends Entity{
    public ColonistEnt(Vector2 position, float rotation, TextureRegion graphic, int drawLevel) {
        super(position, rotation, graphic, drawLevel);
        this.name = "Colonist";

        this.addComponent(new Colonist());
        GridComponent gridComp = this.addComponent(new GridComponent());
        gridComp.setGridType(Constants.GRIDACTIVE);
        gridComp.setGrid(ColonyGame.worldGrid);
        gridComp.setExploreRadius(3);
        Interactable inter = this.addComponent(new Interactable());
        inter.setInterType("humanoid");
        this.addComponent(new Stats());
        this.addComponent(new Inventory());
        this.addComponent(new BehaviourManagerComp());
        this.makeCollider();

        this.tags.addTag("humanoid");
        this.tags.addTag("colonist");
        this.tags.addTag("alive");

    }

    private void makeCollider(){
        CircleShape shape = new CircleShape();
        shape.setRadius(0.5f);
        Collider collider = this.addComponent(new Collider(ColonyGame.world, shape));

        collider.body.setType(BodyDef.BodyType.DynamicBody);
        collider.fixture.setSensor(true);

        collider.fixture.setFriction(0.5f);
        collider.fixture.setDensity(1f);
    }
}
