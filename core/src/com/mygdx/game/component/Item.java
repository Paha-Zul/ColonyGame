package com.mygdx.game.component;

/**
 * Created by Paha on 1/18/2015.
 */
public class Item extends Component{
    private String name = "default";
    private String itemType = "default";

    private int stackLimit = 100;
    private int weight = 1;
    private int currStack = 1;
    private boolean stackable = true;

    public Item(String name, String itemType, boolean stackable, int stackLimit, int weight) {
        super();
        this.name = name;
        this.stackable = stackable;
        this.stackLimit = stackLimit;
        this.weight = weight;
    }

    @Override
    public void start() {
        super.start();
    }

    /**
     * Sets the name of this Item.
     * @param name The new name of this Item.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the stack limit of this Item. For instance, maybe wood can only be stacked to 100.
     * @param stackLimit The new stack limit of the Item.
     */
    public void setStackLimit(int stackLimit) {
        this.stackLimit = stackLimit;
    }

    /**
     * The weight of this Item. For instance, wood logs are really heavy and have a weight of 100.
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

    public String getName() {
        return name;
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
        return "[Name: "+this.name+",Type: "+this.itemType+",Weight: "+this.weight+",CurrStack: "+this.currStack+"]";
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
