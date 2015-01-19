package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Paha on 1/12/2015.
 */
public class Health extends Component implements IDisplayable{
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

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name) {
        float x = rect.getX();
        float y = rect.getY() + rect.getHeight();

        if(name == "general"){
            GUI.Text("MaxHealth: "+this.maxHealth, batch, x, y);
            y-=20;
            GUI.Text("CurrHealth: "+this.maxHealth, batch, x, y);
            y-=20;
        }
    }
}
