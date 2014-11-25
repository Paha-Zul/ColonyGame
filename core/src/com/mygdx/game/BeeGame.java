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

public class BeeGame extends Game {
	public static boolean server = false;
	public static boolean singlePlayer;

	ServerPlayer serverPlayer;
	ClientPlayer clientPlayer;

	@Override
	public void create () {
		if(server)
			serverPlayer = new ServerPlayer();
		else
			clientPlayer = new ClientPlayer();
	}

	@Override
	public void render () {
		float delta = Gdx.graphics.getDeltaTime();

		if(server)
			serverPlayer.render(delta);
		else
			clientPlayer.render(delta);
	}
}
