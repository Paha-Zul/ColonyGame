package com.mygdx.game.helpers.timer;

import com.mygdx.game.interfaces.Functional;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public abstract class Timer {
	protected boolean oneShot = false;
	protected double length;
	protected double currCounter;
	protected boolean canceled = false;
	protected boolean finished = false;

	private Functional.Callback callback;

	public Timer(double length, Functional.Callback callback){
		this.length = length;
		this.currCounter = 0;

		this.callback = callback;
	}

	public abstract void update(float delta);

	/**
	 * Sets the lenght of this timer.
	 * @param length The length to make the timer;
	 */
	public void setLength(double length){
		this.length = length;
	}

	/**
	 * Sets the callback of this timer. This is mostly used for cases where we can't lazy evaluate.
	 * @param callback The callback to use for the timer.
	 */
	public void setCallback(Functional.Callback callback){
		this.callback = callback;
	}

	/**
	 * Cancels this Timer and stops and further ticking or callback calls.
	 */
	public void cancel(){
		this.canceled = true;
	}

	/**
	 * @return True if canceled, false otherwise.
	 */
	public boolean isCanceled(){
		return this.canceled;
	}

	public boolean isFinished(){return this.finished;}

	/**
	 * Restarts the timer with the initial length.
	 */
	public void restart(){
		this.restart(this.length);
	}

	public boolean isOneShot(){
		return this.oneShot;
	}

	/**
	 * Restarts the timer with a given length of time.
	 * @param length The length of time the Timer runs for.
	 */
	public void restart(double length){
		this.currCounter = 0;
		this.canceled = false;
		this.finished = false;
		this.length = length;
	}

	/**
	 * This should be called when the timer finishes its current timer. Calls the callback function.
	 */
	protected void finish(){
		if(callback != null)
			this.callback.callback();
	}

	/**
	 * Gets the percentage completed of this Timer.
	 * @return A float between 0 and 1 representing its percentage towards being done.
	 */
	public float getPercentDone(){
		return (float)(this.length/this.currCounter);
	}

}
