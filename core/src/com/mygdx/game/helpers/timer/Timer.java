package com.mygdx.game.helpers.timer;

import com.mygdx.game.interfaces.Functional;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public abstract class Timer {
	protected double length;
	protected double currCounter;
	protected boolean expired = false;

	private Functional.Callback callback;

	public Timer(double length, Functional.Callback callback){
		this.length = length;
		this.currCounter = 0;

		this.callback = callback;
	}

	public abstract void update(float delta);

	public void cancel(){
		this.expired = true;
	}

	public void restart(){
		this.currCounter = 0;
		this.expired = false;
	}

	/**
	 * This should be called when the timer finishes its current timer. Calls the callback function.
	 */
	protected void finish(){
		this.callback.callback();
	}

}
