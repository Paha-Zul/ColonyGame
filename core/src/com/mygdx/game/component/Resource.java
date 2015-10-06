package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.collider.BoxCollider;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.StringTable;
import com.mygdx.game.util.Tags;
import com.mygdx.game.util.managers.DataManager;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;

/**
 * Created by Paha on 1/10/2015.
 * This class holds a reference to the JsonResource object as well as copying its values. The values (like the name and number of resources) so that
 * the Resource values can be modified, which in the JsonResource they cannot.
 */
public class Resource extends Component implements IInteractable{
    @JsonProperty
    public Tags effectTags = new Tags("effect");
    @JsonProperty
    public Tags resourceTypeTags = new Tags("resource");
    @JsonProperty
    private int itemIndex = 0;
    @JsonProperty
    private float gatherTime = 1f;
    @JsonProperty
    private float gatherTick = 1f;

    private volatile Entity taken = null;
    private StringBuilder contentsToDisplay; //Builds the string to display the contents
    private Array<String> itemNames; //The names
    private Array<Integer> itemAmounts; //The amounts
    private DataBuilder.JsonResource resRef; //The reference resource

    public Resource() {
        super();
        this.setActive(false);
    }

    public void copyResource(Resource resource){
        this.resRef = DataManager.getData(resource.getResRef().resourceName, DataBuilder.JsonResource.class);
        initResource(resource);
    }

    /**
     * @return The JsonResource that this resource mimics.
     */
    public DataBuilder.JsonResource getResRef() {
        return this.resRef;
    }

    /**
     * Initializes the resource.
     * @param resource The resource to copy and initialize from.
     */
    private void initResource(Resource resource){
        this.resRef = resource.getResRef();
        this.gatherTime = resource.gatherTime;

        generateItemInfo(resource.resRef.itemNames, resource.resRef.itemAmounts, resource.resRef.itemChances);
    }

    /**
     * Generates the item information about the resource being added, such as the names and item itemAmounts.
     * This basically randoms an amount for each item in the resource.
     * @param itemNames The String array of item names.
     * @param itemAmounts The 2D int array of itemAmounts for each item compName.
     */
    private void generateItemInfo(String[] itemNames, int[][] itemAmounts, int[] itemChances){
        Array<Integer> amounts = new Array<>(10);
        Array<String> names = new Array<>(10);
        int total = 0, highest = 0;

        //For each item in itemAmounts...
        for(int i=0;i<itemAmounts.length; i++) {
            if(MathUtils.random() > itemChances[i]) continue;

            //We random an amount (between the lower and higher bound)
            int amount = MathUtils.random(itemAmounts[i][1] - itemAmounts[i][0]) + itemAmounts[i][0]; //Add diff to base.
            if(amount != 0){
                amounts.add(amount); //Adds the item amount to this resource.
                names.add(itemNames[i]); //Adds the item compName to this resource.
                DataBuilder.JsonItem itemRef = DataManager.getData(itemNames[i], DataBuilder.JsonItem.class); //Get the itemRef
                if(itemRef == null) GH.writeErrorMessage("There is no item named "+itemNames[i]+" for resource "+this.resRef.displayName+". Check the resource.json and items.json files and make sure resource/item pairs match.", true);
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

    private void initResourceLight(DataBuilder.JsonResource jRes){
        this.resRef = jRes;
        this.gatherTime = jRes.gatherTime;
    }

    public void copyResource(DataBuilder.JsonResource jRes){
        this.resRef = jRes;
        initResource(jRes);
    }

    /**
     * Initializes the resource.
     * @param jRes The JsonResource to initialize from.
     */
    private void initResource(DataBuilder.JsonResource jRes){
        this.resRef = jRes;
        this.gatherTime = jRes.gatherTime;

        generateItemInfo(jRes.itemNames, jRes.itemAmounts, jRes.itemChances);
    }

    @Override
    public void save() {

    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.initLoad(entityMap, compMap);
        this.makeCollider();
    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.load(entityMap, compMap);

        contentsToDisplay = new StringBuilder();
    }

    @Override
    public void init() {
        super.init();
        this.initLoad(null, null);
    }

    private void makeCollider(){
        //Get the graphic for the collider size
        GraphicIdentity graphic = this.getComponent(GraphicIdentity.class);
        if(graphic == null || graphic.getSprite() == null) return;

        //Try to get the collider. If null, make a new one!
        BoxCollider collider = getComponent(BoxCollider.class);
        if(collider == null){
            collider = new BoxCollider();
            collider.setActive(false);
            collider = this.addComponent(collider);
            collider.setupBody(BodyDef.BodyType.StaticBody, ColonyGame.instance.world, graphic.getSprite().getWidth()/4, graphic.getSprite().getHeight()/2,
                    new Vector2(graphic.getSprite().getX() + graphic.getSprite().getWidth()/2, graphic.getSprite().getY() + graphic.getSprite().getHeight()/2), false, false);
        }else if(collider.getBody() == null) {
            collider.setupBody(BodyDef.BodyType.StaticBody, ColonyGame.instance.world, graphic.getSprite().getWidth() / 4, graphic.getSprite().getHeight() / 2,
                    new Vector2(graphic.getSprite().getX() + graphic.getSprite().getWidth() / 2, graphic.getSprite().getY() + graphic.getSprite().getHeight() / 2), false, false);
        }
    }

    @Override
    public void start() {
        super.start();

        owner.name = this.resRef.displayName;

        if(resRef.resourceType.equals("water")) {
            owner.active = false;
        }else{
            BoxCollider collider = this.getComponent(BoxCollider.class);
            this.owner.getGraphicIdentity().configureSprite();
            Sprite sprite = this.getEntityOwner().getComponents().getIdentity().getSprite();
        }

        this.load(null, null);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);
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
     * @return True if the resource has resources left, false otherwise. If false, also either destroys the resource or re-initializes it.
     */
    private boolean peek(){
        int size = itemAmounts.size;

        //If it's empty, either destroy it or re-initialize it.
        if(size == 0 && !this.resRef.infinite)
            this.owner.setToDestroy();
        else if(size == 0)
            this.initResource(this.resRef);

        return size > 0;
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

    /**
     * Only used for saving.
     * @return A list of items and amounts (name, amount).
     */
    @JsonProperty("resourceItems")
    private Array<String[]> getResItems(){
        Array<String[]> list = new Array<>(5);

        for(int i=0;i<this.itemNames.size;i++)
            list.add(new String[]{this.itemNames.get(i), ""+this.itemAmounts.get(i)});

        return list;
    }

        /**
         * Only used for saving.
         * @return The name of the JsonResource that this resource mimics.
         */
        @JsonProperty("resourceRef")
        private String getResRefName(){
        return this.resRef.resourceName;
    }

    /**
     * Uses the resRefName to initialize the resource. Only used for loading.
     * @param resRefName The name of the JsonResource reference.
     */
    @JsonProperty("resourceRef")
    private void setResRefName(String resRefName){
        this.resRef = DataManager.getData(resRefName, DataBuilder.JsonResource.class);
    }

    /**
     * Only used for loading
     * @param resourceItems The items to load into the resource.
     */
    @JsonProperty("resourceItems")
    private void setResourceItems(ArrayList<String[]> resourceItems){
        this.itemNames = new Array<>();
        this.itemAmounts = new Array<>();

        for(String[] item : resourceItems){
            this.itemNames.add(item[0]);
            this.itemAmounts.add(Integer.parseInt(item[1]));
        }
    }

    /**
     * @return The name of this resource.
     */
    public String getResourceName(){
        return this.resRef.resourceName;
    }

    /**
     * @return The number of items (not individual amounts, but total item types) left in this resource.
     */
    public String[] getCurrItems(){
        return itemNames.toArray();
    }

    /**
     * @return The gather time for this resource. This is the total time it takes to fully gather the resource.
     */
    public float getGatherTime(){
        return this.gatherTime;
    }

    /**
     * Sets the gather time for this Resource.
     * @param gatherTime The amount of time to gather.
     */
    public void setGatherTime(float gatherTime){
        this.gatherTime = gatherTime;
    }

    /**
     * @return The gather tick for this resource. This is the time it takes to gather part of the resource. The gather tick will be an X number of ticks
     * for the gather time, meaning if a resource takes 5 seconds to gather, there could be 5 ticks (one per second) to gather.
     */
    public float getGatherTick(){
        return this.gatherTick;
    }

    /**
     * Checks if this resource is taken by the Entity passed in.
     * @param entity The Entity that is in question of owning this resource.
     * @return True if the resource is taken by the Entity passed in, false otherwise.
     */
    public boolean isTakenBy(Entity entity){
        return this.getTaken() == entity;
    }

    /**
     * @return The Entity that has marked this resource as taken, null if not taken.
     */
    public Entity getTaken(){
        return this.taken;
    }

    /**
     * Sets the interType for this Resource.
     * @param resourceType A String denoting the interType.
     */
    public void setResourceType(String resourceType) {
        this.resRef.resourceType = resourceType;
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
        contentsToDisplay.setLength(0);
        for(int i=0;i<itemNames.size;i++)
            contentsToDisplay.append(itemAmounts.get(i)).append(" ").append(itemNames.get(i)).append(System.lineSeparator());

        return contentsToDisplay.toString() + "\ntaken: "+this.isTaken()+" by: "+takenBy+"\ntags: "+this.resourceTypeTags.toString();
    }

    /**
     * @return True if this Resource is taken, false otherwise.
     */
    public boolean isTaken(){
        return this.taken != null;
    }

    /**
     * Sets this Resource as taken or not taken.
     * @param entity The Entity to take this resource. Null if setting the resource as not taken.
     */
    
    public void setTaken(Entity entity){
        this.taken = entity;
    }

    @Override
    
    public String getName() {
        return this.getDisplayName();
    }

    /**
     * @return A String which is the formal display compName of this Resource.
     */
    public String getDisplayName() {
        return this.resRef.displayName;
    }

    @Override
    
    public BehaviourManagerComp getBehManager() {
        return null;
    }

    @Override
    
    public Component getComponent() {
        return this;
    }

    @Override
    
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
