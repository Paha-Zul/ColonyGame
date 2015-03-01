package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.Gdx;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.Functional;

/**
 * Created by Paha on 2/17/2015.
 */
public class FindClosestUnexplored extends LeafTask{
    Entity target;

    public FindClosestUnexplored(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

    }

    public FindClosestUnexplored(String name, BlackBoard blackBoard, Entity target) {
        this(name, blackBoard);

        this.target = target;
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

        Functional.Perform<Grid.Node[][]> findClosestUnexplored = grid -> {
            int radius = 0;
            float closestDst = 999999999999999f;
            boolean finished = false;
            Grid.Node closestNode = null;
            Grid.Node targetNode = this.blackBoard.colonyGrid.getNode(this.target);
            Grid.Node myNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.getEntityOwner());
            int xDiff = Math.abs(targetNode.getCol() - myNode.getCol());
            int yDiff = Math.abs(targetNode.getRow() - myNode.getRow());
            int minRadius = xDiff > yDiff ? xDiff : yDiff;
            minRadius+=2;

            while(closestNode == null || radius < minRadius) {
                int startX = targetNode.getCol() - radius < 0 ? -1 : targetNode.getCol() - radius;
                int endX = targetNode.getCol() + radius >= grid.length ? grid.length : targetNode.getCol() + radius;
                int startY = targetNode.getRow() - radius < 0 ? -1 : targetNode.getRow() - radius;
                int endY = targetNode.getRow() + radius >= grid[targetNode.getCol()].length ? grid[targetNode.getCol()].length : targetNode.getRow() + radius;
                finished = true; //Reset the flag.

                for(int x = startX; x <= endX; x++){
                    for(int y = startY; y <= endY; y++){
                        //Check if we are still on the grid.
                        Grid.Node tmpNode = this.blackBoard.colonyGrid.getNode(x, y);
                        if(tmpNode == null)
                            continue;

                        //Check terrain and visibility.
                        boolean avoid = WorldGen.getInstance().getNode(x,y).avoid;
                        int visibility = WorldGen.getInstance().getVisibilityMap()[x][y].getVisibility();
                        if(visibility != Constants.VISIBILITY_UNEXPLORED || avoid)
                            continue;


                        finished = false;

                        //Get the distance from the current node to the tmpNode on the graph. If the closestNode is null or the dst is less than the closestDst, assign a new node!
                        float dst = Math.abs(myNode.getCol() - tmpNode.getCol()) + Math.abs(myNode.getRow() - tmpNode.getRow());
                        if(closestNode == null || dst < closestDst){
                            closestNode = tmpNode;
                            closestDst = dst;
                        }
                    }
                }

                radius++;
            }

            this.blackBoard.targetNode = closestNode;
        };

        this.blackBoard.colonyGrid.perform(findClosestUnexplored);
        if(this.blackBoard.targetNode == null)
            this.control.finishWithFailure();
        else
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
