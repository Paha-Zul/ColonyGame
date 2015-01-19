package com.mygdx.game.component;

/**
 * Created by Paha on 1/18/2015.
 */
public class Item extends Component{
    private String name = "default";
    private int stackLimit = 100;
    private int weight = 1;
    private int currStack = 1;
    private boolean stackable = true;

    public Item(String name, boolean stackable, int stackLimit, int weight) {
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

    public void setName(String name) {
        this.name = name;
    }

    public void setStackLimit(int stackLimit) {
        this.stackLimit = stackLimit;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setCurrStack(int currStack) {
        this.currStack = currStack;
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

    public boolean isStackable() {
        return stackable;
    }

    public void addToStack(int amt){
        this.currStack+=amt;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
