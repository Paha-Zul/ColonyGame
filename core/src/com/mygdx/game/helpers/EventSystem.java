package com.mygdx.game.helpers;

import com.mygdx.game.entity.Entity;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paha on 3/26/2015.
 */
public class EventSystem {
    //A map of maps of lists of consumer interfaces.
    private static HashMap<Double, HashMap<String, ArrayList<java.util.function.Consumer<Object[]>>>> entityMap = new HashMap<>();
    private static HashMap<String, java.util.function.BiConsumer<ArrayList<java.util.function.Consumer<Object[]>>, Object[]>> handlerMap = new HashMap<>();

    public static void addEventHandler(String name, java.util.function.BiConsumer<ArrayList<java.util.function.Consumer<Object[]>>, Object[]> handler){
        handlerMap.put(name, handler);
    }

    /**
     * Adds a function to the EventSystem.
     * @param entity The Entity to use for its ID. This will direct the call to the correct Entity.
     * @param handlerName The handler name, ie: "collidestart" to indicate which event to call.
     * @param function The Consumer function to be called on an event.
     */
    public static void registerEvent(Entity entity, String handlerName, java.util.function.Consumer<Object[]> function){
        //If the map for the Entity id is null, add a new map.
        if(entityMap.get(entity.getID()) == null)
            entityMap.put(entity.getID(), new HashMap<>());

        //If the list of the handlers is null, add a new list.
        if(entityMap.get(entity.getID()).get(handlerName) == null)
            entityMap.get(entity.getID()).put(handlerName, new ArrayList<>());

        //Get the list.
        ArrayList<java.util.function.Consumer<Object[]>> list = entityMap.get(entity.getID()).get(handlerName);
        //If the list is null, add a new one.
        if(list == null) {
            list = new ArrayList<>();
            entityMap.get(entity.getID()).put(handlerName, new ArrayList<>());
        }


        list.add(function);
        //System.out.println("Registered event to entity "+entity.getID());
    }

    /**
     * Removes an Event from the system.
     * @param entity The Entity to call the event on.
     * @param handlerName The name of the Event/Handler.
     * @param function The function/Event to remove from the system.
     */
    public static void unregisterEvent(Entity entity, String handlerName, java.util.function.Consumer<Object[]> function){
        if(entityMap.get(entity.getID()) != null && entityMap.get(entity.getID()).get(handlerName) != null)
            entityMap.get(entity.getID()).get(handlerName).remove(function);
    }

    /**
     * Calls all Events with the "handleName" on a specific Entity.
     * @param entity The Entity to call the event on.
     * @param handlerName The name of the Event.
     * @param args The arguments to pass into the list.
     */
    public static void notify(@NotNull Entity entity, String handlerName, Object... args){
        if(entity == null) return; //If the Entity is null, return.
        if(entityMap.get(entity.getID()) == null || entityMap.get(entity.getID()).get(handlerName) == null)
           return; //If any of the maps or lists are null, return.

        //get the list.
        ArrayList<java.util.function.Consumer<Object[]>> list = entityMap.get(entity.getID()).get(handlerName);

        if(list == null) return; //If the list is null, return.
        list.forEach(evt -> evt.accept(args)); //For each item in the list, call it!
    }
}
