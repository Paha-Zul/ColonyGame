package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Interactable extends Component{
    public String type;

    public Resource resource;
    public Colony colony;
    public Colonist colonist;
    public Animal animal;

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

        if(this.type == "humanoid") {
            this.colonist = this.owner.getComponent(Colonist.class);
        }

        if(this.type == "colony") {
            this.colony = this.owner.getComponent(Colony.class);
        }

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
}
