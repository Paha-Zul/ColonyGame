package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;
import com.mygdx.game.interfaces.IInteractable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Interactable extends Component{
    public String type;

    public IInteractable interactable;

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
            this.interactable = this.owner.getComponent(Resource.class);

        if(this.type == "humanoid") {
            this.interactable = this.owner.getComponent(Colonist.class);
        }

        if(this.type == "colony") {
            this.interactable = this.owner.getComponent(Colony.class);
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
    }
}
