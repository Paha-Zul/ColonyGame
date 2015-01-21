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

    public void addItem(Item item){
        InventoryItem invItem = this.inventory.get(item.getName());
        if(invItem != null)
            invItem.amount += item.getCurrStack(); //Add the amount of the stack.
        else
            invItem = this.inventory.put(item.getName(), new InventoryItem(item, item.getCurrStack())); //Make a new inventory item in the hashmap.

        item.setCurrStack(0); //Reset the item's stack amount.
    }

    public Item removeItemAll(String name){
        InventoryItem invItem = this.inventory.get(name);

        //If it didn't exist or it was empty, return null.
        if(invItem == null || invItem.amount <= 0)
            return null;

        //Make a copy of the item and set some values. Then return it.
        Item item = new Item(invItem.item.getName(), invItem.item.isStackable(), invItem.item.getStackLimit(), invItem.item.getWeight());
        item.setCurrStack(invItem.amount);
        invItem.amount = 0;

        return item;
    }

    public Item removeItemAmount(String name, int amount){
        InventoryItem invItem = this.inventory.get(name);

        //If it didn't exist or it was empty, return null.
        if(invItem == null || invItem.amount <= 0)
            return null;

        Item item = new Item(invItem.item.getName(), invItem.item.isStackable(), invItem.item.getStackLimit(), invItem.item.getWeight()); //Copy the item.
        int amt = (amount >= invItem.amount) ? invItem.amount : amount; //If amount is equal or more than the inv amount, take all of it, otherwise the amount.
        invItem.amount = amt; //Set the inventory Item's amount.
        item.setCurrStack(amt); //Give that amount to the new item.

        return item;
    }

    public ArrayList<InventoryItem> getItemList(){
        return new ArrayList<InventoryItem>(inventory.values());
    }

    @Override
    public void destroy() {
        super.destroy();

        this.inventory.clear();
        this.allowedTypes = null;
    }

    public class InventoryItem{
        public int amount;
        public Item item;

        public InventoryItem(){

        }

        public InventoryItem(Item item, int amount){
            this.item = item;
            this.amount = amount;
        }
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
}
