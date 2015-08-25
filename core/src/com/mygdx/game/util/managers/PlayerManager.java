package com.mygdx.game.util.managers;

import com.mygdx.game.component.Colony;
import com.mygdx.game.util.Logger;

import java.util.HashMap;

/**
 * Created by Paha on 5/19/2015.
 * <p>Manages players in the game. The human player will always be player 0.</p>
 */
public class PlayerManager {
    private static HashMap<String, Player> playerHashMap = new HashMap<>();

    public static Player addPlayer(String playerName, Colony colony){
        Player player = new Player(colony);
        playerHashMap.put(playerName, player);
        return player;
    }

    public static Player getPlayer(String playerName){
        Player player = playerHashMap.get(playerName);
        if(player == null) Logger.log(Logger.WARNING, "Player by the name of "+playerName+" does not exist. Did you spell it wrong?");
        return player;
    }

    public static class Player{
        public Colony colony;

        public Player(Colony colony){
            this.colony = colony;
        }
    }
}

