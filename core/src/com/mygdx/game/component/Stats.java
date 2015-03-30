package com.mygdx.game.component;

import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;

/**
 * Stat Component for an Entity that includes stats like health, hunger, thirst, etc. Also uses the update() method to
 * increment/decrement stats like lowering health due to hunger/thirst.
 */
public class Stats extends Component{
    private float maxHealth=100, currHealth=100;
    private int food = 15, water = 100, energy = 100;

    private Timer waterTimer, foodTimer, healthTimer;

    public Stats() {
        super();
    }

    @Override
    public void start() {
        super.start();

        waterTimer = new RepeatingTimer(100f, ()->addWater(-1));
        foodTimer = new RepeatingTimer(2f, ()->addFood(-1));
        healthTimer = new RepeatingTimer(10f, ()->{
            if(this.getFood() > 0) this.addHealth(1);
        });
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        waterTimer.update(delta);
        foodTimer.update(delta);
        healthTimer.update(delta);
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getCurrHealth() {
        return currHealth;
    }

    public int getFood() {
        return food;
    }

    public int getWater() {
        return water;
    }

    public int getEnergy() {
        return energy;
    }

    public void addHealth(float health){
        this.currHealth += health;
        if(currHealth > 100) currHealth = 100;
        else if(currHealth <= 0) currHealth = 0;
    }

    public void addFood(int food){
        this.food += food;
        if(this.food > 100) this.food = 100;
        else if(this.food <= 0) {
            this.food = 0;
            this.addHealth(food);
        }
    }

    public void addWater(int water){
        this.water += water;
        if(this.water > 100) this.water = 100;
        else if(this.water <= 0) {
            this.water = 0;
            this.addHealth(water);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

}
