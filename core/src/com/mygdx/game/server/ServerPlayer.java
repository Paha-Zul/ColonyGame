package com.mygdx.game.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.Colony;
import com.mygdx.game.entity.AnimalEnt;
import com.mygdx.game.entity.ColonyEntity;
import com.mygdx.game.helpers.*;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.worldgeneration.WorldGen;

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

    private final int off = 5;

    public static boolean drawGrid = false;

	public static String[] firstNames = {"Bobby","Sally","Jimmy","Bradley","Willy","Tommy","Brian",
            "Doug","Ben","Jacob","Sammy","Jason","David","Sarah","Betty","Tom","James"};

    public static String[] lastNames = {"Poopers"};

    private boolean generatedTrees = false;
    private Vector2 startLocation = new Vector2();

	//Box2d stuff

	private Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);

	public ServerPlayer(SpriteBatch batch, ShapeRenderer renderer, ColonyGame game){

		//Start the server
		//Server.start(1337);
		this.grid = ColonyGame.worldGrid;

		//Make a new spritebatch and shaperenderer.
		this.batch = batch;
		this.shapeRenderer = renderer;
		this.game = game;

		//Create the Box2D world.
		ColonyGame.debugRenderer = new Box2DDebugRenderer();

        ColonyGame.camera.position.set((ColonyGame.worldGrid.getNumCols() / 2) * ColonyGame.worldGrid.getSquareSize(), (ColonyGame.worldGrid.getNumRows() / 2) * ColonyGame.worldGrid.getSquareSize(), 0);

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

		Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//Step the Box2D simulation.
		ColonyGame.world.step(1f/60f, 8, 3);

		this.batch.begin();
		this.batch.setProjectionMatrix(ColonyGame.camera.combined);

        Profiler.begin("ServerPlayer: Rendering Terrain"); //Start the profiler.
        renderMap(); //Render the map.
        batch.setColor(Color.WHITE); //Set the color back to white.
        Profiler.end(); //End the profiler.

        Profiler.begin("ServerPlayer: Updating Entities");

        ListHolder.update(delta);
        ListHolder.updateFloatingTexts(delta, batch);

        Profiler.end();

        this.batch.end();

        //Draw the grid squares if enabled.
        if(drawGrid) {
            this.grid.debugDraw();
            drawBox2DDebug();
        }

	}

    //Renders the map
    private void renderMap(){
        WorldGen.TerrainTile[][] map = WorldGen.map;

        float squareSize = ColonyGame.worldGrid.getSquareSize();
        int halfWidth = (int)((ColonyGame.camera.viewportWidth*ColonyGame.camera.zoom)/2f);
        int halfHeight = (int)((ColonyGame.camera.viewportHeight*ColonyGame.camera.zoom)/2f);
        int xc = (int)ColonyGame.camera.position.x;
        int yc = (int)ColonyGame.camera.position.y;

        int startX = ((xc - halfWidth)/squareSize) - off >= 0 ? (int)((xc - halfWidth)/squareSize) - off : 0;
        int endX = ((xc + halfWidth)/squareSize) + off < ColonyGame.worldGrid.getNumCols() ? (int)((xc + halfWidth)/squareSize) + off : ColonyGame.worldGrid.getNumCols()-1;
        int startY = ((yc - halfHeight)/squareSize) - off >= 0 ? (int)((yc - halfHeight)/squareSize) - off : 0;
        int endY = ((yc + halfHeight)/squareSize) + off < ColonyGame.worldGrid.getNumRows() ? (int)((yc + halfHeight)/squareSize) + off : ColonyGame.worldGrid.getNumRows()-1;

        //Loop over the array
        for(int x=startX;x<=endX;x++) {
            for (int y = startY; y <= endY; y++) {
                WorldGen.TerrainTile tile = map[x][y];
                tile.changeVisibility(WorldGen.getInstance().getVisibilityMap()[x][y].getVisibility());
                tile.terrainSprite.draw(batch);
            }
        }
    }

    //Draws the box2D debug.
    private void drawBox2DDebug(){
        //Draw the box2d debug
        this.batch.begin();
        ColonyGame.debugRenderer.render(ColonyGame.world, ColonyGame.camera.combined);
        this.batch.end();
    }

    public boolean getPaused(){
        return this.paused;
    }

    public void setPaused(boolean paused){
        this.paused = paused;
    }

	private void generateStart(Vector2 start){
		ColonyEntity colonyEnt = new ColonyEntity(start, 0, new TextureRegion(ColonyGame.assetManager.get("Colony", Texture.class)), this.batch, 11);
        Colony colony = colonyEnt.getComponent(Colony.class);

        for(int i=0;i<10;i++) {
            TextureAtlas atlas = ColonyGame.assetManager.get("interactables", TextureAtlas.class);
            new AnimalEnt(start, 0, atlas.findRegion("squirrel"), this.batch, 11);
        }

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

		this.grid.perform(destroyNearbyResources);
		this.grid.perform(detectNearbyResources);
	}

	private void initPlayer(){
		new PlayerInterface(ColonyGame.batch, this.game, this, ColonyGame.world);
	}
}
