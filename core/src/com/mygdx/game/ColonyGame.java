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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.ISaveable;
import com.mygdx.game.screens.PreLoadingScreen;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.util.*;
import com.mygdx.game.util.gui.GUI;
import com.mygdx.game.util.managers.EventSystem;
import com.mygdx.game.util.managers.NotificationManager;
import com.mygdx.game.util.managers.PlayerManager;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColonyGame extends Game implements ISaveable{
	public static ColonyGame instance;
	public boolean server = true;
	public boolean singlePlayer = true;
	public OrthographicCamera camera;
	public OrthographicCamera UICamera;
	public Grid.GridInstance worldGrid;
	public ListHolder listHolder;
	public long currTick = 0;
	@JsonProperty
	public PlayerManager playerManager;
	public NotificationManager notificationManager;
	public SpriteBatch batch;
	public ShapeRenderer renderer;
	public World world;
	public Box2DDebugRenderer debugRenderer;
    public ExecutorService threadPool;
    public boolean closed = false;
    public EasyAssetManager assetManager;
	private Color screenColor = new Color(0, 0, 0, 1);

	@Override
	public void create () {
		this.batch = new SpriteBatch();
		this.renderer = new ShapeRenderer();
		this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.UICamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.world = new World(new Vector2(0,0), true);
		this.assetManager = new EasyAssetManager();
		this.debugRenderer = new Box2DDebugRenderer(); //Create the Box2D saveContainer.
		this.playerManager = new PlayerManager();
		this.listHolder = new ListHolder();
		this.notificationManager = new NotificationManager();
		ColonyGame.instance.instance = this;

		world.setContactListener(new Collision());
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		listHolder.init();

		this.setScreen(new PreLoadingScreen(this));
	}

	@Override
	public void addedLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

	}
@Override
	public void save() {

	}@Override
	public void render () {
		this.currTick++;

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

			this.batch.begin();
			updateGUI(delta);
			listHolder.updateTimers(delta);
			this.batch.end();

			//Update the camera.
			camera.update();
		}catch(Exception e){
			e.printStackTrace();
			Logger.log(Logger.ERROR, "Game closing due to error");
			Logger.close();
            e.printStackTrace(Logger.getPrintWriter());
            GH.writeErrorMessage(e);
            this.dispose();
            Gdx.app.exit();
		}

    }

	@Override
	public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
		playerManager.initLoad(entityMap, compMap);
		notificationManager.initLoad(entityMap, compMap);
	}
@Override
	public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
		playerManager.load(entityMap, compMap);
		notificationManager.load(entityMap, compMap);
	}private void updateVarious(float delta){
		Profiler.update(delta);
		GUI.update();
	}

		private void updateGUI(float delta){
		//Draw GUI stuff.
		batch.setProjectionMatrix(UICamera.combined);
		listHolder.updateGUI(delta, batch);
        EventSystem.notifyGameEvent("render_GUI", delta, batch);
    }

	    @Override
    public void dispose() {
        super.dispose();

        closed = true;
        threadPool.shutdownNow();
        threadPool = null;
        assetManager = null;
        Logger.log(Logger.ERROR, "Game closing normally");
        Logger.close();
    }








}
