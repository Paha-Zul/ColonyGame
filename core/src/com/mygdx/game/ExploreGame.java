package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.bees.Bee;
import com.mygdx.game.client.ClientPlayer;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.server.Server;
import com.mygdx.game.server.ServerPlayer;
import sun.security.pkcs11.wrapper.Functions;

import java.util.ArrayList;

public class ExploreGame extends Game {
	public static boolean server = true;
	public static boolean singlePlayer = true;


	public static SpriteBatch batch;
	public static ShapeRenderer renderer;

	@Override
	public void create () {
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();

		this.setScreen(new GameScreen());
	}

	@Override
	public void render () {
		super.render();

	}
}
