package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.timer.OneShotTimer;
import com.mygdx.game.util.timer.Timer;

/**
 * Created by Bbent_000 on 11/29/2014.
 */
public class Projectile extends Component{
	public Entity projOwner;

	public float speed = 1500;
	public float lifetime = 3;

	private Timer lifetimeTimer;
	private Collider coll;

	public Projectile() {
		super();
	}

	@Override
	public void start() {
		super.start();

		this.owner.getTransform().setScale(0.25f);

		this.coll = this.getComponent(Collider.class);
		this.lifetimeTimer = new OneShotTimer(this.lifetime, this.owner::setToDestroy);
	}

	@Override
	public void update(float delta) {
		super.update(delta);

		this.lifetimeTimer.update(delta);

		if(this.owner != null) {
			float rot = this.owner.getTransform().getRotation();
			float x = MathUtils.cosDeg(rot) * speed * delta;
			float y = MathUtils.sinDeg(rot) * speed * delta;
			coll.body.setLinearVelocity(x, y);
		}
	}
}
