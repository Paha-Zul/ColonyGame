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
 * <p> A Task that will find the closest Entity on the map and assign it to the 'target' field of the blackboard. The 'successCriteria' field of control.callbacks
 * will be used to determine if the Entity is valid for using or not. In short...</p>
 *
 * <ul>
 * <li>Uses: control.callbacks.successCriteria to determine valid target. This will have an Entity parameter so a cast is needed.</li>
 * <li>Assigns: task.blackBoard.target the valid Entity that passed the criteria check.</li>
 * </ul>
 */
public class FindClosestEntity extends LeafTask{
    private boolean done = false, failed = false;

    public FindClosestEntity(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        this.done = false; //Need to reset this on start or else when we reset this behaviour, we get errors.
        this.getClosestResource();
    }

    private void getClosestResource(){
        Grid.GridInstance grid = ColonyGame.worldGrid;
        Grid.Node[][] gridMap = grid.getGrid();
        Functional.Callback getClosestResource = () -> {
            Grid.Node currNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.getEntityOwner()); //Get the nod we are standing on.
            boolean finished = false; //Flag
            int radius = 0; //The radius to start at and keep track of.

            //If we couldn't get our node we are standing on, something went wrong. Give up.
            if(currNode == null){
                this.failed = true;
                this.done = true;
                this.blackBoard.target = null;
                return;
            }

            //While the flag is still false, go!
            while(!finished) {
                //Get the starting and ending bounds.
                int startX = (currNode.getX() - radius < 0) ? -1 : currNode.getX() - radius;
                int endX = (currNode.getX() + radius >= gridMap.length) ? gridMap.length : currNode.getX() + radius;
                int startY = (currNode.getY() - radius < 0) ? -1 : currNode.getY() - radius;
                int endY = (currNode.getY() + radius >= gridMap[currNode.getX()].length) ? gridMap.length : currNode.getY() + radius;

                finished = true;

                //Loops over the nodes in the radius
                for (int col = startX; col <= endX; col++){
                    for(int row = startY; row <= endY; row++){

                        //If it's not on the edge, simply ignore it and continue.
                        if(!(col == startX || col == endX || row == startY || row == endY))
                            continue;

                        //If we try to get the node and it's null, continue.
                        Grid.Node node = this.blackBoard.colonyGrid.getNode(col, row);
                        if(node == null || grid.getVisibilityMap()[col][row].getVisibility() == Constants.VISIBILITY_UNEXPLORED)
                            continue;

                        finished = false; //Set this to false. We still have places to check obviously!

                        //Loop over the Entity list in the current node and try to an entity that matches the successCriteria.
                        for(Entity entity : node.getEntityList()) {
                            //If there is no callback or the callback passes, finish successfully.
                            if(control.callbacks == null || control.callbacks.successCriteria == null || control.callbacks.successCriteria.test(entity)) {
                                //If we have a valid Entity, store it and finish with success.
                                this.blackBoard.target = entity;
                                this.done = true;
                                this.failed = false;
                                return;
                            }
                        }
                    }
                }

                radius++; //Increase the radius
            }

            //If we reach this area, we've covered everywhere we can. Fail this Task.
            this.blackBoard.target = null;
            this.failed = true;
            this.done = true;
        };

        ColonyGame.threadPool.submit(new CallbackRunnable(getClosestResource));
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //This needs to be kept out of the threaded method. Causes issues.
        if(this.done){
            if(failed) this.control.finishWithFailure();
            else this.control.finishWithSuccess();
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
