package com.mygdx.game.component;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component{
    public int maxResources = 100;
    public int currResources = 100;

    public Resource() {
        super();

        this.setActive(false);
    }

    @Override
    public void start() {
        super.start();
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
