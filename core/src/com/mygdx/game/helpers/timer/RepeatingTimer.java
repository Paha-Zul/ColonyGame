package com.mygdx.game.helpers.timer;

import com.mygdx.game.interfaces.Functional;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class RepeatingTimer extends Timer{
	/**
	 * Creates a repeating timer that will continue to repeat and call the callback at each completion.
	 * @param length The length of the timer to run.
	 * @param callback The Callback function to run when completed.
	 */
	public RepeatingTimer(double length, Functional.Callback callback) {
		super(length, callback);
	}

	@Override
	public void update(float delta) {
		if(!expired) {
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
	}
}
