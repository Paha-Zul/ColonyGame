package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;
import com.mygdx.game.interfaces.IInteractable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component implements IInteractable{
    private String resourceName = "default", displayName = "default", resourceType = "default";
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
    }

    public Resource(DataBuilder.JsonResource jRes){
        this(jRes.resourceName);
        this.displayName = jRes.displayName;
        this.resourceType = jRes.resourceType;
        this.itemNames = jRes.items;
        this.itemAmounts = jRes.amounts;
    }

    @Override
    public void start() {
        super.start();

        this.getEntityOwner().name = this.displayName;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public String getResourceName(){
        return this.resourceName;
    }

    /**
     * @return A String which is the formal display name of this Resource.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the array of item names for this Resource. This is essentially the inventory of this Resource.
     * @return A String array of item names.
     */
    public String[] getItemNames() {
        return itemNames;
    }

    /**
     * Gets the amounts of the item in this Resource.
     * @return A 2D int array which represents the possible (low-high) range of amount of items.
     */
    public int[][] getItemAmounts() {
        return itemAmounts;
    }

    /**
     * @return The type of this Resource.
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * @return The gather time for this Resource.
     */
    public float getGatherTime(){
        return this.gatherTime;
    }

    /**
     * @return True if this Resource is taken, false otherwise.
     */
    public boolean isTaken(){
        return this.taken;
    }

    /**
     * Sets the gather time for this Resource.
     * @param gatherTime The amount of time to gather.
     */
    public void setGatherTime(float gatherTime){
        this.gatherTime = gatherTime;
    }

    /**
     * Sets the type for this Resource.
     * @param resourceType A String denoting the type.
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Sets this Resource as taken or not taken.
     * @param taken If this Resource is taken or not.
     */
    public void setTaken(boolean taken){
        this.taken = taken;
    }

    /**
     * Sets the name of this Resource.
     * @param resourceName The name to set for this Resource.
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Sets the display name of this Resource.
     * @param displayName The display name of this Resource.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the item names for this Resource.
     * @param itemNames A String array which is the itemNames.
     */
    public void setItemNames(String[] itemNames) {
        this.itemNames = itemNames;
    }

    /**
     * Sets the item amounts for this Resource.
     * @param itemAmounts A 2D int array which are the (low-high) range amounts.
     */
    public void setItemAmounts(int[][] itemAmounts) {
        this.itemAmounts = itemAmounts;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public Stats getStats() {
        return null;
    }

    @Override
    public Skills getSkills() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public BehaviourManagerComp getBehManager() {
        return null;
    }
}
