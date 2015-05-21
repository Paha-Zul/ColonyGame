package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.Grid;

/**
 * Created by Paha on 1/17/2015.
 */
public class GridComponent extends Component{
    private int gridType;
    private Grid.Node currNode;
    private Grid.GridInstance grid;
    public int exploreRadius = 3;

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
        this.currNode = this.grid.addToGrid(this.owner);
        this.grid.addViewer(this.currNode, this.exploreRadius);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.currNode = this.grid.checkNode(this.currNode, this.owner, true, exploreRadius);
    }

    public Grid.Node getCurrNode(){
        return this.currNode;
    }

    @Override
    public void destroy(Entity destroyer) {
        this.currNode.removeEntity(this.owner);

        super.destroy(destroyer);
    }
}
