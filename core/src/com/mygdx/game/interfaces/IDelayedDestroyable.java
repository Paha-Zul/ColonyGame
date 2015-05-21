package com.mygdx.game.interfaces;

/**
 * Created by Paha on 3/27/2015.
 */
public interface IDelayedDestroyable {
    /**
     * Sets the Entity to be destroyed. This is used to destroy the Entity on the next frame. This is needed for cases such
     * as destroying Entities/Components during a collision, which will crash the program if done using the immediate destroy().
     */
    void setToDestroy();

    /**
     * Used for clearing objects to prevent memory leaks. Can also be used for any
     * flags.
     * @param ownerID The ownerID of the entity that destroyed it.
     */
    void destroy(double ownerID);

    /**
     * @return True if destroyed, false otherwise.
     */
    boolean isDestroyed();

    /**
     * @return True if this object is set to be destroyed, false otherwise.
     */
    boolean isSetToBeDestroyed();
}
