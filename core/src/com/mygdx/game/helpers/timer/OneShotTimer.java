package com.mygdx.game.helpers.timer;

import com.mygdx.game.interfaces.Functional;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class OneShotTimer extends Timer{

	/**
	 * Creates a OneShot timer that will only run once until restart() is called.
	 * @param length The length of the Timer.
	 * @param callback The Callback to call when the Timer completes.
	 */
	public OneShotTimer(double length, Functional.Callback callback) {
		super(length, callback);
	}

	@Override
	public void update(float delta) {
		if (!expired) {
			if (this.currCounter >= this.length) {
				this.currCounter -= this.length;
				this.finish();
			}

			this.currCounter += delta;
		}
	}

	@Override
	protected void finish() {
		super.finish();
		this.expired = true;
	}

}