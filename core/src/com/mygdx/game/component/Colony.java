package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colony extends Component implements IDisplayable {
    private String colonyName = "Colony";

    private ArrayList<Colonist> colonistList = new ArrayList<>(20);
    private HashMap<String, ArrayList<Resource>> nearbyResources = new HashMap<>();
    private HashMap<String, ArrayList<Resource>> stockedResource = new HashMap<>();

    private int totalNearbyResources = 0;
    private int totalStockedResources = 0;

    private Inventory inventory;

    public Colony(String colonyName) {
        super();

        this.colonyName = colonyName;
    }
    public Colony() {
        super();
    }

    @Override
    public void start() {
        super.start();

        this.inventory = this.owner.getComponent(Inventory.class);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * Adds a Colonist to this colony. This will also set the Colonist's Colony when added.
     * @param colonist The Colonist Component to add.
     */
    public void addColonist(Colonist colonist){
        this.colonistList.add(colonist);
        colonist.setColony(this);
    }

    /**
     * Adds an available nearby Resource to this colony.
     * @param resource The nearby Resource Component to add.
     */
    public void addNearbyResource(Resource resource){
        if(this.nearbyResources.get(resource.getResourceType()) == null)
            this.nearbyResources.put(resource.getResourceType(), new ArrayList<>(20));

        this.nearbyResources.get(resource.getResourceType()).add(resource);
        this.totalNearbyResources++;
    }

    /**
     * Adds a stocked Resource to this colony.
     * @param resource The Resource Component to add.
     */
    public void addStockedResource(Resource resource){
        if(stockedResource.get(resource.getResourceType()) == null)
            stockedResource.put(resource.getResourceType(), new ArrayList<>(20));

        stockedResource.get(resource.getResourceType()).add(resource);
        this.totalStockedResources++;
    }



    /**
     * Removes a nearby Resource from this colony.
     * @param resource The Resource to remove.
     */
    public void removeNearbyResource(Resource resource){
        if(this.nearbyResources.get(resource.getResourceType()) == null)
            return;

        this.nearbyResources.get(type).remove(resource);
        this.totalNearbyResources--;
    }

    /**
     * Removes a nearby Resource from this colony.
     * @param resource The Resource to remove.
     */
    public void removeStockedResource(Resource resource){
        if(this.stockedResource.get(resource.getResourceType()) == null)
            return;

        this.stockedResource.get(type).remove(resource);
        this.totalStockedResources--;
    }

    /**
     * Returns the list of nearby Resources by type.
     * @param type The type of resource.
     * @return An empty ArrayList if there is no list for the given type, otherwise the ArrayList of nearby resources for the given type.
     */
    public ArrayList<Resource> getNearbyResourceListByType(String type){
        if(this.nearbyResources.get(type) == null)
            return new ArrayList<>();

        return this.nearbyResources.get(type);
    }

    /**
     * Returns the list of stocked Resources by type.
     * @param type The type of resource.
     * @return An empty ArrayList if there is no list for the given type, otherwise the ArrayList of stocked resources for the given type.
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
        return this.totalStockedResources;
    }

    /**
     * Gets the number of colonists this Colony has.
     * @return An integer which is the number of colonists.
     */
    public int getNumColonists(){
        return this.colonistList.size();
    }

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name) {
        float x = rect.x;
        float y = rect.y + rect.getHeight() - 5;

        if(name == "general") {
            GUI.Label("Name: " + this.owner.name, batch, rect.getX() + rect.getWidth() / 2 - 5, y, true);
            y -= 20;
        }else if(name == "colony"){
            GUI.Text("ColonyName: " + this.colonyName, batch, x, y);
            y -= 20;
            GUI.Text("Type: Colony", batch, x, y);
            y -= 20;
            GUI.Text("NumResourcesNearby: " + this.getTotalNearbyResources(), batch, x, y);
            y -= 20;
            GUI.Text("NumStockedResources: " + this.getTotalStockedResources(), batch, x, y);
            y -= 20;
            GUI.Text("NumColonists: " + this.getNumColonists(), batch, x, y);
        }else if(name == "inventory"){
            this.inventory.display(rect, batch, name);
        }
    }
}
