package com.mygdx.game.util.managers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.beust.jcommander.internal.Nullable;
import com.mygdx.game.ui.PlacingConstructionWindow;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;

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
     * Sets the building being placed to the JsonBuilding being passed in.
     * @param building The JsonBuilding to be placed. Use null to indicate to stop placing a construction.
     */
    public void setPlacingConstruction(@Nullable DataBuilder.JsonBuilding building){
        this.buildingBeingPlaced = building;
        if(this.buildingBeingPlaced != null)
            this.view.setBuildingBeingPlaced(this.buildingBeingPlaced);
        else
            this.view.clearBuildingBeingPlaced();
    }

    /**
     * @return The JsonBuilding that is being placed. Can be null.
     */
    public DataBuilder.JsonBuilding getPlacingConstruction(){
        return this.buildingBeingPlaced;
    }

    /**
     * Queries the Box2D World to check if the building can be placed. Size of the object is taken from the buildingBeingPlaced in this Manager.
     * The position is passed in from the Window that records the building's position from the player.
     * @param world The Box2D World.
     * @param position The Vector2 position of the building.
     * @return True if it can be placed, false otherwise.
     */
    public boolean canPlace(World world, Vector2 position){
        this.canPlace = true; //Set this to true first.
        float width = GH.toMeters(this.buildingBeingPlaced.dimensions[0]/2);
        float height = GH.toMeters(this.buildingBeingPlaced.dimensions[1]/2);
        //Then we query the World for any intersections. If at least one is found, we cannot place. (canPlace is set to false).
        world.QueryAABB(callback, position.x - width, position.y - height, position.x + width, position.y + height);

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

    public DataBuilder.JsonBuilding getBuildingBeingPlaced(){
        return this.buildingBeingPlaced;
    }

    public static PlaceConstructionManager instance(){
        return instance;
    }

    QueryCallback callback = fixture -> {
        this.canPlace = false;
        return false;
    };
}
