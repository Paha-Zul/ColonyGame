package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.screens.GameScreen;
import com.mygdx.game.screens.LoadingScreen;

public class ColonyGame extends Game {
	public static boolean server = true;
	public static boolean singlePlayer = true;
	public static OrthographicCamera camera;

	public static SpriteBatch batch;
	public static ShapeRenderer renderer;


	private Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);


	@Override
	public void create () {
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		this.setScreen(new LoadingScreen(this));
	}

	@Override
	public void render () {
		super.render();

//		Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.batch.setProjectionMatrix(camera.combined);

		this.batch.begin();

		ListHolder.update(Gdx.graphics.getDeltaTime());

		this.batch.end();

	}
}
