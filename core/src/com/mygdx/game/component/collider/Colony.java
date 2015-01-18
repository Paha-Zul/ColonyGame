package com.mygdx.game.component.collider;

import com.mygdx.game.component.Colonist;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.Resource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colony extends Component {
    private ArrayList<Colonist> colonistList = new ArrayList<>(20);
    private HashMap<String, ArrayList<Resource>> nearbyResources = new HashMap<>();
    private HashMap<String, ArrayList<Resource>> stockedResource = new HashMap<>();

    public Colony() {
        super();
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
    }

    /**
     * Adds a stocked Resource to this colony.
     * @param resource The Resource Component to add.
     */
    public void addStockedResource(Resource resource){
        if(stockedResource.get(resource.getResourceType()) == null)
            stockedResource.put(resource.getResourceType(), new ArrayList<>(20));

        stockedResource.get(resource.getResourceType()).add(resource);
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
     * Removes a nearby Resource from this colony.
     * @param resource The Resource to remove.
     */
    public void removeNearbyResource(Resource resource){
        if(this.nearbyResources.get(resource.getResourceType()) == null)
            return;

        this.nearbyResources.get(type).remove(resource);
    }

    /**
     * Removes a nearby Resource from this colony.
     * @param resource The Resource to remove.
     */
    public void removeStockedResource(Resource resource){
        if(this.stockedResource.get(resource.getResourceType()) == null)
            return;

        this.stockedResource.get(type).remove(resource);
    }

}
