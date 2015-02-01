package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.gui.GUI;
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
     * Adds all of the Item passed in. The Item passed in will have its current stack amount set to 0.
     * @param item The Item to add.
     */
    public void addItem(Item item){
        InventoryItem invItem = this.inventory.get(item.getName());
        if(invItem == null) {
            invItem = new InventoryItem(item, 0);
            this.inventory.put(item.getName(), invItem); //Make a new inventory item in the hashmap.
        }

        invItem.addAmount(item.getCurrStack());
        this.currTotalItems+=item.getCurrStack();
        item.setCurrStack(0); //Reset the item's stack amount.
    }

    public Item removeItemAll(String name){
        InventoryItem invItem = this.inventory.get(name);

        //If it didn't exist or it was empty, return null.
        if(invItem == null || invItem.amount <= 0)
            return null;

        //Make a copy of the item and set some values. Then return it.
        Item item = new Item(invItem.item.getName(), invItem.item.getItemType(), invItem.item.isStackable(), invItem.item.getStackLimit(), invItem.item.getWeight());
        item.setCurrStack(invItem.amount); //Set the item stack to the amount removed.
        this.currTotalItems-=invItem.amount; //Subtract the current item counter by the amount being removed.
        invItem.amount = 0;

        return item;
    }

    public Item removeItemAmount(String name, int amount){
        InventoryItem invItem = this.inventory.get(name);

        //If it didn't exist or it was empty, return null.
        if(invItem == null || invItem.amount <= 0)
            return null;

        Item item = new Item(invItem.item.getName(), invItem.item.getItemType(), invItem.item.isStackable(), invItem.item.getStackLimit(), invItem.item.getWeight()); //Copy the item.
        int amt = (amount >= invItem.amount) ? invItem.amount : amount; //If amount is equal or more than the inv amount, take all of it, otherwise the amount.
        invItem.amount = amt; //Set the inventory Item's amount.
        this.currTotalItems-=amt; //Subtract the amount being removed from the counter.
        item.setCurrStack(amt); //Give that amount to the new item.

        return item;
    }

    public void clearInventory(){
        this.inventory.clear();
    }

    public ArrayList<InventoryItem> getItemList(){
        return new ArrayList<InventoryItem>(inventory.values());
    }

    public boolean canTakeItem(Item item){
        boolean amt = (this.totalItemsAllowed == -1 || this.totalItemsAllowed - this.currTotalItems >= item.getCurrStack());
        if(allowsType(item.getItemType()) && amt)
            return true;

        return false;
    }

    public boolean allowsType(String type){
        String types[] = this.allowedTypes.split(",");
        for(String tmpType : types){
            tmpType = tmpType.trim();
            if(tmpType == "all") return true; //If allows all, return true.
            if(tmpType == type) return true; //If allows the item type, return true.
        }
        return false;
    }

    public int getCurrTotalItems(){
        return this.currTotalItems;
    }

    public void printInventory(){
        System.out.println("[Inventory]Inventory of "+this.getEntityOwner().name);
        for(InventoryItem item : this.inventory.values())
            System.out.println("[Inventory]Item: "+item.item);
    }

    @Override
    public void destroy() {
        super.destroy();

        this.inventory.clear();
        this.allowedTypes = null;
    }

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name) {
        float x = rect.x;
        float y = rect.y + rect.getHeight() - 5;

        GUI.Text("Inventory Items", batch, x, y);
        y-=20;
        for(Inventory.InventoryItem item : this.getItemList()){
            GUI.Text(item.item.getName()+": "+item.amount, batch, x, y);
            y-=20;
        }
    }

    public class InventoryItem{
        private int amount;
        public Item item;

        /**
         * Creates a new InventoryItem. Uses the Item passed in to clone a new Item for reference.
         * @param item The Item to clone.
         * @param amount The amount of the item to initially store.
         */
        public InventoryItem(Item item, int amount){
            this.item = new Item(item.getName(), item.getItemType(), item.isStackable(), item.getStackLimit(), item.getWeight());
            this.item.setCurrStack(amount);
            this.amount = amount;
        }

        public void addAmount(int amount){
            this.amount += amount;
            this.item.setCurrStack(this.amount);
        }

        public int getAmount(){
            return this.amount;
        }
    }
}
