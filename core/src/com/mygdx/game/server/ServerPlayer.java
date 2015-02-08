package com.mygdx.game.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.Colony;
import com.mygdx.game.entity.ColonistEnt;
import com.mygdx.game.entity.ColonyEntity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.ItemManager;
import com.mygdx.game.helpers.Profiler;
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
	private Grid.Node[] path;

    public static boolean drawGrid = false;

	public static String[] names = {"Bobby","Sally","Jimmy","Bradley","Willy","Tommy","Brian","Doug","Ben","Jacob","Sammy","Jason","David","Sarah","Betty","Tom","James"};

	//Box2d stuff

	private Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);

	public ServerPlayer(SpriteBatch batch, ShapeRenderer renderer, ColonyGame game){

		//Start the server
		//Server.start(1337);
		this.grid = game.worldGrid;

		//Make a new spritebatch and shaperenderer.
		this.batch = batch;
		this.shapeRenderer = renderer;
		this.game = game;

		//Create the Box2D world.
		ColonyGame.debugRenderer = new Box2DDebugRenderer();

		generateStart(new Vector2(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2));

		initPlayer();

    }

	public void render(float delta){
		Profiler.begin("ServerPlayer Render");

		Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//Step the Box2D simulation.
		ColonyGame.world.step(delta, 8, 3);

		//this.debug();

		batch.begin();
		this.batch.setProjectionMatrix(ColonyGame.camera.combined);

		WorldGen.TerrainTile[][] map = WorldGen.map;
		float halfTileSize = ((float)WorldGen.tileSize+20)/2f;

		//Loop over the array
        for(int x=0;x<map.length;x++) {
            for (int y = 0; y < map[0].length; y++) {
                WorldGen.TerrainTile tile = map[x][y];
                if(!ColonyGame.camera.frustum.boundsInFrustum(tile.terrainSprite.getX(), tile.terrainSprite.getY(), 0, halfTileSize, halfTileSize, 0))
                    continue;

                tile.terrainSprite.draw(batch);
            }
        }

        batch.setColor(Color.WHITE);

        batch.draw(WorldGen.grayTexture, 0, 0, 200, 200);
        this.batch.end();

		this.batch.begin();
        ColonyGame.debugRenderer.render(ColonyGame.world, ColonyGame.camera.combined);
		this.batch.end();

        //Draw the grid squares if enabled.
        if(drawGrid)
		    this.grid.debugDraw();

		Profiler.end();

	}

	private void drawPath(){
		ShapeRenderer renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(ColonyGame.camera.combined);
		renderer.setColor(Color.BLUE);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		int size = this.game.worldGrid.getSquareSize();
		for(Grid.Node node : path)
			renderer.rect(node.getCol()*size, node.getRow()*size, size, size);

		renderer.end();
	}

	private void generateTest(Vector2 position){
		Texture square = new Texture("img/BlackSquare.png");
		ColonistEnt colonist = new ColonistEnt(position, 0, square, this.batch, 12);
		colonist.name = names[MathUtils.random(names.length-1)];
	}

	private void generateStart(Vector2 start){
		ColonyEntity colonyEnt = new ColonyEntity(start, 0, new Texture("img/colony.png"), this.batch, 11);
		Colony colony = colonyEnt.getComponent(Colony.class);

		Item item = ItemManager.getItemByName("Wood Log");
		item.setCurrStack(10);
		colony.getComponent(Inventory.class).addItem(item);
		item = ItemManager.getItemByName("Stone");
		item.setCurrStack(10);
		colony.getComponent(Inventory.class).addItem(item);

		int radius = 5;
		Functional.Perform<Grid.Node[][]> destroyNearbyResources = (grid) -> {
			int[] index= this.grid.getIndex(colonyEnt.transform.getPosition());
			int startX = index[0]-radius;
			int endX = index[0]+radius;
			int startY = index[1]-radius;
			int endY = index[1]+radius;

			for(int col = startX; col<=endX; col++){
				for(int row = startY; row <= endY; row++){
					Grid.Node node = this.grid.getNode(col, row);
					if(node == null) continue;
					for(Entity ent : new ArrayList<>(node.getEntityList())){
						if(ent.getComponent(Resource.class) != null)
							ent.destroy();
					}
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
						if((resource = ent.getComponent(Resource.class)) != null)
							colony.addNearbyResource(resource);
					}
				}
			}
		};

		this.grid.perform(destroyNearbyResources);
		this.grid.perform(detectNearbyResources);
	}

	private void initPlayer(){
		new PlayerInterface(ColonyGame.batch, this.game, ColonyGame.world);
	}

	/*
	public void debug(){
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		Functional.Perform<Grid.Cell> perform = cell -> {
			shapeRenderer.setColor(1f, 0, 0, cell.food/100f);
			shapeRenderer.rect(cell.getCol()*Grid.activeGrid.squareSize, cell.getRow()*Grid.activeGrid.squareSize, Grid.activeGrid.squareSize, Grid.activeGrid.squareSize);
			shapeRenderer.setColor(0, 1f, 0, cell.trail/10f);
			shapeRenderer.arc((float)cell.getCol()*Grid.activeGrid.squareSize + Grid.activeGrid.squareSize/2, (float)cell.getRow()*Grid.activeGrid.squareSize + Grid.activeGrid.squareSize/2, (float)Grid.activeGrid.squareSize/2, 0, 360);
		};
		Grid.activeGrid.iterate(perform);

		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
	*/




}
