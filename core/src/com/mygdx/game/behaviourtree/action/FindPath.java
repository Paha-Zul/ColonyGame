package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Enterable;
import com.mygdx.game.util.BlackBoard;
import com.mygdx.game.util.Grid;
import com.mygdx.game.util.Pathfinder;

/**
 * A Task that finds a path from where the Entity owner of this task is standing to a target Entity or target node.
 */
public class FindPath extends LeafTask {

    public FindPath(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        //Get the start node and target node.
        Grid.Node startNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.myManager.getEntityOwner());
        Grid.Node targetNode;
        if(this.blackBoard.targetNode != null)
            targetNode = this.blackBoard.targetNode;
        else {
            Enterable enterable = this.blackBoard.target.getComponent(Enterable.class);
            if(enterable == null)
                targetNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.target);
            else {
                targetNode = this.blackBoard.colonyGrid.getNode(enterable.getEnterPositions()[0]);
            }
        }

        this.blackBoard.path = Pathfinder.findPath(startNode, targetNode);
        this.control.finishWithSuccess();
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
