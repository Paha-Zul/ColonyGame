package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.entity.BuildingEntity;
import com.mygdx.game.entity.ColonistEnt;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.util.DataBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

/**
 * Created by Paha on 1/17/2015.
 * The Colony Component should be attached to an empty Entity. The Colony is not the main building, but more of a hidden control center.
 */
public class Colony extends Component implements IInteractable {
    @JsonProperty
    private String colonyName = "Colony";

    private ArrayList<Colonist> colonistList;
    private HashMap<Class<? extends Component>, Array<Component>> ownedMap;
    private HashMap<String, Inventory.InventoryItem> quickInv;
    private Inventory inventory;

    public Colony() {
        super();
    }

    @Override
    public void save() {

    }

    @Override
    public void initLoad() {
        super.initLoad();

        this.inventory = this.owner.getComponent(Inventory.class);
        this.quickInv = new HashMap<>();
        this.ownedMap = new HashMap<>();
        this.colonistList = new ArrayList<>(20);
    }

    @Override
    public void init() {
        super.init();

        this.initLoad();
    }

    @Override
    public void start() {
        super.start();
        this.owner.name = "emptyColonyObject";
        this.inventory = this.owner.addComponent(new Inventory());
        this.inventory.setMaxAmount(-1);
        load();

    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);

        this.inventory = null;
        this.colonistList = null;
        this.ownedMap = null;
        this.quickInv = null;
    }

    /**
     * Makes a new colonist (hiding some ugly stuff) using the start position + a random offset.
     * @param start The location to center the creation on.
     * @param offset The offset which is random'd and added to the start (-offset to +offset)
     * @param textureName The name of the texture to use for the colonist.
     * @return The newly added colonist!
     */
    public Entity makeColonist(Vector2 start, float offset, String textureName){
        Vector2 newPos = new Vector2(start.x + MathUtils.random()*offset*2 - offset, start.y + MathUtils.random()*offset*2 - offset);
        return new ColonistEnt(newPos, 0, new String[]{textureName,""}, 10);
    }

    /**
     * Gets a (T) Component from this colony.
     * @param cls The Class type of the component to get (ie: Building.class).
     * @param predicate The Predicate function to test the Component for. (ie: does Building have inventory?)
     * @param <T> The Component type (ie: Building.class) which extends component.
     * @return The Component that matches the predicate, if any. Otherwise, null if no match was found.
     */
    @SuppressWarnings("unchecked")
    @JsonIgnore
    public <T extends Component> T getOwnedFromColony(Class<T> cls, Predicate<T> predicate){
        Array<Component> list = ownedMap.get(cls);
        if(list != null) {
            for (Component comp : ownedMap.get(cls))
                if (predicate.test((T) comp)) return (T) comp;
        }

        return null;
    }

    @JsonIgnore
    public <T extends Component> Array<Component> getOwnedListFromColony(Class<T> cls){
        return ownedMap.get(cls);
    }

    /**
     * Under construction!!
     * @param position
     * @param rotation
     * @param buildingRef
     * @param drawLevel
     * @return
     */
    @JsonIgnore
    public Entity addBuildingEntity(Vector2 position, float rotation, DataBuilder.JsonBuilding buildingRef, int drawLevel){
        //TODO I should do things like this?
        Entity building = new BuildingEntity(position, rotation, buildingRef, drawLevel);
        return building;
    }

    /**
     * Adds a Colonist to this colony. This will also set the Colonist's Colony when added.
     * @param colonist The Colonist Component to add.
     */
    @JsonIgnore
    public void addColonist(Colonist colonist){
        this.colonistList.add(colonist);
        colonist.setColony(this);
        this.addOwnedToColony(colonist);
    }

    /**
     * Adds a Component to the ownerByColony structure. The Component must also implement the
     * IOwnable interface.
     * @param comp The Component to add to this colony.
     * @param <T> The class type.
     */
    @JsonIgnore
    public <T extends Component & IOwnable> void addOwnedToColony(T comp){
        Class<? extends Component> cls = comp.getClass();
        ownedMap.putIfAbsent(cls, new Array<>());
        ownedMap.get(comp.getClass()).add(comp);
        comp.addedToColony(this);
    }

    /**
     * Adds the item to a hashmap for the colony to use.
     * @param itemRef The JsonItem reference.
     * @param amount The amount to add.
     */
    @JsonIgnore
    public void addItemToGlobal(DataBuilder.JsonItem itemRef, int amount){
        if(amount > 0) this.inventory.addItem(itemRef.getItemName(), amount);
        else if(amount < 0) this.inventory.removeItem(itemRef.getItemName(), -amount);
    }

    @JsonIgnore
    public final HashMap<String, Inventory.InventoryItem> getGlobalInv(){
        return quickInv;
    }

    /**
     * Gets the number of colonists this Colony has.
     * @return An integer which is the number of colonists.
     */
    @JsonIgnore
    public int getNumColonists(){
        return this.colonistList.size();
    }

    @Override
    @JsonIgnore
    public Inventory getInventory(){
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
        return null;
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
    @JsonIgnore
    public CraftingStation getCraftingStation() {
        return null;
    }

    @Override
    @JsonIgnore
    public Building getBuilding() {
        return null;
    }

    @Override
    @JsonIgnore
    public Enterable getEnterable() {
        return null;
    }
}
