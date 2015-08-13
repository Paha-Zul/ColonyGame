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
import com.mygdx.game.screens.PreLoadingScreen;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.util.*;
import com.mygdx.game.util.gui.GUI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColonyGame extends Game {
	public static boolean server = true;
	public static boolean singlePlayer = true;
	public static OrthographicCamera camera;
	public static OrthographicCamera UICamera;
	public static Grid.GridInstance worldGrid;
	public static long currTick = 0;

	public static SpriteBatch batch;
	public static ShapeRenderer renderer;
	public static World world;
	public static Box2DDebugRenderer debugRenderer;
    public static ExecutorService threadPool;

    public static boolean closed = false;

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
		ColonyGame.debugRenderer = new Box2DDebugRenderer(); //Create the Box2D saveContainer.

		world.setContactListener(new Collision());

        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		this.setScreen(new PreLoadingScreen(this));
	}

	@Override
	public void render () {
		ColonyGame.currTick++;

		try {
            Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			super.render();

			//Update the profile and GUI.
			float delta = Gdx.graphics.getDeltaTime();
			updateVarious(delta);

			if(PlayerInterface.active){
				delta*=PlayerInterface.getInstance().gameSpeed;
				if(PlayerInterface.getInstance().paused) delta = 0;
			}

			ColonyGame.batch.begin();
			updateGUI(delta);
			ListHolder.updateTimers(delta);
			ColonyGame.batch.end();

			//Update the camera.
			camera.update();
		}catch(Exception e){
			e.printStackTrace();
            e.printStackTrace(Logger.getPrintWriter());
            GH.writeErrorMessage(e);
            this.dispose();
            Gdx.app.exit();
		}

    }

	private void updateVarious(float delta){
		Profiler.update(delta);
		GUI.update();
	}

	private void updateGUI(float delta){
		//Draw GUI stuff.
		batch.setProjectionMatrix(UICamera.combined);
		ListHolder.updateGUI(delta, batch);
        EventSystem.notifyGameEvent("render_GUI", delta, batch);
    }

    @Override
    public void dispose() {
        super.dispose();

        closed = true;
        threadPool.shutdownNow();
        threadPool = null;
        assetManager = null;
        Logger.log(Logger.ERROR, "Game closing due to error...");
        Logger.close();
    }
}
