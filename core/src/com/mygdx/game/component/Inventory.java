package com.mygdx.game.component;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paha on 1/18/2015.
 */
public class Inventory extends Component{
    private String allowedTypes = "";
    private int totalTypesAllowed = 0;
    private int totalItemsAllowed = 0;

    private HashMap<String, InventoryItem> inventory = new HashMap<>(20);

    public Inventory(String allowedTypes, int totalTypesAllowed, int totalItemsAllowed) {
        super();
        this.allowedTypes = allowedTypes;
        this.totalTypesAllowed = totalTypesAllowed;
        this.totalItemsAllowed = totalItemsAllowed;
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
}
