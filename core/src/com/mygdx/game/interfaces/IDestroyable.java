package com.mygdx.game.interfaces;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public interface IDestroyable {
	/**
	 * Used for clearing objects to prevent memory leaks. Can also be used for any
	 * flags.
	 */
	public void destroy();

	/**
	 * @return True if destroyed, false otherwise.
	 */
	public boolean isDestroyed();
}
