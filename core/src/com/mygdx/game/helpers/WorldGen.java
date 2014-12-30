package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.Entity;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/24/2014.
 */
public class WorldGen {
    public static TerrainTile[][] map;
    public static int tileSize = 25;
    public static float treeScale = 0.3f;
    public static float freq = 5;
    public static int numStep = 1;
    public static float percentageDone = 0;

    private static Texture[] grassTiles;
    private static Texture[] tallGrassTiles;
    private static Texture[] treeTextures;

    private static ArrayList<Entity> treeList = new ArrayList<>();

    private static int numX, numY, currX = 0, currY = 0;

    //Load up the initial textures
    static{

    }

    public static void init(long seed){
        grassTiles = new Texture[4];
        tallGrassTiles = new Texture[3];
        treeTextures = new Texture[13];

        grassTiles[0] = new Texture("img/grass1.png");
        grassTiles[1] = new Texture("img/grass2.png");
        grassTiles[2] = new Texture("img/grass3.png");
        grassTiles[3] = new Texture("img/grass4.png");

        tallGrassTiles[0] = new Texture("img/tallgrass1.png");
        tallGrassTiles[1] = new Texture("img/tallgrass2.png");
        tallGrassTiles[2] = new Texture("img/tallgrass3.png");

        for(int i=2;i<treeTextures.length;i++){
            if(i!= 3 && i!= 12 && i!=13)
                treeTextures[i-2] = new Texture("img/trees/Tree"+i+".png");
            else
                treeTextures[i-2] = new Texture("img/trees/Tree5.png");
        }

        SimplexNoise.genGrad(seed);

        numX = Gdx.graphics.getWidth()/tileSize + 1;
        numY = Gdx.graphics.getHeight()/tileSize + 1;

        map = new TerrainTile[numX][numY];
    }

    public static boolean generateTerrain(){
        int stepLeft = numStep;
        boolean done = true; //Flag for completion.

        while(stepLeft > 0 && currX < numX){
            TerrainTile tile = map[currX][currY] = new TerrainTile();
            tile.noiseValue = SimplexNoise.noise((double)currX/freq,(double)currY/freq); //Generate the noise for this tile.
            tile.position = new Vector2(currX*tileSize, currY*tileSize); //Set the position.

            if(tile.noiseValue < -0.2) {
                tile.type = 0;
                tile.image = new Texture("img/DarkWater.png");
            }else if (tile.noiseValue < 0) {
                tile.type = 0;
                tile.image = new Texture("img/LightWater.png");

            }else if (tile.noiseValue < 0.6){
                tile.type = 1;
                tile.image = grassTiles[(int)(MathUtils.random()*grassTiles.length)];
                tile.rotation = (int)(MathUtils.random()*4)*90;
            }else{
                tile.type = 1;
                tile.image = tallGrassTiles[(int)(MathUtils.random()*tallGrassTiles.length)];
                tile.rotation = (int)(MathUtils.random()*4)*90;
            }

            if(tile.type == 1){
                if(MathUtils.random() < 0.6){
                    Vector2 pos = new Vector2(tile.position.x + MathUtils.random()*tileSize, tile.position.y + MathUtils.random()*tileSize);
                    Entity entity = new Entity(pos, MathUtils.random()*360, treeTextures[(int)(MathUtils.random()*treeTextures.length)], ColonyGame.batch, 11);
                    entity.transform.setScale(treeScale);
                    treeList.add(entity);
                }

            }

            done = false;
            stepLeft--;
            if(currY < numY-1) currY++;
            else{
                currY = 0;
                currX++;
            }

            float currDone = currX + (currX*numY + currY);
            float total = (numX+1)*(numY+1);
            percentageDone = (float)currDone/(float)total;
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


