package com.mygdx.game.helpers.worldgeneration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Resource;
import com.mygdx.game.entity.ResourceEnt;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.managers.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Bbent_000 on 12/24/2014.
 */
public class WorldGen {
    //Some default values that can be modified globally.
    private int tileSize = 25;
    public float treeScale = 0.8f;
    public float freq = 5;
    public float percentageDone = 0;

    public Texture whiteTex;

    private TextureAtlas terrainAtlas;


    private int currX = 0, currY = 0;
    private int numTrees = 0;

    private ColonyGame game;
    private static WorldGen instance;

    private int currDone = 0;
    private int currIndex = 0;
    private int lastIndex = -1;

    //Variables for performing breadth first resource spawning;
    private LinkedList<Grid.TerrainTile> neighbors = new LinkedList<>();
    private HashMap<Integer, Grid.TerrainTile> visitedMap = new HashMap<>(1000);
    private ArrayList<Grid.TerrainTile> tmpNeighbors = new ArrayList<>(4);
    private int[] startIndex;
    private Vector2 center = new Vector2();
    private boolean started = false;
    private String noiseMapName = "";


    /**
     * Initializes the World Generator. For now, most stuff is temporary for prototyping.
     */
    public void init(ColonyGame game){
        this.game = game;

        //Generate a white square (pixel).
        Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGBA4444);
        Color color = new Color(Color.WHITE);
        pixmap.setColor(color);
        pixmap.fillRectangle(0,0,1,1);
        whiteTex = new Texture(pixmap);
        pixmap.dispose();

        terrainAtlas = new TextureAtlas(Gdx.files.internal("atlas/terrain.atlas"));
    }

    /**
     * Called every frame to generate the world. This will return true when the world is fully generated.
     * @return True when finished, false otherwise.
     */
    public boolean generateWorld(){
        int maxAmount = ColonyGame.worldGrid.getWidth()*ColonyGame.worldGrid.getHeight() * DataBuilder.worldData.noiseMapHashMap.size();
        int stepsLeft = Constants.WORLDGEN_GENERATESPEED;
        boolean done = true; //Flag for completion.
        HashMap<String, DataBuilder.JsonTileGroup> tileGroupsMap = DataBuilder.tileGroupsMap;
        Grid.GridInstance grid = ColonyGame.worldGrid;

        //This will loop until everything is done.
        while(this.currDone < maxAmount && stepsLeft > 0) {
            freq = DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).freq; //Get the frequency of the current noise map.
            String noiseMapName = DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).name; //Get the name of the current noise map.

            //If the index has changed, we need to increment and get the next noise level.
            if(this.lastIndex != this.currIndex){
                this.lastIndex = this.currIndex;
                SimplexNoise.genGrad(DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).noiseSeed);
            }

            //Loop for a certain amount of steps. This is done for each noise level. Resets after each one.
            while (stepsLeft > 0 && currX < grid.getWidth()) {
                if (!(currX < grid.padding || currX >= grid.getWidth() - grid.padding || currY < grid.padding || currY >= grid.getHeight() - grid.padding)) {

                    double noiseValue = SimplexNoise.noise((double) currX / freq, (double) currY / freq); //Generate the noise for this tile.
                    Vector2 position = new Vector2(currX * grid.getSquareSize(), currY * grid.getSquareSize()); //Set the position.
                    Sprite terrainSprite; //Prepare the sprite object.
                    int type = 0; //The interType of tile.
                    float rotation = 0; //The rotation of the tile.

                    done = false; //Set done to false signifying that we are not finished yet.

                    //Gets the jTile as the noise height.
                    DataBuilder.JsonTile jtile = this.getTileAtHeight(tileGroupsMap.get(noiseMapName).tiles, (float) noiseValue);
                    if (jtile == null && noiseMapName.equals("main"))
                        GH.writeErrorMessage("No tile found at height " + noiseValue + " for noise map '" + noiseMapName + "'. A tile must exist for all heights on noise map '" + noiseMapName + "'.");

                        //If the tile is not null, let use assign a new tile to the terrain.
                    else if (jtile != null) {
                        //Gets the texture that the tile should be.
                        int randTileIndex = MathUtils.random(jtile.img.length - 1);
                        Grid.TerrainTile tile = grid.getGrid()[currX][currY].getTerrainTile();

                        if (tile == null)
                            tile = new Grid.TerrainTile();

                        //Creates a new sprite and a new tile, assigns the Sprite to the tile and puts it into the map array.
                        terrainSprite = terrainAtlas.createSprite(jtile.img[randTileIndex]);
                        terrainSprite.setSize(grid.getSquareSize(), grid.getSquareSize());
                        tile.set(terrainSprite, noiseValue, rotation, type, position); //Create a new terrain tile.
                        tile.avoid = jtile.avoid;
                        if (jtile.tileNames.length <= randTileIndex)
                            GH.writeErrorMessage("Tile with image " + jtile.img[randTileIndex] + " and category " + jtile.category + " does not have a name assigned to it");
                        tile.tileName = jtile.tileNames[randTileIndex]; //Assign the name
                        tile.category = jtile.category; //Assign the category.
                        grid.getGrid()[currX][currY].setTerrainTile(tile); //Assign the tile to the map.
                    }

                    //Decrement steps remaining and increment currDone.
                    stepsLeft--;
                }

                //Generate the visibility tile for this location
                grid.getVisibilityMap()[currX][currY] = new Grid.VisibilityTile();

                //If we still have Y tiles to cover, simply increment currY.
                if (currY < grid.getHeight() - 1) currY++;
                else {
                    //If our currY went past the grid height, we need to reset currY to 0 and start over, increment X to move over 1.
                    currY = 0; //Reset currY
                    currX++; //Increment currX
                }

                this.currDone++;
            }

            percentageDone = this.currDone / maxAmount; //Calcs the percentage done so that the player's UI can use this.

            //If we are done with the current noise level but we have more levels to go, reset the currX and currY, increment currIndex and continue.
            if (done && this.currIndex < DataBuilder.worldData.noiseMapHashMap.size()) {
                currX = currY = 0;
                this.currIndex++;
                done = false;
            }

            //If we exit the loop, set stepsLeft to 0 so we can restart if we need to.
            stepsLeft = 0;
        }

        //If we're done, clear some values.
        if(done){
            currIndex = 0;
            currX = currY = 0;
        }

        return done;
    }

    public boolean generateResources(Vector2 startPos, int blockRadius, int step){
        boolean done = false;
        Grid.GridInstance grid = ColonyGame.worldGrid;

        if(!started){
            this.neighbors.add(grid.getNode(startPos).getTerrainTile());
            this.startIndex = grid.getIndex(startPos);
            this.started = true;
            noiseMapName = DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).name;
        }

        //Partially executes a breadth first search.
        for (int i = 0; i < step; i++) {
            Grid.TerrainTile currTile = neighbors.pop(); //Pop the first neighbor
            int[] index = {(int) (currTile.terrainSprite.getX() / grid.getSquareSize()), (int) (currTile.terrainSprite.getY() / grid.getSquareSize())}; //Get the index.

            //Get left/right/up/down
            Grid.Node left = grid.getNode(index[0] - 1, index[1]);
            Grid.Node right = grid.getNode(index[0] + 1, index[1]);
            Grid.Node up = grid.getNode(index[0], index[1] + 1);
            Grid.Node down = grid.getNode(index[0] - 1, index[1] - 1);

            //If a neighbor is not null and it hasn't been visited already...
            if (left != null && left.getTerrainTile() != null && !visitedMap.containsKey(left.hashCode())) {
                neighbors.add(left.getTerrainTile());
                visitedMap.put(left.hashCode(), left.getTerrainTile());
            }
            if (right != null && right.getTerrainTile() != null && !visitedMap.containsKey(right.hashCode())) {
                neighbors.add(right.getTerrainTile());
                visitedMap.put(right.hashCode(), right.getTerrainTile());
            }
            if (up != null && up.getTerrainTile() != null && !visitedMap.containsKey(up.hashCode())) {
                neighbors.add(up.getTerrainTile());
                visitedMap.put(up.hashCode(), up.getTerrainTile());
            }
            if (down != null && down.getTerrainTile() != null && !visitedMap.containsKey(down.hashCode())) {
                neighbors.add(down.getTerrainTile());
                visitedMap.put(down.hashCode(), down.getTerrainTile());
            }

            //Add the current tile to the visitedMap.
            visitedMap.put(currTile.hashCode(), currTile);

            int[] currIndex = grid.getIndex(currTile.terrainSprite.getX(), currTile.terrainSprite.getY()); //Get the current index.
            //If the resource is within the blockRadius, simply continue.
            if (Math.abs(currIndex[0] - startIndex[0]) <= blockRadius && Math.abs(currIndex[1] - startIndex[1]) <= blockRadius)
                continue;

            //Set the center Vector2 and spawn the tree using the center position.
            center.set(currTile.terrainSprite.getX() + grid.getSquareSize() * 0.5f, currTile.terrainSprite.getY() + grid.getSquareSize() * 0.5f);
            spawnTree(getTileAtHeight(DataBuilder.tileGroupsMap.get(noiseMapName).tiles, (float) currTile.noiseValue), center, currTile);

            //If there are no more neighbors, we are done.
            if (neighbors.size() == 0 && this.currIndex < DataBuilder.worldData.noiseMapHashMap.size() - 1) {
                done = false;
                this.neighbors.clear();
                this.visitedMap.clear();
                this.started = false;
                this.currIndex++;
                break;
            }else if(neighbors.size() == 0){
                done = true;
                this.neighbors.clear();
                this.visitedMap.clear();
                this.started = false;
                break;
            }
        }

        return done;
    }

    //Spawns a tree.
    private void spawnTree(DataBuilder.JsonTile jtile, Vector2 centerPos, Grid.TerrainTile currTile){
        if(jtile == null || currTile == null || !currTile.category.equals(jtile.category))
            return;

        //Gets the resource that should be spawned on this tile
        DataBuilder.JsonResource jRes = this.getResourceOnTile(jtile, (float)Math.random());
        if(jRes == null)
            return;

        //Create a new resource from the JsonResource.
        Resource res = new Resource(jRes);

        //Get the atlas so we can get the image from it.
        TextureAtlas interactableAtlas = ColonyGame.assetManager.get("interactables", TextureAtlas.class); //Get the atlas
        TextureRegion reg = null;
        if(jRes.img != null && jRes.img.length > 0) {
            String textureName = jRes.img[MathUtils.random(jRes.img.length - 1)]; //Get the texture name.
            reg = interactableAtlas.findRegion(textureName);
        }

        //Create the resource and stuff.
        ResourceEnt resEnt = new ResourceEnt(centerPos, 0, reg, 11);
        resEnt.addComponent(res);
        resEnt.transform.setScale(treeScale);
        resEnt.name = res.getDisplayName();
    }

    /**
     * Returns a JsonTile object for the specified 'height' parameter. Throws an Exception if a tile cannot be found at the height parameter.
     * @param tileList The JsonTile array to search through.
     * @param height The height of the desired tile.
     * @return A JsonTile if on was found at the desired height.
     */
    private DataBuilder.JsonTile getTileAtHeight(DataBuilder.JsonTile[] tileList, float height){
        for (DataBuilder.JsonTile tile : tileList) {
            if (height >= tile.height[0] && height <= tile.height[1])
                return tile;
        }

        return null;
    }

    /**
     * Retrieves a resource from the DataBuilder's tileList using the 'value' value passed in. If a resource is found at that chance, a
     * Resource is created and returned.
     * @param tile The JsonTile to search through. The 'resources' and 'resourcesChance' will be searched through.
     * @param value The value of the resource. This
     * @return A ResourceComponent if the 'value' parameter was between a resource's chance to spawn. If no valid resource was found, returns null.
     */
    private DataBuilder.JsonResource getResourceOnTile(DataBuilder.JsonTile tile, float value){
        if(tile.resources == null || tile.resources.length <= 0)
            return null;

        for (int i=0;i<tile.resources.length;i++) {
            if (value >= tile.resourcesChance[i][0] && value <= tile.resourcesChance[i][1])
                return DataManager.getData(tile.resources[i], DataBuilder.JsonResource.class);
        }

        return null;
    }

    public int numTrees(){
        return numTrees;
    }

    public void clean(){
//        for(Texture tex : grassTiles)
//            tex.dispose();
//
//        grassTiles = null;
//
//        for(Texture tex : tallGrassTiles)
//            tex.dispose();
//
//        tallGrassTiles = null;
//
//        treeTexture.dispose();
//        treeTexture = null;
//        rockTexture.dispose();
//        rockTexture = null;
//        darkWater.dispose();
//        darkWater = null;
//        lightWater.dispose();
//        lightWater = null;
//        whiteTex.dispose();
//        whiteTex = null;
//
//        treeList.clear();
//        treeList = null;
    }

    public static WorldGen getInstance(){
        if(instance == null) instance = new WorldGen();
        return instance;
    }
}


