package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.helpers.Grid;

/**
 * Created by Paha on 4/30/2015.
 */
public class FindNearbyTile extends LeafTask{
    public FindNearbyTile(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        Grid.GridInstance grid = this.blackBoard.colonyGrid;
        float myDisToTarget = Math.abs(blackBoard.getEntityOwner().transform.getPosition().x - blackBoard.target.transform.getPosition().x)
                + Math.abs(blackBoard.getEntityOwner().transform.getPosition().y - blackBoard.target.transform.getPosition().y);

        boolean result = grid.performOnNodeInRadius(control.callbacks.successCriteria, 1, grid.getIndex(blackBoard.getEntityOwner()));

        if(result) this.control.finishWithSuccess();
        else this.control.finishWithFailure();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void end() {
        super.end();
    }
}
