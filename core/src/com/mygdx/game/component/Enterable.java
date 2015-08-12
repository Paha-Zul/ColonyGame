package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.entity.Entity;

/**
 * Created by Paha on 7/17/2015.
 * A Component that designates an Entity as being able to be entered. Has a max occupancy and holds a list of the
 * Entities currently in the Enterable object. This is a separate component instead of being built into a class (Building for instance)
 * as it allows many different things to be Enterable (vehicles, buildings... etc).
 *
 */
public class Enterable extends Component{
    private Vector2[] originalEnterPositions; //The original enter positions offsets. X and Y are between 0 and 1 to scale easier.
    private Vector2[] enterPositions; //The updated positions including rotation and world location of the Entity owner. These are the real values (not just 0-1)
    private Vector2 lastPosition = new Vector2(0,0);
    private float lastRotation = 0;

    private int maxOccupants = 0;
    private Array<Entity> currOccupants;

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
        this.currOccupants = new Array<>(this.maxOccupants);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    /**
     * Sets the original enter positions which are the offsets from the center of the Entity that is Enterable.
     * @param positions The offset positions from the bottom left of the Entity. These values should be between 0 and 1 (0 being bottom/left and 1 being top/right)
     */
    public void setEnterPositions(Vector2... positions){
        //Make some new arrays.
        this.originalEnterPositions = new Vector2[positions.length];
        this.enterPositions = new Vector2[positions.length];

        //Fill in the original and regular positions.
        for(int i=0;i<positions.length;i++) {
            this.originalEnterPositions[i] = new Vector2(positions[i].x, positions[i].y);
            this.enterPositions[i] = new Vector2(0,0);
        }
        this.calculatePositions(this.originalEnterPositions);
    }

    /**
     * Sets the original enter positions which are the offsets from the center of the Entity that is Enterable.
     * @param positions The offset positions from the bottom left of the Entity. These values should be between 0 and 1 (0 being bottom/left and 1 being top/right)
     */
    public void setEnterPositions(float[] ...positions){
        //Make some new arrays.
        this.originalEnterPositions = new Vector2[positions.length];
        this.enterPositions = new Vector2[positions.length];

        //Fill in the original and regular positions.
        for(int i=0;i<positions.length;i++) {
            this.originalEnterPositions[i] = new Vector2(positions[i][0], positions[i][1]);
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
        GraphicIdentity graphic = this.owner.getGraphicIdentity();
        //Copy the positions and factor in rotation and Entity owner position
        for(int i=0;i<original.length;i++) {
            Vector2 pos = this.originalEnterPositions[i];
            float x = graphic.getSprite().getWidth()*pos.x; //Get the position relevant to the Entities position and graphic width
            float y = graphic.getSprite().getHeight()*pos.y; //Get the position relevant to the Entities position and graphic height

            x = MathUtils.cos(trans.getRotation())*x - MathUtils.sin(trans.getRotation())*y; //cosX - sinY
            y = MathUtils.sin(trans.getRotation())*x + MathUtils.cos(trans.getRotation())*y; //sinX + cosY ... some property I guess?

            this.enterPositions[i].set(trans.getPosition().x + x - graphic.getSprite().getWidth()*graphic.getAnchor().x,
                    trans.getPosition().y + y - graphic.getSprite().getWidth()*graphic.getAnchor().y);
        }

        return this.enterPositions;
    }

    public boolean enter(Entity entity){
        if(isFull()) return false;
        this.currOccupants.add(entity);
        return true;
    }

    public boolean leave(Entity entity) {
        return !isEmpty() && this.currOccupants.removeValue(entity, true);
    }

    public boolean isFull(){
        return this.currOccupants.size >= this.maxOccupants;
    }

    public boolean isEmpty(){
        return this.currOccupants.size == 0;
    }

    public void setMaxOccupants(int amount){
        this.maxOccupants = amount;
    }

    public int getMaxOccupants(){
        return this.maxOccupants;
    }
}
