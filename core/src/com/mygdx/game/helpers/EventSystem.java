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

    /**
     * Adds a function to the EventSystem.
     * @param entity The Entity to use for its ID. This will direct the call to the correct Entity.
     * @param eventName The event name, ie: "collidestart" to indicate which event to call.
     * @param function The Consumer function to be called on an event.
     */
    public static void onEntityEvent(Entity entity, String eventName, java.util.function.Consumer<Object[]> function){
        registerEvent(entity.getID(), eventName, function);
    }

    /**
     * Adds a function to the EventSystem for a game event.
     * @param eventName The name of the event.
     * @param function THe Consumer function to be called on an event.
     */
    public static void onGameEvent(String eventName, java.util.function.Consumer<Object[]> function){
        registerEvent(-1, eventName, function);
    }

    private static void registerEvent(double id, String eventName, java.util.function.Consumer<Object[]> function){
        //If the map for the Entity id is null, add a new map.
        if(entityMap.get(id) == null)
            entityMap.put(id, new HashMap<>());

        //If the list of the handlers is null, add a new list.
        if(entityMap.get(id).get(eventName) == null)
            entityMap.get(id).put(eventName, new ArrayList<>());

        //Get the list.
        ArrayList<java.util.function.Consumer<Object[]>> list = entityMap.get(id).get(eventName);
        //If the list is null, add a new one.
        if(list == null) {
            list = new ArrayList<>();
            entityMap.get(id).put(eventName, new ArrayList<>());
        }

        list.add(function);
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
     * Unreigsters all events with 'handlerName' passed in.
     * @param entity The Entity to remove from.
     * @param handlerName The name of the handler to remove.
     */
    public static void unregisterHandler(Entity entity, String handlerName){
        if(entityMap.get(entity.getID()) != null && entityMap.get(entity.getID()).get(handlerName) != null)
            entityMap.get(entity.getID()).remove(handlerName);
    }

    /**
     * Removes an Entity from the event system, effectively clearing all events linked to the Entity.
     * @param entity The Entity to remove from the EventSystem.
     */
    public static void unregisterEntity(Entity entity){
        if(entityMap.get(entity.getID()) != null)
            entityMap.remove(entity.getID()); //Remove the Entity from the system.
    }

    /**
     * Calls all Events with the "handleName" on a specific Entity.
     * @param entity The Entity to call the event on.
     * @param handlerName The name of the Event.
     * @param args The arguments to pass into the list.
     */
    public static void notifyEntityEvent(@NotNull Entity entity, String handlerName, Object... args){
        if(entity == null) return; //If the Entity is null, return.
        if(entityMap.get(entity.getID()) == null || entityMap.get(entity.getID()).get(handlerName) == null)
           return; //If any of the maps or lists are null, return.

        //get the list.
        ArrayList<java.util.function.Consumer<Object[]>> list = entityMap.get(entity.getID()).get(handlerName);

        if(list == null) return; //If the list is null, return.
        list.forEach(evt -> evt.accept(args)); //For each item in the list, call it!
    }

    /**
     * Calls all Events with the "handleName" on a specific Entity.
     * @param handlerName The name of the Event.
     * @param args The arguments to pass into the list.
     */
    public static void notifyGameEvent(String handlerName, Object... args){
        double id = -1;

        if(entityMap.get(id) == null || entityMap.get(id).get(handlerName) == null)
            return; //If any of the maps or lists are null, return.

        //get the list.
        ArrayList<java.util.function.Consumer<Object[]>> list = entityMap.get(id).get(handlerName);

        if(list == null) return; //If the list is null, return.
        list.forEach(evt -> evt.accept(args)); //For each item in the list, call it!
    }
}
