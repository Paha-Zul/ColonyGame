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
    private Grid grid;

    public GridComponent(int gridType, Grid grid) {
        super();

        this.grid = grid;

        this.gridType = gridType;
        if(this.gridType == Constants.GRIDSTATIC)
            this.setActive(false);

    }

    @Override
    public void start() {
        super.start();

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

    @Override
    public void destroy() {
        super.destroy();
    }
}
