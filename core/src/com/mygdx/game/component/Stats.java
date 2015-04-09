package com.mygdx.game.component;

import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.Functional;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stat Component for an Entity that includes stats like health, hunger, thirst, etc. Also uses the update() method to
 * increment/decrement stats like lowering health due to hunger/thirst.
 */
public class Stats extends Component{
    private HashMap<String, Stat> statMap = new HashMap<>();
    private ArrayList<Stat> statList = new ArrayList<>();
    private ArrayList<RepeatingTimer> timerList = new ArrayList<>();

    public Stats() {
        super();
    }

    @Override
    public void start() {
        super.start();
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
     * Adds a new Stat object to the Stats Component.
     * @param name
     * @param initCurrValue
     * @param initMaxValue
     */
    public Stat addStat(String name, float initCurrValue, float initMaxValue){
        Stat stat = new Stat(name, this, initCurrValue, initMaxValue);
        this.statMap.put(name, stat);
        this.statList.add(stat);
        return stat;
    }

    /**
     * Gets a Stat from the statMap.
     * @param name The name of the stat.
     * @return The Stat object referenced by 'name'. Null if it doesn't exist.
     */
    public Stat getStat(String name){
        if(this.statMap.containsKey(name))
            return this.statMap.get(name);

        return null;
    }

    public final ArrayList<Stat> getStatList(){
        return this.statList;
    }

    public void clearAllStats(){
        this.statMap.clear();
    }

    @Override
    public void destroy() {
        super.destroy();
        statMap.clear();
        statList.clear();
        timerList.clear();
    }

    public static class Stat{
        public Functional.Callback onZero, onFull;

        public String name;
        private Stats stats;
        private float current, max;

        public Stat(String name, Stats stats, float current, float max) {
            this.name = name;
            this.current = current;
            this.max = max;
            this.stats = stats;

            onZero = stats.owner::setToDestroy;
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
        public float getCurrVal(){
            return this.current;
        }

        /**
         * @return The max value of this Stat.
         */
        public float getMaxVal(){
            return this.max;
        }
    }

}
