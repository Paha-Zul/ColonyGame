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
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.helpers.Profiler;
import com.mygdx.game.screens.GameScreen;
import com.mygdx.game.screens.LoadingScreen;
import com.mygdx.game.screens.MainMenuScreen;

public class ColonyGame extends Game {
	public static boolean server = true;
	public static boolean singlePlayer = true;
	public static OrthographicCamera camera;
	public static OrthographicCamera UICamera;
	public static Grid worldGrid;

	public static SpriteBatch batch;
	public static ShapeRenderer renderer;
	public static World world;
	public static Box2DDebugRenderer debugRenderer;

	private Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);


	@Override
	public void create () {
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.UICamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		world = new World(new Vector2(0,0), true);

		this.worldGrid = new Grid(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 100);

		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render () {
		super.render();

		Profiler.begin("ColonyGame Render");

		float delta = Gdx.graphics.getDeltaTime();

		camera.update();
		Profiler.update(delta);

//		Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.batch.setProjectionMatrix(camera.combined);

		//Draw everything that is modified by the regular camera.
		this.batch.begin();

		Profiler.begin("UpdateEntities");
		ListHolder.update(delta);
		Profiler.end();

		Profiler.begin("UpdateGUI");
		ListHolder.updateGUI(delta);
		Profiler.end();

		this.batch.end();
		Profiler.end();
	}
}
