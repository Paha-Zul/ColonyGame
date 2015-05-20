package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.BuildingEntity;
import com.mygdx.game.entity.ColonistEnt;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.screens.GameScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colony extends Component implements IInteractable {
    private String colonyName = "Colony";

    private ArrayList<Colonist> colonistList = new ArrayList<>(20);
    private HashMap<String, ArrayList<Resource>> nearbyResources = new HashMap<>();
    private HashMap<String, ArrayList<Resource>> stockedResource = new HashMap<>();
    private HashMap<Class<? extends Component>, Array<Component>> ownedMap = new HashMap<>();
    private HashMap<String, Inventory.InventoryItem> quickInv = new HashMap<>();

    private int totalNearbyResources = 0;

    private Inventory inventory;
    private int lastInvCount = 0;

    public Colony(String colonyName) {
        super();

        this.colonyName = colonyName;
        this.setActive(false);
    }

    public Colony() {
        this("Colony");
    }

    @Override
    public void start() {
        super.start();
        this.owner.name = "emptyColonyObject";

        placeStart();

        this.inventory = this.owner.getComponent(Inventory.class);
        TextureRegion colonistTexture = new TextureRegion(ColonyGame.assetManager.get("colonist", Texture.class));
        Building building = this.getOwnedFromColony(Building.class);

        //Make some colonists!
        for(int i=0;i<5;i++) {
            Entity c = this.makeColonist(building.owner.transform.getPosition(), GH.toMeters(200), colonistTexture);
            c.getComponent(Colonist.class).setName(GameScreen.firstNames[MathUtils.random(GameScreen.firstNames.length - 1)], GameScreen.lastNames[MathUtils.random(GameScreen.lastNames.length - 1)]);
            this.addColonist(c.getComponent(Colonist.class));
        }
    }

    private Entity makeColonist(Vector2 start, float offset, TextureRegion texture){
        Vector2 newPos = new Vector2(start.x + MathUtils.random()*offset*2 - offset, start.y + MathUtils.random()*offset*2 - offset);
        return new ColonistEnt(newPos, 0, texture, 10);
    }

    private void placeStart(){
        //Find a suitable place to spawn our Colony
        int radius = 0;
        boolean placed = false;
        Grid.GridInstance grid = ColonyGame.worldGrid;
        Vector2 start = new Vector2(grid.getWidth()/2, grid.getHeight()/2);
        int[] index = grid.getIndex(start);

        while (!placed) {
            int startX = index[0] - radius;
            int endX = index[0] + radius;
            int startY = index[1] - radius;
            int endY = index[1] + radius;

            //Loop over each tile.
            for (int x = startX; x <= endX && !placed; x++) {
                for (int y = startY; y <= endY && !placed; y++) {
                    if (x != startX && x != endX && y != startY && y != endY)
                        continue;

                    if(startX < 0 || endX > grid.getWidth() || startY < 0 || endY > grid.getHeight())
                        GH.writeErrorMessage("Couldn't find a place to spawn the base!");

                    //For each tile, we want to check if there is a 4x4 surrounding area.
                    int innerStartX = x - 5;
                    int innerEndX = x + 5;
                    int innerStartY = y - 5;
                    int innerEndY = y + 5;

                    //If the node is null (outside the bounds), continue.
                    if (grid.getNode(innerStartX, innerStartY) == null || grid.getNode(innerEndX, innerEndY) == null)
                        continue;

                    placed = true;

                    //Check over the inner area. If all tiles are not set to avoid, we have a place we can spawn our Colony.
                    for (int innerX = innerStartX; innerX <= innerEndX && placed; innerX++) {
                        for (int innerY = innerStartY; innerY <= innerEndY && placed; innerY++) {
                            Grid.TerrainTile tile = grid.getNode(innerX, innerY).getTerrainTile();
                            if (tile.avoid)//If there is a single tile set to avoid, break!
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
        BuildingEntity colonyEnt = new BuildingEntity(start, 0, new TextureRegion(ColonyGame.assetManager.get("Colony", Texture.class)), 10);
        ColonyGame.camera.position.set(colonyEnt.transform.getPosition().x, colonyEnt.transform.getPosition().y, 0);
        this.addOwnedToColony(colonyEnt.getComponent(Building.class));

        //Destroys resources in an area around the Colony Entity.
        radius = 8;
        Predicate<Grid.Node> notWaterNode = node -> !node.getTerrainTile().category.equals("water");

        //A consumer function to use. If the entity is a tree, destroy it!
        Consumer<Entity> treeConsumer = ent -> {
            if(ent.getTags().hasTag("resource")) ent.setToDestroy();
        };

        //Perform the things.
        //this.grid.perform(destroyNearbyResources);
        grid.performOnEntityInRadius(treeConsumer, notWaterNode, radius, grid.getIndex(colonyEnt.transform.getPosition()));
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

    @SuppressWarnings("unchecked")
    public <T extends Component> T getOwnedFromColony(Class<T> cls){
        return (T)ownedMap.get(cls).get(0);
    }

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
            quickInv.put(itemRef.getItemName(), new Inventory.InventoryItem(itemRef, amount, 1000000));
            System.out.println("adding item to such.");
        }else
            invItem.addAmount(amount);
    }

    public final HashMap<String, Inventory.InventoryItem> getGlobalInv(){
        return quickInv;
    }

    /**
     * Adds an available nearby Resource to this colony.
     * @param resource The nearby Resource Component to add.
     */
    public void addNearbyResource(Resource resource){
        if(this.nearbyResources.get(resource.getResourceName()) == null)
            this.nearbyResources.put(resource.getResourceName(), new ArrayList<>(20));

        this.nearbyResources.get(resource.getResourceName()).add(resource);
        this.totalNearbyResources++;
    }

    /**
     * Removes a nearby Resource from this colony.
     * @param resource The Resource to remove.
     */
    public void removeNearbyResource(Resource resource){
        if(this.nearbyResources.get(resource.getResourceName()) == null)
            return;

        this.nearbyResources.get(type).remove(resource);
        this.totalNearbyResources--;
    }

    /**
     * Returns the list of nearby Resources by interType.
     * @param type The interType of resource.
     * @return An empty ArrayList if there is no list for the given interType, otherwise the ArrayList of nearby resources for the given interType.
     */
    public ArrayList<Resource> getNearbyResourceListByType(String type){
        if(this.nearbyResources.get(type) == null)
            return new ArrayList<>();

        return this.nearbyResources.get(type);
    }

    /**
     * Returns the list of stocked Resources by interType.
     * @param type The interType of resource.
     * @return An empty ArrayList if there is no list for the given interType, otherwise the ArrayList of stocked resources for the given interType.
     */
    public ArrayList<Resource> getStockedResourceListByType(String type){
        if(this.stockedResource.get(type) == null)
            return new ArrayList<>();

        return this.stockedResource.get(type);
    }

    /**
     * Gets the total number of nearby resources that the Colony detects.
     * @return An integer which is the total number of nearby resources.
     */
    public int getTotalNearbyResources(){
        return this.totalNearbyResources;
    }

    /**
     * Gets the total number of stocked resources for this Colony.
     * @return An integer which is the total number of stocked resources.
     */
    public int getTotalStockedResources(){
        return this.inventory.getCurrTotalItems();
    }

    /**
     * Gets the number of colonists this Colony has.
     * @return An integer which is the number of colonists.
     */
    public int getNumColonists(){
        return this.colonistList.size();
    }

    @Override
    public Inventory getInventory(){
        return this.inventory;
    }

    @Override
    public Stats getStats() {
        return null;
    }

    @Override
    public String getStatsText() {
        return null;
    }

    @Override
    public Skills getSkills() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public BehaviourManagerComp getBehManager() {
        return null;
    }
}
