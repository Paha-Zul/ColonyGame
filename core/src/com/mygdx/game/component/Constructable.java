package com.mygdx.game.component;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.Entity;

import java.util.HashMap;

/**
 * Created by Paha on 6/20/2015.
 */
public class Constructable extends Component{
    private Inventory inventory;
    private HashMap<String, ConstructableItemAmounts> itemMap = new HashMap<>();
    private int totalItemsNeeded, totalItemsSupplied;

    public Constructable(){
        this.setActive(false);
    }

    @Override
    public void init(Entity owner) {
        super.init(owner);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void initLoad() {
        super.initLoad();
    }

    @Override
    public void load() {
        super.load();

        this.inventory = this.getComponent(Inventory.class);
    }

    /**
     * Adds an item and amount to this constructable.
     * @param itemName The name of the item.
     * @param itemAmount The amount of the item.
     * @return The Constructable for chaining.
     */
    public Constructable addItem(String itemName, int itemAmount){
        this.itemMap.put(itemName, new ConstructableItemAmounts(itemName, itemAmount));
        this.totalItemsSupplied+=itemAmount;
        return this;
    }

    public Constructable build(){
        for(ConstructableItemAmounts item : itemMap.values()){
            int amount = this.inventory.getItemAmount(item.itemName);
            if(amount > 0){
                this.inventory.removeItem(item.itemName, 1);
                this.totalItemsSupplied+=1;
            item.amountFulfilled+=1;
                return this;
            }
        }
        return this;
    }

    /**
     * Gets the remaining items needed and the amounts to finish this construction.
     * @return An object that holds an item name and an amount needed.
     */
    public Array<ItemsNeeded> getItemsNeeded(){
        Array<ItemsNeeded> items = new Array<>();
        for(String item : itemMap.keySet()){
            int amountNeeded = itemAmountNeeded(item);
            if(amountNeeded > 0) //Only add if we actually need some.
                items.add(new ItemsNeeded(item, amountNeeded));
        }

        return items;
    }

    /**
     * Gets the amount needed of an item by the name of 'itemName' to fulfill the need for that item.
     * @param itemName The name of the item.
     * @return A positive number if the constructable requires more of the item. 0 or negative means none needed or an overflow respectively.
     */
    public int itemAmountNeeded(String itemName){
        ConstructableItemAmounts amounts = itemMap.get(itemName);
        return amounts.amountNeeded - amounts.amountFulfilled - this.inventory.getItemAmount(itemName);
    }

    public float getPercentageDone(){
        return this.totalItemsSupplied/this.totalItemsNeeded;
    }

    public class ItemsNeeded{
        public String itemName;
        public int amountNeeded;

        public ItemsNeeded(String itemName, int amountNeeded) {
            this.itemName = itemName;
            this.amountNeeded = amountNeeded;
        }
    }

    private class ConstructableItemAmounts{
        public String itemName;
        public int amountNeeded=0;
        public int amountFulfilled=0;

        public ConstructableItemAmounts(String itemName, int amountNeeded){
            this.itemName = itemName;
            this.amountNeeded = amountNeeded;
        }
    }
}
