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
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.server.Server;
import sun.security.pkcs11.wrapper.Functions;

import java.util.ArrayList;

public class BeeGame extends Game {
	SpriteBatch batch;
	Texture img;
	ShapeRenderer shapeRenderer;
	Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);

	public static final int QUEEN = 0;
	public static final int LARVA = 1;
	public static final int WORKER = 2;

	static ArrayList<Bee> beeList = new ArrayList<Bee>();
	static ArrayList<Bee> queenList = new ArrayList<Bee>();

	public static Grid grid;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		this.shapeRenderer = new ShapeRenderer();

		Server.start(1337);

		grid = new Grid(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 25);

		for(int i=0;i<10;i++) {
			Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
			Bee bee = new Bee(QUEEN, color, 0, MathUtils.random()*Gdx.graphics.getWidth(), MathUtils.random()*Gdx.graphics.getHeight(), 20, 20, 30, null, 100, 256);
			bee.queenRef = bee;
			this.queenList.add(bee);
		}

		Functional.Perform<Grid.Cell> perform = cell -> { if(Math.random() < 0.25) cell.food = (float)(10 + Math.random()*90);};
		grid.iterate(perform);
	}

	@Override
	public void render () {
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

	public static void AddBee(Bee bee){
		if(bee.caste == QUEEN)
			queenList.add(bee);
		else
			beeList.add(bee);
	}

	public void debug(){
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		Functional.Perform<Grid.Cell> perform = cell -> {
			shapeRenderer.setColor(1f, 0, 0, cell.food/100f);
			shapeRenderer.rect(cell.getCol()*grid.squareSize, cell.getRow()*grid.squareSize, grid.squareSize, grid.squareSize);
			shapeRenderer.setColor(0, 1f, 0, cell.trail/10f);
			shapeRenderer.arc((float)cell.getCol()*grid.squareSize + grid.squareSize/2, (float)cell.getRow()*grid.squareSize + grid.squareSize/2, (float)grid.squareSize/2, 0, 360);
		};
		grid.iterate(perform);

		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

}
