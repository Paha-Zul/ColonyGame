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
public class Stats extends Component implements IDisplayable{
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

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name) {
        float x = rect.getX();
        float y = rect.getY() + rect.getHeight() - 5;

        float leftOffset = 50;

        drawHealth(x, y, leftOffset, batch);
        y-=20;
        drawFood(x, y, leftOffset, batch);
        y-=20;
        drawWater(x, y, leftOffset, batch);
    }

    private void drawHealth(float x, float y, float leftOffset, SpriteBatch batch){

        batch.setColor(Color.BLACK);
        GUI.Texture(x + leftOffset, y - barH, barW, barH, WorldGen.getInstance().whiteTex, batch);
        batch.setColor(Color.GREEN);
        GUI.Texture(x + leftOffset + 2, y - barH + 2, barW - barOff, barH - barOff, WorldGen.getInstance().whiteTex, batch);

        GUI.Label("Health: ", batch, x, y, false);
        GUI.Label((int)currHealth+"/"+(int)maxHealth, batch, x + leftOffset + halfBarW, y - halfBarH, true);
    }

    private void drawFood(float x, float y, float leftOffset, SpriteBatch batch){

        batch.setColor(Color.BLACK);
        GUI.Texture(x + leftOffset, y - barH, barW, barH, WorldGen.getInstance().whiteTex, batch);
        batch.setColor(Color.GREEN);
        GUI.Texture(x + leftOffset + 2, y - barH + 2, barW - barOff, barH - barOff, WorldGen.getInstance().whiteTex, batch);

        GUI.Label("Food: ", batch, x, y, false);
        GUI.Label(food+"/100", batch, x + leftOffset + halfBarW, y - halfBarH, true);
    }

    private void drawWater(float x, float y, float leftOffset, SpriteBatch batch){

        batch.setColor(Color.BLACK);
        GUI.Texture(x + leftOffset, y - barH, barW, barH, WorldGen.getInstance().whiteTex, batch);
        batch.setColor(Color.GREEN);
        GUI.Texture(x + leftOffset + 2, y - barH + 2, barW - barOff, barH - barOff, WorldGen.getInstance().whiteTex, batch);

        GUI.Label("Water: ", batch, x, y, false);
        GUI.Label(water+"/100", batch, x + leftOffset + halfBarW, y - halfBarH, true);
    }
}
