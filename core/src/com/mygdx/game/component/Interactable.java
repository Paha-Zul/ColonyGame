package com.mygdx.game.component;

/**
 * Created by Paha on 1/10/2015.
 */
public class Interactable extends Component{
    public String type;

    public Resource resource;
    public Health health;

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
