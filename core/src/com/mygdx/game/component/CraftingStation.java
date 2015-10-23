package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.ItemNeeded;
import com.mygdx.game.util.Logger;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.MessageEventSystem;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Paha on 8/18/2015.
 * <p>A Component that designates an Entity as a crafting station, meaning items can be crafted on this Entity.</p>
 * <p>The Entity that contains the crafting station must have an inventory to work with. It also needs an Enterable
 * Component for colonists to enter to craft items.</p>
 */
public class CraftingStation extends Component implements IOwnable{
    /** Jobs that have not been started yet. */
    private LinkedList<CraftingJob> availableJobs;
    /** Jobs that are being actively worked on.*/
    private LinkedList<CraftingJob> inProgressJobs;
    /** Jobs that were started but stopped for some reason.*/
    private LinkedList<CraftingJob> stalledJobs;

    private Inventory inventory; //Crafting stations need an inventory, so this better have one.
    private Colony owningColony;
    private String[] craftingList;

    public CraftingStation() {
        this.setActive(false);
    }

    @Override
    public void added(Entity owner) {
        super.added(owner);

        this.availableJobs = new LinkedList<>();
        this.inProgressJobs = new LinkedList<>();
        this.stalledJobs = new LinkedList<>();
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.initLoad(entityMap, compMap);
    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.load(entityMap, compMap);

        this.inventory = this.getComponent(Inventory.class);
        if(this.inventory == null) Logger.log(Logger.ERROR, "CraftingStation on Entity "+this.owner.name+" does not have an inventory and probably should. Get ready for a crash!", true);
    }

    @Override
    public void init() {
        super.init();

    }

    @Override
    public void start() {
        super.start();

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
     * Adds a new CraftingJob to the availableJobs list.
     * @param itemName The name of the item to craft.
     * @param amount The amount of the item to craft.
     */
    public void addCraftingJob(String itemName, int amount){
        this.availableJobs.add(new CraftingJob(null, itemName, amount));
    }

    /**
     * @return True if this crafting station has an available job to be started.
     */
    public boolean hasAvailableJob(){
        return !this.availableJobs.isEmpty();
    }

    /**
     * @return True if there is a job in progress, false otherwise.
     */
    public boolean hasJobInProgress(){
        return !this.inProgressJobs.isEmpty();
    }

    /**
     * @return True if there is a stalled job, false otherise.
     */
    public boolean hasStalledJob(){
        return !this.stalledJobs.isEmpty();
    }

    /**
     * Removes (finishes) a CraftingJob in the in-progress list.
     * @param id The ID of the CraftingJob to finish (remove).
     * @return True if the CraftingJob was found and removed, false otherwise.
     */
    public boolean finishJobInProgress(long id){
        //Gotta use an iterator since we are using a linked list and need to remove while iterating...
        Iterator<CraftingJob> iter = this.inProgressJobs.iterator();
        while(iter.hasNext()){
            CraftingJob j = iter.next();
            if(j.id == id){
                iter.remove();
                MessageEventSystem.notifyEntityEvent(this.owner, "crafting_job_switched", "inProgress", "finished", j);
                return true; //Found and removed!! Return true!
            }
        }

        //Nothing was found, return false...
        return false;
    }

    /**
     * Transfers the first available CraftingJob from the available list to the in-progress list.
     * @return The CraftingJob that was transferred from available to in progress.
     */
    public CraftingJob setFirstAvailableToInProgress(){
        CraftingJob job = null;
        if(this.availableJobs.size() > 0)
            job = this.availableJobs.pop();

        if(job == null) return null;
        this.inProgressJobs.add(job);
        MessageEventSystem.notifyEntityEvent(this.owner, "crafting_job_switched", "available", "inProgress", job);

        return job;
    }

    /**
     * Sets an in-progress job to stalled.
     * @param id The ID of the in-progress job to set to stalled.
     * @return True if the job with 'id' was able to be transferred. False otherwise.
     */
    public boolean setInProgressToStalled(long id){
        CraftingJob job = null;
        //Gotta use an iterator since we are using a linked list and need to remove while iterating...
        Iterator<CraftingJob> iter = this.inProgressJobs.iterator();
        while(iter.hasNext()){
            CraftingJob j = iter.next();
            if(j.id == id){
                job = j;
                iter.remove();
                break;
            }
        }

        if(job == null) return false;
        this.stalledJobs.add(job);
        MessageEventSystem.notifyEntityEvent(this.owner, "crafting_job_switched", "inProgress", "stalled", job);
        return true;
    }

    /**
     * Gets the first available job if possible.
     * @return The first available job, null otherwise (if no jobs).
     */
    public CraftingJob getFirstAvailableJob(){
        return this.availableJobs.peekFirst();
    }

    /**
     * Gets the items the job with id 'id' needs to begin crafting. This is the amount needed from the recipe minus the amount in the inventory.
     * @param id The ID of the job to get items for.
     * @return An Array of ItemNeeded that indicates the item names and amounts needed. Returns an empty list if nothing is needed, and null if no job with that ID could be found.
     */
    public Array<ItemNeeded> getItemsNeededForJob(long id) {
        //TODO For now we are only basing this on one item for testing...

        CraftingJob job = null;
        //Try to get a job from the available jobs
        for (CraftingJob j : this.availableJobs)
            if (j.id == id) {
                job = j;
                break;
            }

        //If the job is still null, return null.
        if (job == null) return null;

        //Make a list of the items that we need.
        Array<ItemNeeded> list = new Array<>();
        DataBuilder.JsonRecipe recipe = job.itemRecipe;
        for (int i = 0; i < recipe.items.length; i++) {
            String itemName = recipe.items[i];
            int recipeAmount = recipe.itemAmounts[i];

            //Get the amount needed. If it's more than 0, we need some more! Otherwise, we don't need any, don't add!
            int amountNeeded = recipeAmount - this.inventory.getItemAmount(itemName);
            if (amountNeeded > 0) list.add(new ItemNeeded(itemName, amountNeeded));
        }

        return list;
    }

    /**
     * @return A String array of item names that can be crafting by this building.
     */
    public String[] getCraftingList(){
        return this.craftingList;
    }

    /**
     * Sets the items that this CraftingStation can craft.
     * @param craftingList The String array of item names.
     */
    public void setCraftingList(String[] craftingList){
        this.craftingList = craftingList;
    }

    /**
     * @return The available jobs list.
     */
    public LinkedList<CraftingJob> getAvailableList(){
        return this.availableJobs;
    }

    /**
     * @return The in-progress jobs list.
     */
    public LinkedList<CraftingJob> getInProgressJobs(){
        return this.inProgressJobs;
    }

    /**
     * @return The stalled jobs list.
     */
    public LinkedList<CraftingJob> getStalledJobs(){
        return this.stalledJobs;
    }

    /**
     * @return The Inventory that this CraftingStation is using for items.
     */
    public Inventory getInventory(){
        return this.inventory;
    }

    @Override
    public void addedToColony(Colony colony) {
        this.owningColony = colony;
    }

    @Override
    public Colony getOwningColony() {
        return this.owningColony;
    }

    /**
     * Holds important information about a crafting job including the building, item reference, item recipe, amount, and percentage done.
     */
    public static class CraftingJob{
        public Building building;
        public DataBuilder.JsonItem itemRef;
        public DataBuilder.JsonRecipe itemRecipe;
        public int amount;
        public long id;
        public float percentageDone = 0;

        private CraftingJob(Building building, String itemName, int amount){
            this.building = building;
            this.itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
            this.itemRecipe = DataManager.getData(itemName, DataBuilder.JsonRecipe.class);
            this.amount = amount;
            this.id = (long)(MathUtils.random()*Long.MAX_VALUE);
        }
    }
}
