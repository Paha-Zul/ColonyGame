package com.mygdx.game.helpers;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.HashMap;

/**
 * Created by Paha on 4/8/2015.
 */
public class StringTable {
    /**
     * The counter map is separate but interacts with the stringToIntTable. The counter map will keep track of the number
     * of unique String values. When a new String is entered into the stringToIntTable, it grabs the current counterMap value and increments it.
     *
     * The stringToIntTable holds a string (category, ie: "entity") and maps it to a String->int stringToIntTable.
     * This inner stringToIntTable will map each string to a unique int. So, "entity" holds "alive"->0, "dead"->1, "resource"->2, etc...
     */
    private static TObjectIntHashMap counterMap = new TObjectIntHashMap(32);
    private static HashMap<String, HashMap<String, StringIntPair>> stringToIntTable = new HashMap<>();

    /**
     * Gets a string from the string stringToIntTable.
     * @param category The category of the String value.
     * @param string The actual String value.
     * @return The integer which is the key of the String value.
     */
    @SuppressWarnings("unchecked")
    public static int StringToInt(String category, String string){
        stringToIntTable.putIfAbsent(category, new HashMap<>()); //Puts in the object -> int hashmap.
        StringIntPair value = stringToIntTable.get(category).get(string); //Gets the string -> StringIntPair object.
        //If it's null, we need to initialize it
        if(value == null){
            int val = counterMap.adjustOrPutValue(category, 1, 1);
            stringToIntTable.get(category).put(string, value = new StringIntPair(string, val));
        }

        return value.integer;
    }

//    public static String IntToString(String category, int num){
//        HashMap<String, StringIntPair> tmpTable = stringToIntTable.get(category);
//        if(tmpTable == null) GH.writeErrorMessage("A category has not been entered and set up in StringTable.java for "+category+". you are doing something wrong...", true);
//        return tmpTable.get()
//    }

    private static class StringIntPair{
        public String string;
        public int integer;

        public StringIntPair(String string, int integer) {
            this.string = string;
            this.integer = integer;
        }
    }
}
