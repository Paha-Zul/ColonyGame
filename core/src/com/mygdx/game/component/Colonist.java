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
    public void display(@NotNull Rectangle rect, SpriteBatch batch, String name, GUI.GUIStyle style) {
        if(rect == null) {
            if (name.equals("path"))
                this.manager.display(0, 0, 0, 0, batch, name, style);
        }else
            this.display(rect.x, rect.y + rect.height - 10, rect.width, rect.height, batch, name, style);
    }

    @Override
    public void display(float x, float y, float width, float height, SpriteBatch batch, String name, GUI.GUIStyle style) {
        switch (name) {
            //Draw name and current task.
            case "general":
                GUI.Label(this.owner.name, batch, x + width / 2, y, true, style);
                GUI.Label("Current Task: " + this.manager.getCurrentTaskName(), batch, x + width / 2, y - 20, true, style);
                break;

            //Draw the order buttons.
            case "orders":
                this.gatherButton.set(x, y - 70, 35, 35);
                if (GUI.Button(this.gatherButton, "", batch, GUI.gatherGUIStyle))
                    this.manager.gather();

                this.gatherButton.set(x + 40, y - 70, 35, 35);
                if (GUI.Button(this.gatherButton, "", batch, GUI.exploreGUIStyle)) {
                    this.manager.explore();
                }
                break;

            //Draw health bars.
            case "stats":
                stats.display(x+5, y, width, height, batch, name, style);
                break;

            //Draw inventory.
            case "inventory":
                this.inventory.display(x, y, width, height, batch, name,style);
                break;

            //Draw the path.
            case "path":
                this.manager.display(x, y, width, height, batch, name, style);
                break;
        }

        //This should be called at the end of all processing to reset the batch to the UI screen. Otherwise, UI stuff may get drawn on the world and not the UI.
        batch.setProjectionMatrix(ColonyGame.UICamera.combined);
    }
}
