package com.mygdx.game.util.managers;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * Stores all data that needs to be referenced for information in the future. This is for stuff like
 * itemNames (JsonItem), resources (JsonResource), animals (JsonAnimal), etc.
 */
public class DataManager {
    private static HashMap<Class<?>, HashMap<String, Object>> dataMap = new HashMap<>();

    /**
     * Adds a piece of data to this manager.
     * @param dataName The compName of the data to add.
     * @param data The actual Object of data.
     * @param c The class of the data.
     * @param <T> The class interType of the Data.
     */
    public static <T> void addData(String dataName, T data, Class<T> c){
        HashMap<String, Object> map = dataMap.get(c);
        if(map == null){
            map = new HashMap<>();
            dataMap.put(c, map);
        }

        map.put(dataName, data);
    }

    /**
     * Retrieves data from this manager.
     * @param dataName The compName of the data we are retrieving.
     * @param c The Class of the data we are retrieving.
     * @param <T> The Class interType of data.
     * @return The T data retrieved from this manager. Throws an exception if no data is found.
     */
    public static <T> T getData(String dataName, Class<T> c){
        HashMap<String, Object> map = dataMap.get(c);
        //if(map == null || !map.containsKey(dataName)) GH.writeErrorMessage("Data of class "+c.getName()+" with compName "+dataName+" does not exist!");

        return (T)map.get(dataName);
    }

    public static <T> String[] getKeysForType(Class <T> c){
        HashMap<String, Object> map = dataMap.get(c);

        return map.keySet().toArray(new String[map.size()]);
    }
}
