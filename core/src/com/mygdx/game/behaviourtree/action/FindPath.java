package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Enterable;
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
        Vector2 target;
        if(this.blackBoard.targetNode != null) {
            targetNode = this.blackBoard.targetNode;
            target = new Vector2(targetNode.getXCenter(), targetNode.getYCenter());
        }else {
            Enterable enterable = this.blackBoard.target.getComponent(Enterable.class);
            if(enterable == null) {
                targetNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.target);
                target = this.blackBoard.target.getTransform().getPosition();
            }else {
                targetNode = this.blackBoard.colonyGrid.getNode(enterable.getEnterPositions()[0]);
                target = enterable.getEnterPositions()[0];
            }
        }

        Pathfinder.GetInstance().findPath(new Vector2(startNode.getXCenter(), startNode.getYCenter()), target, path -> {
            this.blackBoard.path = path;
            this.control.finishWithSuccess();
        });
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
