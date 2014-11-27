package com.mygdx.game.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Grid;
import com.mygdx.game.bees.Bee;
import com.mygdx.game.component.GraphicIdentity;
import com.mygdx.game.component.Move;
import com.mygdx.game.component.SetDestination;
import com.mygdx.game.component.SquareGraphic;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 11/23/2014.
 */
public class ServerPlayer {
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;
	Color screenColor = new Color(163f/255f, 154f/255f, 124f/255f, 1);

	public ServerPlayer(){
		//Start the server
		Server.start(1337);
		Grid.NewGrid(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 25);

		//Make a new spritebatch and shaperenderer.
		batch = new SpriteBatch();
		this.shapeRenderer = new ShapeRenderer();

		for(int i=0;i<1;i++) {

			GraphicIdentity identity = new GraphicIdentity(new Texture("img/bar.png"), batch);
			GraphicIdentity turretText = new GraphicIdentity(new Texture("img/turret.png"), batch);
			GraphicIdentity turretText1 = new GraphicIdentity(new Texture("img/turret.png"), batch);
			GraphicIdentity turretText2 = new GraphicIdentity(new Texture("img/turret.png"), batch);

			Move move =  new Move(true);
			move.rotateSpeed = 10;

			Entity ent = new Entity("Queen", new Vector2(200,200), 0, identity, move);
			ent.transform.setScale(5f);

			Move turretMove = new Move(true);
			turretMove.rotateSpeed = -25;

			Vector2 pos1 = new Vector2(300,300);
			Entity turret = new Entity("Turret", pos1, 90, turretText, turretMove);
			turret.transform.setScale(.5f);

			Move turretMove2 = new Move(true);
			turretMove2.rotateSpeed = 0;

			Vector2 pos2 = new Vector2(300,100);
			Entity turret1 = new Entity("Turret1", pos2, 90, turretText1, turretMove2);
			turret1.transform.setScale(.5f);

			Move turretMove3 = new Move(true);
			turretMove3.rotateSpeed = 25;

			Vector2 pos3 = new Vector2(100,100);
			Entity turret2 = new Entity("Turret2", pos3, 90, turretText2, turretMove3);
			turret2.transform.setScale(.5f);

			ent.transform.addChild(turret);
			ent.transform.addChild(turret1);
			ent.transform.addChild(turret2);

			ListHolder.addEntity(0, ent);
			ListHolder.addEntity(2, turret);
			ListHolder.addEntity(2, turret1);
			ListHolder.addEntity(2, turret2);
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

		ListHolder.update(Gdx.graphics.getDeltaTime());

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
}
