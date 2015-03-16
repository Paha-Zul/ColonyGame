package com.mygdx.game.component;

import com.mygdx.game.helpers.DataBuilder;

/**
 * A class that represents an item.
 */
public class Item extends Component{
    private String itemName = "default";
    private String displayName = "default";
    private String description = "default";
    private String itemType = "default";

    private int stackLimit = 100;
    private int weight = 1;
    private int currStack = 1;
    private boolean stackable = true;

    public Item(String itemName, String itemType, boolean stackable, int stackLimit, int weight) {
        super();
        this.itemName = itemName;
        this.stackable = stackable;
        this.stackLimit = stackLimit;
        this.weight = weight;
    }

    /**
     * Copy constructor.
     * @param item The Item to be copied.
     */
    public Item(Item item){
        this.itemName = item.itemName;
        this.displayName = item.displayName;
        this.description = item.description;
        this.itemType = item.itemType;
        this.stackLimit = item.getStackLimit();
        this.weight = item.getWeight();
        this.stackable = item.isStackable();
    }

    /**
     * Copy constructor for a JsonItem
     * @param jItem The JsonItem to copy.
     */
    public Item(DataBuilder.JsonItem jItem){
        this.itemName = jItem.getItemName();
        this.displayName = jItem.getDisplayName();
        this.description = jItem.getDescription();
        this.itemType = jItem.getItemType();
        this.stackLimit = 100000;
        this.weight = 1;
        this.stackable = true;
    }

    @Override
    public void start() {
        super.start();
    }

    /**
     * Sets the itemName of this Item.
     * @param itemName The new itemName of this Item.
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Sets the display name for this Item.
     * @param displayName The name to use for display.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the description of this Item.
     * @param description The string for the description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the stack limit of this Item. For instance, maybe wood can only be stacked to 100.
     * @param stackLimit The new stack limit of the Item.
     */
    public void setStackLimit(int stackLimit) {
        this.stackLimit = stackLimit;
    }

    /**
     * The weight of this Item. For instance, wood changes are really heavy and have a weight of 100.
     * @param weight The new weight of this Item.
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Sets the current stack amount of this Item. Just took 5 away from the stack of 100? The new amount is 95.
     * @param currStack The new stack amount of this Item.
     */
    public void setCurrStack(int currStack) {
        this.currStack = currStack;
    }

    /**
     * Sets the type of this Item. Wood log would be a 'resource'
     * @param itemType The new type of this Item.
     */
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getStackLimit() {
        return stackLimit;
    }

    public int getWeight() {
        return weight;
    }

    public int getCurrStack() {
        return currStack;
    }

    public String getItemType() {
        return itemType;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void addToStack(int amt){
        this.currStack+=amt;
    }

    @Override
    public String toString() {
        return "[Name: "+this.itemName +",Type: "+this.itemType+",Weight: "+this.weight+",CurrStack: "+this.currStack+"]";
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
