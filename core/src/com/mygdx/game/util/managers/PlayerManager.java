package com.mygdx.game.util.managers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.component.Colony;
import com.mygdx.game.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Paha on 5/19/2015.
 * <p>Manages players in the game. The human player will always be player 0.</p>
 */
public class PlayerManager{
    private HashMap<String, Player> playerHashMap = new HashMap<>();
    private Player localPlayer;

    public PlayerManager(){

    }

    public void init(){
        this.playerHashMap = new HashMap<>();
    }

    /**
     * Adds a player with an associated colony
     * @param playerName The name of the player.
     * @param colony The Colony that this player is in control of.
     * @return The Player object that holds the Colony.
     */
    public Player addPlayer(String playerName, Colony colony){
        Player player = new Player(colony);
        playerHashMap.put(playerName, player);
        this.localPlayer = player; //TODO Need to check if local human player before assigning.
        return player;
    }

    public Player getPlayer(String playerName){
        Player player = playerHashMap.get(playerName);
        if(player == null) Logger.log(Logger.WARNING, "Player by the name of "+playerName+" does not exist. Did you spell it wrong?");
        return player;
    }

    @JsonProperty("playerManagerData")
    public ArrayList<String[]> getPlayerManagerData(){
        ArrayList<String[]> data = new ArrayList<>();
        for(Map.Entry<String, Player> entry : playerHashMap.entrySet())
            data.add(new String[]{entry.getKey(), ""+entry.getValue().colony.getCompID()});

        return data;
    }

    public Player getLocalPlayer(){
        return this.localPlayer;
    }

    public class Player{
        public Colony colony;

        public Player(Colony colony){
            this.colony = colony;
        }
    }
}

