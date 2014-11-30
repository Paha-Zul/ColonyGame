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

	/**
	 * Cancels this Timer and stops and further ticking or callback calls.
	 */
	public void cancel(){
		this.expired = true;
	}

	/**
	 * @return True if canceled, false otherwise.
	 */
	public boolean isCanceled(){
		return this.expired;
	}

	/**
	 * Restarts the timer with the initial length.
	 */
	public void restart(){
		this.restart(this.length);
	}

	/**
	 * Restarts the timer with a given length of time.
	 * @param length The length of time the Timer runs for.
	 */
	public void restart(double length){
		this.currCounter = 0;
		this.expired = false;
		this.length = length;
	}

	/**
	 * This should be called when the timer finishes its current timer. Calls the callback function.
	 */
	protected void finish(){
		this.callback.callback();
	}

}
