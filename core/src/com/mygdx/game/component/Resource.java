package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.StringTable;
import com.mygdx.game.helpers.Tags;
import com.mygdx.game.helpers.managers.DataManager;
import com.mygdx.game.interfaces.IInteractable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component implements IInteractable{
    private String resourceName = "default", displayName = "default", resourceType = "default";
    private Array<String> itemNames;
    private Array<Integer> itemAmounts;
    private int itemIndex = 0;
    private float gatherTime = 1f;
    private float gatherTick = 1f;
    private volatile Entity taken = null;

    private StringBuilder contents = new StringBuilder();
    private DataBuilder.JsonResource resRef;

    public Tags effectTags = new Tags("effect");
    public Tags resourceTypeTags = new Tags("resource");

    public Resource(String resourceName) {
        super();
        this.resourceName = resourceName; //Record name

        this.setActive(false);
    }

    public Resource() {
        this("NothingResource");
    }

    /**
     * A copy constructor that copies the data from another resource.
     * @param resource The Resource Component to copy.
     */
    public Resource(Resource resource) {
        this(resource.resourceName);
        this.displayName = resource.displayName;
        this.resourceType = resource.resourceType;
        this.itemNames = resource.itemNames;
        this.itemAmounts = resource.itemAmounts;
    }

    /**
     * Copies a JSonResource object.
     * @param jRes The JSonResource to copy.
     */
    public Resource(DataBuilder.JsonResource jRes){
        this(jRes.resourceName);
        this.resRef = jRes;

        initItem(jRes);
    }

    //Initializes the item.
    private void initItem(DataBuilder.JsonResource jRes){
        this.displayName = jRes.displayName;
        this.resourceType = jRes.resourceType;
        this.itemNames = new Array<>(jRes.itemNames);
        this.gatherTime = jRes.gatherTime;

        generateItemInfo(jRes.itemNames, jRes.itemAmounts, jRes.itemChances);
    }

    /**
     * Generates the item information about the resource being created, such as the names and item itemAmounts.
     * @param itemNames The String array of item names.
     * @param itemAmounts The 2D int array of itemAmounts for each item name.
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
                names.add(itemNames[i]); //Adds the item name to this resource.
                DataBuilder.JsonItem itemRef = DataManager.getData(itemNames[i], DataBuilder.JsonItem.class); //Get the itemRef
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
    public void init(Entity owner) {
        super.init(owner);
        owner.name = this.displayName;

        if(resRef.resourceType.equals("water"))
            owner.active = false;
    }

    @Override
    public void start() {
        super.start();

    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public DataBuilder.JsonResource getResRef() {
        return resRef;
    }

    public String getResourceName(){
        return this.resourceName;
    }

    /**
     * @return A String which is the formal display name of this Resource.
     */
    public String getDisplayName() {
        return displayName;
    }

    public String[] getCurrItems(){
        return itemNames.toArray();
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
     * Only peeks for available resources that we want, which is supplied by the 'wanted' parameter.
     * @param inventory The Inventory to check against.
     * @param wanted The String array of wanted types. Every item checked to be added will first be checked if it is wanted.
     * @return True if an item is wanted, false otherwise.
     */
    public boolean peekAvailableOnlyWanted(Inventory inventory, String[] wanted){
        if(!peek()) return false; //If this resource has nothing left, return false.

        //Try to find an item we can take.
        int flag = itemIndex;
        do{
            //First, check that the current item from the resource is one that we want and can be added to the inventory. Return true if so.
            for(String want : wanted)
                if(want.equals(itemNames.get(itemIndex)) && inventory.canAddItem(itemNames.get(itemIndex)))
                    return true; //If we can take this, return true.

            itemIndex = (itemIndex + 1)% itemNames.size;
        }while(itemIndex != flag);

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

        do {
            //If we can't add this to our inventory, simply continue.
            if(!inventory.canAddItem(itemNames.get(itemIndex))) continue;

            inventory.addItem(itemNames.get(itemIndex)); //Add the item.

            //Decrement the value and remove if 0.
            int amt = itemAmounts.get(itemIndex); //Get the amount
            itemAmounts.set(itemIndex, --amt); //Set the amount to one less in the list.
            if(amt <= 0) { //If it's below 0, remove it...
                itemNames.removeIndex(itemIndex); //Remove the name.
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


    /**
     * @return The gather time for this resource. This is the total time it takes to fully gather the resource.
     */
    public float getGatherTime(){
        return this.gatherTime;
    }

    /**
     * @return The gather tick for this resource. This is the time it takes to gather part of the resource. The gather tick will be an X number of ticks
     * for the gather time, meaning if a resource takes 5 seconds to gather, there could be 5 ticks (one per second) to gather.
     */
    public float getGatherTick(){
        return this.gatherTick;
    }

    /**
     * @return True if this Resource is taken, false otherwise.
     */
    public boolean isTaken(){
        return this.taken != null;
    }

    /**
     * @return The Entity that has marked this resource as taken, null if not taken.
     */
    public Entity getTaken(){
        return this.taken;
    }

    /**
     * Sets this Resource as taken or not taken.
     * @param entity The Entity to take this resource. Null if setting the resource as not taken.
     */
    public void setTaken(Entity entity){
        this.taken = entity;
    }

    /**
     * Sets the gather time for this Resource.
     * @param gatherTime The amount of time to gather.
     */
    public void setGatherTime(float gatherTime){
        this.gatherTime = gatherTime;
    }

    /**
     * Sets the interType for this Resource.
     * @param resourceType A String denoting the interType.
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Checks if the Skills Component passed needs and has the required Skill.
     * @param skills The Skills Component to check the Skill for.
     * @return True if it has the required skill or does not need a skill, false otherwise.
     */
    public boolean hasRequiredSkill(Skills skills) {
        return !resRef.skillRequired || skills.getSkill(resRef.skill) != null;
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public Stats getStats() {
        return null;
    }

    @Override
    public String getStatsText() {
        String takenBy = this.taken == null ? "null" : this.taken.getComponent(Colonist.class).getName();
        contents.setLength(0);
        for(int i=0;i<itemNames.size;i++) contents.append(itemAmounts.get(i)).append(" ").append(itemNames.get(i)).append(System.lineSeparator());
        return contents.toString() + "\ntaken: "+this.isTaken()+" by: "+takenBy+"\ntags: "+this.resourceTypeTags.toString();
    }

    @Override
    public Skills getSkills() {
        return null;
    }

    @Override
    public String getName() {
        return this.getDisplayName();
    }

    @Override
    public BehaviourManagerComp getBehManager() {
        return null;
    }
}
