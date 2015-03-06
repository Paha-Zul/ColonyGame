package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Paha on 1/12/2015.
 */
public class Stats extends Component{
    private float maxHealth=100, currHealth=100;
    private int food = 100, water = 100;

    private float barW = 75, barH = 14, barOff = 4;
    private float halfBarW = barW*0.5f, halfBarH = barH*0.5f;

    public Stats() {
        super();

    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getCurrHealth() {
        return currHealth;
    }

    public int getFood() {
        return food;
    }

    public int getWater() {
        return water;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setCurrHealth(float currHealth) {
        this.currHealth = currHealth;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public void setWater(int water) {
        this.water = water;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

}
