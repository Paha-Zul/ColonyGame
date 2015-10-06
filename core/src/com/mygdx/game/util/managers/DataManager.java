package com.mygdx.game.util.managers;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.util.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

        if(map.put(dataName, data) != null)
            Logger.log(Logger.NORMAL, dataName+" of type "+c.getSimpleName()+" already has data in the DataManager. Either getting overwritten by a duplicate or a mod.");

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
        if(map == null) Logger.log(Logger.ERROR, "The map for class type "+c.getSimpleName()+" doesn't exist. This will crash...");
        Object data = map.get(dataName);
        if(data == null) Logger.log(Logger.WARNING, "Data with "+dataName+" trying to be retrieved from the DataManager doesn't exist. Hope this null value is handled...");
        return (T)data;
    }

    /**
     * Convenience method to get a TextureRegion from an Atlas without having to go through the AssetManager for the Atlas.
     * @param dataName The texture name.
     * @param atlasName The atlas name.
     * @return The TextureRegion from the Atlas if the Atlas was found and the TextureRegion was found in the Atlas. Null otherwise.
     */
    public static TextureRegion getTextureFromAtlas(String dataName, String atlasName){
        TextureAtlas atlas = ColonyGame.instance.assetManager.get(atlasName, TextureAtlas.class);
        if(atlas == null) return null;
        return atlas.findRegion(dataName);
    }

    public static <T> String[] getKeyListForType(Class <T> c){
        HashMap<String, Object> map = dataMap.get(c);

        return map.keySet().toArray(new String[map.size()]);
    }

    public static <T> Object[] getValueListForType(Class <T> c){
        HashMap<String, Object> map = dataMap.get(c);

        return map.values().toArray(new Object[map.size()]);
    }

    public static <T> Set<Map.Entry<String, Object>> getEntrySetForType(Class <T> c){
        HashMap<String, Object> map = dataMap.get(c);

        return map.entrySet();
    }

//    private static <T> void assignFields(DataBuilder.JsonPrefab.FieldInit[] fields, Class<T> clazz, Object objectInstance){
//        try {
//            //For each field
//            for(DataBuilder.JsonPrefab.FieldInit field : fields) {
//                Field tmp = objectInstance.getClass().getDeclaredField(field.fieldName);
//                Object object  = null;
//                try {
//                    object = tmp.getType().newInstance();
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                }
//                tmp.setAccessible(true);
//                Object value = tmp.get(object);
//                //If there is a value, assign it.
//                if (field.value != null) tmp.set(objectInstance, tmp.getType().cast(field.value));
//                //Otherwise, do any method calls.
//                else {
//                    //For each method call...
//                    for (DataBuilder.JsonPrefab.MethodCall call : field.methodCalls) {
//                        Object[] parameters = new Object[call.parameters.length]; //List for all the parameters
//                        Class<?>[] methodParamTypes = null;
//                        if(call.methodParamType != null) methodParamTypes = new Class<?>[1];
//                        else methodParamTypes = new Class<?>[call.parameterTypes.length];
//                        //For each parameter, cast it to the type that is dictated in the JSON.
//                        for(int i=0;i<call.parameters.length;i++) {
//                            parameters[i] = Class.forName(call.parameterTypes[i]).cast(call.parameters[i]); //Get the class by name and cast the object.
//                            if(methodParamTypes.length == 1) methodParamTypes[0] = Class.forName(call.methodParamType[0]);
//                            else methodParamTypes[i] = Class.forName(call.parameterTypes[i]);
//                        }
//
//                        Method meth = tmp.getType().getMethod(call.method, methodParamTypes);     //Get the method.
//
//                        System.out.println(objectInstance.getClass().getName());
//
//                        //Invoke with the new parameters.
//                        meth.invoke(objectInstance, parameters);
//                    }
//                }
//            }
//        }catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static <T> void assignComponents(Entity entity, DataBuilder.JsonPrefab prefab){
//        try {
//            for(DataBuilder.JsonPrefab.ComponentObject compObject : prefab.components){
//                Component component = (Component)Class.forName(compObject.className).newInstance();
//            }
//        }catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
//            e.printStackTrace();
//        }
//    }
}
