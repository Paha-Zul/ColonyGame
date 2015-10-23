package com.mygdx.game.util.managers;

import com.mygdx.game.entity.Entity;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by Paha on 3/26/2015.
 * Handles linking messages to function calls for easy communication for events (like adding an item to an inventory).
 */
public class MessageEventSystem {
    private static final int gameEventID = 0;
    //A map of maps of lists of consumer interfaces.
    private static HashMap<Long, HashMap<String, ArrayList<java.util.function.Consumer<Object[]>>>> entityMap = new HashMap<>();

    /**
     * Adds a function to the MessageEventSystem.
     * @param entity The Entity to use for its ID. This will direct the call to the correct Entity.
     * @param eventName The event name, ie: "collidestart" to indicate which event to call.
     * @param function The Consumer function to be called on an event.
     */
    public static Consumer<Object[]> onEntityEvent(Entity entity, String eventName, java.util.function.Consumer<Object[]> function){
        return registerEvent(entity.getID(), eventName, function);
    }

    /**
     * Registers an event to the system.
     * @param id The ID of the Entity if an Entity is receiving the event. Otherwise, an ID of 'gameEventID' (class variable) dictates a game event.
     * @param eventName The name of the Event to call.
     * @param function The function to register to this Event.
     */
    private static Consumer<Object[]> registerEvent(long id, String eventName, java.util.function.Consumer<Object[]> function){
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
        return function;
    }

    /**
     * Adds a function to the MessageEventSystem for a game event.
     * @param eventName The name of the event.
     * @param function THe Consumer function to be called on an event.
     */
    public static Consumer<Object[]> onGameEvent(String eventName, java.util.function.Consumer<Object[]> function){
        return registerEvent(gameEventID, eventName, function);
    }

    /**
     * Removes an Event from the system.
     * @param entity The Entity to call the event on.
     * @param handlerName The compName of the Event/Handler.
     * @param function The function/Event to remove from the system.
     */
    public static void unregisterEventFunction(Entity entity, String handlerName, java.util.function.Consumer<Object[]> function){
        if(entityMap.get(entity.getID()) != null && entityMap.get(entity.getID()).get(handlerName) != null)
            entityMap.get(entity.getID()).get(handlerName).remove(function);
    }

    /**
     * Unreigsters all events with 'handlerName' passed in.
     * @param entity The Entity to remove from.
     * @param eventName The compName of the event to remove.
     */
    public static void UnregisterEvent(Entity entity, String eventName){
        if(entityMap.get(entity.getID()) != null && entityMap.get(entity.getID()).get(eventName) != null)
            entityMap.get(entity.getID()).remove(eventName);
    }

    /**
     * Removes an Entity from the event system, effectively clearing all events linked to the Entity.
     * @param entity The Entity to remove from the MessageEventSystem.
     */
    public static void unregisterEntity(Entity entity){
        if(entityMap.get(entity.getID()) != null)
            entityMap.remove(entity.getID()); //Remove the Entity from the system.
    }

    /**
     * Calls all Events with the "eventName" on a specific Entity.
     * @param entity The Entity to call the event on.
     * @param eventName The name of the Event.
     * @param args The arguments to pass into the list.
     */
    public static void notifyEntityEvent(@NotNull Entity entity, String eventName, Object... args){
        if(entity == null) return; //If the Entity is null, return.
        if(entityMap.get(entity.getID()) == null || entityMap.get(entity.getID()).get(eventName) == null)
           return; //If any of the maps or lists are null, return.

        //get the list.
        ArrayList<java.util.function.Consumer<Object[]>> list = entityMap.get(entity.getID()).get(eventName);

        if(list == null) return; //If the list is null, return.
        list.forEach(evt -> evt.accept(args)); //For each item in the list, call it!
    }

    /**
     * Calls all Events with the "eventName" on a specific Entity.
     * @param eventName The compName of the Event.
     * @param args The arguments to pass into the list.
     */
    public static void notifyGameEvent(String eventName, Object... args){
        long id = gameEventID;

        if(entityMap.get(id) == null || entityMap.get(id).get(eventName) == null)
            return; //If any of the maps or lists are null, return.

        //get the list.
        ArrayList<java.util.function.Consumer<Object[]>> list = entityMap.get(id).get(eventName);

        if(list == null) return; //If the list is null, return.
        list.forEach(evt -> evt.accept(args)); //For each item in the list, call it!
    }
}
