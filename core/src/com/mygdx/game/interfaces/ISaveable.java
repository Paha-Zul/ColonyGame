package com.mygdx.game.interfaces;

/**
 * Created by Paha on 5/21/2015.
 */
public interface ISaveable {
    /**
     * Called on the component when it is added to the SaveGameHelper list.
     */
    void save();

    /**
     * Called right after any init() methods that apply, but before any start() methods. For instance, if used
     * on a Component, the component's init() method will have already been called meaning that component's owner field
     * is set. Other fields/Components are not guaranteed to be set.
     */
    void initLoad();

    /**
     * Called after all Entities and Components have been linked and initialized. Other Components (if called on a component)
     * are available in this method.
     */
    void load();
}
