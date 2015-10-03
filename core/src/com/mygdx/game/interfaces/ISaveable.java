package com.mygdx.game.interfaces;

import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Created by Paha on 5/21/2015.
 * An interface for Components that are able to be saved.
 */
public interface ISaveable {
    /**
     * Called when a loaded component is added to its owning Entity or World.
     * @param entityMap The Entity map that has all the loaded Entities.
     * @param compMap The Component map that has all the loaded Components.
     */
    void addedLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap);

    /**
     * Called on the component when it is added to the SaveGameHelper list.
     */
    void save();

    /**
     * Called right after any init() methods that apply, but before any start() methods. For instance, if used
     * on a Component, the component's init() method will have already been called meaning that component's owner field
     * is set. Other fields/Components are not guaranteed to be set.
     * @param entityMap The Entity map that has all the loaded Entities.
     * @param compMap The Component map that has all the loaded Components.
     */
    void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap);

    /**
     * Called after all Entities and Components have been linked and initialized. Other Components (if called on a component)
     * are available in this method.
     * @param entityMap The Entity map that has all the loaded Entities.
     * @param compMap The Component map that has all the loaded Components.
     */
    void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap);
}
