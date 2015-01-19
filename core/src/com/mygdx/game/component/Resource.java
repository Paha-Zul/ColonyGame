package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component implements IDisplayable{
    private String resourceType = "";
    private int maxResources = 100;
    private int currResources = 100;

    public Resource(String resourceType) {
        super();

        this.resourceType = resourceType;
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

    public String getResourceType(){
        return this.resourceType;
    }

    public void setMaxResources(int amt){
        this.maxResources = amt;
    }

    public void setCurrResources(int amt){
        this.currResources = amt;
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
        float y = rect.getY() + rect.getHeight();

        if(name == "general"){
            GUI.Text("Name: "+this.owner.name, batch, x, y);
            y-=20;
            GUI.Text("MaxResources: "+this.maxResources, batch, x, y);
            y-=20;
            GUI.Text("CurrResources: "+this.currResources, batch, x, y);
            y-=20;
        }
    }
}
