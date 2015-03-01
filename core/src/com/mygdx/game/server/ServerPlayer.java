package com.mygdx.game.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    public static boolean drawGrid = false;

	public static String[] names = {"Bobby","Sally","Jimmy","Bradley","Willy","Tommy","Brian","Doug","Ben","Jacob","Sammy","Jason","David","Sarah","Betty","Tom","James"};

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

		generateStart(new Vector2(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2));

		initPlayer();
    }

	public void render(float delta){
        if(paused)
            delta = 0f;

		Profiler.begin("ServerPlayer Render");

		Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//Step the Box2D simulation.
		ColonyGame.world.step(delta, 8, 3);

		batch.begin();
		this.batch.setProjectionMatrix(ColonyGame.camera.combined);

		WorldGen.TerrainTile[][] map = WorldGen.map;
		float halfTileSize = ((float)WorldGen.getInstance().tileSize+20)/2f;

        Vector3[] points = ColonyGame.camera.frustum.planePoints;

        Profiler.begin("RenderingWorld");

		//Loop over the array
        for(int x=0;x<map.length;x++) {
            for (int y = 0; y < map[0].length; y++) {
                WorldGen.TerrainTile tile = map[x][y];
                if(!ColonyGame.camera.frustum.boundsInFrustum(tile.terrainSprite.getX(), tile.terrainSprite.getY(), 0, halfTileSize, halfTileSize, 0))
                    continue;

                tile.changeVisibility(WorldGen.getInstance().getVisibilityMap()[x][y].getVisibility());
                tile.terrainSprite.draw(batch);
            }
        }

        //Set the color back to white.
        batch.setColor(Color.WHITE);

        Profiler.begin("UpdateEntities");
        ListHolder.update(delta);
        Profiler.end();

        ListHolder.updateFloatingTexts(delta, batch);

        Profiler.end();

        Profiler.begin("Box2DDebug");
        this.batch.end();

        drawBox2DDebug();
        //Draw the grid squares if enabled.
        if(drawGrid)
		    this.grid.debugDraw();

        Profiler.end();
		Profiler.end();
	}

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
		ColonyEntity colonyEnt = new ColonyEntity(start, 0, new Texture("img/colony.png"), this.batch, 11);
        for(int i=0;i<10;i++)
            new AnimalEnt(start, 0, ColonyGame.assetManager.get("animal2", Texture.class), this.batch, 11);
		Colony colony = colonyEnt.getComponent(Colony.class);

		int radius = 8;
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
                    if(Math.abs(node.getCol() - index[0]) + Math.abs(node.getRow() - index[1]) >= radius*1.5) continue;

					for(Entity ent : new ArrayList<>(node.getEntityList())){
						if(ent.hasTag(Constants.ENTITY_RESOURCE))
							ent.destroy();
					}

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
		new PlayerInterface(ColonyGame.batch, this.game, this, ColonyGame.world);
	}

}
