package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.helpers.*;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.managers.ScriptManager;
import com.mygdx.game.screens.PreLoadingScreen;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColonyGame extends Game {
	public static boolean server = true;
	public static boolean singlePlayer = true;
	public static OrthographicCamera camera;
	public static OrthographicCamera UICamera;
	public static Grid.GridInstance worldGrid;

	public static SpriteBatch batch;
	public static ShapeRenderer renderer;
	public static World world;
	public static Box2DDebugRenderer debugRenderer;
    public static ExecutorService threadPool;

    public static EasyAssetManager assetManager;

	private Color screenColor = new Color(0, 0, 0, 1);

	@Override
	public void create () {
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		ColonyGame.UICamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		world = new World(new Vector2(0,0), true);
        ColonyGame.assetManager = new EasyAssetManager();
		ColonyGame.debugRenderer = new Box2DDebugRenderer(); //Create the Box2D world.

		world.setContactListener(new Collision());

        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		ScriptManager.load("./scripts");

		this.setScreen(new PreLoadingScreen(this));
	}

	@Override
	public void render () {
		try {
			super.render();
			Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			//Update the profile and GUI.
			float delta = Gdx.graphics.getDeltaTime();
			updateVarious(delta);

			ColonyGame.batch.begin();
			updateEntities(delta);
			updateGUI(delta);
			ListHolder.updateTimers(delta);
			ColonyGame.batch.end();

			//Update the camera.
			camera.update();
		}catch(Exception e){
			e.printStackTrace();
			threadPool.shutdownNow();
			Gdx.app.exit();
		}

    }

	private void updateVarious(float delta){
		Profiler.update(delta);
		GUI.checkState();
	}

	private void updateEntities(float delta){
		batch.setProjectionMatrix(ColonyGame.camera.combined);
		Profiler.begin("ServerPlayer: Rendering Terrain"); //Start the profiler.
		this.renderMap(); //Render the map.
		batch.setColor(Color.WHITE); //Set the color back to white.
		Profiler.end(); //End the profiler.
		Profiler.begin("ServerPlayer: Updating Entities");
		ListHolder.update(delta);
		ListHolder.updateFloatingTexts(delta, batch);
		EventSystem.notifyGameEvent("update", delta);
		Profiler.end();

		//Step the Box2D simulation.
		ColonyGame.world.step(1f / 60f, 8, 3);
	}

	private void updateGUI(float delta){
		//Draw GUI stuff.
		batch.setProjectionMatrix(UICamera.combined);
		ListHolder.updateGUI(delta, batch);
	}

	//Renders the map
	private void renderMap(){
		if(worldGrid == null) return;

		int off = 5;

		float squareSize = ColonyGame.worldGrid.getSquareSize();
		int halfWidth = (int)((ColonyGame.camera.viewportWidth*ColonyGame.camera.zoom)/2f);
		int halfHeight = (int)((ColonyGame.camera.viewportHeight*ColonyGame.camera.zoom)/2f);
		int xc = (int)ColonyGame.camera.position.x;
		int yc = (int)ColonyGame.camera.position.y;

		int startX = ((xc - halfWidth)/squareSize) - off >= 0 ? (int)((xc - halfWidth)/squareSize) - off : 0;
		int endX = ((xc + halfWidth)/squareSize) + off < ColonyGame.worldGrid.getWidth() ? (int)((xc + halfWidth)/squareSize) + off : ColonyGame.worldGrid.getWidth()-1;
		int startY = ((yc - halfHeight)/squareSize) - off >= 0 ? (int)((yc - halfHeight)/squareSize) - off : 0;
		int endY = ((yc + halfHeight)/squareSize) + off < ColonyGame.worldGrid.getHeight() ? (int)((yc + halfHeight)/squareSize) + off : ColonyGame.worldGrid.getHeight()-1;

		//Loop over the array
		for(int x=startX;x<=endX;x++) {
			for (int y = startY; y <= endY; y++) {
				Grid.TerrainTile tile = worldGrid.getNode(x, y).getTerrainTile();
				if(tile == null) continue;
				tile.changeVisibility(worldGrid.getVisibilityMap()[x][y].getVisibility());
				tile.terrainSprite.draw(batch);
			}
		}
	}
}
