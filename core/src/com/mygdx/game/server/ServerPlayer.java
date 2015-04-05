package com.mygdx.game.server;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Colony;
import com.mygdx.game.component.Resource;
import com.mygdx.game.entity.AnimalEnt;
import com.mygdx.game.entity.ColonyEntity;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.managers.DataManager;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.ui.PlayerInterface;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 11/23/2014.
 */
public class ServerPlayer {
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private ColonyGame game;
	private Grid.GridInstance grid;
    private boolean paused = false;

	public static String[] firstNames = {"Bobby","Sally","Jimmy","Bradley","Willy","Tommy","Brian",
            "Doug","Ben","Jacob","Sammy","Jason","David","Sarah","Betty","Tom","James"};

    public static String[] lastNames = {"Poopers"};

    private boolean generatedTrees = false;
    private Vector2 startLocation = new Vector2();

	public ServerPlayer(SpriteBatch batch, ShapeRenderer renderer, ColonyGame game){
		//Server.start(1337); //Start the server
		this.grid = ColonyGame.worldGrid;

		//Store spritebatch and shaperenderer.
		this.batch = batch;
		this.shapeRenderer = renderer;
		this.game = game;

		initPlayer();
    }

	public void render(float delta){
        if(paused)
            delta = 0f;

        if(!generatedTrees) {
            generatedTrees = WorldGen.getInstance().generateResources(new Vector2((ColonyGame.worldGrid.getNumCols() - 1) * WorldGen.getInstance().getTileSize(), (ColonyGame.worldGrid.getNumRows() - 1) * WorldGen.getInstance().getTileSize()), 0, Constants.WORLDGEN_RESOURCEGENERATESPEED);
            if(generatedTrees){
                startLocation.set((ColonyGame.worldGrid.getNumCols()/2)*ColonyGame.worldGrid.getSquareSize(), (ColonyGame.worldGrid.getNumRows()/2)*ColonyGame.worldGrid.getSquareSize());
                generateStart(startLocation);
            }
        }
	}

    public boolean getPaused(){
        return this.paused;
    }

    public void setPaused(boolean paused){
        this.paused = paused;
    }

	private void generateStart(Vector2 start){
        //Find a suitable place to spawn our Colony
        block: {
            int radius = 0;
            boolean placed = false;
            WorldGen world = WorldGen.getInstance();
            int[] index = world.getIndex(start);

            while (!placed) {
                int startX = index[0] - radius;
                int endX = index[0] + radius;
                int startY = index[1] - radius;
                int endY = index[1] + radius;

                //Loop over each tile.
                for (int x = startX; x <= endX && !placed; x++) {
                    for (int y = startY; y <= endY && !placed; y++) {
                        if (x != startX && x != endX && y != startY && y != endY)
                            continue;

                        if(startX < 0 || endX > world.getWidth() || startY < 0 || endY > world.getHeight())
                            GH.writeErrorMessage("Couldn't find a place to spawn the base!");

                        //For each tile, we want to check if there is a 4x4 surrounding area.
                        int innerStartX = x - 5;
                        int innerEndX = x + 5;
                        int innerStartY = y - 5;
                        int innerEndY = y + 5;

                        //If the node is null (outside the bounds), continue.
                        if (world.getNode(innerStartX, innerStartY) == null || world.getNode(innerEndX, innerEndY) == null)
                            continue;

                        placed = true;

                        //Check over the inner area. If all tiles are not set to avoid, we have a place we can spawn our Colony.
                        for (int innerX = innerStartX; innerX <= innerEndX && placed; innerX++) {
                            for (int innerY = innerStartY; innerY <= innerEndY && placed; innerY++) {
                                WorldGen.TerrainTile tile = world.getNode(innerX, innerY);
                                if (tile.avoid)//If there is a single tile set to avoid, break!
                                    placed = false;
                            }
                        }

                        //If passed, calculate the start vector.
                        if(placed)
                            start.set(x * world.getTileSize(), y * world.getTileSize());
                    }
                }
                radius++;
            }
        }

        //Spawns the Colony Entity and centers the camera on it.
        ColonyEntity colonyEnt = new ColonyEntity(start, 0, new TextureRegion(ColonyGame.assetManager.get("Colony", Texture.class)), 10);
        Colony colony = colonyEnt.getComponent(Colony.class);
        ColonyGame.camera.position.set(colonyEnt.transform.getPosition().x, colonyEnt.transform.getPosition().y, 0);

        //Spawns some squirrels
        for(int i=0;i<5;i++) {
            TextureAtlas atlas = ColonyGame.assetManager.get("interactables", TextureAtlas.class);
            DataBuilder.JsonAnimal animalRef = DataManager.getData("squirrel", DataBuilder.JsonAnimal.class);
            new AnimalEnt(animalRef, start, 0, atlas.findRegion("squirrel"), 11);
        }

        //Destroys resources in an area around the Colony Entity.
		int radius = 8;
		Functional.Perform<Grid.Node[][]> destroyNearbyResources = (grid) -> {
			int[] index= this.grid.getIndex(colonyEnt.transform.getPosition());
			int startX = index[0]-radius;
			int endX = index[0]+radius;
			int startY = index[1]-radius;
			int endY = index[1]+radius;

            //Loops over the boundaries and draws the map.
			for(int col = startX; col<=endX; col++){
				for(int row = startY; row <= endY; row++){
					Grid.Node node = this.grid.getNode(col, row);
					if(node == null) continue;
                    if(Math.abs(node.getCol() - index[0]) + Math.abs(node.getRow() - index[1]) >= radius*1.5) continue;

                    new ArrayList<>(node.getEntityList()).stream().filter(ent -> ent.hasTag(Constants.ENTITY_RESOURCE)).forEach(com.mygdx.game.entity.Entity::setToDestroy);

                    //WorldGen.getVisibilityMap()[col][row].addViewer();
				}
			}
		};

        //Detects resources around the Colony Entity.
		Functional.Perform<Grid.Node[][]> detectNearbyResources = (grid) -> {
			for(int col = 0; col<grid.length; col++){
				for(int row = 0; row < grid[col].length; row++){
					Grid.Node node = this.grid.getNode(col, row);
					if(node == null) continue;
					for(Entity ent : node.getEntityList()){
						Resource resource;
						if((resource = ent.getComponent(Resource.class)) != null) {
                            colony.addNearbyResource(resource);
                        }
					}
				}
			}
		};

        //Perform the things.
		this.grid.perform(destroyNearbyResources);
		this.grid.perform(detectNearbyResources);
	}

	private void initPlayer(){
		new PlayerInterface(ColonyGame.batch, this.game, this, ColonyGame.world);
	}
}
