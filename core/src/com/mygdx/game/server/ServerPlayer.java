package com.mygdx.game.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.Grid;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.component.ui.PlayerInterface;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.helpers.timer.OneShotTimer;

/**
 * Created by Bbent_000 on 11/23/2014.
 */
public class ServerPlayer {
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;

	private OneShotTimer testTimer;
	private Entity test;
	boolean up = true;

	//Box2d stuff

	private Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);

	public ServerPlayer(SpriteBatch batch, ShapeRenderer renderer){
		//Start the server
		Server.start(1337);
		Grid.NewGrid("spatial",Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 25, true);

		//Make a new spritebatch and shaperenderer.
		this.batch = batch;
		this.shapeRenderer = renderer;

		//Create the Box2D world.
		ColonyGame.debugRenderer = new Box2DDebugRenderer();

		generateTest(new Vector2(800,500));
		generateTest(new Vector2(810,500));

		initPlayer();

		Grid.NewGrid("terrain", Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 25);
	}

	public void render(float delta){
		Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//Step the Box2D simulation.
		ColonyGame.world.step(delta, 6, 2);

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

		ColonyGame.debugRenderer.render(ColonyGame.world, ColonyGame.camera.combined);
		batch.end();
	}

	private void generateTest(Vector2 position){
		GraphicIdentity graphic = new GraphicIdentity(new Texture("img/BlackSquare.png"), ColonyGame.batch);
		CircleShape shape = new CircleShape();
		shape.setRadius(10f);

		Collider collider = new Collider(ColonyGame.world, shape);

		collider.body.setType(BodyDef.BodyType.DynamicBody);

		collider.fixture.setFriction(0.5f);
		collider.fixture.setDensity(1f);

		Entity ent2 = new Entity(position, 0, 14, graphic, collider);
	}

	private void initPlayer(){
		PlayerInterface UI = new PlayerInterface(ColonyGame.batch, ColonyGame.world);
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
