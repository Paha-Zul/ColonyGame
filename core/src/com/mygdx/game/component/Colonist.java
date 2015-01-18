package com.mygdx.game.component;

import com.mygdx.game.component.collider.Colony;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colonist extends Component{
    private Colony colony;

    public Colonist() {
        super();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
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
}
