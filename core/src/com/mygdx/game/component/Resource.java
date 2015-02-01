package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.ItemManager;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component implements IDisplayable{
    private String resourceName = "";
    private int maxResources = 5;
    private int currResources = 5;
    private float gatherTime = 1;
    private Item item;
    private boolean taken = false;

    public Resource(String resourceName) {
        super();

        this.resourceName = resourceName; //Record name
        this.item = ItemManager.getItemByName(this.resourceName); //Generate item.
        this.item.setCurrStack(currResources);

        this.setActive(false);
    }

    public Resource() {
        this("NothingResource");
    }

    public int getMaxResources(){
        return this.maxResources;
    }

    public int getCurrResources(){
        return this.currResources;
    }

    public String getResourceName(){
        return this.resourceName;
    }

    public float getGatherTime(){
        return this.gatherTime;
    }

    public Item getItem(){
        return this.item;
    }

    public boolean isTaken(){
        return this.taken;
    }

    public void setMaxResources(int amt){
        this.maxResources = amt;
    }

    public void setCurrResources(int amt){
        this.currResources = amt;
    }

    public void setGatherTime(float gatherTime){
        this.gatherTime = gatherTime;
    }

    public void setTaken(boolean taken){
        this.taken = taken;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name) {
        float x = rect.getX();
        float y = rect.getY() + rect.getHeight() - 5;

        //Print name of the entity
        if(name == "general"){
            GUI.Label("Name: "+this.owner.name, batch, rect.getX() + rect.getWidth()/2, rect.getY() + rect.getHeight() - 5, true);

        //Print resource info
        }else if(name == "resource"){
            GUI.Text("ResourceType: "+this.owner.name, batch, x, y);
            y-=20;
            GUI.Text("MaxResources: "+this.maxResources, batch, x, y);
            y-=20;
            GUI.Text("CurrResources: "+this.currResources, batch, x, y);
            y-=20;
        }
    }
}
