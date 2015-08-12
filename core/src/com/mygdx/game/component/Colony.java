package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.ColonistEnt;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.ItemNeeded;
import com.mygdx.game.util.managers.DataManager;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by Paha on 1/17/2015.
 * The Colony Component should be attached to an empty Entity. The Colony is not the main building, but more of a hidden control center.
 */
public class Colony extends Component implements IInteractable {
    @JsonProperty
    private String colonyName = "Colony";
    @JsonIgnore
    private ArrayList<Colonist> colonistList = new ArrayList<>(20);
    @JsonIgnore
    private HashMap<Class<? extends Component>, Array<Component>> ownedMap = new HashMap<>();
    @JsonIgnore
    private HashMap<String, Inventory.InventoryItem> quickInv = new HashMap<>();
    @JsonIgnore
    private Inventory inventory;

    public Colony() {
        super();
    }

    @Override
    public void start() {
        super.start();
        this.owner.name = "emptyColonyObject";

        this.inventory = this.owner.addComponent(new Inventory());
        this.inventory.setMaxAmount(-1);
        load();

        CraftingJob job = new CraftingJob(null, "wood_hatchet", 1);
        System.out.println("Okayer");
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        this.inventory = this.owner.getComponent(Inventory.class);
    }

    /**
     * Makes a new colonist (hiding some ugly stuff) using the start position + a random offset.
     * @param start The location to center the creation on.
     * @param offset The offset which is random'd and added to the start (-offset to +offset)
     * @param textureName The name of the texture to use for the colonist.
     * @return The newly created colonist!
     */
    public Entity makeColonist(Vector2 start, float offset, String textureName){
        Vector2 newPos = new Vector2(start.x + MathUtils.random()*offset*2 - offset, start.y + MathUtils.random()*offset*2 - offset);
        return new ColonistEnt(newPos, 0, new String[]{textureName,""}, 10);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    /**
     * Adds a Component to the ownerByColony structure. The Component must also implement the
     * IOwnable interface.
     * @param comp The Component to add to this colony.
     * @param <T> The class type.
     */
    public <T extends Component & IOwnable> void addOwnedToColony(T comp){
        Class<? extends Component> cls = comp.getClass();
        ownedMap.putIfAbsent(cls, new Array<>());
        ownedMap.get(comp.getClass()).add(comp);
        comp.addedToColony(this);
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
        for(Component comp : ownedMap.get(cls)){
            if(predicate.test((T)comp)) return (T)comp;
        }

        return null;
    }

    @JsonIgnore
    public <T extends Component> Array<Component> getOwnedListFromColony(Class<T> cls){
        return ownedMap.get(cls);
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);
    }

    /**
     * Adds a Colonist to this colony. This will also set the Colonist's Colony when added.
     * @param colonist The Colonist Component to add.
     */
    public void addColonist(Colonist colonist){
        this.colonistList.add(colonist);
        colonist.setColony(this);
        this.addOwnedToColony(colonist);
    }

    /**
     * Adds the item to a hashmap for the colony to use.
     * @param itemRef The JsonItem reference.
     * @param amount The amount to add.
     */
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

    public static class CraftingJob{
        public Building building;
        public DataBuilder.JsonItem itemRef;
        public DataBuilder.JsonRecipe itemRecipe;
        public int amount;
        public Array<ItemNeeded> materials, raw;

        public CraftingJob(Building building, String itemName, int amount){
            this.building = building;
            this.itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
            this.itemRecipe = DataManager.getData(itemName, DataBuilder.JsonRecipe.class);
            this.amount = amount;
            this.materials = new Array<>();
            this.raw = new Array<>();

            this.calculateMaterials();
        }

        //TODO This needs some serious recursiveness or something...

        private void calculateMaterials(){
            HashMap<String, Integer> materialMap = new HashMap<>(10);
            HashMap<String, Integer> rawMap = new HashMap<>(10);

            for(int i=0;i<itemRecipe.items.length;i++){
                DataBuilder.JsonItem subItem = DataManager.getData(itemRecipe.items[i], DataBuilder.JsonItem.class);
                //If it's a material, add it to the hashmap and recurse!!
                if(subItem.getItemCategory().equals("material")){
                    this.addToMap(materialMap, subItem, itemRecipe.itemAmounts[i]);
                    this.calc(subItem, materialMap, rawMap);
                //Otherwise, just add to raw map.
                }else
                    this.addToMap(rawMap, subItem, itemRecipe.itemAmounts[i]);
            }

            //Convert the materialMap into an array of ItemNeeded objects.
            for(Map.Entry<String,Integer> entry : materialMap.entrySet()){
                String itemName = entry.getKey();
                int amount = entry.getValue();
                this.materials.add(new ItemNeeded(itemName, amount));
            }

            //Convert the rawMap into an array of ItemNeeded objects.
            for(Map.Entry<String,Integer> entry : rawMap.entrySet()){
                String itemName = entry.getKey();
                int amount = entry.getValue();
                this.raw.add(new ItemNeeded(itemName, amount));
            }
        }

        private void calc(DataBuilder.JsonItem itemRef, HashMap<String, Integer> materialMap, HashMap<String, Integer> rawMap){
            //Get the recipe...
            DataBuilder.JsonRecipe recipe = DataManager.getData(itemRef.getItemName(), DataBuilder.JsonRecipe.class);
            //For each item of the recipe, get it's items...
            for(int i=0;i<recipe.items.length;i++){
                //Get the sub item.
                DataBuilder.JsonItem subItem = DataManager.getData(recipe.items[i], DataBuilder.JsonItem.class);
                //If it's a material, add it to the hashmap and recurse!!
                if(subItem.getItemCategory().equals("material")){
                    this.addToMap(materialMap, subItem, recipe.itemAmounts[i]);
                    this.calc(subItem, materialMap, rawMap);
                //Otherwise, just add to raw map.
                }else
                    this.addToMap(rawMap, subItem, recipe.itemAmounts[i]);
            }
        }

        private void addToMap(HashMap<String, Integer> map, DataBuilder.JsonItem itemRef, int amountToAdd){
            Integer amount = map.get(itemRef.getItemName()); //Get the amount already in the hashmap
            int _amount = 0;
            if(amount != null) _amount = amount; //If it existed, set it to our temp variable
            _amount += amountToAdd; //Add the amount needed.
            map.put(itemRef.getItemName(), _amount); //Put it back into the hashmap.
        }
    }
}
