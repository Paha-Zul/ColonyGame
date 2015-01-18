package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.timer.RepeatingTimer;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class Move extends Component{
	public float rotateSpeed = 100;
	public float moveSpeed = 50;

	private Vector2 dest = new Vector2(0,0);
	private Collider collider;

	public Move() {
		super();
	}
	public Move(float moveSpeed) {
		super();

		this.moveSpeed = moveSpeed;
	}

	@Override
	public void start() {
		super.start();

		this.collider = this.owner.getComponent(Collider.class);
	}

	@Override
	public void update(float delta) {
		super.update(delta);

		this.collider.body.setLinearVelocity(moveSpeed, 0);
	}

	public void setDest(float x, float y){
		dest.x = x;
		dest.y = y;
	}

	public void setDest(Vector2 newDes){
		this.setDest(newDes.x, newDes.y);
	}
}
