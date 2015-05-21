package com.mygdx.game.util;

import com.mygdx.game.interfaces.IRecyclable;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Paha on 5/1/2015.
 */
public class ObjectPool {
    private static HashMap<Class, LinkedList<IRecyclable>> objectPool = new HashMap<>();
    private static final int limit = 50;

    public static <T extends IRecyclable> void recycleObject(T object, Class<T> cls){
        LinkedList<IRecyclable> list = objectPool.get(cls);
        if(list == null){
            list = new LinkedList<>();
            objectPool.put(cls, list);
        }

        if(list.size() < limit)
            list.add(object);
    }

    public static <T extends IRecyclable> T getObject(Class<T> cls){
        LinkedList<IRecyclable> list = objectPool.get(cls);
        if(list == null) return null;
        return (T)list.poll();
    }
}
