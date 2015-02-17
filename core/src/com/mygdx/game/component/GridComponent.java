package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
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
    private int exploreRadius = 3;

    public GridComponent(int gridType, Grid.GridInstance grid, int exploreRadius) {
        super();

        this.grid = grid;
        this.exploreRadius = exploreRadius;
        this.gridType = gridType;
        if(this.gridType == Constants.GRIDSTATIC)
            this.setActive(false);

    }

    @Override
    public void init(Entity owner) {
        super.init(owner);

        //Gets a node to start.
        this.currNode = this.grid.addToGrid(this.owner, exploreRadius);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        Profiler.begin("GridComponent update");

        this.currNode = this.grid.checkNode(this.currNode, this.owner, true, exploreRadius);

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
