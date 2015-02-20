package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.helpers.*;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.screens.MainMenuScreen;
import com.mygdx.game.screens.PreLoadingScreen;

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

    public static EasyAssetManager assetManager;

	private Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);


	@Override
	public void create () {
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		ColonyGame.UICamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		world = new World(new Vector2(0,0), true);
        ColonyGame.assetManager = new EasyAssetManager();

		ColonyGame.worldGrid = Grid.newGridInstance("grid", Constants.GRID_WIDTH, Constants.GRID_HEIGHT, Constants.GRID_SQUARESIZE);

		this.setScreen(new PreLoadingScreen(this));
	}

	@Override
	public void render () {
		super.render();

        GUI.checkState();

		Profiler.begin("ColonyGame Render");

		float delta = Gdx.graphics.getDeltaTime();

		Profiler.update(delta);

//		Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.batch.setProjectionMatrix(camera.combined);

		//Draw everything that is modified by the regular camera.
		this.batch.begin();



		Profiler.begin("UpdateGUI");
		ListHolder.updateGUI(delta);
		Profiler.end();

		this.batch.end();
		Profiler.end();

        camera.update();
    }
}
