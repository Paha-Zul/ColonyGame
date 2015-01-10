package com.mygdx.game.component;

/**
 * Created by Paha on 1/10/2015.
 */
public class Interactable extends Component{
    public String type;

    public Resource resource;

    public Interactable() {
        super();


    }

    @Override
    public void start() {
        super.start();

        if(this.type == "resource");
            this.resource = this.owner.getComponent(Resource.class);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
