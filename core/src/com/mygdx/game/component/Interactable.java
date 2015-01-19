package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Interactable extends Component implements IDisplayable{
    public String type;

    public Resource resource;
    public Health health;
    public Colony colony;

    public Interactable(String type) {
        super();
        this.type = type;
    }

    public Interactable(){
        this("NothingType");
    }

    @Override
    public void start() {
        super.start();

        if(this.type == "resource")
            this.resource = this.owner.getComponent(Resource.class);

        if(this.type == "humanoid")
            this.health = this.owner.getComponent(Health.class);

        if(this.type == "colony")
            this.colony = this.owner.getComponent(Colony.class);

    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void destroy() {
        super.destroy();
        this.type = null;
        this.resource = null;
    }

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name) {
        float x = rect.getX();
        float y = rect.getY() + rect.getHeight();

        if(name == "general"){
            GUI.Text("Name: "+this.owner.name, batch, x, y);
            y-=20;
        }
    }
}
