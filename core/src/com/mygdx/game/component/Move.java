package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.helpers.timer.RepeatingTimer;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class Move extends Component{
	public float moveSpeed;
	public float threshold = 0.2f;

	private Vector2 dest = new Vector2(0,0);

	public Move(String name, int type, boolean active) {
		super(name, type, active);
	}

	@Override
	public void start() {
		super.start();

	}

	@Override
	public void update(float delta) {
		super.update(delta);

		Vector2 pos = owner.transform.getWorldPosition();

		//If we are not within the movement threshold, move!
		if(Math.abs(pos.x - dest.x) > threshold && Math.abs(pos.y - dest.y) > threshold){
			float rotation = MathUtils.atan2(this.dest.y - pos.y, this.dest.x - pos.x);
			float x = MathUtils.cos(rotation)*this.moveSpeed*delta;
			float y = MathUtils.sin(rotation)*this.moveSpeed*delta;
			this.owner.transform.translate(x, y);
		}
	}

	public void setDest(float x, float y){
		dest.x = x;
		dest.y = y;
	}

	public void setDest(Vector2 newDes){
		this.setDest(newDes.x, newDes.y);
	}
}
