package com.mygdx.game.util.timer;

import com.mygdx.game.interfaces.Functional;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class RepeatingTimer extends Timer{
	public RepeatingTimer(double length, boolean immediate, Functional.Callback callback){
		super(length, callback);
		if(immediate) this.currCounter = length;
	}

	/**
	 * Creates a repeating timer that will continue to repeat and call the callback at each completion.
	 * @param length The length of the timer to run in seconds.
	 * @param callback The Callback function to run when completed.
	 */
	public RepeatingTimer(double length, Functional.Callback callback) {
		this(length, false, callback);
	}

	@Override
	public void update(float delta) {
		if(!canceled && !finished) {
			if (this.currCounter >= this.length) {
				while(this.currCounter >= this.length) {
					this.currCounter -= this.length;
					this.finish();
				}
			}

			this.currCounter += delta;
		}
	}

	@Override
	protected void finish() {
		super.finish();
	}
}
