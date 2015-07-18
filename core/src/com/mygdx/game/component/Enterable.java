package com.mygdx.game.component;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Paha on 7/17/2015.
 */
public class Enterable extends Component{
    private Vector2[] originalEnterPositions;
    private Vector2[] enterPositions;

    private float lastRotation = 0;

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

    public void setEnterPositions(Vector2... positions){
        this.originalEnterPositions = positions;
        for(int i=0;i<positions.length;i++)
            this.enterPositions[i] = new Vector2(positions[i].x, positions[i].y); //Copy a separate vector2;
    }

    public Vector2[] getEnterPositions(){
        Transform trans = this.owner.getTransform(); //Cache the transform
        if(this.lastRotation != trans.getRotation()){ //If the rotations are not the same
            //For each position in the original positions
            for(int i=0;i<this.originalEnterPositions.length;i++){
                Vector2 pos = this.originalEnterPositions[i];
                //this.enterPositions

            }
            this.lastRotation = trans.getRotation();
        }

        return this.enterPositions;
    }
}
