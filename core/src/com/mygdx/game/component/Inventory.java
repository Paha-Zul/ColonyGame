package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.managers.ItemManager;
import com.mygdx.game.interfaces.IDisplayable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paha on 1/18/2015.
 */
public class Inventory extends Component implements IDisplayable{
    private String allowedTypes = "all";
    private int totalTypesAllowed = -1;
    private int totalItemsAllowed = -1;
    private int totalWeightAllowed = -1;

    private int currTotalItems = 0;

    private HashMap<String, InventoryItem> inventory = new HashMap<>(20);

    /**
     * Creates an Inventory Component to hold Items.
     * @param allowedTypes The allowed types for this inventory. This should be in the form of "resource,furniture,weapons,...etc". Using "all" will
     *                     indicate that all types can be stored here. (NOT IMPLEMENTED)
     * @param totalTypesAllowed The total number of different types allowed. For instance, if "all" is used but 'totalTypesAllowed' is 2, then if furniture and weapon types are stored first,
     *                          no other types can be put in until another type is cleared. -1 indicates infinite. (NOT IMPLEMENTED)
     * @param totalItemsAllowed The total number of items allowed in this inventory. For instance, maybe a person can only hold 10 items? -1 indicates infinite. (NOT IMPLEMENTED)
     * @param totalWeightAllowed The total weight allowed for this inventory. For instance, maybe a person can hold a total weight of 50? -1 indicates infinite. (NOT IMPLEMENTED)
     */
    public Inventory(String allowedTypes, int totalTypesAllowed, int totalItemsAllowed, int totalWeightAllowed) {
        super();
        this.allowedTypes = allowedTypes;
        this.totalTypesAllowed = totalTypesAllowed;
        this.totalItemsAllowed = totalItemsAllowed;
        this.totalWeightAllowed = totalWeightAllowed;
    }

    /**
     * Creates a default Inventory Component with the default values. This means this inventory can hold unlimited of everything.
     */
    public Inventory(){
        this("all", -1, -1, -1);
    }

    @Override
    public void start() {
        super.start();
    }

    /**
     * Adds one of the item designated by the itemName passed in.
     * @param itemName The name of the item to add.
     */
    public void addItem(String itemName){
        InventoryItem invItem = this.inventory.get(itemName);
        //If the invItem doesn't exist, create a new one and add it to the hash map.
        if(invItem == null) {
            invItem = new InventoryItem(itemName, 1);
            this.inventory.put(itemName, invItem); //Make a new inventory itemRef in the hashmap.
            //Otherwise, simply add the amount from the itemRef.
        }else
            invItem.addAmount(1);

        //Keeps track of total items in this inventory.
        this.currTotalItems+=1;
    }

    /**
     * Adds an amount of the item designated by the name passed in.
     * @param itemName The name of the item to add.
     * @param amount The amount of the item to add.
     */
    public void addItem(String itemName, int amount){
        InventoryItem invItem = this.inventory.get(itemName);
        //If the invItem doesn't exist, create a new one and add it to the hash map.
        if(invItem == null) {
            invItem = new InventoryItem(itemName, amount);
            this.inventory.put(itemName, invItem); //Make a new inventory itemRef in the hashmap.
            //Otherwise, simply add the amount from the itemRef.
        }else
            invItem.addAmount(amount);

        //Keeps track of total items in this inventory.
        this.currTotalItems+=amount;
    }

    /**
     * Removes all of an itemRef (by name) and returns that itemRef with the amount removed.
     * @param itemName The name of the Item.
     * @return The Item that was completely removed from the inventory with the quantity that was removed.
     */
    public int removeItemAll(String itemName){
        InventoryItem invItem = this.inventory.get(name);

        //If it didn't exist or it was empty, return 0.
        if(invItem == null || invItem.amount <= 0)
            return 0;

        this.currTotalItems-=invItem.amount; //Subtract the current itemRef counter by the amount being removed.
        this.inventory.remove(name); //Remove it from the inventory.

        return invItem.amount; //Return the amount of the item we removed.
    }

    /**
     * Attempts to remove an amount of an item from this inventory. It will either remove the requested amount
     * or will remove all of the item if the amount requested was higher than the stock of the item. Returns the amount
     * removed from this inventory.
     * @param itemName The name of the Item.
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
     * @return An ArrayList containing the items of the inventory.
     */
    public final ArrayList<InventoryItem> getItemList(){
        return new ArrayList<>(inventory.values());
    }

    public int getCurrTotalItems(){
        return this.currTotalItems;
    }

    public final InventoryItem getItemReference(String name){
        return this.inventory.get(name);
    }

    public void printInventory(){
        System.out.println("[Inventory]Inventory of "+this.getEntityOwner().name);
        for(InventoryItem item : this.inventory.values())
            System.out.println("[Inventory]Item: "+item.itemRef);
    }

    @Override
    public void destroy() {
        super.destroy();

        this.inventory.clear();
        this.allowedTypes = null;
    }

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name, GUI.GUIStyle style) {

    }

    @Override
    public void display(float x, float y, float width, float height, SpriteBatch batch, String name, GUI.GUIStyle style) {
        GUI.Text("Inventory Items", batch, x, y);
        y-=20;
        for(Inventory.InventoryItem item : this.getItemList()){
            GUI.Text(item.itemRef.getDisplayName()+": "+item.amount, batch, x, y);
            y-=20;
        }
    }

    public class InventoryItem{
        private int amount;
        public DataBuilder.JsonItem itemRef;

        /**
         * Creates a new InventoryItem. Uses the Item passed in to clone a new Item for reference.
         * @param itemRef The Item to clone.
         * @param amount The amount of the itemRef to initially store.
         */
        public InventoryItem(DataBuilder.JsonItem itemRef, int amount){
            this.itemRef = itemRef;
            this.amount = amount;
        }

        public InventoryItem(String itemName, int amount){
            this(ItemManager.getItemReference(itemName), amount);
        }

        public void addAmount(int amount){
            this.amount += amount;
        }

        public int getAmount(){
            return this.amount;
        }
    }
}
