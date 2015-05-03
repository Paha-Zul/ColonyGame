package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.runnables.CallbackRunnable;
import com.mygdx.game.interfaces.Functional;

/**
 * Created by Paha on 2/17/2015.
 */
public class FindClosestUnexplored extends LeafTask{
    Entity target;

    public FindClosestUnexplored(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void start() {
        super.start();

        if(this.target == null)
            this.target = this.blackBoard.getEntityOwner();

        Grid.GridInstance grid = ColonyGame.worldGrid;
        Grid.Node[][] gridMap = grid.getGrid();
        Functional.Callback findClosestUnexplored = () -> {
            int radius = 0;
            float closestDst = 999999999999999f;
            Grid.Node closestNode = null;

            Grid.Node targetNode = this.blackBoard.colonyGrid.getNode(this.target);
            Grid.Node myNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.getEntityOwner());

            int xDiff = Math.abs(targetNode.getX() - myNode.getX());
            int yDiff = Math.abs(targetNode.getY() - myNode.getY());
            int minRadius = xDiff > yDiff ? xDiff : yDiff;
            minRadius+=2;

            while(closestNode == null || radius < minRadius) {
                //Some bounds stuff.
                int startX = targetNode.getX() - radius < 0 ? -1 : targetNode.getX() - radius;
                int endX = targetNode.getX() + radius >= gridMap.length ? -1 : targetNode.getX() + radius;
                int startY = targetNode.getY() - radius < 0 ? -1 : targetNode.getY() - radius;
                int endY = targetNode.getY() + radius >= gridMap[targetNode.getX()].length ? -1 : targetNode.getY() + radius;

                //So we don't keep checking the whole map or more than we need to.
                if(startX == -1 && endX == -1 && startY == -1 && endY == -1){
                    this.blackBoard.targetNode = null;
                    break;
                }

                //Iterate over the radius
                for(int x = startX; x <= endX; x++){
                    for(int y = startY; y <= endY; y++){
                        //Check if we are still on the gridMap.
                        Grid.Node tmpNode = this.blackBoard.colonyGrid.getNode(x, y);
                        if(tmpNode == null)
                            continue;

                        //Check terrain and visibility.
                        boolean avoid = grid.getNode(x, y).getTerrainTile().avoid;
                        int visibility = grid.getVisibilityMap()[x][y].getVisibility();
                        if(visibility != Constants.VISIBILITY_UNEXPLORED || avoid)
                            continue;


                        //Get the distance from the current node to the tmpNode on the graph. If the closestNode is null or the dst is less than the closestDst, assign a new node!
                        float dstToMe = Math.abs(myNode.getX() - tmpNode.getX()) + Math.abs(myNode.getY() - tmpNode.getY());
                        float dstToTarget = Math.abs(targetNode.getX() - tmpNode.getX()) + Math.abs(targetNode.getY() - tmpNode.getY());
                        if(closestNode == null || dstToMe + dstToTarget < closestDst){
                            closestNode = tmpNode;
                            closestDst = dstToMe + dstToTarget;
                        }
                    }
                }

                radius++;
            }

            this.blackBoard.targetNode = closestNode;
            if(this.blackBoard.targetNode == null)
                this.control.finishWithFailure();
            else
                this.control.finishWithSuccess();
        };

        ColonyGame.threadPool.submit(new CallbackRunnable(findClosestUnexplored));
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
