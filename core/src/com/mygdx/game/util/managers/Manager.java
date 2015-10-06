package com.mygdx.game.util.managers;

import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDestroyable;
import com.mygdx.game.interfaces.ISaveable;
import com.mygdx.game.interfaces.IStartable;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Created by Paha on 10/6/2015.
 */
public abstract class Manager implements ISaveable, IDestroyable, IStartable{
    private boolean destroyed = false;

    @Override
    public void destroy() {
        this.destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return this.destroyed;
    }

    @Override
    public void addedLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

    }

    @Override
    public void save() {

    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

    }

    @Override
    public void added(Entity owner) {

    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }
}
