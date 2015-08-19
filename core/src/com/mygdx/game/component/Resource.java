package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.StringTable;
import com.mygdx.game.util.Tags;
import com.mygdx.game.util.managers.DataManager;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component implements IInteractable{
    @JsonProperty
    private String resourceName = "default", displayName = "default", resourceType = "default";
    @JsonIgnore
    private Array<String> itemNames;
    @JsonIgnore
    private Array<Integer> itemAmounts;
    @JsonProperty
    private int itemIndex = 0;
    @JsonProperty
    private float gatherTime = 1f;
    @JsonProperty
    private float gatherTick = 1f;
    @JsonIgnore
    private volatile Entity taken = null;
    @JsonIgnore
    private StringBuilder contents = new StringBuilder();
    @JsonIgnore
    private DataBuilder.JsonResource resRef;
    @JsonProperty
    public Tags effectTags = new Tags("effect");
    @JsonProperty
    public Tags resourceTypeTags = new Tags("resource");

    public Resource() {
        super();
        this.setActive(false);
    }

    public void copyResource(Resource resource){
        this.resRef = DataManager.getData(resource.resourceName, DataBuilder.JsonResource.class);
        initItem(resource);
    }

    public void copyResource(DataBuilder.JsonResource jRes){
        this.resRef = jRes;
        initItem(jRes);
    }

    //Initializes the item.
    private void initItem(Resource resource){
        this.displayName = resource.displayName;
        this.resourceType = resource.resourceType;
        this.itemNames = new Array<>(resource.itemNames);
        this.gatherTime = resource.gatherTime;
        this.resourceName = resource.resourceName;

        generateItemInfo(resource.resRef.itemNames, resource.resRef.itemAmounts, resource.resRef.itemChances);
    }

    //Initializes the item.
    private void initItem(DataBuilder.JsonResource jRes){
        this.displayName = jRes.displayName;
        this.resourceType = jRes.resourceType;
        this.itemNames = new Array<>(jRes.itemNames);
        this.gatherTime = jRes.gatherTime;
        this.resourceName = jRes.resourceName;

        generateItemInfo(jRes.itemNames, jRes.itemAmounts, jRes.itemChances);
    }

    /**
     * Generates the item information about the resource being created, such as the names and item itemAmounts.
     * @param itemNames The String array of item names.
     * @param itemAmounts The 2D int array of itemAmounts for each item compName.
     */
    private void generateItemInfo(String[] itemNames, int[][] itemAmounts, int[] itemChances){
        Array<Integer> amounts = new Array<>(10);
        Array<String> names = new Array<>(10);
        int total = 0, highest = 0;

        for(int i=0;i<itemAmounts.length; i++) {
            if(MathUtils.random() > itemChances[i]) continue;

            int amount = MathUtils.random(itemAmounts[i][1] - itemAmounts[i][0]) + itemAmounts[i][0]; //Add diff to base.
            if(amount != 0){
                amounts.add(amount); //Adds the item amount to this resource.
                names.add(itemNames[i]); //Adds the item compName to this resource.
                DataBuilder.JsonItem itemRef = DataManager.getData(itemNames[i], DataBuilder.JsonItem.class); //Get the itemRef
                if(itemRef == null) GH.writeErrorMessage("There is no item named "+itemNames[i]+" for resource "+this.displayName+". Check the resource.json and items.json files and make sure resource/item pairs match.", true);
                resourceTypeTags.addTag(itemRef.getItemType()); //Adds the type to the resource type tags.

                //For every item effect, add the effect to the effectTags.
                if(itemRef.getEffects() != null)
                    for(String effect : itemRef.getEffects())
                        effectTags.addTag(StringTable.StringToInt("item_effect", effect));

                if(amount > highest) highest = amount;
                total += amount;
            }
        }

        this.itemAmounts = amounts;
        this.itemNames = names;
        this.gatherTick = gatherTime/total;
    }

    @Override
    public void init() {
        super.init();

    }

    @Override
    public void start() {
        super.start();

        owner.name = this.displayName;

        if(resRef.resourceType.equals("water"))
            owner.active = false;
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }


    /**
     * @return True if the resource has resources left, false otherwise. If false, also either destroys the resource or re-initializes it.
     */
    private boolean peek(){
        int size = itemAmounts.size;

        //If it's empty, either destroy it or re-initialize it.
        if(size == 0 && !DataManager.getData(this.resourceName, DataBuilder.JsonResource.class).infinite)
            this.owner.setToDestroy();
        else if(size == 0)
            this.initItem(this.resRef);

        return size > 0;
    }

    /**
     * Peeks at the available items left in this resource. Loops over the items until it finds one that the inventory can accept or until it fully loops around,
     * in which case, will return false. The itemIndex will either remain unchanged (loop around to where it was) or remain where the next available item that can be taken is.
     * It is recommended to call this function first to position the itemIndex at the right location and then call gatherFrom, which won't need to check again.
     * @param inventory The Inventory to use for checking against.
     * @return True if the inventory can take an item from here, false otherwise.
     */
    public boolean peekAvailable(Inventory inventory){
        if(!peek()) return false; //If this resource has nothing left, return false.

        //Try to find an item we can take.
        int flag = itemIndex;
        do{
            if(inventory.canAddItem(itemNames.get(itemIndex))) return true; //If we can take this, return true.
            itemIndex = (itemIndex + 1)% itemNames.size;
        }while(itemIndex != flag);

        //If we could find nothing we could take, return false.
        return false;
    }

    /**
     * Peeks in the resource for any items that match the 'wanted' string list. If any one of the items in the list
     * are found to be in the resource, the function returns true.
     * @param inventory The Inventory to check against.
     * @param wanted The String array of wanted types. Every item checked to be added will first be checked if it is wanted.
     * @return True if an item is wanted, false otherwise.
     */
    public boolean peekAvailableOnlyWanted(Inventory inventory, String[] wanted){
        if(!peek()) return false; //If this resource has nothing left, return false.

        //Try to find an item we can take.
        int flag = this.itemIndex;
        do{
            //First, check that the current item from the resource is one that we want and can be added to the inventory. Return true if so.
            for(String want : wanted)
                if(want.equals(itemNames.get(this.itemIndex)) && inventory.canAddItem(itemNames.get(this.itemIndex)))
                    return true; //If we can take this, return true.

            this.itemIndex = (this.itemIndex + 1)% itemNames.size; //Increment the itemIndex. This will basically preposition the index.
        }while(this.itemIndex != flag);

        //If we could find nothing we could take, return false.
        return false;
    }

    /**
     * Attempts to gather an item from this Resource and place it into the Inventory passed in.
     * @param inventory The Inventory to add an item to.
     * @return True if an item was able to be added from the resource, false otherwise.
     */
    public boolean gatherFrom(Inventory inventory){
        return gatherItemRotating(inventory) && peekAvailable(inventory);
    }

    //Add exactly one item to the list, rotating each time.
    private boolean gatherItemRotating(Inventory inventory) {
        int flag = itemIndex;
        if(!peek()) return false;

        do {
            //If we can't add this to our inventory, simply continue.
            if(!inventory.canAddItem(itemNames.get(itemIndex))) continue;

            inventory.addItem(itemNames.get(itemIndex)); //Add the item.

            //Decrement the value and remove if 0.
            int amt = itemAmounts.get(itemIndex); //Get the amount
            itemAmounts.set(itemIndex, --amt); //Set the amount to one less in the list.
            if(amt <= 0) { //If it's below 0, remove it...
                itemNames.removeIndex(itemIndex); //Remove the compName.
                itemAmounts.removeIndex(itemIndex); //Remove the amount.
            }

            //If the itemNames is not empty, increment the itemIndex
            if(itemNames.size != 0)
                itemIndex = (itemIndex + 1) % itemNames.size; //Increment the index and wrap around to the start if needed (mod)

            //Return true if we made it here!
            return true;
            //Loop this until we have at least one item in the list.
        } while (itemIndex != flag);

        //Return false if we made it here. THis means we couldn't add anythimg.
        return false;
    }

    //Add each item to the list if available (amount more than 0).
    private void gatherItemEach(Array<String> list){
        list.clear();

        for(int i=0;i<itemNames.size;i++){
            int amt = itemAmounts.get(i);
            if(amt <= 0) continue;
            list.add(itemNames.get(itemIndex));
            itemAmounts.set(itemIndex, amt-1);
        }
    }


    @JsonIgnore
    public DataBuilder.JsonResource getResRef() {
        return resRef;
    }

    @JsonIgnore
    public String getResourceName(){
        return this.resourceName;
    }

    /**
     * @return A String which is the formal display compName of this Resource.
     */
    @JsonIgnore
    public String getDisplayName() {
        return displayName;
    }

    @JsonIgnore
    public String[] getCurrItems(){
        return itemNames.toArray();
    }

    /**
     * @return The gather time for this resource. This is the total time it takes to fully gather the resource.
     */
    @JsonIgnore
    public float getGatherTime(){
        return this.gatherTime;
    }

    /**
     * @return The gather tick for this resource. This is the time it takes to gather part of the resource. The gather tick will be an X number of ticks
     * for the gather time, meaning if a resource takes 5 seconds to gather, there could be 5 ticks (one per second) to gather.
     */
    @JsonIgnore
    public float getGatherTick(){
        return this.gatherTick;
    }

    /**
     * @return True if this Resource is taken, false otherwise.
     */
    @JsonIgnore
    public boolean isTaken(){
        return this.taken != null;
    }

    /**
     * @return The Entity that has marked this resource as taken, null if not taken.
     */
    @JsonIgnore
    public Entity getTaken(){
        return this.taken;
    }

    /**
     * Sets this Resource as taken or not taken.
     * @param entity The Entity to take this resource. Null if setting the resource as not taken.
     */
    @JsonIgnore
    public void setTaken(Entity entity){
        this.taken = entity;
    }

    /**
     * Sets the gather time for this Resource.
     * @param gatherTime The amount of time to gather.
     */
    @JsonIgnore
    public void setGatherTime(float gatherTime){
        this.gatherTime = gatherTime;
    }

    /**
     * Sets the interType for this Resource.
     * @param resourceType A String denoting the interType.
     */
    @JsonIgnore
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);
    }

    @Override
    @JsonIgnore
    public Inventory getInventory() {
        return null;
    }

    @Override
    @JsonIgnore
    public Stats getStats() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getStatsText() {
        String takenBy = this.taken == null ? "null" : this.taken.getComponent(Colonist.class).getName();
        contents.setLength(0);
        for(int i=0;i<itemNames.size;i++) contents.append(itemAmounts.get(i)).append(" ").append(itemNames.get(i)).append(System.lineSeparator());
        return contents.toString() + "\ntaken: "+this.isTaken()+" by: "+takenBy+"\ntags: "+this.resourceTypeTags.toString();
    }

    @Override
    @JsonIgnore
    public String getName() {
        return this.getDisplayName();
    }

    @Override
    @JsonIgnore
    public BehaviourManagerComp getBehManager() {
        return null;
    }

    @Override
    @JsonIgnore
    public Component getComponent() {
        return this;
    }

    @Override
    @JsonIgnore
    public Constructable getConstructable() {
        return null;
    }

    @Override
    public CraftingStation getCraftingStation() {
        return null;
    }

    @Override
    public Building getBuilding() {
        return null;
    }

    @Override
    public Enterable getEnterable() {
        return null;
    }
}
