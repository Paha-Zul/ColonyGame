package com.mygdx.game.util.managers;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.DataBuilder;

import java.util.HashMap;

/**
 * Created by Paha on 5/24/2015.
 * Manages GameEvents for Entities.
 */
public class GameEventManager {
    private static HashMap<String, GameEvent> eventMap = new HashMap<>(10);

    /**
     * Adds a GameEvent to this manager.
     * @param gameEvent The GameEvent to add.
     */
    public static void addGameEvent(DataBuilder.JsonPlayerEvent gameEvent){
        eventMap.put(gameEvent.eventName, new GameEvent(gameEvent));
    }

    /**
     * Triggers a GameEvent, setting the eventTarget and eventTargetOther as null.
     * @param name The name of the event.
     * @return The GameEvent that was triggered.
     */
    public static GameEvent triggerGameEvent(String name){
        return triggerGameEvent(name, null, null);
    }

    /**
     * Triggers a GameEvent by a name. Does nothing if the GameEvent is already triggered but still returns it.
     * @param name The name of the GameEvent.
     * @param eventTarget The Entity target of the Event, ie: A miner has gone crazy! (the miner is the target)
     * @param otherTarget The other target of the Event. A Colonist has encountered the Big Bad Wolf! (Big Bad Wolf is the other target).
     * @return The GameEvent that was triggered.
     */
    public static GameEvent triggerGameEvent(String name, Entity eventTarget, Entity otherTarget){
        GameEvent event =  eventMap.get(name);
        if(!event.triggered) {
            event.triggered = true;
            event.playerEvent.eventTarget = eventTarget;
            event.playerEvent.eventTargetOther = otherTarget;
        }
        return event;
    }

    /**
     * Triggers a GameEvent, setting the eventTargetOther as null.
     * @param name The name of the event.
     * @param eventTarget The Entity target of the Event, ie: A miner has gone crazy! (the miner is the target)
     * @return The GameEvent that was triggered.
     */
    public static GameEvent triggerGameEvent(String name, Entity eventTarget){
        return triggerGameEvent(name, eventTarget, null);
    }

    /**
     * Gets a GameEvent from this manager.
     * @param name The name of the event to get.
     * @return The GameEvent if found, null otherwise.
     */
    public static GameEvent getGameEvent(String name){
        return eventMap.get(name);
    }

    public static boolean isGameEventTriggered(String name){
        return eventMap.get(name).triggered;
    }

    public static class GameEvent{
        public DataBuilder.JsonPlayerEvent playerEvent;
        public boolean triggered = false;

        public GameEvent(DataBuilder.JsonPlayerEvent playerEvent){
            this.playerEvent = playerEvent;
        }
    }
}
