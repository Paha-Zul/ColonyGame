package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.interfaces.Functional;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class SetDestination extends Component{
	public float moveDis = 100;

	private RepeatingTimer timer;

	public SetDestination() {
		super();
	}

	@Override
	public void start() {
		super.start();

		Vector2 pos = this.ownerID.transform.getPosition();

		Functional.Callback callback = () ->{
			float x = pos.x + (MathUtils.random()*moveDis - moveDis/2f);
			float y = pos.y + (MathUtils.random()*moveDis - moveDis/2f);
			if(x < 0 || y < 0 || x >= 800 || y >= 800)
				return;

		};

		this.timer = new RepeatingTimer(0.2, callback);
	}

	@Override
	public void update(float delta) {
		super.update(delta);

		this.timer.update(delta);
	}
}
