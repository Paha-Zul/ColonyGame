package com.mygdx.game.bees;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.mygdx.game.CircularArray;
import com.mygdx.game.interfaces.IRenderable;
import com.mygdx.game.interfaces.IUpdateable;

/**
 * Created by Bbent_000 on 11/22/2014.
 */
public class SimpleBee implements IUpdateable, IRenderable {
	public int size=5;
	public double id;

	private CircularArray<Position> circArray = new CircularArray(5);
	private float counter;

	public SimpleBee(Vector2 loc, int size, double id){
		circArray.push(new Position(System.currentTimeMillis(), loc));
		this.size = size;
		this.id = id;
	}

	public void setLocation(double currTime, Vector2 loc){
		circArray.push(new Position(currTime, loc));
	}

	@Override
	public void update(float deltaTime) {
		counter+=deltaTime;
	}

	@Override
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		Position first = circArray.get(0);
		Position second = circArray.get(1);

		float timePercent = (float)((counter*1000)/(second.time - first.time));

		float x = MathUtils.lerp(first.pos.x, second.pos.x, timePercent);
		float y = MathUtils.lerp(first.pos.y, second.pos.y, timePercent);

		shapeRenderer.rect(x-size/2, y-size/2, size, size);
	}

	public class Position{
		double time;
		Vector2 pos;

		public Position(double time, Vector2 pos){
			this.time = time;
			this.pos = pos;
		}
	}
}
