package com.mygdx.game.util.managers;

import com.mygdx.game.util.DataBuilder;

import java.util.HashMap;

/**
 * Created by Paha on 5/24/2015.
 */
public class GameEventManager {
    private static HashMap<String, GameEvent> eventMap = new HashMap<>(10);

    public static void addGameEvent(DataBuilder.JsonPlayerEvent playerEvent){
        eventMap.put(playerEvent.eventName, new GameEvent(playerEvent));
    }

    public static void triggetGameEvent(String name){
        eventMap.get(name).triggered = true;
    }

    public static boolean isGameEventTriggered(String name){
        return eventMap.get(name).triggered;
    }

    public static GameEvent getGameEvent(String name){
        return eventMap.get(name);
    }

    public static class GameEvent{
        public DataBuilder.JsonPlayerEvent playerEvent;
        public boolean triggered = false;

        public GameEvent(DataBuilder.JsonPlayerEvent playerEvent){
            this.playerEvent = playerEvent;
        }
    }
}
