package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component implements IDisplayable{
    private String resourceName = "default", displayName = "default", resourceType = "default", textureName="";
    private String[] itemNames;
    private int[][] itemAmounts;
    private float gatherTime = 1;
    private boolean taken = false;

    public Resource(String resourceName) {
        super();
        this.resourceName = resourceName; //Record name

        this.setActive(false);
    }

    public Resource() {
        this("NothingResource");
    }

    /**
     * A copy constructor that copies the data from another resource.
     * @param resource
     */
    public Resource(Resource resource) {
        this(resource.resourceName);
        this.displayName = resource.displayName;
        this.resourceType = resource.resourceType;
        this.itemNames = resource.itemNames;
        this.itemAmounts = resource.itemAmounts;
        this.displayName = resource.displayName;
        this.textureName = resource.textureName;
    }

    @Override
    public void start() {
        super.start();

        this.getEntityOwner().name = this.displayName;
        this.getComponent(GraphicIdentity.class).setTexture(ColonyGame.assetManager.get(textureName, Texture.class));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public String getResourceName(){
        return this.resourceName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getItemNames() {
        return itemNames;
    }

    public int[][] getItemAmounts() {
        return itemAmounts;
    }

    public String getTextureName() {
        return textureName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public float getGatherTime(){
        return this.gatherTime;
    }

    public boolean isTaken(){
        return this.taken;
    }

    public void setGatherTime(float gatherTime){
        this.gatherTime = gatherTime;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setTaken(boolean taken){
        this.taken = taken;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setItemNames(String[] itemNames) {
        this.itemNames = itemNames;
    }

    public void setItemAmounts(int[][] itemAmounts) {
        this.itemAmounts = itemAmounts;
    }

    public void setTextureName(String textureName) {
        this.textureName = textureName;
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
//            GUI.Text("MaxResources: "+this.maxResources, batch, x, y);
//            y-=20;
//            GUI.Text("CurrResources: "+this.currResources, batch, x, y);
//            y-=20;
        }
    }
}
