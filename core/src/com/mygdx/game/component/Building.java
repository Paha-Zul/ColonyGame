package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.util.Tags;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Paha on 5/19/2015.
 */
public class Building extends Component implements IOwnable, IInteractable{
    @JsonIgnore
    private Timer timer;
    @JsonIgnore
    private Constructable constructable;
    @JsonIgnore
    private Colony colonyOwner;
    @JsonIgnore
    private Inventory inventory;
    @JsonProperty
    public final Tags buildingTags = new Tags("building");

    public Building(){

    }

    @Override
    public void start() {
        super.start();

        this.load();
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        this.inventory = this.getComponent(Inventory.class);
        this.inventory.setMaxAmount(-1);
        this.constructable = this.getComponent(Constructable.class);
        if(this.owner.getTags().hasTag("constructing")){
            this.timer = new RepeatingTimer(1000, this.constructable::build);
            Sprite sprite = this.owner.getGraphicIdentity().getSprite();
            this.owner.getGraphicIdentity().getSprite().setColor(1, 1, 1, 0.1f);
            sprite.setAlpha(0.1f);
            System.out.println("Such: "+sprite.getColor().a);
        }else{
            this.setActive(false);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if(this.timer != null)
            this.timer.update(delta);
    }

    @Override
    public void addedToColony(Colony colony) {
        this.colonyOwner = colony;
    }

    @Override
    @JsonIgnore
    public Colony getOwningColony() {
        return this.colonyOwner;
    }

    @Override
    @JsonIgnore
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    @JsonIgnore
    public Stats getStats() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getStatsText() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return this.owner.name;
    }

    @Override
    @JsonIgnore
    public BehaviourManagerComp getBehManager() {
        return null;
    }

    @Override
    @JsonIgnore
    public Component getComponent() {
        return this;
    }

    @Override
    @JsonIgnore
    public Constructable getConstructable() {
        return this.constructable;
    }
}
