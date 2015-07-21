package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.util.GH;

/**
 * Created by Paha on 7/17/2015.
 */
public class Enterable extends Component{
    private Vector2[] originalEnterPositions; //The original enter positions offsets.
    private Vector2[] enterPositions; //The updated positions including rotation and world location of the Entity owner.

    private float lastRotation = 0;
    private Vector2 lastPosition = new Vector2(0,0);

    public Enterable() {
        super();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    /**
     * Sets the original enter positions which are the offsets from the center of the Entity that is Enterable.
     * @param positions The offset positions from the center of the Entity.
     */
    public void setEnterPositions(Vector2... positions){
        //Make some new arrays.
        this.originalEnterPositions = new Vector2[positions.length];
        this.enterPositions = new Vector2[positions.length];

        //Fill in the original and regular positions.
        for(int i=0;i<positions.length;i++) {
            this.originalEnterPositions[i] = new Vector2(GH.toMeters(positions[i].x), GH.toMeters(positions[i].y));
            this.enterPositions[i] = new Vector2(0,0);
        }
        this.calculatePositions(this.originalEnterPositions);
    }

    /**
     * Gets the world enter positions of this Entity, which are recalculated if rotation or position has changed.
     * @return An array of world positions.
     */
    public Vector2[] getEnterPositions(){
        Transform trans = this.owner.getTransform(); //Cache the transform
        //If the rotations/positions are not the same, calculate the new positions and update the last rotation/position.
        if(this.lastRotation != trans.getRotation() || this.lastPosition.x != trans.getPosition().x || this.lastPosition.y != trans.getPosition().y){
            this.calculatePositions(this.originalEnterPositions);
            this.lastRotation = trans.getRotation();
            this.lastPosition.set(trans.getPosition().x, trans.getPosition().y);
        }

        return this.enterPositions;
    }

    /**
     * Calculates the enterPositions using the 'original' positions parameter.
     * @param original The original positions to use for calculations.
     * @return The changed enter positions.
     */
    private Vector2[] calculatePositions(Vector2[] original){
        Transform trans = this.owner.getTransform();
        //Copy the positions and factor in rotation and Entity owner position
        for(int i=0;i<original.length;i++) {
            Vector2 pos = this.originalEnterPositions[i];
            float x = MathUtils.cos(trans.getRotation())*pos.x - MathUtils.sin(trans.getRotation())*pos.y; //cosX - sinY
            float y = MathUtils.sin(trans.getRotation())*pos.x + MathUtils.cos(trans.getRotation())*pos.y; //sinX + cosY ... some property I guess?
            this.enterPositions[i].set(trans.getPosition().x + x, trans.getPosition().y + y);
        }

        return this.enterPositions;
    }
}
