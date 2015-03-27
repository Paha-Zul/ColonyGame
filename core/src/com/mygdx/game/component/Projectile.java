package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.helpers.timer.OneShotTimer;
import com.mygdx.game.helpers.timer.Timer;

/**
 * Created by Bbent_000 on 11/29/2014.
 */
public class Projectile extends Component{
	public float speed = 20;
	public float lifetime = 3;

	private Timer lifetimeTimer;

	public Projectile() {
		super();
	}

	@Override
	public void start() {
		super.start();

		this.lifetimeTimer = new OneShotTimer(this.lifetime, this.owner::setToDestroy);
	}

	@Override
	public void update(float delta) {
		super.update(delta);

		this.lifetimeTimer.update(delta);

		if(this.owner != null) {
			float rot = this.owner.transform.getRotation();
			float x = MathUtils.cosDeg(rot) * speed * delta;
			float y = MathUtils.sinDeg(rot) * speed * delta;
			this.owner.transform.translate(x, y);
		}

	}
}
