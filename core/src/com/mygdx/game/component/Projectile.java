package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.EventSystem;
import com.mygdx.game.helpers.timer.OneShotTimer;
import com.mygdx.game.helpers.timer.Timer;

/**
 * Created by Bbent_000 on 11/29/2014.
 */
public class Projectile extends Component{
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

		this.owner.transform.setScale(0.25f);

		this.coll = this.getComponent(Collider.class);
		this.lifetimeTimer = new OneShotTimer(this.lifetime, this.owner::setToDestroy);

		EventSystem.registerEntityEvent(this.owner, "collidestart", args -> {
			Entity other = (Entity) args[0]; //Get the other entity.
			if (other.hasTag(Constants.ENTITY_ANIMAL)) { //If the other Entity is a projectile, kill both of us!
				this.owner.setToDestroy();
				other.getComponent(Stats.class).getStat("health").addToCurrent(-50);
			}
		});
	}

	@Override
	public void update(float delta) {
		super.update(delta);

		this.lifetimeTimer.update(delta);

		if(this.owner != null) {
			float rot = this.owner.transform.getRotation();
			float x = MathUtils.cosDeg(rot) * speed * delta;
			float y = MathUtils.sinDeg(rot) * speed * delta;
			coll.body.setLinearVelocity(x, y);
		}
	}
}
