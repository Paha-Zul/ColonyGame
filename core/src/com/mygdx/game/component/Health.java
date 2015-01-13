package com.mygdx.game.component;

/**
 * Created by Paha on 1/12/2015.
 */
public class Health extends Component{
    private float maxHealth, currHealth;

    public Health(float maxHealth) {
        super();

        this.maxHealth = this.currHealth = maxHealth;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getCurrHealth() {
        return currHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setCurrHealth(float currHealth) {
        this.currHealth = currHealth;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
