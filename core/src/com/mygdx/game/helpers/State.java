package com.mygdx.game.helpers;

import java.util.HashMap;

/**
 * Created by Paha on 4/11/2015.
 */
public class State {
    private HashMap<String, String> stateMap = new HashMap<>();
    private String currState = "invalid";
    private String defaultString = "invalid";

    public State(){

    }

    public void addState(String stateName){
        this.stateMap.putIfAbsent(stateName, stateName);
    }

    public void addState(String stateName, boolean defaultString){
        this.stateMap.putIfAbsent(stateName, stateName);
        if(defaultString) this.defaultString = stateName;
    }

    public String getCurrState(){
        return this.currState;
    }

    public boolean isState(String stateName){
        return this.currState.equals(stateName);
    }

    public void setCurrState(String stateName){
        this.currState = this.stateMap.getOrDefault(stateName, "invalid");
    }
}
