package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ExploreGame;
import com.mygdx.game.entity.Entity;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/24/2014.
 */
public class WorldGen {
    public static TerrainTile[][] map;
    public static int tileSize = 25;
    public static float freq = 5;

    private static Texture[] grassTiles;
    private static Texture[] tallGrassTiles;
    private static Texture[] treeTextures;

    private static ArrayList<Entity> treeList = new ArrayList<>();

    public static void generateTerrain(long seed){
        SimplexNoise.genGrad(seed);

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

        int numX = Gdx.graphics.getWidth()/tileSize;
        int numY = Gdx.graphics.getHeight()/tileSize;

        map = new TerrainTile[numX+1][numY+1]; //Initialize the size

        //Loop over the array
        for(int x=0;x<map.length;x++){
            for(int y=0;y<map[0].length;y++){
                TerrainTile tile = map[x][y] = new TerrainTile();
                tile.noiseValue = SimplexNoise.noise((double)x/freq,(double)y/freq); //Generate the noise for this tile.
                tile.position = new Vector2(x*tileSize, y*tileSize); //Set the position.

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
            }
        }

        for(int i=0;i<100;i++){
            Vector2 pos;
            do {
                pos = new Vector2(Gdx.graphics.getWidth() * MathUtils.random(), Gdx.graphics.getHeight() * MathUtils.random());
            }while(map[(int)(pos.x/tileSize)][(int)(pos.y/tileSize)].type != 1);

            Entity entity = new Entity(pos,MathUtils.random()*360, treeTextures[(int)(MathUtils.random()*treeTextures.length)], ExploreGame.batch, 11);
            entity.transform.setScale(0.5f);
            treeList.add(entity);
        }
    }

    public static class TerrainTile{
        public Texture image;
        public Vector2 position;
        public double noiseValue;
        public float rotation;
        public int type;

    }

}


