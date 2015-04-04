package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.interfaces.IInteractable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component implements IInteractable{
    private String resourceName = "default", displayName = "default", resourceType = "default";
    private String[] itemNames;
    private int[] itemAmounts;
    private float gatherTime = 1;
    private volatile Entity taken = null;

    private StringBuilder info = new StringBuilder();

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
        this.gatherTime = jRes.gatherTime;

        this.itemAmounts = new int[jRes.amounts.length];
        for(int i=0;i<jRes.amounts.length; i++)
            this.itemAmounts[i] = MathUtils.random(jRes.amounts[i][1] - jRes.amounts[i][0]) + jRes.amounts[i][0]; //Add diff to base.
    }

    public Resource(DataBuilder.JsonAnimal jAnimal){
        this(jAnimal.name);
        this.displayName = jAnimal.displayName;
        this.resourceType = "animal";
        this.itemNames = jAnimal.items;
        this.gatherTime = 3f;

        this.itemAmounts = new int[jAnimal.itemAmounts.length];
        for(int i=0;i<jAnimal.itemAmounts.length; i++)
            this.itemAmounts[i] = MathUtils.random(jAnimal.itemAmounts[i][1] - jAnimal.itemAmounts[i][0]) + jAnimal.itemAmounts[i][0]; //Add diff to base.
    }

    @Override
    public void start() {
        super.start();

        this.getEntityOwner().name = this.displayName;

        for(int i=0;i<itemNames.length;i++){
            info.append(itemAmounts[i]).append(" ").append(itemNames[i]).append(System.lineSeparator());
        }
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
     * Gets the array of itemRef firstNames for this Resource. This is essentially the inventory of this Resource.
     * @return A String array of itemRef firstNames.
     */
    public String[] getItemNames() {
        return itemNames;
    }

    /**
     * Gets the amounts of the itemRef in this Resource.
     * @return A 2D int array which represents the possible (low-high) range of amount of items.
     */
    public int[] getItemAmounts() {
        return itemAmounts;
    }

    /**
     * Gets the item amount for a specific index.
     * @param index The index to get the amount from.
     * @return An integer which is the amount of the item.
     */
    public int getItemAmount(int index){
        return this.itemAmounts[index];
    }

    /**
     * @return The interType of this Resource.
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
        return this.taken != null;
    }

    /**
     * @return The Entity that has marked this resource as taken, null if not taken.
     */
    public Entity getTaken(){
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
     * Sets the interType for this Resource.
     * @param resourceType A String denoting the interType.
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Sets this Resource as taken or not taken.
     * @param entity The Entity to take this resource. Null if setting the resource as not taken.
     */
    public void setTaken(Entity entity){
        this.taken = entity;
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
    public String getStatsText() {
        return info.toString();
    }

    @Override
    public Skills getSkills() {
        return null;
    }

    @Override
    public String getName() {
        return this.getDisplayName();
    }

    @Override
    public BehaviourManagerComp getBehManager() {
        return null;
    }
}
