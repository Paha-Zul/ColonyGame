package com.mygdx.game.helpers.worldgeneration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Resource;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.entity.ResourceEnt;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.managers.ResourceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Bbent_000 on 12/24/2014.
 */
public class WorldGen {
    public static TerrainTile[][] map;

    //Some default values that can be modified globally.
    public int tileSize = 25;
    public float treeScale = 0.8f;
    public float freq = 5;
    public float percentageDone = 0;

    public Texture whiteTex;

    private TextureAtlas terrainAtlas;

    private VisibilityTile[][] visibilityMap;

    private int numX, numY, currX = 0, currY = 0;
    private int numTrees = 0;

    private ColonyGame game;
    private static WorldGen instance;

    private int currDone = 0;
    private int currIndex = 0;
    private int lastIndex = -1;

    //Variables for performing breadth first resource spawning;
    private LinkedList<TerrainTile> neighbors = new LinkedList<>();
    private HashMap<Integer, TerrainTile> visitedMap = new HashMap<>(1000);
    private ArrayList<TerrainTile> tmpNeighbors = new ArrayList<>(4);
    private int[] startIndex;
    private Vector2 center = new Vector2();
    private boolean started = false;
    private String noiseMapName = "";


    /**
     * Initializes the World Generator. For now, most stuff is temporary for prototyping.
     */
    public void init(ColonyGame game){
        this.game = game;

        //Sets the number of tiles in X (numX) and Y (numY) by getting the screen width/height.
        numX = Constants.GRID_WIDTH/tileSize + 1;
        numY = Constants.GRID_HEIGHT/tileSize + 1;

        //Initializes a new array
        map = new TerrainTile[numX][numY];
        //Initialize a new int array.
        this.visibilityMap = new VisibilityTile[numX][numY];

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
        int maxAmount = numTiles() * DataBuilder.worldData.noiseMapHashMap.size();
        int stepsLeft = Constants.WORLDGEN_GENERATESPEED;
        boolean done = true; //Flag for completion.
        HashMap<String, DataBuilder.JsonTileGroup> tileGroupsMap = DataBuilder.tileGroupsMap;

        //This will loop until everything is done.
        while(this.currDone < maxAmount && stepsLeft > 0) {
            freq = DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).freq;
            String noiseMapName = DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).name;

            if(this.lastIndex != this.currIndex){
                this.lastIndex = this.currIndex;
                SimplexNoise.genGrad(DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).noiseSeed);
            }

            //Loop for a certain amount of steps. This is done for each noise level. Resets after each one.
            while (stepsLeft > 0 && currX < numX) {
                double noiseValue = SimplexNoise.noise((double) currX / freq, (double) currY / freq); //Generate the noise for this tile.
                Vector2 position = new Vector2(currX * tileSize, currY * tileSize); //Set the position.
                Sprite terrainSprite; //Prepare the sprite object.
                int type = 0; //The type of tile.
                float rotation = 0; //The rotation of the tile.

                done = false; //Set done to false signifying that we are not finished yet.
                //Gets the jTile as the noise height.
                DataBuilder.JsonTile jtile = this.getTileAtHeight(tileGroupsMap.get(noiseMapName).tiles, (float) noiseValue);
                if (jtile == null && noiseMapName.equals("main"))
                    GH.writeErrorMessage("No tile found at height "+noiseValue+" for noise map '"+noiseMapName+"'. A tile must exist for all heights on noise map '"+noiseMapName+"'.");

                //If the tile is not null, let use assign a new tile to the terrain.
                else if(jtile != null) {
                    //Gets the texture that the tile should be.
                    int randTileIndex = MathUtils.random(jtile.img.length - 1);
                    TerrainTile tile;

                    if(map[currX][currY] != null){
                        tile = map[currX][currY];
                    }else{
                        tile = new TerrainTile();
                    }

                    //Creates a new sprite and a new tile, assigns the Sprite to the tile and puts it into the map array.
                    terrainSprite = terrainAtlas.createSprite(jtile.img[randTileIndex]);
                    terrainSprite.setSize(tileSize, tileSize);
                    tile.set(terrainSprite, noiseValue, rotation, type, position); //Create a new terrain tile.
                    tile.avoid = jtile.avoid;
                    if(jtile.tileNames.length <= randTileIndex) GH.writeErrorMessage("Tile with image "+jtile.img[randTileIndex]+" and category "+jtile.category+" does not have a name assigned to it");
                    tile.tileName = jtile.tileNames[randTileIndex]; //Assign the name
                    tile.category = jtile.category; //Assign the category.
                    map[currX][currY] = tile; //Assign the tile to the map.
                }

                //Generate the visibility tile for this location
                this.visibilityMap[currX][currY] = new VisibilityTile();

                //Decrement steps remaining and increment currDone.
                stepsLeft--;
                this.currDone++;

                if (currY < numY - 1) currY++; //Increment currY
                //Otherwise, set currY to 0 and increment X.
                else {
                    currY = 0; //Reset currY
                    currX++; //Increment currX
                }

                float currDone = this.currDone;
                percentageDone = currDone / maxAmount; //Calcs the percentage done so that the player's UI can use this.
            }

            //If we are done with the current noise level but we have more levels to go, reset the currX and currY, increment currIndex and continue.
            if (done && this.currIndex < DataBuilder.worldData.noiseMapHashMap.size()) {
                currX = currY = 0;
                this.currIndex++;
                done = false;
            //Otherwise, if we are simply done, let's be done!
            }

            //If we exit the loop, set stepsLeft to 0 so we can restart if we need to.
            stepsLeft = 0;
        }

        if(done){
            currIndex = 0;
            currX = currY = 0;
        }

        return done;
    }

    public boolean generateResources(Vector2 startPos, int blockRadius, int step){
        boolean done = false;

        if(!started){
            this.neighbors.add(this.getNode(startPos));
            this.startIndex = this.getIndex(startPos);
            this.started = true;
            noiseMapName = DataBuilder.worldData.noiseMapHashMap.get(this.currIndex).name;
        }

        //Partially executes a breadth first search.
        for (int i = 0; i < step; i++) {
            TerrainTile currTile = neighbors.pop(); //Pop the first neighbor
            int[] index = {(int) (currTile.terrainSprite.getX() / Constants.GRID_SQUARESIZE), (int) (currTile.terrainSprite.getY() / Constants.GRID_SQUARESIZE)}; //Get the index.
            //Get left/right/up/down
            TerrainTile left = this.getNode(index[0] - 1, index[1]);
            TerrainTile right = this.getNode(index[0] + 1, index[1]);
            TerrainTile up = this.getNode(index[0], index[1] + 1);
            TerrainTile down = this.getNode(index[0] - 1, index[1] - 1);

            //If a neighbor is not null and it hasn't been visited already...
            if (left != null && !visitedMap.containsKey(left.hashCode())) {
                neighbors.add(left);
                visitedMap.put(left.hashCode(), left);
            }
            if (right != null && !visitedMap.containsKey(right.hashCode())) {
                neighbors.add(right);
                visitedMap.put(right.hashCode(), right);
            }
            if (up != null && !visitedMap.containsKey(up.hashCode())) {
                neighbors.add(up);
                visitedMap.put(up.hashCode(), up);
            }
            if (down != null && !visitedMap.containsKey(down.hashCode())) {
                neighbors.add(down);
                visitedMap.put(down.hashCode(), down);
            }

            //Add the current tile to the visitedMap.
            visitedMap.put(currTile.hashCode(), currTile);

            int[] currIndex = getIndex(currTile.terrainSprite.getX(), currTile.terrainSprite.getY()); //Get the current index.
            //If the resource is within the blockRadius, simply continue.
            if (Math.abs(currIndex[0] - startIndex[0]) <= blockRadius && Math.abs(currIndex[1] - startIndex[1]) <= blockRadius)
                continue;

            //Set the center Vector2 and spawn the tree using the center position.
            center.set(currTile.terrainSprite.getX() + Constants.GRID_SQUARESIZE * 0.5f, currTile.terrainSprite.getY() + Constants.GRID_SQUARESIZE * 0.5f);
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

    private void spawnTree(DataBuilder.JsonTile jtile, Vector2 centerPos, TerrainTile currTile){
        if(jtile == null || currTile == null || !currTile.category.equals(jtile.category))
            return;

        //Gets the resource that should be spawned on this tile
        Resource res = this.getResourceOnTile(jtile, (float)Math.random());

        if(res != null) {
            ResourceEnt resEnt = new ResourceEnt(centerPos, 0, ColonyGame.assetManager.get(res.getTextureName(), Texture.class), ColonyGame.batch, 11);
            resEnt.addComponent(res);
            resEnt.transform.setScale(treeScale);
        }
    }

    public int[] getIndex(Vector2 pos){
        return getIndex(pos.x, pos.y);
    }

    public int[] getIndex(float x, float y){
        return new int[]{(int)(x/Constants.GRID_SQUARESIZE), (int)(y/Constants.GRID_SQUARESIZE)};
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
    private Resource getResourceOnTile(DataBuilder.JsonTile tile, float value){
        if(tile.resources == null || tile.resources.length <= 0)
            return null;

        for (int i=0;i<tile.resources.length;i++) {
            if (value >= tile.resourcesChance[i][0] && value <= tile.resourcesChance[i][1]) {
                return ResourceManager.getResourceByname(tile.resources[i]);
            }
        }

        return null;
    }

    /**
     * Gets the Node at the Entity's location.
     *
     * @param entity The Entity to use for a location.
     * @return A Node at the Entity's location.
     */
    public TerrainTile getNode(Entity entity) {
        return getNode((int) (entity.transform.getPosition().x / tileSize), (int) (entity.transform.getPosition().y / tileSize));
    }

    /**
     * Gets the Node at the Vector2 position.
     *
     * @param pos The Vector2 position to get a Node at.
     * @return A Node at the Vector2 position.
     */
    public TerrainTile getNode(Vector2 pos) {
        return getNode((int) (pos.x / tileSize), (int) (pos.y / tileSize));
    }

    /**
     * Gets a Node by a X and Y index.
     *
     * @param x The X (col) index to get the Node at.
     * @param y The Y (row) index to get the Node at.
     * @return The Node if the index was valid, null otherwise.
     */
    public TerrainTile getNode(int x, int y) {
        //If the index is not in bounds, return null.
        if (x < 0 || x >= map.length || y < 0 || y >= map[x].length)
            return null;

        return map[x][y];
    }

    /**
     * Gets a Node by an index.
     *
     * @param index An integer array containing X and Y index.
     * @return The Node if the index was valid, null otherwise.
     */
    public TerrainTile getNode(int[] index) {
        if (index.length < 2)
            return null;

        return getNode(index[0], index[1]);
    }

    public int numTiles() {
        return numX*numY;
    }

    public VisibilityTile[][] getVisibilityMap(){
        return this.visibilityMap;
    }

    public int numTrees(){
        return numTrees;
    }

    public float getTileSize(){
        return this.tileSize;
    }

    public class TerrainTile {
        public String tileName, category;
        public Sprite terrainSprite;
        public double noiseValue;
        public int type;
        public boolean avoid = false;
        private int visibility = Constants.VISIBILITY_UNEXPLORED;

        public TerrainTile(){

        }

        public TerrainTile(Sprite sprite, double noiseValue, float rotation, int type, Vector2 position) {
            this.terrainSprite = new Sprite(sprite);
            this.terrainSprite.setColor(Constants.COLOR_UNEXPLORED);
            this.noiseValue = noiseValue;
            this.type = type;
            this.terrainSprite.setPosition(position.x, position.y);
            this.terrainSprite.setRotation(rotation);
        }

        public void changeVisibility(int visibility){
            if(this.visibility == visibility)
                return;

            this.visibility = visibility;
            if(visibility == Constants.VISIBILITY_UNEXPLORED) this.terrainSprite.setColor(Constants.COLOR_UNEXPLORED);
            if(visibility == Constants.VISIBILITY_EXPLORED) this.terrainSprite.setColor(Constants.COLOR_EXPLORED);
            if(visibility == Constants.VISIBILITY_VISIBLE) this.terrainSprite.setColor(Constants.COLOR_VISIBILE);
        }

        public void set(Sprite sprite, double noiseValue, float rotation, int type, Vector2 position){
            if(terrainSprite != null)
                this.terrainSprite.set(sprite);
            else this.terrainSprite = sprite;

            this.noiseValue = noiseValue;
            this.terrainSprite.setRotation(rotation);
            this.type = type;
            this.terrainSprite.setPosition(position.x, position.y);
            this.terrainSprite.setColor(Constants.COLOR_UNEXPLORED);
        }

        @Override
        public int hashCode() {
            int hash = (int)(terrainSprite.getX() + terrainSprite.getY() + terrainSprite.getWidth() + terrainSprite.getY());
            hash += hash*type;
            hash += noiseValue*10000d;
            hash += terrainSprite.hashCode();

            return hash;
        }
    }

    public class VisibilityTile{
        private int visibility = Constants.VISIBILITY_UNEXPLORED;
        private int currViewers = 0;

        public VisibilityTile(){
            this.visibility = Constants.VISIBILITY_UNEXPLORED;
        }

        /**
         * Adds a viewer to this Tile. This will immediately mark the Tile as visible.
         */
        public void addViewer(){
            this.currViewers++;
            this.changeVisibility(Constants.VISIBILITY_VISIBLE);
        }

        /**
         * Removes a viewer from this tile. If it reaches 0, the terrain is set to explored and not visibile.
         */
        public void removeViewer(){
            this.currViewers--;
            if(currViewers <= 0)
                this.changeVisibility(Constants.VISIBILITY_EXPLORED);
        }

        public int getVisibility() {
            return visibility;
        }

        public void changeVisibility(int visibility){
            this.visibility = visibility;
        }

        public int getCurrViewers() {
            return currViewers;
        }
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


