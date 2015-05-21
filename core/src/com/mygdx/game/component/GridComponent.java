package com.mygdx.game.component;

import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.ListHolder;

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
    public void init(double id) {
        super.init(id);

        //Gets a node to start.
        this.currNode = this.grid.addToGrid(ListHolder.getIdToEntityMap().get(id));
        this.grid.addViewer(this.currNode, this.exploreRadius);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.currNode = this.grid.checkNode(this.currNode, ListHolder.getIdToEntityMap().get(this.ownerID), true, exploreRadius);
    }

    public Grid.Node getCurrNode(){
        return this.currNode;
    }

    @Override
    public void destroy(double id) {
        this.currNode.removeEntity(ListHolder.getIdToEntityMap().get(this.ownerID));

        super.destroy(id);
    }
}
