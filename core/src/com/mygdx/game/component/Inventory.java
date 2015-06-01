package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.managers.DataManager;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paha on 1/18/2015.
 */
public class Inventory extends Component implements IOwnable{
    @JsonProperty
    public String lasAddedItem = "";
    @JsonIgnore
    private Colony colony;
    @JsonProperty
    private String allowedTypes = "all";
    @JsonProperty
    private int totalTypesAllowed = -1;
    @JsonProperty
    private int totalItemsAllowed = -1;
    @JsonProperty
    private int totalWeightAllowed = -1;
    @JsonProperty
    private int currTotalItems = 0;
    @JsonProperty
    private int maxAmount = 10;
    @JsonIgnore
    private HashMap<String, InventoryItem> inventory = new HashMap<>(20);

    /**
     * Creates a default Inventory Component with the default values. This means this inventory can hold unlimited of everything.
     */
    public Inventory(){

    }

    @Override
    public void start() {
        super.start();
        load();
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        Building building = this.getComponent(Building.class);
        if(building != null) this.colony = building.getOwningColony();
    }

    /**
     * Checks if this Inventory can add a certain amount of item.
     * @param itemName The compName of the item to add.
     * @param amount The amount to add.
     * @return True if it can be added, false otherwise.
     */
    public boolean canAddItem(String itemName, int amount) {
        InventoryItem invItem = this.inventory.get(itemName);
        return (invItem == null && (this.maxAmount < 0 || amount <= this.maxAmount)) || (invItem != null && invItem.canAddAmount(amount));
    }

    /**
     * Checks if this Inventory can add 1 of an item.
     * @param itemName The compName of the Item to add
     * @return True if it can be added, false otherwise.
     */
    public boolean canAddItem(String itemName){
        return this.canAddItem(itemName, 1);
    }

    /**
     * Adds an amount of the item designated by the compName passed in.
     * @param itemName The compName of the item to add.
     * @param amount The amount of the item to add.
     */
    public void addItem(String itemName, int amount){
        this.lasAddedItem = null;
        if(amount == 0) return;

        InventoryItem invItem = this.inventory.get(itemName);
        //If the invItem doesn't exist, create a new one and add it to the hash map.
        if(invItem == null) {
            invItem = new InventoryItem(itemName, amount, this.maxAmount);
            this.inventory.put(itemName, invItem); //Make a new inventory itemRef in the hashmap.
            //Otherwise, simply add the amount from the itemRef.
        }else
            invItem.addAmount(amount);

        //Keeps track of total itemNames in this inventory.
        this.currTotalItems+=amount;
        this.lasAddedItem = itemName;
        if(colony != null) colony.addItemToGlobal(invItem.itemRef, amount);
    }

    /**
     * Adds one of the item designated by the itemName passed in.
     * @param itemName The compName of the item to add.
     */
    public void addItem(String itemName){
        this.addItem(itemName, 1);
    }



    /**
     * Removes all of an itemRef (by compName) and returns that itemRef with the amount removed.
     * @param itemName The compName of the Item.
     * @return The Item that was completely removed from the inventory with the quantity that was removed.
     */
    public int removeItemAll(String itemName){
        InventoryItem invItem = this.inventory.get(compName);
        return this.removeItemAmount(itemName, invItem.amount);
    }

    /**
     * Attempts to remove an amount of an item from this inventory. It will either remove the requested amount
     * or will remove all of the item if the amount requested was higher than the stock of the item. Returns the amount
     * removed from this inventory.
     * @param itemName The compName of the Item.
     * @param amount The amount of the item to remove.
     * @return The amount removed from the inventory.
     */
    public int removeItemAmount(String itemName, int amount){
        InventoryItem invItem = this.inventory.get(itemName); //Get the item.

        //If it didn't exist or it was empty, return 0.
        if(invItem == null || invItem.amount <= 0)
            return 0;

        int amt = (amount >= invItem.amount) ? invItem.amount : amount; //If amount is equal or more than the inv amount, take all of it, otherwise the amount.
        invItem.amount -= amt; //Set the inventory Item's amount.
        this.currTotalItems-=amt; //Subtract the amount being removed from the counter.

        //Remove the item from the inventory if all of it has been taken.
        if(invItem.amount <= 0)
            this.inventory.remove(itemName);

        if(colony != null) colony.addItemToGlobal(invItem.itemRef, -amt);
        return amt;
    }

    /**
     * Clears the inventory.
     */
    public void clearInventory(){
        this.inventory.clear();
    }

    /**
     * Gets a list of the inventory.
     * @return An ArrayList containing the itemNames of the inventory.
     */
    @JsonIgnore
    public final ArrayList<InventoryItem> getItemList(){
        return new ArrayList<>(inventory.values());
    }

    @JsonIgnore
    public int getCurrTotalItems(){
        return this.currTotalItems;
    }

    @JsonIgnore
    public final InventoryItem getItemReference(String name){
        return this.inventory.get(name);
    }

    @Override
    @JsonIgnore
    public Colony getOwningColony() {
        return this.colony;
    }

    /**
     * Gets the max amount per item.
     * @return The max amount per item
     */
    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     * Sets the max amount per item.
     * @param maxAmount The max amount per item.
     */
    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public void printInventory(){
        System.out.println("[Inventory]Inventory of "+this.getEntityOwner().name);
        for(InventoryItem item : this.inventory.values())
            System.out.println("[Inventory]Item: "+item.itemRef);
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);

        this.inventory.clear();
        this.allowedTypes = null;
    }


    @Override
    public void addedToColony(Colony colony) {
        this.colony = colony;
    }


    public static class InventoryItem{
        private int amount, maxAmount;
        public DataBuilder.JsonItem itemRef;

        /**
         * Creates a new InventoryItem. Uses the Item passed in to clone a new Item for reference.
         * @param itemRef The Item to clone.
         * @param amount The amount of the itemRef to initially store.
         */
        public InventoryItem(DataBuilder.JsonItem itemRef, int amount, int maxAmount){
            this.itemRef = itemRef;
            this.amount = amount;
            this.maxAmount = maxAmount;
        }

        public InventoryItem(String itemName, int amount, int maxAmount){
            this(DataManager.getData(itemName, DataBuilder.JsonItem.class), amount, maxAmount);
        }

        public boolean canAddAmount(int amount){
            return this.amount + amount <= maxAmount || this.maxAmount < 0;
        }

        public void addAmount(int amount){
            this.amount += amount;
        }

        public int getAmount(){
            return this.amount;
        }

        public int getMaxAmount(){
            return this.maxAmount;
        }
    }
}
