package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.BehaviourManager;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colonist extends Component implements IDisplayable{
    private Colony colony;
    private Inventory inventory;
    private Stats stats;
    private BehaviourManagerComp manager;

    public Colonist() {
        super();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void start() {
        super.start();

        this.inventory = this.getComponent(Inventory.class);
        this.stats = this.getComponent(Stats.class);
        this.manager = this.getComponent(BehaviourManagerComp.class);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public Colony getColony() {
        return colony;
    }

    public void setColony(Colony colony) {
        this.colony = colony;
    }

    public Inventory getInventory(){
        return this.inventory;
    }

    public Stats getStats(){
        return this.stats;
    }

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name) {
        float x = 0;
        float y = 0;

        if(rect != null){
            x = rect.getX();
            y = rect.getY() + rect.getHeight() - 5;
        }

        if(name == "general"){
            GUI.Label("Name: "+this.owner.name, batch, rect.x + rect.getWidth()/2, rect.y + rect.getHeight() - 5, true);
        }else if(name == "health"){
            stats.display(rect, batch, name);
        }else if(name == "inventory"){
            this.inventory.display(rect, batch, name);
        }else if(name == "path"){
            this.manager.display(rect, batch, name);
        }
    }
}
