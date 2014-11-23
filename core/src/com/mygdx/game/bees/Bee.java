package com.mygdx.game.bees;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.BeeGame;
import com.mygdx.game.GH;
import com.mygdx.game.Grid;
import com.mygdx.game.interfaces.IRenderable;
import com.mygdx.game.interfaces.IUpdateable;

import java.io.Console;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by Bbent_000 on 11/16/2014.
 */
public class Bee implements IUpdateable, IRenderable{
	public int caste;
	public Color color;
	public float age, size, speed, food, health, maxAge;
	public Vector2 loc, dest = new Vector2();
	public Bee queenRef;
	public double id;

	private Grid.Cell currCell;
	private float damage = 10;

	private float setDestTick = 0.5f;
	private float setDestCounter = 0;

	private float spawnLarvaTick = 0.2f;
	private float spawnLarvaCounter = 0;

	private float checkForEnemiesTick = 0.2f;
	private float checkForEnemiesCounter = 0;

	private float dist = 100;

	public Bee(int caste, Color color, float age, float x, float y, float size, float speed, float food, Bee queenRef, float health, float maxAge){
		this.caste = caste;
		this.color = color;
		this.age = age;
		this.loc = new Vector2(x, y);
		this.size = size;
		this.speed = speed;
		this.food = food;
		this.queenRef = queenRef;
		this.health = health;
		this.maxAge = maxAge;

		this.id = MathUtils.random()*Double.MAX_VALUE;
	}

	@Override
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		shapeRenderer.setColor(this.color);
		shapeRenderer.rect(this.loc.x - size/2, this.loc.y - size/2, this.size, this.size);

		if(this.caste == BeeGame.QUEEN) {
			shapeRenderer.end();
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.rect(this.loc.x - size/2, this.loc.y - size/2, this.size, this.size);
			shapeRenderer.end();
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		}
	}

	@Override
	public void update(float deltaTime) {
		this.move(deltaTime);
		this.setDestination(deltaTime);
		this.age(deltaTime);
		this.checkCurrCell();

		if(this.caste != BeeGame.LARVA){
			this.checkForFood(deltaTime);
		}

		if(this.caste == BeeGame.WORKER) {
			this.checkForEnemies(deltaTime);
		}

		if(this.caste == BeeGame.QUEEN) {
			this.spawnLarva(deltaTime);
		}
	}

	public void checkCurrCell(){
		Grid.Cell tmpCell = BeeGame.grid.getCell(this.loc.x, this.loc.y);
		if((this.currCell == null || this.currCell != tmpCell) && tmpCell != null){
			if(this.currCell != null) this.currCell.removeFromCell(this);
			this.currCell = tmpCell;
			this.currCell.addToCell(this);
		}
	}

	public boolean isDead(){
		return health <= 0;
	}

	public void kill(){
		if(this.currCell != null) {
			this.currCell.removeFromCell(this);
			this.currCell.food += this.food;
		}
	}

	public void move(float deltaTime){
		if(this.loc.x - this.dest.x <= 0.2 && this.loc.y - this.dest.y <= 0.2)
			return;

		float rotation = MathUtils.atan2(this.dest.y - this.loc.y, this.dest.x - this.loc.x);
		this.loc.x = this.loc.x + MathUtils.cos(rotation)*this.speed*deltaTime;
		this.loc.y = this.loc.y + MathUtils.sin(rotation)*this.speed*deltaTime;

		if(this.caste == BeeGame.WORKER){
			if(Math.abs(this.loc.x - this.queenRef.loc.x) <= 0.3 && Math.abs(this.loc.y - this.queenRef.loc.y) <= 0.3) {
				this.queenRef.food += this.food;
				this.food = 0;
			}

			//Leave a trail if it has food
			if(this.food > 1)
				this.currCell.trail += this.food;

			//Degrade the trail on this cell.
			this.currCell.trail *= 0.25;

		}
	}

	public void setDestination(float deltaTime){
		this.setDestCounter+=deltaTime;
		if(this.setDestCounter >= this.setDestTick){
			this.setDestCounter -= this.setDestTick;

			//If food is greater than 0 and we are a worker, move to the queen!
			if(this.food > 0 && this.caste == BeeGame.WORKER){
				this.dest.x = this.queenRef.loc.x;
				this.dest.y = this.queenRef.loc.y;
				return;
			//Otherwise, we have no food and we should look for the nearest path.
			}else if(this.caste == BeeGame.WORKER){
				int startX = this.currCell.getCol() - 1;
				int startY = this.currCell.getRow() - 1;
				int endX = this.currCell.getCol() + 1;
				int endY = this.currCell.getRow() + 1;

				for(int x=startX; x<=endX;x++){
					for(int y=startY;y<=endY;y++){
						Grid.Cell tmpCell = BeeGame.grid.getCell(x,y);
						if(tmpCell == null) continue;

						if(tmpCell.trail > 1){
							this.dest.x = x*BeeGame.grid.squareSize + BeeGame.grid.squareSize/2f;
							this.dest.y = y*BeeGame.grid.squareSize + BeeGame.grid.squareSize/2f;
							return;
						}
					}
				}
			}

			//If the above failed, let's try to move randomly!
			if(this.caste == BeeGame.QUEEN || Math.random() > 0.10) {
				this.dest.x = this.loc.x + (this.dist * MathUtils.random() - this.dist * 0.4f);
				this.dest.y = this.loc.y + (this.dist * MathUtils.random() - this.dist * 0.4f);

				if((this.dest.x - this.size/2 < 0 || this.dest.x + this.size/2 > Gdx.graphics.getWidth())
				|| (this.dest.y - this.size/2 < 0 || this.dest.y + this.size/2 > Gdx.graphics.getHeight())){
					this.dest.x = this.loc.x;
					this.dest.y = this.loc.y;
				}
			//Small chance to move towards the queen.
			}else{
				this.dest.x = this.queenRef.loc.x;
				this.dest.y = this.queenRef.loc.y;
			}
		}
	}

	public void age(float deltaTime){
		this.age += deltaTime;
		if(this.age > this.maxAge){
			morph(deltaTime);
		}
	}

	public void morph(float deltaTime){
		if(caste == BeeGame.LARVA){
			this.caste = BeeGame.WORKER;
			this.speed = 50;
			this.size *= 1.4;
			this.age = 0;
			this.maxAge = 100;
			this.setDestTick = 0.1f;
			this.dist = 300;
		}
	}

	public void checkForFood(float deltaTime){
		if(this.currCell == null) return;

		if(this.currCell.food > 0) {
			float amount = deltaTime*2;
			float foodTaken = (this.currCell.food - amount <= 0) ? this.currCell.food : amount;
			this.food += foodTaken;
			this.currCell.food -= foodTaken;
		}
	}

	public void checkForEnemies(float deltaTime){
		if(this.currCell == null) return;

		int startX = this.currCell.getCol() - 1;
		int startY = this.currCell.getRow() - 1;
		int endX = this.currCell.getCol() + 1;
		int endY = this.currCell.getRow() + 1;

		for(int x = startX; x <= endX; x++){
			for(int y=startY; y<= endY; y++){
				Grid.Cell tmpCell = BeeGame.grid.getCell(x,y);
				if(tmpCell == null) continue;

				ArrayList<?> list = tmpCell.getObjectList();
				for(int i=0;i<list.size();i++){
					Bee bee = GH.as(Bee.class, list.get(i));
					if(bee.queenRef != this.queenRef){
						float combinedSize = bee.size/2 + this.size/2;
						if(Math.abs(this.loc.x - bee.loc.x) <= combinedSize && Math.abs(this.loc.y - bee.loc.y) <= combinedSize){
							if(bee.caste == BeeGame.QUEEN){
								bee.health -= this.damage*deltaTime;
								this.food = 2;
							}else{
								bee.health -= this.damage*deltaTime;
								this.food = 2;
							}
							return;
						}
					}
				}
			}
		}
	}

	public void spawnLarva(float deltaTime){
		this.spawnLarvaCounter+=deltaTime;
		if(this.spawnLarvaCounter >= this.spawnLarvaTick) {
			this.spawnLarvaCounter -= this.spawnLarvaTick;

			if(this.food >= 5){
				this.food -= 5;
				BeeGame.AddBee(new Bee(BeeGame.LARVA, this.color, 0, this.loc.x, this.loc.y, this.size/6, this.speed/5, 5, this, 1, 5));
			}
		}
	}
}
