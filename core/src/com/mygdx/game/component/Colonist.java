package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;
import com.sun.istack.internal.NotNull;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colonist extends Component implements IDisplayable{
    private Colony colony;
    private Inventory inventory;
    private Stats stats;
    private BehaviourManagerComp manager;

    private Rectangle gatherButton = new Rectangle();

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
    public void display(@NotNull Rectangle rect, SpriteBatch batch, String name) {
        float x=0, y=0, height=0, width=0;

        if(rect != null){
            x = rect.getX();
            y = rect.getY() + rect.getHeight() - 5;
            height = rect.getHeight();
            width = rect.getWidth();
        }

        if(name.equals("general")){
            GUI.Label("Name: " + this.owner.name, batch, x + width / 2, y, true);
            GUI.Label("Current Task: "+this.manager.getCurrentTaskName(), batch, x + width/2, y - 20, true);

            this.gatherButton.set(x, y - 70, 35, 35);
            if(GUI.Button(this.gatherButton, "", batch, GUI.gatherGUIStyle))
                this.manager.gather();

            this.gatherButton.set(x + 40, y - 70, 35, 35);
            if(GUI.Button(this.gatherButton, "", batch, GUI.exploreGUIStyle)) {
                this.manager.explore();
            }

        }else if(name.equals("health")){
            stats.display(rect, batch, name);
        }else if(name.equals("inventory")){
            this.inventory.display(rect, batch, name);
        }else if(name.equals("path")){
            this.manager.display(rect, batch, name);
        }

        //This should be called at the end of all processing to reset the batch to the UI screen. Otherwise, UI stuff may get drawn on the world and not the UI.
        batch.setProjectionMatrix(ColonyGame.UICamera.combined);
    }
}
