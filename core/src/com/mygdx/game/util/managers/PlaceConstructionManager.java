package com.mygdx.game.util.managers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.beust.jcommander.internal.Nullable;
import com.mygdx.game.ui.PlacingConstructionWindow;
import com.mygdx.game.util.DataBuilder;

/**
 * Created by Paha on 8/24/2015.
 * Manages stuff when the user is placingConstruction stuff (buildings...)
 */
public class PlaceConstructionManager {
    private static PlaceConstructionManager instance = new PlaceConstructionManager();

    private PlacingConstructionWindow view;
    private DataBuilder.JsonBuilding buildingBeingPlaced;
    private boolean canPlace = false;

    private PlaceConstructionManager(){

    }

    /**
     * Sets the building being placed to the building matching the buildingName passed in. Calls the DataManager to get the data.
     * @param buildingName The name of the building to set as being placed. Use null to indicate to stop placing a construction.
     */
    public void setPlacingConstruction(@Nullable String buildingName){
        if(buildingName != null)
            this.buildingBeingPlaced = DataManager.getData(buildingName, DataBuilder.JsonBuilding.class);
        else
            this.buildingBeingPlaced = null;

        if(this.buildingBeingPlaced != null)
            this.view.setupWindow(this.buildingBeingPlaced);
    }

    /**
     * Sets the building being placed to the JsonBuilding being passed in.
     * @param building The JsonBuilding to be placed. Use null to indicate to stop placing a construction.
     */
    public void setPlacingConstruction(@Nullable DataBuilder.JsonBuilding building){
        this.buildingBeingPlaced = building;
        if(this.buildingBeingPlaced != null)
            this.view.setupWindow(this.buildingBeingPlaced);
    }

    /**
     * @return The JsonBuilding that is being placed. Can be null.
     */
    public DataBuilder.JsonBuilding getPlacingConstruction(){
        return this.buildingBeingPlaced;
    }

    public boolean canPlace(World world, Vector2 position){
        this.canPlace = true; //Set this to true first.
        //Then we query the World for any intersections. If at least one is found, we cannot place. (canPlace is set to false).
        world.QueryAABB(callback, position.x - this.buildingBeingPlaced.dimensions[0]/2, position.y - this.buildingBeingPlaced.dimensions[1]/2,
                position.x + this.buildingBeingPlaced.dimensions[0]/2, position.y + this.buildingBeingPlaced.dimensions[1]/2);

        return this.canPlace; //True if nothing was overlapping, false otherwise.
    }

    /**
     * @return True if there is a building being placed (buildingBeingPlaced != null), false otherwise.
     */
    public boolean isPlacingConstruction(){
        return this.buildingBeingPlaced != null;
    }

    public void setView(PlacingConstructionWindow view){
        this.view = view;
    }

    public static PlaceConstructionManager instance(){
        return instance;
    }

    QueryCallback callback = fixture -> {
        this.canPlace = false;
        return false;
    };
}
