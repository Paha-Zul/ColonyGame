package com.mygdx.game.util.worldgeneration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Resource;
import com.mygdx.game.entity.ResourceEnt;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.Grid;
import com.mygdx.game.util.managers.DataManager;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Bbent_000 on 12/24/2014.
 * Generates the world.
 */
public class WorldGen {
    public static Texture whiteTex;
    private static WorldGen instance;

    static{
        //Generate a white square (pixel).
        Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGBA4444);
        Color color = new Color(Color.WHITE);
        pixmap.setColor(color);
        pixmap.fillRectangle(0,0,1,1);
        whiteTex = new Texture(pixmap);
        pixmap.dispose();
    }

    public float treeScale = 0.8f;
    public float percentageDone = 0;
    private TextureAtlas terrainAtlas;
    //Some default values that can be modified globally.
    private ColonyGame game;
    private int tileSize = 25;
    private int currX = 0, currY = 0;
    private int numTrees = 0;
    private int currDone = 0;
    private int currIndex = 0;
    private int lastIndex = -1;

    //Variables for performing breadth first resource spawning;
    private LinkedList<Grid.Node> openList = new LinkedList<>();
    private HashMap<Integer, Grid.Node> visitedMap = new HashMap<>(1000);
    private int[] startIndex;
    private Vector2 center = new Vector2();
    private boolean started = false;
    private String noiseMapName = "";

    public static Texture makeTexture(Color color){
        Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGBA4444);
        pixmap.setColor(color);
        pixmap.fillRectangle(0,0,1,1);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    /**
     * @return The instance of the WorldGen.
     */
    public static WorldGen getInstance(){
        if(instance == null) instance = new WorldGen();
        return instance;
    }

    /**
     * Initializes the World Generator. For now, most stuff is temporary for prototyping.
     */
    public void init(ColonyGame game){
        this.game = game;
        this.terrainAtlas = new TextureAtlas(Gdx.files.internal("atlas/terrain.atlas"));
        this.currDone = this.currIndex = this.currX = this.currY = 0;
    }

    /**
     * Called every frame to generate the saveContainer. This will return true when the saveContainer is fully generated.
     * @return True when finished, false otherwise.
     */
    public boolean generateWorld(){
        int maxAmount = ColonyGame.instance.worldGrid.getWidth()*ColonyGame.instance.worldGrid.getHeight() * DataBuilder.worldData.noiseMapHashMap.size();
        int stepsLeft = Constants.WORLDGEN_GENERATESPEED;
        boolean done = true; //Flag for completion.

        HashMap<String, DataBuilder.JsonTileGroup> tileGroupsMap = DataBuilder.tileGroupsMap;
        Grid.GridInstance grid = ColonyGame.instance.worldGrid;

        //This will loop until everything is done.
        while(this.currDone < maxAmount && stepsLeft > 0) {
            float freq = DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).freq; //Get the frequency of the current noise map.
            String noiseMapName = DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).name; //Get the compName of the current noise map.

            //If the index has changed, we need to increment and get the next noise level.
            if(this.lastIndex != this.currIndex){
                this.lastIndex = this.currIndex;
                SimplexNoise.genGrad(DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).noiseSeed);
            }

            //Loop for a certain amount of steps. This is done for each noise level. Resets after each one.
            while (stepsLeft > 0 && currX < grid.getWidth()) {
                if (!(currX < grid.padding || currX >= grid.getWidth() - grid.padding || currY < grid.padding || currY >= grid.getHeight() - grid.padding)) {
                    done = false; //Set done to false signifying that we are not finished yet.

                    //Get the info about the tile.
                    double noiseValue = SimplexNoise.noise((double) currX / freq, (double) currY / freq); //Generate the noise for this tile.
                    Vector2 position = new Vector2(currX * grid.getSquareSize(), currY * grid.getSquareSize()); //Set the position.
                    Sprite terrainSprite; //Prepare the sprite object.
                    float rotation = 0; //The rotation of the tile.

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
                        tile.set(terrainSprite, noiseValue, rotation, position, jtile, jtile.img[randTileIndex], "terrain"); //Create a new terrain tile.

                        if (jtile.tileNames.length <= randTileIndex)
                            GH.writeErrorMessage("Tile with image " + jtile.img[randTileIndex] + " and category " + jtile.category + " does not have a compName assigned to it");
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

            percentageDone = (float)this.currDone / maxAmount; //Calcs the percentage done so that the player's UI can use this.

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
            currDone = 0;
        }

        return done;
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
     * This function will generate resources for the world. The world will be scanned over for each
     * noise map the exists. So if we have 3 noise maps (normal, water, mountains), the world will be scanned 3 times.
     * Each scan will get each node, get the terrain tile (if not null), check the tile list from a structure made from 'tiles.json' file,
     * searches for a valid resource to spawn, and will spawn it if the random number is within the bounds of that the resource requires.
     * @param startPos The starting position of the scan.
     * @param blockRadius The block radius of where not to spawn stuff?
     * @param step The number of steps to perform before halting.
     * @return True if the task is completely finished, false otherwise.
     */
    public boolean generateResources(Vector2 startPos, int blockRadius, int step){
        boolean done = false;
        Grid.GridInstance grid = ColonyGame.instance.worldGrid;

        //Partially executes a breadth first search.
        for (int i = 0; i < step; i++) {
            if(!started){
                this.openList.add(grid.getNode(startPos));
                this.startIndex = grid.getIndex(startPos);
                this.started = true;
                noiseMapName = DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).name;
            }

            Grid.Node currNode = openList.pop(); //Pop the first neighbor
            Grid.Node[] neighbors = grid.getNeighbors8(currNode);

            for(Grid.Node node : neighbors)
                if(node != null && !visitedMap.containsKey(node.hashCode())) {
                    openList.add(node);
                    visitedMap.put(node.hashCode(), node);
                }

            //Add the current tile to the visitedMap.
            visitedMap.put(currNode.hashCode(), currNode);
            if(currNode.getTerrainTile() != null) {
                int[] currIndex = {currNode.getX(), currNode.getY()};
                //If the resource is within the blockRadius, simply continue.
                if (Math.abs(currIndex[0] - startIndex[0]) <= blockRadius && Math.abs(currIndex[1] - startIndex[1]) <= blockRadius)
                    continue;

                //Set the center Vector2 and spawn the tree using the center position.
                center.set(currNode.getX()*grid.getSquareSize() +  grid.getSquareSize() * 0.5f, currNode.getY()*grid.getSquareSize() +  grid.getSquareSize() * 0.5f);
                this.spawnResource(getTileAtHeight(DataBuilder.tileGroupsMap.get(noiseMapName).tiles, (float) currNode.getTerrainTile().noiseValue), center, currNode.getTerrainTile());
            }

            //If there are no more nodes in openList, but we still have noise maps to go, continue again!
            if (openList.size() == 0 && this.currIndex < DataBuilder.worldData.noiseMapHashMap.size() - 1) {
                done = false;
                this.openList = new LinkedList<>();
                this.visitedMap = new HashMap<>();
                this.started = false;
                this.currIndex++;

            //Otherwise, we are completely finished.
            }else if(openList.size() == 0){
                done = true;
                this.openList = null;
                this.visitedMap.clear();
                this.started = false;
                break;
            }
        }

        return done;
    }

    /**
     * Spawns a resource.
     * @param jtile The JsonTile to reference.
     * @param centerPos The center position of the resource.
     * @param currTile The TerrainTile to spawn the
     */
    private void spawnResource(DataBuilder.JsonTile jtile, Vector2 centerPos, Grid.TerrainTile currTile){
        if(jtile == null || currTile == null || !currTile.tileRef.category.equals(jtile.category))
            return;

        //Gets the resource that should be spawned on this tile
        DataBuilder.JsonResource jRes = this.getResourceOnTile(jtile, (float)Math.random());
        if(jRes == null)
            return;


        //Get the atlas so we can get the image from it.
        TextureAtlas interactableAtlas = ColonyGame.instance.assetManager.get("interactables", TextureAtlas.class); //Get the atlas
        String textureName = null;
        String atlasName = "interactables";
        if(jRes.img != null && jRes.img.length > 0) {
            textureName = jRes.img[MathUtils.random(jRes.img.length - 1)]; //Get the texture compName.
        }

        //Create the resource and stuff.
        ResourceEnt resEnt = new ResourceEnt(centerPos, 0, new String[]{textureName, atlasName}, 11);
        //Get the Resource component and set its name and copy the jRes resource.
        Resource res = resEnt.getComponent(Resource.class);
        res.copyResource(jRes);
        resEnt.name = res.getDisplayName();

        resEnt.getTransform().setScale(treeScale);
        ColonyGame.instance.listHolder.addEntity(resEnt);
    }

    /**
     * Retrieves a resource from the DataBuilder's tileList using the 'value' value passed in. If a resource is found at that chance, a
     * Resource is added and returned.
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

    /**
     * Used for saving
     * @return A String[][] object with data to save the WorldGen with.
     */
    @JsonProperty("worldData")
    private String[][] getWorldData(){
        String[][] data = new String[DataBuilder.worldData.noiseMaps.length][4];

        for(int i=0; i < DataBuilder.worldData.noiseMaps.length; i++){
            DataBuilder.NoiseMap noiseMap = DataBuilder.worldData.noiseMaps[i];
            data[i][0] = noiseMap.name;
            data[i][1] = ""+noiseMap.noiseSeed;
            data[i][2] = ""+noiseMap.freq;
            data[i][3] = ""+noiseMap.rank;
        }

        return data;
    }

    /**
     * Used only for loading.
     * @param data A String[][] object to load values for the WorldGen from.
     */
    @JsonProperty("worldData")
    private void setWorldData(String[][] data){
        DataBuilder.worldData.noiseMaps = new DataBuilder.NoiseMap[data.length];
        DataBuilder.worldData.noiseMapHashMap = new HashMap<>(data.length);

        for(int i=0; i < data.length; i++){
            DataBuilder.NoiseMap noiseMap = DataBuilder.worldData.noiseMaps[i] = new DataBuilder.NoiseMap();
            noiseMap.name = data[i][0];
            noiseMap.noiseSeed = Long.parseLong(data[i][1]);
            noiseMap.freq = Float.parseFloat(data[i][2]);
            noiseMap.rank = Integer.parseInt(data[i][3]);

            DataBuilder.worldData.noiseMapHashMap.put(i, noiseMap);
        }
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
}


