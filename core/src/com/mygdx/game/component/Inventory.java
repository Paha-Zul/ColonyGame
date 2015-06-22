package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.EventSystem;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.managers.DataManager;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paha on 1/18/2015.
 */
public class Inventory extends Component implements IOwnable {
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
    public Inventory() {

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
        if (building != null) this.colony = building.getOwningColony();
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);

        this.inventory.clear();
        this.allowedTypes = null;
    }

    /**
     * Checks if this Inventory can add 1 of an item.
     *
     * @param itemName The compName of the Item to add
     * @return True if it can be added, false otherwise.
     */
    public boolean canAddItem(String itemName) {
        return this.canAddItem(itemName, 1);
    }

    /**
     * Checks if this Inventory can add a certain amount of item.
     *
     * @param itemName The compName of the item to add.
     * @param amount   The amount to add.
     * @return True if it can be added, false otherwise.
     */
    public boolean canAddItem(String itemName, int amount) {
        InventoryItem invItem = this.inventory.get(itemName);
        return (invItem == null && (this.maxAmount < 0 || amount <= this.maxAmount)) || (invItem != null && invItem.canAddAmount(amount));
    }

    /**
     * Reserves 1 of an item.
     *
     * @param itemName The name of the item.
     * @return A positive number if the item was able to be reserved. This could be the amount requested or the a partial amount available. -1 indicates the item
     * does not exist in the inventory, and 0 indicates that the item had none left available.
     */
    public int reserveItem(String itemName) {
        return this.reserveItem(itemName, 1);
    }

    /**
     * Reserves an amount of an item.
     *
     * @param itemName The name of the item.
     * @param amount   The amount to reserve.
     * @return A positive number if the item was able to be reserved. This could be the amount requested or the a partial amount available. 0 indicates either the item
     * does not exist or there were none available to reserve. To check more precisely if an item exists, use {@link Inventory#hasItem(String) hasItem} or {@link Inventory#getItemAmount(String) getItemAmount}.
     */
    public int reserveItem(String itemName, int amount) {
        InventoryItem invItem = this.inventory.get(itemName);
        if (invItem == null) return 0;
        return invItem.reserve(amount);
    }

    public int unReserveItem(String itemName) {
        return this.unReserveItem(itemName, 1);
    }

    public int unReserveItem(String itemName, int amount) {
        InventoryItem invItem = this.inventory.get(itemName);
        if (invItem == null) return -1;
        return invItem.unReserve(amount);
    }

    /**
     * Adds an amount of the item designated by the compName passed in.
     *
     * @param itemName The compName of the item to add.
     * @param amount   The amount of the item to add. If the amount is <= 0, returns early.
     */
    public void addItem(String itemName, int amount) {
        this.lasAddedItem = null;
        if (amount <= 0) return;

        InventoryItem invItem = this.inventory.get(itemName);
        //If the invItem doesn't exist, create a new one and add it to the hash map.
        if (invItem == null) {
            invItem = new InventoryItem(itemName, amount, this.maxAmount);
            this.inventory.put(itemName, invItem); //Make a new inventory itemRef in the hashmap.
            //Otherwise, simply add the amount from the itemRef.
        } else
            invItem.addAmount(amount);

        //Keeps track of total itemNames in this inventory.
        this.currTotalItems += amount;
        this.lasAddedItem = itemName;
        if (colony != null) colony.addItemToGlobal(invItem.itemRef, amount);

        EventSystem.notifyEntityEvent(this.owner, "added_item", invItem.itemRef, amount);
    }

    /**
     * Adds one of the item designated by the itemName passed in.
     *
     * @param itemName The compName of the item to add.
     */
    public void addItem(String itemName) {
        this.addItem(itemName, 1);
    }

    /**
     * Removes all of an itemRef (by compName) and returns that itemRef with the amount removed.
     *
     * @param itemName The compName of the Item.
     * @return The Item that was completely removed from the inventory with the quantity that was removed.
     */
    public int removeItemAll(String itemName) {
        InventoryItem invItem = this.inventory.get(compName);
        return this.removeItem(itemName, invItem.amount);
    }

    /**
     * Removes 1 item from the inventory.
     *
     * @param itemName The name of the item to remove.
     * @return The amount removed of the item from the Inventory.
     */
    public int removeItem(String itemName) {
        return this.removeItem(itemName, 1, false);
    }

    /**
     * Removes an amount of an item from this Inventory.
     *
     * @param itemName The name of the item to remove.
     * @param amount   The amount to remove from the inventory.
     * @return The amount of the item removed from the Inventory.
     */
    public int removeItem(String itemName, int amount) {
        return this.removeItem(itemName, amount, false);
    }

    /**
     * Attempts to remove an amount of an item from this inventory. It will either remove the requested amount
     * or will remove all of the item if the amount requested was higher than the stock of the item. Returns the amount
     * removed from this inventory.
     *
     * @param itemName       The compName of the Item.
     * @param amount         The amount of the item to remove.
     * @param takingReserved True if the item being removed was reserved first, false otherwise.
     * @return The amount removed from the inventory.
     */
    public int removeItem(String itemName, int amount, boolean takingReserved) {
        //If the amount to remove is <= 0, return 0.
        if (amount <= 0) return 0;
        InventoryItem invItem = this.inventory.get(itemName); //Get the item.
        //If it didn't exist or it was empty, return 0.
        if (invItem == null) return 0;

        int removeAmount = (amount >= invItem.amount) ? invItem.amount : amount; //If amount is equal or more than the inv amount, take all of it, otherwise the amount.
        invItem.amount -= removeAmount;                         //Set the inventory Item's amount.
        if (takingReserved)
            invItem.reserved -= removeAmount;    //If we are taking a reserved item, reduce the reserved amount also.
        this.currTotalItems -= removeAmount;                      //Subtract the amount being removed from the counter.

        //Remove the item from the inventory if all of it has been taken.
        if (invItem.amount <= 0)
            this.inventory.remove(itemName);

        if (colony != null) colony.addItemToGlobal(invItem.itemRef, -removeAmount);

        EventSystem.notifyEntityEvent(this.owner, "removed_item", invItem.itemRef, removeAmount);
        return removeAmount;
    }

    /**
     * Clears the inventory.
     */
    public void clearInventory() {
        this.inventory.clear();
    }

    /**
     * Gets a list of the inventory.
     *
     * @return An ArrayList containing the itemNames of the inventory.
     */
    @JsonIgnore
    public final ArrayList<InventoryItem> getItemList() {
        return new ArrayList<>(inventory.values());
    }

    @JsonIgnore
    public int getCurrTotalItems() {
        return this.currTotalItems;
    }

    /**
     * Gets an amount of an item from the inventory if it exists.
     *
     * @param itemName The name of the item to get an amount of.
     * @return The amount of the item in the inventory. 0 if the item does not exist.
     */
    @JsonIgnore
    public int getItemAmount(String itemName) {
        return this.getItemAmount(itemName, false);
    }

    /**
     * Gets an amount of an item from the inventory if it exists.
     *
     * @param itemName        The name of the item to get an amount of.
     * @param includeOnTheWay If the result should include items that are planned to arrive.
     * @return The amount of the item in the inventory. 0 if the item does not exist.
     */
    @JsonIgnore
    public int getItemAmount(String itemName, boolean includeOnTheWay) {
        InventoryItem item = this.inventory.get(itemName);
        int amount = 0;
        if (item != null) {
            amount = item.getAmount(includeOnTheWay);
            if (item.amount == 0)
                GH.writeErrorMessage("The item " + item.itemRef.getItemName() + " has 0 amount, should not exist in this inventory.", true);
        }

        return amount;
    }

    /**
     * Checks if this item contains an item.
     *
     * @param itemName The name of the item.
     * @return True if the item exists in this Inventory, false otherwise.
     */
    public boolean hasItem(String itemName) {
        return this.inventory.containsKey(itemName);
    }

    /**
     * Checks if this inventory has no items.
     *
     * @return True if empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.inventory.size() == 0;
    }

    @JsonIgnore
    public final InventoryItem getItemReference(String name) {
        return this.inventory.get(name);
    }

    /**
     * Gets the max amount per item.
     *
     * @return The max amount per item
     */
    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     * Sets the max amount per item.
     *
     * @param maxAmount The max amount per item.
     */
    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public void printInventory() {
        System.out.println("[Inventory]Inventory of " + this.getEntityOwner().name);
        for (InventoryItem item : this.inventory.values())
            System.out.println("[Inventory]Item: " + item.itemRef);
    }

    @Override
    public void addedToColony(Colony colony) {
        this.colony = colony;
    }

    @Override
    @JsonIgnore
    public Colony getOwningColony() {
        return this.colony;
    }

    public static class InventoryItem {
        public DataBuilder.JsonItem itemRef;
        private int amount, maxAmount, reserved, onTheWay;

        /**
         * Creates a new InventoryItem. Uses the Item passed in to clone a new Item for reference.
         *
         * @param itemRef The Item to clone.
         * @param amount  The amount of the itemRef to initially store.
         */
        public InventoryItem(DataBuilder.JsonItem itemRef, int amount, int maxAmount) {
            this.itemRef = itemRef;
            this.amount = amount;
            this.maxAmount = maxAmount;
        }

        public InventoryItem(String itemName, int amount, int maxAmount) {
            this(DataManager.getData(itemName, DataBuilder.JsonItem.class), amount, maxAmount);
        }

        /**
         * Places a reserve on this item for some amount.
         *
         * @param amount The amount of this item to reserve.
         * @return The amount reserved.
         */
        public int reserve(int amount) {
            int _available = this.getAvailable(); //Get the available amount.
            int _reserved = amount <= _available ? amount : _available; //Take what we can!
            this.reserved += _reserved; //Add this amount to the item's reserved amount.
            return _reserved; //Return it!
        }

        /**
         * Unreserves an amount on this item.
         *
         * @param amount The amount to unreserve.
         * @return The amount that was unreserved.
         */
        public int unReserve(int amount) {
            //Either take the amount requested (if under the total amount), or the total amount if amount > total
            int _unReserved = amount <= this.getAmount(false) ? amount : this.getAmount(false);
            this.reserved -= _unReserved; //Take away from the reserves.
            return _unReserved; //Return it!
        }

        public void addOnTheWay(int amount) {
            this.onTheWay += amount;
        }

        public void removeOnTheWay(int amount) {
            this.onTheWay = this.onTheWay - amount < 0 ? 0 : this.onTheWay - amount;
        }

        /**
         * @return The amount of the item plus the amount on the way.
         */
        public int getAmountAndOnTheWay() {
            return this.amount + this.onTheWay;
        }

        public boolean canAddAmount(int amount) {
            return this.amount + amount <= maxAmount || this.maxAmount < 0;
        }

        public void addAmount(int amount) {
            this.amount += amount;
        }

        /**
         * @return The amount that this item currently has. Does not include any reserves or onTheWay amounts.
         */
        public int getAmount(){
            return this.getAmount(false);
        }

        /**
         * Gets the amount of this item.
         * @param includeOnTheWay If the result should include items on the way.
         * @return The amount of this item in this inventory, including items on the way if 'includeOnTheWay' is true.
         */
        public int getAmount(boolean includeOnTheWay) {
            if (includeOnTheWay) return this.amount + this.onTheWay;
            return this.amount;
        }

        public int getMaxAmount() {
            return this.maxAmount;
        }

        public int getReserved() {
            return this.reserved;
        }

        public int getAvailable() {
            return this.amount - this.reserved;
        }
    }
}
