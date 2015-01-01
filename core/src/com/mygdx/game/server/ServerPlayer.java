package com.mygdx.game.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.Grid;
import com.mygdx.game.component.*;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.entity.TurretEnt;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.helpers.WorldGen;
import com.mygdx.game.helpers.timer.OneShotTimer;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 * Created by Bbent_000 on 11/23/2014.
 */
public class ServerPlayer {
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;

	OneShotTimer testTimer;
	Entity test;
	boolean up = true;

	private Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);


	public ServerPlayer(SpriteBatch batch, ShapeRenderer renderer){
		//Start the server
		Server.start(1337);
		Grid.NewGrid("spatial",Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 25, true);

		//Make a new spritebatch and shaperenderer.
		this.batch = batch;
		this.shapeRenderer = renderer;

		generateTest();

		initPlayer();

		Grid.NewGrid("terrain", Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 25);


	}

	public void render(float delta){
		Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if(this.testTimer != null && this.test != null) {
			this.testTimer.update(delta);

			if (up)
				this.test.transform.setScale(test.transform.getScale() + delta);
			else
				this.test.transform.setScale(test.transform.getScale() + -delta);

			if (this.test.transform.getScale() > 3)
				up = false;
			if (this.test.transform.getScale() < 0.5)
				up = true;
		}

		//this.debug();

		batch.begin();

		WorldGen.TerrainTile[][] map = WorldGen.map;
		//Loop over the array
		for(int x=0;x<map.length;x++) {
			for (int y = 0; y < map[0].length; y++) {
				WorldGen.TerrainTile tile = map[x][y];
				batch.draw(tile.image, tile.position.x, tile.position.y, WorldGen.tileSize / 2f, WorldGen.tileSize / 2f, WorldGen.tileSize, WorldGen.tileSize, 1, 1, tile.rotation, 0, 0, WorldGen.tileSize, WorldGen.tileSize, false, false);
				//batch.draw(tile.image, tile.position.x, tile.position.y);
			}
		}

		batch.end();
	}

	private void generateTest(){
		GraphicIdentity identity = new GraphicIdentity(new Texture("img/bar.png"), batch);
		BehaviourManager behaviours = new BehaviourManager();

		Move move =  new Move();
		move.rotateSpeed = 50;

		Entity ent = new Entity(new Vector2(200,200), 0, 10, identity, move, behaviours);
		ent.name = "Square";
		test = ent;

		Vector2 pos1 = new Vector2(225,225);
		Entity turret = new TurretEnt(pos1, 90, new Texture("img/turret.png"), batch, 11);
		turret.getComponent(Move.class).rotateSpeed = 0;
		turret.name = "Turret1";

//			Vector2 pos2 = new Vector2(175,225);
//			Entity turret2 = new TurretEnt(pos2, 90, new Texture("img/turret.png"), batch, 11);
//			turret2.getComponent(Move.class).rotateSpeed = 25;
//			turret2.name = "Turret2";
//
//			Vector2 pos3 = new Vector2(225,175);
//			Entity turret3 = new TurretEnt(pos3, 90, new Texture("img/turret.png"), batch, 11);
//			turret3.getComponent(Move.class).rotateSpeed = -25;
//			turret3.name = "Turret3";

		ent.transform.addChild(turret);
//			ent.transform.addChild(turret2);
//			ent.transform.addChild(turret3);

		Vector2 pos4 = new Vector2(400,200);
		Entity enemy = new Entity(pos4, 90, new Texture("img/turret.png"), batch, 11);
		Turret.addEnemy(enemy);

		ListHolder.addEntity(2, enemy);

		this.testTimer = new OneShotTimer(10f, ()->{
			System.out.println("Destroyed");
			enemy.destroy();
		});
	}

	private void initPlayer(){
		PlayerInterface UI = new PlayerInterface(ColonyGame.batch);
		Entity playerObj = new Entity(new Vector2(0,0), 0, 20, UI);
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
