package com.mygdx.game.util.managers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.component.Colony;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.Logger;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Paha on 5/19/2015.
 * <p>Manages players in the game. The human player will always be player 0.</p>
 */
public class PlayerManager extends Manager{
    private HashMap<String, Player> playerHashMap = new HashMap<>();
    private Player localPlayer;

    public PlayerManager(){

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

    /**
     * Takes the pair of data (playerName, colonyID) and makes new players with a null colony but with a colonyID.
     * The colonyID must be matched to a Colony during initLoad() or load().
     * @param data A list of the pair of data (playerName, colonyID)
     */
    @JsonProperty("playerManagerData")
    public void setPlayerManagerData(ArrayList<String[]> data){
        for(String[] pair : data)
            this.addPlayer(pair[0], null).colonyID = Long.parseLong(pair[1]);
    }

    /**
     * Adds a player with an associated colony
     * @param playerName The name of the player.
     * @param colony The Colony that this player is in control of.
     * @return The Player object that holds the Colony.
     */
    public Player addPlayer(String playerName, Colony colony){
        Player player = new Player(playerName, colony);
        playerHashMap.put(playerName, player);
        this.localPlayer = player; //TODO Need to check if local human player before assigning.
        return player;
    }

    /**
     * @return The local Player of the game.
     */
    public Player getLocalPlayer(){
        return this.localPlayer;
    }

    /**
     * Used only for loading. Makes a new player and assigns it the name. The Colony must be
     * assigned during the initLoad() or load() functions.
     * @param localPlayer The name of the local Player.
     */
    @JsonProperty("localPlayer")
    private void setLocalPlayer(String localPlayer){
        this.localPlayer = new Player(localPlayer, null);
    }

    /**
     * Used only for saving.
     * @return The local Player's name.
     */
    @JsonProperty("localPlayer")
    private String getLocalPlayerName(){
        return this.localPlayer.name;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void addedLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

    }

    @Override
    public void save() {

    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

        if(entityMap != null && compMap != null) {
            //We loaded the playerNames and colonyIDs before, now we link the Colony and colonyID.
            for (Player player : this.playerHashMap.values())
                player.colony = (Colony) compMap.get(player.colonyID);
        }
    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

    }

    @Override
    public void init(){
        this.playerHashMap = new HashMap<>();
        this.initLoad(null, null);
    }

    /**
     * Simple POJO for linking a player name to a colony.
     */
    public class Player{
        public Colony colony;
        public String name;
        public long colonyID;

        public Player(String name, Colony colony){
            this.colony = colony;
            this.name = name;
            if(colony != null) this.colonyID = colony.getCompID();
        }
    }
}

