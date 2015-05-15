package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.StringTable;
import com.mygdx.game.helpers.Tags;
import com.mygdx.game.helpers.managers.DataManager;
import com.mygdx.game.interfaces.IInteractable;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component implements IInteractable{
    private String resourceName = "default", displayName = "default", resourceType = "default";
    private String[] itemNames;
    private int itemIndex = 0;
    private int[] itemAmounts;
    private float gatherTime = 1f;
    private float gatherTick = 1f;
    private volatile Entity taken = null;
    private Array<String> gatherList = new Array<>(false, 10, String.class);

    private StringBuilder contents = new StringBuilder();
    private DataBuilder.JsonResource resRef;

    public Tags effectTags = new Tags();
    public Tags resourceTypeTags = new Tags();

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
        this.itemNames = jRes.itemNames;
        this.gatherTime = jRes.gatherTime;

        generateItemInfo(jRes.itemNames, jRes.itemAmounts);
    }

    /**
     * Generates the item information about the resource being created, such as the names and item itemAmounts.
     * @param itemNames The String array of item names.
     * @param itemAmounts The 2D int array of itemAmounts for each item name.
     */
    private void generateItemInfo(String[] itemNames, int[][] itemAmounts){
        TIntArrayList amounts = new TIntArrayList(10);
        ArrayList<String> names = new ArrayList<>(10);
        int total = 0, highest = 0;

        for(int i=0;i<itemAmounts.length; i++) {
            int amount = MathUtils.random(itemAmounts[i][1] - itemAmounts[i][0]) + itemAmounts[i][0]; //Add diff to base.
            if(amount != 0){
                amounts.add(amount); //Adds the item amount to this resource.
                names.add(itemNames[i]); //Adds the item name to this resource.
                DataBuilder.JsonItem itemRef = DataManager.getData(itemNames[i], DataBuilder.JsonItem.class); //Get the itemRef
                resourceTypeTags.addTag(StringTable.getString("resource_type", itemRef.getItemType())); //Adds the type to the resource type tags.

                //For every item effect, add the effect to the effectTags.
                if(itemRef.getEffects() != null)
                    for(String effect : itemRef.getEffects())
                        effectTags.addTag(StringTable.getString("item_effect", effect));

                if(amount > highest) highest = amount;
                total += amount;
            }
        }

        this.itemAmounts = amounts.toArray();
        this.itemNames = names.toArray(new String[names.size()]);
        this.gatherTick = gatherTime/total;
    }

    /**
     * Generates the item information about the resource being created, such as the names and item itemAmounts.
     * @param itemNames The String array of item names.
     * @param itemAmounts The 2D int array of itemAmounts for each item name.
     */
    private void generateItemInfo(String[] itemNames, int[] itemAmounts){
        TIntArrayList amounts = new TIntArrayList(10);
        ArrayList<String> names = new ArrayList<>(10);
        int total = 0, highest = 0;

        for(int i=0;i<itemAmounts.length; i++) {
            if(itemAmounts[i] != 0){
                amounts.add(itemAmounts[i]); //Adds the item amount to this resource.
                names.add(itemNames[i]); //Adds the item name to this resource.
                DataBuilder.JsonItem itemRef = DataManager.getData(itemNames[i], DataBuilder.JsonItem.class); //Get the itemRef
                resourceTypeTags.addTag(StringTable.getString("resource_type", itemRef.getItemType())); //Adds the type to the resource type tags.

                //For every item effect, add the effect to the effectTags.
                if(itemRef.getEffects() != null)
                    for(String effect : itemRef.getEffects())
                        effectTags.addTag(StringTable.getString("item_effect", effect));

                if(itemAmounts[i] > highest) highest = itemAmounts[i];
                total += itemAmounts[i];
            }
        }

        this.itemAmounts = amounts.toArray();
        this.itemNames = names.toArray(new String[names.size()]);
        this.gatherTick = gatherTime/total;
    }

    @Override
    public void init(Entity owner) {
        super.init(owner);
        owner.name = this.displayName;

        //Build the item name string for displaying in the UI.
        for(int i=0;i<itemNames.length;i++)
            contents.append(itemAmounts[i]).append(" ").append(itemNames[i]).append(System.lineSeparator());

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

    /**
     * @return True if the resource has resources left, false otherwise. If false, also either destroys the resource or re-initializes it.
     */
    public boolean peek(){
        int val = 0;
        for(int amt : itemAmounts) val += amt;

        //If it's empty, either destroy it or re-initialize it.
        if(val == 0 && !DataManager.getData(this.resourceName, DataBuilder.JsonResource.class).infinite)
            this.owner.setToDestroy();
        else if(val == 0)
            this.initItem(this.resRef);

        return val != 0;
    }

    /**
     * @return An array of item names gathered from this resource. Empty array if nothing was gathered.
     */
    public String[] gatherFrom(){
        gatherItemRotating(gatherList); //Get the items to give to the one gathering the item.

        //If the gather list is 0, it means it's empty. If not infinite, destroy. Otherwise, re-initialize it.
        if(gatherList.size == 0 && !DataManager.getData(this.resourceName, DataBuilder.JsonResource.class).infinite)
            this.owner.setToDestroy();
        else if(gatherList.size == 0)
            this.initItem(this.resRef);

        //generateItemInfo(itemNames, itemAmounts);

        return gatherList.toArray();
    }

    //Add exactly one item to the list, rotating each time.
    private void gatherItemRotating(Array<String> list) {
        int flag = itemIndex;
        list.clear();

        do {
            //If the amount is more than 0, take it and decrement the amount in this resource.
            if (itemAmounts[itemIndex] > 0) {
                list.add(itemNames[itemIndex]);
                itemAmounts[itemIndex]--;
            }

            itemIndex = (itemIndex + 1) % itemNames.length;
            //If we do a full circle and meet the initial value, return.
            if (itemIndex == flag) return;
            //Loop this until we have at least one item in the list.
        } while (list.size < 1);
    }

    //Add each item to the list if available (amount more than 0).
    private void gatherItemEach(Array<String> list){
        list.clear();

        for(int i=0;i<itemNames.length;i++){
            if(itemAmounts[i] <= 0) continue;
            list.add(itemNames[i]);
            itemAmounts[i]--;
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
