package com.mygdx.game.util.managers;

import com.mygdx.game.util.DataBuilder;

import java.util.HashMap;

/**
 * Created by Paha on 5/24/2015.
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

    public static void triggetGameEvent(String name){
        eventMap.get(name).triggered = true;
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
