package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Paha on 5/19/2015.
 * A building component that can be attached to Entities.
 */
public class Building extends Component implements IOwnable, IInteractable{
    @JsonIgnore
    public DataBuilder.JsonBuilding jBuilding;
    @JsonIgnore
    private Timer timer;
    @JsonIgnore
    private Constructable constructable;
    @JsonIgnore
    private Colony colonyOwner;
    @JsonIgnore
    private Inventory inventory;
    @JsonProperty
    private String buildingName;
    @JsonIgnore
    private Enterable enterable;
    @JsonIgnore
    private CraftingStation craftingStation;

    public Building(){

    }

    @Override
    public void start() {
        super.start();

        //Get the JsonBuilding reference, add the tags, add an inventory if under construction, and set the name.
        DataBuilder.JsonBuilding jBuilding = DataManager.getData(this.buildingName, DataBuilder.JsonBuilding.class);
        this.jBuilding = jBuilding;
        for(String tag : jBuilding.tags) this.owner.getTags().addTag(tag); //Add each tag that exists.

        //Add an inventory if under construction OR if the building is supposed to have one...
        if(this.owner.getTags().hasTag("constructing") || jBuilding.inventory) this.inventory = this.addComponent(new Inventory());
        this.owner.name = jBuilding.displayName; //Set the display name.

        this.load();
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        DataBuilder.JsonBuilding jBuilding = DataManager.getData(this.buildingName, DataBuilder.JsonBuilding.class);
        boolean isConstructing = this.owner.getTags().hasTag("constructing"); //If the building is under construction

        //If we are constructing still on a load OR this building should have an inventory...
        if(isConstructing || jBuilding.inventory){
            //Since this method (load) is called in the init() function, make sure the inventory is still null.
            if(this.inventory == null) this.inventory = this.getComponent(Inventory.class);
            this.inventory.setMaxAmount(-1);
            this.inventory.addItem("wood_pick", 1);
        }

        //If we are constructing this on load, set the timer and get the constructable component.
        if(isConstructing){
            this.constructable = this.getComponent(Constructable.class);
            this.timer = new RepeatingTimer(0.1, this.constructable::build);
        }else{
            this.setActive(false);
        }

        //If we are an Enterable, create the Enterable (if it doesn't exist already or on the Entity) and set some stuff.
        if(jBuilding.enterable) {
            if(this.enterable == null) this.enterable = this.getComponent(Enterable.class); //First try to get
            if(this.enterable == null) this.enterable = this.addComponent(new Enterable()); //Then create.
            enterable.setEnterPositions(jBuilding.enterablePositions);
            enterable.setMaxOccupants(jBuilding.enterableMaxOccupancy);
        }

        //If this building is supposed to craft stuff, add a crafting station to it!
        if(jBuilding.crafting != null){
            if(this.craftingStation == null) this.craftingStation = this.getComponent(CraftingStation.class);
            if(this.craftingStation == null) this.craftingStation = this.addComponent(new CraftingStation());
            this.colonyOwner.addOwnedToColony(this.craftingStation); //Add it to the colony crafting centers...
            this.craftingStation.addCraftingJob("wood_pick", 1);
        }

        //Set the image and dimensions (if not null)
        if(jBuilding.dimensions != null)
            this.owner.getComponents().getIdentity().setSprite(jBuilding.image, jBuilding.spriteSheet, jBuilding.dimensions[0], jBuilding.dimensions[1]);
        else
            this.owner.getComponents().getIdentity().setSprite(jBuilding.image, null);

    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //System.out.println("Updating building");
        if(this.timer != null) {
            //System.out.println("Updating timer");
            this.owner.getGraphicIdentity().getSprite().setAlpha(0.5f);
            //this.timer.update(delta);
        }

        //When construction is complete, remove all constructing related stuff.
        if(this.constructable.isComplete()){
            this.owner.destroyComponent(Constructable.class);
            this.owner.getTags().removeTag("constructing");
            this.constructable = null;
            this.owner.getGraphicIdentity().getSprite().setAlpha(1);
            this.timer = null;
            //When we complete the constructable, if the building is not supposed to have an inventory, remove it!
            if(!DataManager.getData(this.buildingName, DataBuilder.JsonBuilding.class).inventory){
                this.owner.destroyComponent(Inventory.class);
                this.inventory = null;
            }
            this.setActive(false);
        }
    }

    @Override
    public void destroy(Entity destroyer) {

        super.destroy(destroyer);
    }

    public void setBuildingName(String name){
        this.buildingName = name;
    }

    public String getBuildingName(){
        return this.buildingName;
    }

    @Override
    public void addedToColony(Colony colony) {
        this.colonyOwner = colony;
    }

    @Override
    @JsonIgnore
    public Colony getOwningColony() {
        return this.colonyOwner;
    }

    @Override
    @JsonIgnore
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    @JsonIgnore
    public Stats getStats() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getStatsText() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return this.owner.name;
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
        return this.constructable;
    }

    @Override
    public CraftingStation getCraftingStation() {
        return null;
    }

    @Override
    public Building getBuilding() {
        return this;
    }

    @Override
    public Enterable getEnterable() {
        return this.enterable;
    }
}
