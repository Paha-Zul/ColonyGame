package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.BuildingEntity;
import com.mygdx.game.entity.ColonistEnt;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.screens.GameScreen;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.Grid;
import com.mygdx.game.util.ListHolder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Paha on 1/17/2015.
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

        load();
        placeStart();
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        this.inventory = this.owner.getComponent(Inventory.class);
    }

    private Entity makeColonist(Vector2 start, float offset, String textureName){
        Vector2 newPos = new Vector2(start.x + MathUtils.random()*offset*2 - offset, start.y + MathUtils.random()*offset*2 - offset);
        return new ColonistEnt(newPos, 0, new String[]{textureName,""}, 10);
    }

    private void placeStart(){
        //Find a suitable place to spawn our Colony
        int radius = 0, areaToSearch = 5;
        boolean placed = false;
        Grid.GridInstance grid = ColonyGame.worldGrid;
        Vector2 start = new Vector2(grid.getWidth()/2, grid.getHeight()/2);
        int[] index = grid.getIndex(start);

        /**
         * For now, starts in the middle of the map. For each loop, search an area ('areaToSearch') that is suitable. This will check an area (ex: 5x5) to make sure
         * there are no obstacles or terrain problems. If the area is suitable, the building is placed. Otherwise, we increase the radius and keep searching.
         */
        while (!placed) {
            int startX = index[0] - radius;
            int endX = index[0] + radius;
            int startY = index[1] - radius;
            int endY = index[1] + radius;

            //Loop over each tile.
            for (int x = startX; x <= endX && !placed; x++) {
                for (int y = startY; y <= endY && !placed; y++) {

                    //If we're not on the edge, continue. We don't want to search the inner areas as we go.
                    if (x != startX && x != endX && y != startY && y != endY)
                        continue;

                    if(startX < 0 || endX > grid.getWidth() || startY < 0 || endY > grid.getHeight())
                        GH.writeErrorMessage("Couldn't find a place to spawn the base!");

                    //For each tile, we want to check if there is a 4x4 surrounding area.
                    int innerStartX = x - areaToSearch;
                    int innerEndX = x + areaToSearch;
                    int innerStartY = y - areaToSearch;
                    int innerEndY = y + areaToSearch;

                    //If the node is null (outside the bounds), continue.
                    if (grid.getNode(innerStartX, innerStartY) == null || grid.getNode(innerEndX, innerEndY) == null)
                        continue;

                    placed = true;

                    //Check over the inner area. If all tiles are not set to avoid, we have a place we can spawn our Colony.
                    for (int innerX = innerStartX; innerX <= innerEndX && placed; innerX++) {
                        for (int innerY = innerStartY; innerY <= innerEndY && placed; innerY++) {
                            Grid.TerrainTile tile = grid.getNode(innerX, innerY).getTerrainTile();
                            if (tile.tileRef.avoid)//If there is a single tile set to avoid, break!
                                placed = false;
                        }
                    }

                    //If passed, calculate the start vector.
                    if(placed)
                        start.set(x * grid.getSquareSize(), y * grid.getSquareSize());
                }
            }
            radius++;
        }

        //Spawns the Colony Entity and centers the camera on it.
        BuildingEntity colonyEnt = new BuildingEntity(start, 0, new String[]{"Colony",""}, 10);
        ListHolder.addEntity(colonyEnt);
        ColonyGame.camera.position.set(colonyEnt.getTransform().getPosition().x, colonyEnt.getTransform().getPosition().y, 0);
        Building colonyBuilding = colonyEnt.getComponent(Building.class);
        colonyBuilding.setBuildingName("colony_building");
        this.addOwnedToColony(colonyBuilding);
        colonyEnt.getComponent(Constructable.class).setComplete();

        //Spawns the Equipment building.
        BuildingEntity equipEnt = new BuildingEntity(new Vector2(start.x - 5, start.y - 5), 0, new String[]{"Colony",""}, 10);
        equipEnt.getTags().addTag("constructing");
        ListHolder.addEntity(equipEnt);
        Building equipBuilding = equipEnt.getComponent(Building.class);
        equipBuilding.setBuildingName("workshop");
        this.addOwnedToColony(equipBuilding);

        //Destroys resources in an area around the Colony Entity.
        radius = 8;
        Predicate<Grid.Node> notWaterNode = node -> !node.getTerrainTile().tileRef.category.equals("water");

        //A consumer function to use. If the entity is a tree, destroy it!
        Consumer<Entity> treeConsumer = ent -> {
            if(ent.getTags().hasTag("resource")) ent.setToDestroy();
        };

        //Perform the things.
        //this.grid.perform(destroyNearbyResources);
        grid.performOnEntityInRadius(treeConsumer, notWaterNode, radius, grid.getIndex(colonyEnt.getTransform().getPosition()));

        //Make some colonists!
        for(int i=0;i<2;i++) {
            Entity c = this.makeColonist(colonyEnt.getTransform().getPosition(), GH.toMeters(200), "colonist");
            c.getComponent(Colonist.class).setName(GameScreen.firstNames[MathUtils.random(GameScreen.firstNames.length - 1)], GameScreen.lastNames[MathUtils.random(GameScreen.lastNames.length - 1)]);
            this.addColonist(c.getComponent(Colonist.class));
            ListHolder.addEntity(c);
        }
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
        Inventory.InventoryItem invItem = quickInv.get(itemRef.getItemName());
        if(invItem == null) {
            quickInv.put(itemRef.getItemName(), new Inventory.InventoryItem(itemRef, amount, -1));
        }else
            invItem.addAmount(amount);
    }

    @JsonIgnore
    public final HashMap<String, Inventory.InventoryItem> getGlobalInv(){
        return quickInv;
    }

    /**
     * Gets the total number of stocked resources for this Colony.
     * @return An integer which is the total number of stocked resources.
     */
    @JsonIgnore
    public int getTotalStockedResources(){
        return this.inventory.getCurrTotalItems();
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
}
