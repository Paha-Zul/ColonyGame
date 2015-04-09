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
     * @param category The category of the String balue.
     * @param string The actual String value.
     * @return The integer which is the key of the String value.
     */
    @SuppressWarnings("unchecked")
    public static int getString(String category, String string){
        table.putIfAbsent(category, new TObjectIntHashMap<>()); //Puts the category if absent.
        return table.get(category).adjustOrPutValue(string, 0, counterMap.adjustOrPutValue(category, 1, 1));
    }
}
