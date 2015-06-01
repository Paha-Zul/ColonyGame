package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;
import com.sun.istack.internal.NotNull;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stat Component for an Entity that includes stats like health, hunger, thirst, etc. Also uses the update() method to
 * increment/decrement stats like lowering health due to hunger/thirst.
 */
public class Stats extends Component{
    @JsonIgnore
    private HashMap<String, Stat> statMap = new HashMap<>();
    @JsonProperty
    private ArrayList<Stat> statList = new ArrayList<>();
    @JsonIgnore
    private ArrayList<RepeatingTimer> timerList = new ArrayList<>();

    public Stats() {
        super();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        super.load();

        statList.forEach(stat -> statMap.put(stat.name, stat));
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        for(Timer timer : timerList)
            timer.update(delta);
    }

    /**
     * Adds a RepeatingTimer to this Stats Component.
     * @param timer The RepeatingTimer to add.
     */
    public Timer addTimer(@NotNull RepeatingTimer timer){
        this.timerList.add(timer);
        return timer;
    }

    /**
     * Clears the timers from this Stats Component.
     */
    public void clearTimers(){
        this.timerList.clear();
    }

    /**
     * Adds a new Stat object to the Stats Component.
     * @param name The compName of the Stat.
     * @param initCurrValue The initial current value to start.
     * @param initMaxValue THe initial maximum value to start.
     * @return The Stat that was created.
     */
    public Stat addStat(String name, float initCurrValue, float initMaxValue){
        Stat stat = new Stat(name, initCurrValue, initMaxValue);
        this.statMap.put(name, stat);
        this.statList.add(stat);
        return stat;
    }

    /**
     *
     * Adds a new Stat object to the Stats Component.
     * @param name The compName of the Stat.
     * @param effect The effect to put on this Stat.
     * @param initCurrValue The initial current value to start.
     * @param initMaxValue THe initial maximum value to start.
     * @return The Stat that was created.
     */
    public Stat addStat(String name, String effect, float initCurrValue, float initMaxValue){
        Stat stat = new Stat(name, initCurrValue, initMaxValue);
        stat.effect = effect;
        this.statMap.put(name, stat);
        this.statList.add(stat);
        return stat;
    }

    /**
     * Gets a Stat from the statMap.
     * @param name The compName of the stat.
     * @return The Stat object referenced by 'compName'. Null if it doesn't exist.
     */
    @JsonIgnore
    public Stat getStat(String name){
        if(this.statMap.containsKey(name))
            return this.statMap.get(name);

        return null;
    }

    /**
     * Attempts to get the stat with the effect desired.
     * @param effect The compName of the effect.
     * @return The stat the contains the effect if found, otherwise null.
     */
    @JsonIgnore
    public Stat getStatWithEffect(String effect){
        for(Stat stat : statList)
            if(stat.effect != null && stat.effect.equals(effect))
                return stat;

        return null;
    }

    @JsonIgnore
    public final ArrayList<Stat> getStatList(){
        return this.statList;
    }

    public void clearAllStats(){
        this.statMap.clear();
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);
        statMap.clear();
        statList.clear();
        timerList.clear();
    }

    public static class Stat{
        @JsonIgnore
        public Functional.Callback onZero, onFull;
        @JsonIgnore
        public Color color = Color.GREEN;
        @JsonProperty
        public String name, effect;
        @JsonProperty
        private float current, max;

        public Stat(){

        }

        public Stat(String name, float current, float max) {
            this.name = name;
            this.current = current;
            this.max = max;
        }

        /**
         * Adds a value to the current value of this Stat.
         * @param value The value to add to the current value.
         */
        public void addToCurrent(float value){
            this.current += value; //Add the value.
            //If empty (at or below 0), call the onZero callback and set to 0.
            if(this.current <= 0){
                if(onZero != null) onZero.callback();
                this.current = 0;
            //If full (at or above the max value), call the onFull callback and set to the max value.
            }else if(this.current >= this.max){
                if(onFull != null) onFull.callback();
                this.current = this.max;
            }
        }

        /**
         * Adds a value to add to the max value of this Stat.
         * @param value The value to add to the max value.
         */
        public void addToMax(float value){
            this.max += value;
        }

        /**
         * @return The current value of this Stat.
         */
        @JsonIgnore
        public float getCurrVal(){
            return this.current;
        }

        /**
         * @return The max value of this Stat.
         */
        @JsonIgnore
        public float getMaxVal(){
            return this.max;
        }

        @JsonIgnore
        public String getEffect(){
            return this.effect;
        }

        @JsonIgnore
        public void setEffect(String effect){
            this.effect = effect;
        }

    }

}
