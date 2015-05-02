package com.mygdx.game.helpers;

import java.util.HashMap;

/**
 * Created by Paha on 4/11/2015.
 */
public class StateSystem<T>{
    private HashMap<String, State<T>> stateMap = new HashMap<>();
    private State<T> currState = new State<>("default", null);
    private State<T> defaultState = currState;

    public StateSystem(){

    }

    /**
     * Adds a new state to this system.
     * @param stateName The name for the state.
     */
    public void addState(String stateName){
        this.addState(stateName, false, null);
    }

    /**
     * Adds a new state to this system with user data (whatever object you want!)
     * @param stateName The name for the state.
     * @param userData The data for the state to hold.
     */
    public void addState(String stateName, T userData){
        this.addState(stateName, false, userData);
    }

    /**
     * Adds a new state to this system with the option to make it the default state.
     * @param stateName The name of the state.
     * @param defaultState True if the state should be the default state of this system, false otherwise.
     */
    public void addState(String stateName, boolean defaultState){
        this.addState(stateName, defaultState, null);
    }

    /**
     * Adds a new state to this system with the option to make it the default state and to add user data.
     * @param stateName The name of the state.
     * @param defaultState True if it the state should be the default state, false otherwise.
     * @param userData The user data for this state.
     */
    public void addState(String stateName, boolean defaultState, T userData){
        State<T> newState = new State<>(stateName, userData);
        this.stateMap.putIfAbsent(stateName, newState);
        if(defaultState) this.defaultState = newState;
    }

    /**
     * @return The current State of this system.
     */
    public State<T> getCurrState(){
        return this.currState;
    }

    /**
     * @return The default State of this system.
     */
    public State<T> getDefaultState(){
        return this.defaultState;
    }

    /**
     * @param stateName The name of the state to check.
     * @return True if the State name passed in is the name of the current State of this system, false otherwise.
     */
    public boolean isCurrState(String stateName){
        return this.currState.stateName.equals(stateName);
    }

    /**
     * Sets the current State of this system. If the State name does not exist, the default State becomes the current State.
     * @param stateName
     */
    public void setCurrState(String stateName){
        this.currState = this.stateMap.getOrDefault(stateName, defaultState);
    }

    /**
     * Sets the current State to the default State.
     */
    public void setToDefaultState(){
        this.currState = defaultState;
    }

    /**
     * @param stateName The name of the State to check for.
     * @return True if this State system contains the state, false otherwise.
     */
    public boolean stateExists(String stateName){
        return this.stateMap.containsKey(stateName);
    }

    public static class State<T>{
        public String stateName;
        private T userData;

        public State(String stateName, T userData){
            this.stateName = stateName;
            this.userData = userData;
        }

        public T getUserData(){
            return this.userData;
        }
    }
}
