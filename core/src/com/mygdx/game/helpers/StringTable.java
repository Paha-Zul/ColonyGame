package com.mygdx.game.helpers;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.HashMap;

/**
 * Created by Paha on 4/8/2015.
 */
public class StringTable {
    static TObjectIntHashMap counterMap = new TObjectIntHashMap(32);
    static HashMap<String, TObjectIntHashMap<String>> table = new HashMap<>();

    /**
     * Gets a string from the string table.
     * @param category The category of the String value.
     * @param string The actual String value.
     * @return The integer which is the key of the String value.
     */
    @SuppressWarnings("unchecked")
    public static int getString(String category, String string){
        table.putIfAbsent(category, new TObjectIntHashMap<>()); //Puts the category if absent.
        int value = table.get(category).get(string);
        if(value == 0){
            int val = counterMap.adjustOrPutValue(category, 1, 1);
            table.get(category).put(string, val);
            value = val;
        }

        return value;
    }
}
