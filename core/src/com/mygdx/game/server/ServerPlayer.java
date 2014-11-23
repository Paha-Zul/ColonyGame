package com.mygdx.game.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.Grid;
import com.mygdx.game.bees.Bee;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 11/23/2014.
 */
public class ServerPlayer {
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;
	Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);

	static ArrayList<Bee> beeList = new ArrayList<Bee>();
	static ArrayList<Bee> queenList = new ArrayList<Bee>();

	public ServerPlayer(){
		//Start the server
		Server.start(1337);
		Grid.NewGrid(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 25);

		//Make a new spritebatch and shaperenderer.
		batch = new SpriteBatch();
		this.shapeRenderer = new ShapeRenderer();

		//Create 10 queens.
		for(int i=0;i<10;i++) {
			Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
			Bee bee = new Bee(Constants.QUEEN, color, 0, MathUtils.random()*Gdx.graphics.getWidth(), MathUtils.random()*Gdx.graphics.getHeight(), 20, 20, 30, null, 100, 256);
			bee.queenRef = bee;
			this.queenList.add(bee);
		}

		//Add random food around the map.
		Functional.Perform<Grid.Cell> perform = cell -> { if(Math.random() < 0.25) cell.food = (float)(10 + Math.random()*90);};
		Grid.activeGrid.iterate(perform);
	}

	public void render(float delta){
		Gdx.gl.glClearColor(screenColor.r, screenColor.g, screenColor.b, screenColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.debug();

		batch.begin();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		for(int i=0;i<beeList.size();i++){
			Bee bee = beeList.get(i);
			if(bee.isDead()){
				bee.kill();
				beeList.remove(i);
				continue;
			}
			bee.update(Gdx.graphics.getDeltaTime());
			bee.render(this.batch, this.shapeRenderer);
		}

		for(int i=0;i < queenList.size();i++){
			Bee queen = queenList.get(i);
			if(queen.isDead()){
				queen.kill();
				queenList.remove(i);
				continue;
			}
			queen.update(Gdx.graphics.getDeltaTime());
			queen.render(this.batch, this.shapeRenderer);
		}

		shapeRenderer.end();
		batch.end();
	}

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

	public static void AddBee(Bee bee){
		if(bee.caste == Constants.QUEEN)
			queenList.add(bee);
		else
			beeList.add(bee);
	}
}
