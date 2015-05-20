package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDelayedDestroyable;
import com.mygdx.game.interfaces.IInteractable;

/**
 * Created by Paha on 1/10/2015.
 */
public class Interactable extends Component{
    public String interType;

    private IInteractable interactable;

    public Interactable(String type) {
        super();
        this.interType = type;
    }

    public Interactable(){
        this("NothingType");
        this.setActive(false);
    }

    @Override
    public void start() {
        super.start();

        this.changeType(interType);
    }

    public void changeType(String newType){
        if(newType.equals("resource"))
            this.interactable = this.owner.getComponent(Resource.class);
        else if(newType.equals("humanoid"))
            this.interactable = this.owner.getComponent(Colonist.class);
        else if(newType.equals("colony"))
            this.interactable = this.owner.getComponent(Colony.class);
        else if(newType.equals("animal"))
            this.interactable = this.owner.getComponent(Animal.class);
        else if(newType.equals("building"))
            this.interactable = this.owner.getComponent(Building.class);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public IInteractable getInteractable(){
        if(interactable != null && (((IDelayedDestroyable)interactable).isDestroyed() || ((IDelayedDestroyable)interactable).isSetToBeDestroyed()))
            interactable = null;

        return this.interactable;
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);
        this.interType = null;
    }
}
