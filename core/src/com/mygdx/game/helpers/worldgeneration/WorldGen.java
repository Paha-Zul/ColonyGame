package com.mygdx.game.helpers.worldgeneration;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.entity.TreeEnt;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.managers.ResourceManager;

import java.util.ArrayList;
import java.util.HashMap;

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

    private HashMap<String, ArrayList<Texture>> texMap = new HashMap<>();
    private Texture treeTexture;
    private Texture rockTexture;

    public Texture whiteTex;

    private ArrayList<Entity> treeList = new ArrayList<>();
    private VisibilityTile[][] visibilityMap;

    private int numX, numY, currX = 0, currY = 0;

    private ColonyGame game;
    private static WorldGen instance;

    /**
     * Initializes the World Generator. For now, most stuff is temporary for prototyping.
     * @param seed The seed that the world should use for randomly generating.
     */
    public void init(long seed, ColonyGame game){
        this.game = game;

        loadImages();

        //This randomizes the noise by using the seed passed in.
        SimplexNoise.genGrad(seed);

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
    }

    private void loadImages(){
        treeTexture = ColonyGame.assetManager.get("redtree", Texture.class);
        rockTexture = new Texture("img/rock.png");
    }

    /**
     * Called every frame to generate the world. This will return true when the world is fully generated.
     * @return True when finished, false otherwise.
     */
    public boolean generateWorld(){
        int stepsLeft = Constants.WORLDGEN_GENERATESPEED;
        boolean done = true; //Flag for completion.
        ArrayList<Texture> texList;

        //If there's steps left and currX is still less than the total num X, generate!
        while(stepsLeft > 0 && currX < numX){
            double noiseValue = SimplexNoise.noise((double)currX/freq,(double)currY/freq); //Generate the noise for this tile.
            Vector2 position = new Vector2(currX*tileSize, currY*tileSize); //Set the position.
            Sprite terrainSprite;
            int type;
            float rotation=0;

            //If under this value, generate dark water.
            if(noiseValue < -0.6) {
                type = Constants.TERRAIN_WATER;
                texList = getTextureList("darkwater");
                terrainSprite = new Sprite(texList.get((int)(MathUtils.random()*texList.size())));

            //If between 0 and -0.2, light water.
            }else if (noiseValue < -0.4) {
                type = Constants.TERRAIN_WATER;
                texList = getTextureList("lightwater");
                terrainSprite = new Sprite(texList.get((int)(MathUtils.random()*texList.size())));

            //If between 0 and 0.6, random grass.
            }else if (noiseValue < 0.6){
                type = Constants.TERRAIN_GRASS;
                texList = getTextureList("grass1");
                terrainSprite = new Sprite(texList.get((int)(MathUtils.random()*texList.size())));
                //rotation = (int)(MathUtils.random()*4)*90;

            //Otherwise, tall grass!
            }else{
                type = Constants.TERRAIN_GRASS;
                texList = getTextureList("tallgrass");
                terrainSprite = new Sprite(texList.get((int)(MathUtils.random()*texList.size())));
                //rotation = (int)(MathUtils.random()*4)*90;
            }

            //If the tile is not water...
            if(type == 1){
                float rand = MathUtils.random();
                //Random chance for a tree to spawn.
                if(rand < 0.1){
                    Vector2 pos = new Vector2(position.x + tileSize/2, position.y + tileSize/2); //Get a random position in the tile.
                    Entity tree = new TreeEnt(pos, 0, treeTexture, ColonyGame.batch, 11); //Make the Entity
                    tree.transform.setScale(treeScale); //Set the scale.
                    tree.name = "Tree";

                    //We add to a tree list for prototyping.
                    treeList.add(tree);
                }else if(rand < 0.15){
                    Vector2 pos = new Vector2(position.x + tileSize/2, position.y + tileSize/2); //Get a random position in the tile.
                    Entity rock = new Entity(pos, 0, rockTexture, ColonyGame.batch, 11); //Make the Entity
                    rock.transform.setScale(0.6f); //Set the scale.
                    rock.name = "Rock"; //Set the name!

                    rock.addComponent(new Interactable("resource"));
                    rock.addComponent(ResourceManager.getResourceByname("rock"));
                    rock.addComponent(new GridComponent(Constants.GRIDSTATIC, ColonyGame.worldGrid, -1));
                    rock.addTag(Constants.ENTITY_RESOURCE);
                    //circle.dispose();
                }
            }

            map[currX][currY] = new TerrainTile(terrainSprite, noiseValue, rotation, type, position); //Create a new terrain tile.
            this.visibilityMap[currX][currY] = new VisibilityTile(); //Set this to unexplored.

            done = false; //Set done to false signifying that we are not finished yet.
            stepsLeft--; //Decrement the remaining step amount.

            if(currY < numY-1) currY++; //Increment currY
            //Otherwise, set currY to 0 and increment X.
            else{
                currY = 0;
                currX++;
            }

            float currDone = currX + (currX*numY + currY);
            float total = (numX+1)*(numY+1);
            percentageDone = currDone/total; //Calcs the percentage done so that the player's UI can use this.

        }

        return done;
    }

    public void addTexture(String type, Texture texture){
        ArrayList<Texture> list = texMap.get(type);
        if(list == null){
            list = new ArrayList<>();
            texMap.put(type, list);
        }

        list.add(texture);
    }

    public ArrayList<Texture> getTextureList(String type){
        ArrayList<Texture> list = texMap.get(type);
        if(list == null)
            return new ArrayList<>();

        return list;
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
        return treeList.size();
    }

    public class TerrainTile {
        public Sprite terrainSprite;
        public double noiseValue;
        public int type;


        private int visibility = Constants.VISIBILITY_UNEXPLORED;

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


