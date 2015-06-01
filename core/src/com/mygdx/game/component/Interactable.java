package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDelayedDestroyable;
import com.mygdx.game.interfaces.IInteractable;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Paha on 1/10/2015.
 */
public class Interactable extends Component{
    @JsonProperty
    public String interType;
    @JsonIgnore
    private IInteractable interactable;

    public Interactable() {
        super();
        this.setActive(false);
    }

    @Override
    public void start() {
        super.start();

        load();
    }


    @Override
    public void save() {

    }

    @Override
    public void load() {
        this.setInterType(interType);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public void setInterType(String interType) {
        this.interType = interType;

        if(this.started) {
            if (interType.equals("resource"))
                this.interactable = this.owner.getComponent(Resource.class);
            else if (interType.equals("humanoid"))
                this.interactable = this.owner.getComponent(Colonist.class);
            else if (interType.equals("colony"))
                this.interactable = this.owner.getComponent(Colony.class);
            else if (interType.equals("animal"))
                this.interactable = this.owner.getComponent(Animal.class);
            else if (interType.equals("building"))
                this.interactable = this.owner.getComponent(Building.class);
        }
    }

    @JsonIgnore
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
