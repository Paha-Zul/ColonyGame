package com.mygdx.game.interfaces;

/**
 * An Interface to be implemented on any Objects that can be destroyed. This will clear any objects that may cause memory leaks
 * and remove it from the respective update list.
 */
public interface IDestroyable {
	/**
	 * Used for clearing objects to prevent memory leaks. Can also be used for any
	 * flags.
	 */
	void destroy();

	/**
	 * @return True if destroyed, false otherwise.
	 */
	boolean isDestroyed();
}
