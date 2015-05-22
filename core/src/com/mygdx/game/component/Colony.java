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
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.screens.GameScreen;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.Grid;
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
        Building building = this.getOwnedFromColony(Building.class);

        TextureRegion colonistTexture = new TextureRegion(ColonyGame.assetManager.get("colonist", Texture.class));
        //Make some colonists!
        for(int i=0;i<5;i++) {
            Entity c = this.makeColonist(building.owner.getTransform().getPosition(), GH.toMeters(200), colonistTexture);
            c.getComponent(Colonist.class).setName(GameScreen.firstNames[MathUtils.random(GameScreen.firstNames.length - 1)], GameScreen.lastNames[MathUtils.random(GameScreen.lastNames.length - 1)]);
            this.addColonist(c.getComponent(Colonist.class));
        }
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        this.inventory = this.owner.getComponent(Inventory.class);
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
        ColonyGame.camera.position.set(colonyEnt.getTransform().getPosition().x, colonyEnt.getTransform().getPosition().y, 0);
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
        grid.performOnEntityInRadius(treeConsumer, notWaterNode, radius, grid.getIndex(colonyEnt.getTransform().getPosition()));
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
    @JsonIgnore
    public <T extends Component> T getOwnedFromColony(Class<T> cls){
        return (T)ownedMap.get(cls).get(0);
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
            quickInv.put(itemRef.getItemName(), new Inventory.InventoryItem(itemRef, amount, 1000000));
            System.out.println("adding item to such.");
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
}
