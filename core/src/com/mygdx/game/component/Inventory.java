package com.mygdx.game.component;

import com.badlogic.gdx.utils.Array;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.Logger;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.EventSystem;
import gnu.trove.map.hash.TLongObjectHashMap;

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

    //So basically a hashMap -> lists that hold item links.
    @JsonIgnore
    private TLongObjectHashMap<Array<ItemLink>> reserveMap, onTheWayMap;

    /**
     * Creates a default Inventory Component with the default values. This means this inventory can hold unlimited of everything.
     */
    public Inventory() {
        this.reserveMap = new TLongObjectHashMap<>(10, 0.75f);
        this.onTheWayMap = new TLongObjectHashMap<>(10, 0.75f);

        this.setActive(false);
    }

    @Override
    @JsonIgnore
    public void added(Entity owner) {
        super.added(owner);
        Building building = this.getComponent(Building.class);
        if (building != null) this.colony = building.getOwningColony();
    }

    @Override
    @JsonIgnore
    public void save() {

    }

    @Override
    @JsonIgnore
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

    }

    @Override
    @JsonIgnore
    public void start() {
        super.start();
        load(null, null);
    }

    @Override
    @JsonIgnore
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
    @JsonIgnore
    public boolean canAddItem(String itemName) {
        return this.canAddItem(itemName, 1);
    }

    /**
     * Checks if this Inventory can add a certain amount of item.
     * @param itemName The compName of the item to add.
     * @param amount   The amount to add.
     * @return True if it can be added, false otherwise.
     */
    @JsonIgnore
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
    @JsonIgnore
    public int reserveItem(String itemName, int amount, long id) {
        if(amount == 0 || id == 0) return 0; //We don't reserve if the ID is 0... Entities never have 0 as an ID, that indicates NOT an Entity.
        //Gets the item reference and then gets the item from the inventory. If the itemMap or item is null, return 0...
        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
        if(itemRef == null) {
            Logger.log(Logger.WARNING, "Item with name " + itemName + " was not found in the data manager. Is it the correct name?");
            return 0;
        }
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return 0;
        InventoryItem invItem = itemMap.get(itemName);
        if (invItem == null) return 0;

        //First, let's reserve an amount from the item and store it in the amount...
        amount = invItem.reserve(amount);
        if(amount == 0) return 0; //Return 0 if we didn't reserve any.

        //This is where we place a reserve of the item.
        //Get the list of reserves for this id. If it doesn't exist, add one!
        Array<ItemLink> list = this.reserveMap.get(id);
        if(list == null) {
            list = new Array<>(5);
            this.reserveMap.put(id, list);
        }
        ItemLink link=null;

        //Search over the list and find an existing reserve to add to. If we found one, save its reference.
        for(ItemLink _link : list) {
            if (_link.itemName.equals(itemName)) {
                _link.amount += amount;
                link = _link;
                break;
            }
        }

        //If this is null, we didn't find a link. Create a new link and add it to the list.
        if(link == null) {
            link = new ItemLink(itemName, amount);
            list.add(link);
        }

        //Return the amount reserved.
        return amount;
    }

    /**
     * Unreserves an amount of an item using the 'id' for the reserve id.
     * @param itemName The name of the item.
     * @param id The id of the reserve.
     * @return The amount that was unreserved. 0 If the item was not reserved or the id supplied didn't have any reserves.
     */
    @JsonIgnore
    public int unReserveItem(String itemName, long id) {
        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
        if(itemMap == null) return 0;
        InventoryItem item = itemMap.get(itemName);
        if(item == null) return 0;

        return this.unReserveItem(item, id);
    }

    @JsonIgnore
    private int unReserveItem(InventoryItem item, long id){
        Array<ItemLink> list = this.reserveMap.get(id);
        if(list == null) return 0; //Return 0, we don't have a reserve for this id.

        //Search the list for an existing link.
        ItemLink link=null;
        for(int i=0;i<list.size;i++){
            ItemLink _link = list.get(i);
            if(_link.itemName.equals(item.itemRef.getItemName())){
                link = _link;
                list.removeIndex(i); //Since the unreserve will be using the whole amount, remove the link from the list here.
                break;
            }
        }

        if(link == null) return 0; //No link was found, unable to unreserve.

        return item.unReserve(link.amount);
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
    @JsonIgnore
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
        return item.addOnTheWay(amount);
    }

    /**
     * Adds an amount of the item designated by the compName passed in.
     * @param itemName The compName of the item to add.
     * @param amount The amount of the item to add. If the amount is <= 0, returns early.
     */
    @JsonIgnore
    public void addItem(String itemName, int amount) {
        this.addItemToInventory(DataManager.getData(itemName, DataBuilder.JsonItem.class), amount, this.maxAmount);
    }

    /**
     * Internal function that deals with adding an item to the inventory.
     * @param itemRef The JsonItem to add.
     * @param amount The amount to add.
     * @param maxAmount The max amount that this item can stack to.
     */
    @JsonIgnore
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
     * Removes an item from being on the way.
     * @param itemName The name of the item.
     * @param id The id of the on the way record.
     * @return The amount that was removed from being on the way. -1 if the item did not exist.
     */
    @JsonIgnore
    public int removeOnTheWay(String itemName, long id){
//        DataBuilder.JsonItem itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
//        HashMap<String, InventoryItem> itemMap = this.inventory.get(itemRef.getItemType());
//        if(itemMap == null) return 0;
//
//        InventoryItem item = itemMap.get(itemName);
//        if(item == null) return 0;
//        int _removed = item.removeOnTheWay(id);
//        if(item.amount <= 0 && item.onTheWayList.size <= 0) this.inventory.remove(itemName);
//        return _removed;
        return 0;
    }

    /**
     * Adds one of the item designated by the itemName passed in.
     * @param itemName The compName of the item to add.
     */
    @JsonIgnore
    public void addItem(String itemName) {
        this.addItem(itemName, 1);
    }

    /**
     * Removes 1 item from the inventory.
     *
     * @param itemName The name of the item to remove.
     * @return The amount removed of the item from the Inventory.
     */
    @JsonIgnore
    public int removeItem(String itemName) {
        return this.removeItem(itemName, 1, 0);
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
    @JsonIgnore
    public int removeItem(String itemName, int amount, long id) {
        return this.removeItemFromInventory(DataManager.getData(itemName, DataBuilder.JsonItem.class), amount, id);
    }

    /**
     * Internal remove call. Removes an amount of an item.
     * @param itemRef The JsonItem to remove.
     * @param amountToRemove The amount to remove.
     * @param id The id to use if we are removing from a reserve. 0 indicates not taking from reserve.
     */
    @JsonIgnore
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
        //TODO We find the item from the hashmap, and then call unReserveItem which gets the item again from the hashMap. Fix this!
        if(id != 0) removed = item.removeAmount(this.unReserveItem(item, id)); //Remove the amount taken from the reserve.
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
     * Removes an amount of an item from this Inventory.
     *
     * @param itemName The name of the item to remove.
     * @param amount   The amount to remove from the inventory.
     * @return The amount of the item removed from the Inventory.
     */
    @JsonIgnore
    public int removeItem(String itemName, int amount) {
        return this.removeItem(itemName, amount, 0);
    }

    /**
     * Clears the inventory.
     */
    @JsonIgnore
    public void clearInventory() {
        this.inventory.clear();
    }

    /**
     * Gets a list of the items for the specific type passed in.
     * @param itemType The type of item to get a list of.
     * @return An array of items of the 'itemType'. Could be 0 if the type doesn't exist or there are no items of the type.
     */
    @JsonIgnore
    public final InventoryItem[] getItemsOfTypeList(String itemType){
        HashMap<String, InventoryItem> map = this.inventory.get(itemType);
        if(map == null) return new InventoryItem[0];
        return map.values().toArray(new InventoryItem[map.size()]);
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
    @JsonIgnore
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
     * Gets the max amount per item.
     * @return The max amount per item. If the max amount is -1 to signify infinite, it will return the max value of an integer (2147483647).
     */
    @JsonIgnore
    public int getMaxAmount() {
        return this.maxAmount == -1 ? Integer.MAX_VALUE : this.maxAmount;
    }

    /**
     * Sets the max amount per item.
     * @param maxAmount The max amount per item.
     */
    @JsonIgnore
    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    /**
     * Checks if the itemType passed in is the only type in this inventory.
     * @param itemType The type of the item.
     * @return True if the only type in the inventory, false otherwise.
     */
    @JsonIgnore
    public boolean hasItemTypeOnly(String itemType){
        return this.inventory.size() == 1 && this.inventory.containsKey(itemType);
    }

    /**
     * Checks if this item contains an item.
     * @param itemName The name of the item.
     * @return True if the item exists in this Inventory, false otherwise.
     */
    @JsonIgnore
    public boolean hasItem(String itemName) {
        return this.inventory.containsKey(itemName);
    }

    /**
     * Checks if this inventory has no items.
     * @return True if empty, false otherwise.
     */
    @JsonIgnore
    public boolean isEmpty() {
        return this.inventory.size() == 0;
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

        public InventoryItem(String itemName, int amount, int maxAmount) {
            this(DataManager.getData(itemName, DataBuilder.JsonItem.class), amount, maxAmount);
        }

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

        /**
         * Places a reserve on this item for some amount.
         * @param amountToReserve The amount of this item to reserve.
         * @return The amount reserved.
         */
        private int reserve(int amountToReserve) {
            //Either get the amountToReserve, or the amount available if we are requesting over the available amount.
            int _reserved = amountToReserve <= this.getAvailable() ? amountToReserve : this.getAmount();
            this.reserved += _reserved;
            return _reserved;
        }

        /**
         * @return The amount available of this item. The amount available is the total amount minus the amount reserved.
         */
        public int getAvailable() {
            return this.amount - this.reserved;
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
         * Unreserves an amount on this item. Does not actually take from the item amount.
         * @param amountToUnreserve The amount to unreserve.
         * @return The amount that was unreserved.
         */
        private int unReserve(int amountToUnreserve) {
            this.reserved -= amountToUnreserve;
            return amountToUnreserve;
        }

        /**
         * Adds an amount to be on the way.
         * @param amount The amount to add on the way.
         */
        public int addOnTheWay(int amount) {
            return this.onTheWay += amount;
        }

        /**
         * Removes an amount from being on the way.
         */
        public int removeOnTheWay(int amount) {
            return this.onTheWay -= amount;
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
    }

    private class ItemLink{
        public String itemName;
        public int amount;

        public ItemLink(String itemName, int amount){
            this.itemName = itemName;
            this.amount = amount;
        }
    }
}
