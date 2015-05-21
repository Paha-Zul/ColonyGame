package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.util.BlackBoard;
import com.mygdx.game.util.Grid;
import com.mygdx.game.util.runnables.CallbackRunnable;

/**
 * Created by Paha on 4/6/2015.
 */
public class FindClosestTile extends LeafTask{
    public FindClosestTile(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();
        this.blackBoard.target = null;
        this.blackBoard.targetNode = null;

        Entity target = this.blackBoard.myManager.getEntityOwner();
        Grid.Node[][] grid = ColonyGame.worldGrid.getGrid();

        Functional.Callback findClosestUnexplored = () -> {
            int radius = 0; //Start off with 0 radius.
            float closestDst = 999999999999999f; //Start with a really high distance...
            Grid.Node closestNode = null; //Closest
            Grid.Node myNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.myManager.getEntityOwner());

            while(closestNode == null) {
                //Get the bounds for the search...
                int startX = myNode.getX() - radius < 0 ? -1 : myNode.getX() - radius;
                int endX = myNode.getX() + radius >= grid.length ? -1 : myNode.getX() + radius;
                int startY = myNode.getY() - radius < 0 ? -1 : myNode.getY() - radius;
                int endY = myNode.getY() + radius >= grid[myNode.getX()].length ? -1 : myNode.getY() + radius;

                //If all bounds are out of bounds, let's just fail this...
                if(startX == -1 && endX == -1 && startY == -1 && endY == -1){
                    this.blackBoard.targetNode = null;
                    break;
                }

                //For the area... let's search some stuff!
                for(int x = startX; x <= endX; x++){
                    for(int y = startY; y <= endY; y++){
                        //Check if we are still on the grid.
                        Grid.Node tmpNode = this.blackBoard.colonyGrid.getNode(x, y);
                        if(tmpNode == null)
                            continue;

                        //Check the success criteria.
                        if(!this.control.callbacks.successCriteria.test(tmpNode))
                            continue;

                        //Get the distance from the current node to the tmpNode on the graph. If the closestNode is null or the dst is less than the closestDst, assign a new node!
                        float dst = Math.abs(myNode.getX() - tmpNode.getX()) + Math.abs(myNode.getY() - tmpNode.getY());
                        if(closestNode == null || dst < closestDst){
                            closestNode = tmpNode;
                            closestDst = dst;
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
