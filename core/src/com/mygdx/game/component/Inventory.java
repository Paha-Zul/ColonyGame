package com.mygdx.game.component;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.EventSystem;
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
    private HashMap<String, HashMap<String, InventoryItem>> inventory = new HashMap<>(20);

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
     * @param itemName The compName of the item to add.
     * @param amount   The amount to add.
     * @return True if it can be added, false otherwise.
     */
    public boolean canAddItem(String itemName, int amount) {
        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return amount <= this.maxAmount;

        InventoryItem invItem = itemMap.get(itemName);
        return (invItem == null && (this.maxAmount < 0 || amount <= this.maxAmount)) || (invItem != null && invItem.canAddAmount(amount));
    }

    /**
     * Reserves an amount of an item.
     *
     * @param itemName The name of the item.
     * @param amount   The amount to reserve.
     * @param id The id to use for a reserve.
     * @return A positive number if the item was able to be reserved. This could be the amount requested or the a partial amount available. 0 indicates either the item
     * does not exist or there were none available to reserve. To check more precisely if an item exists, use {@link Inventory#hasItem(String) hasItem} or {@link Inventory#getItemAmount(String) getItemAmount}.
     */
    public int reserveItem(String itemName, int amount, long id) {
        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return 0;

        InventoryItem invItem = itemMap.get(itemName);
        if (invItem == null) return 0;
        return invItem.reserve(amount, id);
    }

    /**
     * Unreserves an amount of an item using the 'id' for the reserve id.
     * @param itemName The name of the item.
     * @param id The id of the reserve.
     * @return The amount that was unreserved.
     */
    public int unReserveItem(String itemName, long id) {
        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return 0;

        InventoryItem item = itemMap.get(itemName);
        if(item == null) return 0;
        return item.unReserve(id);
    }

    /**
     * Adds an item to be on the way.
     * @param itemName The name of the item.
     * @param amount The amount of the item.
     * @param id The id for the onTheWay record.
     * @return The amount that was able to be added to this item. If the result is less than the 'amount' passed in, it means the
     * item would not have enough space to fulfill the entire request. If the result is <= 0, either the item did not exist or no more could be
     * put on the way.
     */
    public int addOnTheWay(String itemName, int amount, long id){
        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return 0;

        InventoryItem item = itemMap.get(itemName);
        if(amount <= 0) return 0;
        if(item == null) {
            item = new InventoryItem(itemName, 0, this.maxAmount);
            this.addItem(itemName, 0);
        }
        return item.addOnTheWay(amount, id);
    }

    /**
     * Removes an item from being on the way.
     * @param itemName The name of the item.
     * @param id The id of the on the way record.
     * @return The amount that was removed from being on the way. -1 if the item did not exist.
     */
    public int removeOnTheWay(String itemName, long id){
        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return 0;

        InventoryItem item = itemMap.get(itemName);
        if(item == null) return 0;
        int _removed = item.removeOnTheWay(id);
        if(item.amount <= 0 && item.onTheWayList.size <= 0) this.inventory.remove(itemName);
        return _removed;
    }

    /**
     * Adds one of the item designated by the itemName passed in.
     * @param itemName The compName of the item to add.
     */
    public void addItem(String itemName) {
        this.addItem(itemName, 1);
    }

    /**
     * Adds an amount of the item designated by the compName passed in.
     * @param itemName The compName of the item to add.
     * @param amount The amount of the item to add. If the amount is <= 0, returns early.
     */
    public void addItem(String itemName, int amount) {
        this.addItemToInventory(DataManager.getData(itemName, DataBuilder.JsonItem.class), amount, this.maxAmount);
    }

    /**
     * Internal function that deals with adding an item to the inventory.
     * @param itemRef The JsonItem to add.
     * @param amount The amount to add.
     * @param maxAmount The max amount that this item can stack to.
     */
    private void addItemToInventory(DataBuilder.JsonItem itemRef, int amount, int maxAmount){
        if(itemRef == null || amount <= 0) return; //If the item ref is null, give up.
        //Try to get the map of items for the item type. If null, make a new one and put in the map.
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null){
            itemMap = new HashMap<>(); //Make a new one.
            this.inventory.put(itemRef.getItemType(), itemMap); //Put it!
        }

        //If the item does not exist in the item map, make a new one and put it in there!
        InventoryItem item = itemMap.get(itemRef.getItemName());
        if(item == null){
            item = new InventoryItem(itemRef, 0, maxAmount); //Make new.
            itemMap.put(itemRef.getItemName(), item); //Put!
        }

        item.addAmount(amount); //Add the item amount

        //Increment and set some values, add to the colony global amounts if possible, and call an event.
        this.currTotalItems += amount;
        this.lasAddedItem = itemRef.getItemName();
        if (colony != null) colony.addItemToGlobal(itemRef, amount);
        EventSystem.notifyEntityEvent(this.owner, "added_item", itemRef, amount);
    }

    /**
     * Internal remove call. Removes an amount of an item.
     * @param itemRef The JsonItem to remove.
     * @param amountToRemove The amount to remove.
     * @param id The id to use if we are removing from a reserve. 0 indicates not taking from reserve.
     */
    private int removeItemFromInventory(DataBuilder.JsonItem itemRef, int amountToRemove, long id){
        if(itemRef == null) return 0;
        //Try to get the map of items for the item type. If null, return;
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return 0;

        //If the item does not exist in the item map, return.
        InventoryItem item = itemMap.get(itemRef.getItemName());
        if(item == null) return 0;

        //TODO Watch this area... might be problematic.
        //Remove the item amount.
        int removed;
        if(id != 0) removed = item.removeAmount(item.unReserve(id)); //Remove the amount taken from the reserve.
        else removed = item.removeAmount(amountToRemove); //Otherwise, simply remove the amount

        //If the amount is now 0, remove it from the item map.
        if(item.getAmount() <= 0) itemMap.remove(itemRef.getItemName());
        //If the item map has no more items in it, remove it from the inventory map.
        if(itemMap.size() == 0) this.inventory.remove(itemRef.getItemType());

        //Removed the amount from the colony if possible and fire an event.
        if (this.colony != null) colony.addItemToGlobal(itemRef, -removed);
        EventSystem.notifyEntityEvent(this.owner, "removed_item", itemRef, amountToRemove);

        return removed;
    }

    /**
     * Removes 1 item from the inventory.
     *
     * @param itemName The name of the item to remove.
     * @return The amount removed of the item from the Inventory.
     */
    public int removeItem(String itemName) {
        return this.removeItem(itemName, 1, 0);
    }

    /**
     * Removes an amount of an item from this Inventory.
     *
     * @param itemName The name of the item to remove.
     * @param amount   The amount to remove from the inventory.
     * @return The amount of the item removed from the Inventory.
     */
    public int removeItem(String itemName, int amount) {
        return this.removeItem(itemName, amount, 0);
    }

    /**
     * Attempts to remove an amount of an item from this inventory. It will either remove the requested amount
     * or will remove all of the item if the amount requested was higher than the stock of the item. Returns the amount
     * removed from this inventory.
     * @param itemName The compName of the Item.
     * @param amount The amount of the item to remove.
     * @param id The id to use if we are taking from a reserved item. If this is 0, that indicates not taking from reserve.
     * @return The amount removed from the inventory.
     */
    public int removeItem(String itemName, int amount, long id) {
        return this.removeItemFromInventory(DataManager.getData(itemName, DataBuilder.JsonItem.class), amount, id);
    }

    /**
     * Clears the inventory.
     */
    public void clearInventory() {
        this.inventory.clear();
    }

    /**
     * Gets a list of the inventory.
     * @return An ArrayList containing the InventoryItems of the inventory.
     */
    @JsonIgnore
    public final ArrayList<InventoryItem> getItemList() {
        ArrayList<InventoryItem> list = new ArrayList<>();
        for(HashMap<String, InventoryItem> itemMap : this.inventory.values())
            list.addAll(itemMap.values());
        return list;
    }

    @JsonIgnore
    public int getCurrTotalItems() {
        return this.currTotalItems;
    }

    /**
     * Gets an amount of an item from the inventory if it exists.
     * @param itemName The name of the item to get an amount of.
     * @return The amount of the item in the inventory. 0 if the item does not exist.
     */
    @JsonIgnore
    public int getItemAmount(String itemName) {
        return this.getItemAmount(itemName, false);
    }

    /**
     * Gets an amount of an item from the inventory if it exists.
     * @param itemName The name of the item to get an amount of.
     * @param includeOnTheWay If the result should include items that are planned to arrive.
     * @return The amount of the item in the inventory. 0 if the item does not exist.
     */
    @JsonIgnore
    public int getItemAmount(String itemName, boolean includeOnTheWay) {
        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return 0;

        InventoryItem item = itemMap.get(itemName);
        int amount = 0;
        if (item != null)
            amount = item.getAmount(includeOnTheWay);

        return amount;
    }

    /**
     * Gets the amount of an item that can be added to the stack size. For example, if we have 3 wood and can only hold 10, we can add 7 more wood. Also, if wood is not
     * in the inventory, but we can hold 10 of it, then 10 can be added.
     * @param itemName The name of the item.
     * @return The amount that is able to be added to the item stack. This is 0 or negative to indicate none can be added.
     */
    public int getItemCanAddAmount(String itemName){
        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
        if(itemRef == null) return this.getMaxAmount();

        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return this.getMaxAmount();

        InventoryItem item = itemMap.get(itemName);
        if(item == null) return this.getMaxAmount();

        //-1 means infinite. If -1, return the max int size.
        if(item.maxAmount == -1) return item.getMaxAmount();
        return item.getMaxAmount() - item.getAmount();
    }

    /**
     * Checks if the itemType passed in is the only type in this inventory.
     * @param itemType The type of the item.
     * @return True if the only type in the inventory, false otherwise.
     */
    public boolean hasItemTypeOnly(String itemType){
        return this.inventory.size() == 1 && this.inventory.containsKey(itemType);
    }

    /**
     * Checks if this item contains an item.
     * @param itemName The name of the item.
     * @return True if the item exists in this Inventory, false otherwise.
     */
    public boolean hasItem(String itemName) {
        return this.inventory.containsKey(itemName);
    }

    /**
     * Checks if this inventory has no items.
     * @return True if empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.inventory.size() == 0;
    }

    /**
     * Gets the max amount per item.
     * @return The max amount per item. If the max amount is -1 to signify infinite, it will return the max value of an integer (2147483647).
     */
    public int getMaxAmount() {
        return this.maxAmount == -1 ? Integer.MAX_VALUE : this.maxAmount;
    }

    /**
     * Sets the max amount per item.
     * @param maxAmount The max amount per item.
     */
    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
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
        private Array<ItemLink> reserveList = new Array<>();
        private Array<ItemLink> onTheWayList = new Array<>();

        /**
         * Creates a new InventoryItem. Uses the Item passed in to clone a new Item for reference.
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
         * @param amountToReserve The amount of this item to reserve.
         * @return The amount reserved.
         */
        public int reserve(int amountToReserve, long id) {
            ItemLink link = new ItemLink(id, amountToReserve);
            reserveList.add(link);

            int _available = this.getAvailable(); //Get the available amount.
            int _reserved = amountToReserve <= _available ? amountToReserve : _available; //Take what we can!
            link.amount = _reserved; //Set the link.amount to the amount we were able to reserve.
            this.reserved += _reserved; //Add this amount to the item's reserved amount.
            return _reserved; //Return it!
        }

        /**
         * Unreserves an amount on this item. Does not actaully take from the item amount, simply removes the reserve placed on an item.
         * @param id The id to use for the reserve
         * @return The amount that was unreserved.
         */
        public int unReserve(long id) {
            ItemLink link=null;
            for (ItemLink _link : reserveList) {
                if (_link.id == id) {
                    link = _link;
                    break;
                }
            }

            int _unReserved = 0;
            if(link == null) return _unReserved;
            else {
                //TODO Clean this up! Since we have the reserve list now, we probably don't need to check for bounds.
                //Either take the amount requested (if under the total amount), or the total amount if amount > total
                _unReserved = link.amount <= this.getAmount(false) ? link.amount : this.getAmount(false);
                this.reserved -= _unReserved;   //Take away from the reserves.
                link.amount -= _unReserved;     //Take away from the link amount.

                //If the link amount is 0 or less and the id is not 0, remove it from the list.
                if(link.amount <= 0) this.reserveList.removeValue(link, true);
            }
            return _unReserved; //Return it!
        }

        /**
         * Adds an amount to be on the way.
         * @param amount The amount on the way.
         * @param id The id for the amount on the way.
         */
        public int addOnTheWay(int amount, long id) {
            this.onTheWay += amount;
            //Add to the list if not if 0
            this.onTheWayList.add(new ItemLink(id, amount));
            return amount;
        }

        /**
         * Removes an amount from being on the way.
         * @param id The id to use.
         */
        public int removeOnTheWay(long id) {
            ItemLink link=null;
            for(ItemLink _link : this.onTheWayList){
                if(_link.id == id){
                    link = _link;
                    break;
                }
            }
            if(link == null) return 0;
            this.onTheWay = this.onTheWay - link.amount < 0 ? 0 : this.onTheWay - link.amount;
            this.onTheWayList.removeValue(link, true); //Remove it from the list.
            return link.amount;
        }

        /**
         * @return The amount of the item plus the amount on the way.
         */
        public int getAmountAndOnTheWay() {
            return this.amount + this.onTheWay;
        }

        /**
         * Checks if an amount can be added to this item.
         * @param amount The amount to add.
         * @return True if possible, false otherwise.
         */
        public boolean canAddAmount(int amount) {
            return this.amount + amount <= maxAmount || this.maxAmount < 0;
        }

        /**
         * Adds an amount to this item.
         * @param amountToAdd The amount to add.
         * @return The amount that was able to be added.
         */
        public int addAmount(int amountToAdd) {
            //-1 indicates infinite. So if below 0, just skip this.
            if(this.maxAmount >= 0) {
                //If we are over the max amount, calculate what we can add and use that.
                if(amountToAdd + this.amount >= this.maxAmount)
                    amountToAdd = this.maxAmount - this.amount;
                if(amountToAdd < 0)
                    amountToAdd = 0;
            }
            //Add the amount and return how much we added.
            this.amount += amountToAdd;
            return amountToAdd;
        }

        /**
         * Removes an amount from this inventory.
         * @param amountToRemove The amount to remove.
         * @return The amount that was able to be removed.
         */
        public int removeAmount(int amountToRemove){
            //-1 indicates infinite. So if below 0, just skip this.
            if(this.maxAmount >= 0) {
                if(-amountToRemove + this.amount < 0)
                    amountToRemove = this.amount;
                if(amountToRemove < 0)
                    amountToRemove = 0;

            }
            //Add the amount and return how much we added.
            this.amount -= amountToRemove;
            return amountToRemove;
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

        /**
         * @return The maximum amount that this item can stack to.
         */
        public int getMaxAmount() {
            return this.maxAmount == -1 ? Integer.MAX_VALUE : this.maxAmount;
        }

        /**
         * @return The amount reserved of this item.
         */
        public int getReserved() {
            return this.reserved;
        }

        /**
         * @return The total amount that is on the way to the inventory that owns this item.
         */
        public int getOnTheWay(){
            return this.onTheWay;
        }

        /**
         * @return The amount available of this item. The amount available is the total amount minus the amount reserved.
         */
        public int getAvailable() {
            return this.amount - this.reserved;
        }

        private class ItemLink{
            public long id;
            public int amount;

            public ItemLink(long id, int amount){
                this.id = id;
                this.amount = amount;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ItemLink && this.id == ((ItemLink)obj).id;
            }
        }
    }
}
