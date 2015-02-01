package com.mygdx.game.component;

import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.Profiler;

/**
 * Created by Paha on 1/17/2015.
 */
public class GridComponent extends Component{
    private int gridType;
    private Grid.Node currNode;
    private Grid.GridInstance grid;

    public GridComponent(int gridType, Grid.GridInstance grid) {
        super();

        this.grid = grid;

        this.gridType = gridType;
        if(this.gridType == Constants.GRIDSTATIC)
            this.setActive(false);

    }

    @Override
    public void start() {
        super.start();

        //System.out.println("[GridComponent]Starting for "+this.owner.name);

        //Gets a node to start.
        this.currNode = this.grid.checkNode(this.currNode, this.owner);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        Profiler.begin("GridComponent update");

        this.currNode = this.grid.checkNode(this.currNode, this.owner);

        Profiler.end();
    }

    public Grid.Node getCurrNode(){
        return this.currNode;
    }

    @Override
    public void destroy() {
        this.currNode.removeEntity(this.owner);

        super.destroy();
    }
}
