package com.mygdx.game.helpers.worldgeneration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.component.Resource;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.server.ServerPlayer;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/24/2014.
 */
public class WorldGen {
    public static TerrainTile[][] map;

    //Some default values that can be modified globally.
    public static int tileSize = 25;
    public static float treeScale = 0.8f;
    public static float freq = 5;
    public static int numStep = 1;
    public static float percentageDone = 0;

    private static Texture[] grassTiles;
    private static Texture[] tallGrassTiles;
    private static Texture treeTexture = new Texture("img/trees/tree.png");
    private static Texture rockTexture = new Texture("img/rock.png");

    private static ArrayList<Entity> treeList = new ArrayList<>();

    private static int numX, numY, currX = 0, currY = 0;

    /**
     * Initializes the World Generator. For now, most stuff is temporary for prototyping.
     * @param seed The seed that the world should use for randomly generating.
     */
    public static void init(long seed){
        grassTiles = new Texture[4];
        tallGrassTiles = new Texture[3];

        //Loads in some grass.
        grassTiles[0] = new Texture("img/grass1.png");
        grassTiles[1] = new Texture("img/grass2.png");
        grassTiles[2] = new Texture("img/grass3.png");
        grassTiles[3] = new Texture("img/grass4.png");

        //Loads in some tall grass.
        tallGrassTiles[0] = new Texture("img/tallgrass1.png");
        tallGrassTiles[1] = new Texture("img/tallgrass2.png");
        tallGrassTiles[2] = new Texture("img/tallgrass3.png");

        //This randomizes the noise by using the seed passed in.
        SimplexNoise.genGrad(seed);

        //Sets the number of tiles in X (numX) and Y (numY) by getting the screen width/height.
        numX = Gdx.graphics.getWidth()/tileSize + 1;
        numY = Gdx.graphics.getHeight()/tileSize + 1;

        //Initializes a new array
        map = new TerrainTile[numX][numY];
    }

    /**
     * Called every frame to generate the world. This will return true when the world is fully generated.
     * @return True when finished, false otherwise.
     */
    public static boolean generateWorld(){
        int stepsLeft = numStep;
        boolean done = true; //Flag for completion.

        //If there's steps left and currX is still less than the total num X, generate!
        while(stepsLeft > 0 && currX < numX){
            TerrainTile tile = map[currX][currY] = new TerrainTile(); //Initialize a new terrain tile.
            tile.noiseValue = SimplexNoise.noise((double)currX/freq,(double)currY/freq); //Generate the noise for this tile.
            tile.position = new Vector2(currX*tileSize, currY*tileSize); //Set the position.

            //If under this value, generate dark water.
            if(tile.noiseValue < -0.2) {
                tile.type = 0;
                tile.image = new Texture("img/DarkWater.png");

            //If between 0 and -0.2, light water.
            }else if (tile.noiseValue < 0) {
                tile.type = 0;
                tile.image = new Texture("img/LightWater.png");

            //If between 0 and 0.6, random grass.
            }else if (tile.noiseValue < 0.6){
                tile.type = 1;
                tile.image = grassTiles[(int)(MathUtils.random()*grassTiles.length)];
                tile.rotation = (int)(MathUtils.random()*4)*90;

            //Otherwise, tall grass!
            }else{
                tile.type = 1;
                tile.image = tallGrassTiles[(int)(MathUtils.random()*tallGrassTiles.length)];
                tile.rotation = (int)(MathUtils.random()*4)*90;
            }

            //If the tile is not water...
            if(tile.type == 1){
                float rand = MathUtils.random();
                //Random chance for a tree to spawn.
                if(rand < 0.1){
                    Vector2 pos = new Vector2(tile.position.x + MathUtils.random()*tileSize, tile.position.y + MathUtils.random()*tileSize); //Get a random position in the tile.
                    Entity tree = new Entity(pos, 0, treeTexture, ColonyGame.batch, 11); //Make the Entity
                    tree.transform.setScale(treeScale); //Set the scale.
                    tree.name = "Tree"; //Set the name!

                    //All Box2D stuff below...
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;

                    CircleShape circle = new CircleShape();
                    circle.setRadius(15f);

                    FixtureDef fixtureDef = new FixtureDef();
                    fixtureDef.shape = circle;
                    fixtureDef.density = 0;
                    fixtureDef.friction = 0;
                    fixtureDef.restitution = 0;

                    //Add the Collider Component and an Interactable Component.
                    tree.addComponent(new Collider(ColonyGame.world, bodyDef, fixtureDef));
                    tree.addComponent(new Interactable("resource"));
                    tree.addComponent(new Resource("Wood"));
                    tree.addComponent(new GridComponent(Constants.GRIDSTATIC, ColonyGame.worldGrid));

                    //We add to a tree list for prototyping.
                    treeList.add(tree);

                    //Dispose of the circle.
                    circle.dispose();
                }else if(rand < 0.15){
                    Vector2 pos = new Vector2(tile.position.x + MathUtils.random()*tileSize, tile.position.y + MathUtils.random()*tileSize); //Get a random position in the tile.
                    Entity entity = new Entity(pos, 0, rockTexture, ColonyGame.batch, 11); //Make the Entity
                    entity.transform.setScale(0.6f); //Set the scale.
                    entity.name = "Rock"; //Set the name!

                    //All Box2D stuff below...
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;

                    CircleShape circle = new CircleShape();
                    circle.setRadius(18f);

                    FixtureDef fixtureDef = new FixtureDef();
                    fixtureDef.shape = circle;
                    fixtureDef.density = 0;
                    fixtureDef.friction = 0;
                    fixtureDef.restitution = 0;

                    //Add the Collider Component and an Interactable Component.
                    entity.addComponent(new Collider(ColonyGame.world, bodyDef, fixtureDef));
                    entity.addComponent(new Interactable("resource"));
                    entity.addComponent(new Resource("Rocks n Stuff"));
                }
            }

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
            percentageDone = (float)currDone/(float)total; //Calcs the percentage done so that the player's UI can use this.
        }

        return done;
    }

    public static void changeSeed(long seed){
        SimplexNoise.genGrad(seed);
    }

    public static int numTiles() {
        return numX*numY;
    }

    public static int numTrees(){
        return treeList.size();
    }

    public static class TerrainTile{
        public Texture image;
        public Vector2 position;
        public double noiseValue;
        public float rotation;
        public int type;

    }

}


