package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Resource;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.*;
import com.mygdx.game.helpers.managers.ItemManager;
import com.mygdx.game.helpers.runnables.CallbackRunnable;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.Functional;

/**
 * Created by Paha on 1/29/2015.
 */
public class FindClosestEntity extends LeafTask{
    private boolean done = false, failed = false;
    private int[] tags;

    public FindClosestEntity(String name, BlackBoard blackBoard, int... tags) {
        super(name, blackBoard);

        this.tags = tags;
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
        Grid.Node[][] grid = ColonyGame.worldGrid.getGrid();
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
                int startX = (currNode.getCol() - radius < 0) ? -1 : currNode.getCol() - radius;
                int endX = (currNode.getCol() + radius >= grid.length) ? grid.length : currNode.getCol() + radius;
                int startY = (currNode.getRow() - radius < 0) ? -1 : currNode.getRow() - radius;
                int endY = (currNode.getRow() + radius >= grid[currNode.getCol()].length) ? grid.length : currNode.getRow() + radius;

                finished = true;

                //Loops over the nodes in the radius
                for (int col = startX; col <= endX; col++){
                    for(int row = startY; row <= endY; row++){

                        //If it's not on the edge, simply ignore it and continue.
                        if(!(col == startX || col == endX || row == startY || row == endY))
                            continue;

                        //If we try to get the node and it's null, continue.
                        Grid.Node node = this.blackBoard.colonyGrid.getNode(col, row);
                        if(node == null || WorldGen.getInstance().getVisibilityMap()[col][row].getVisibility() == Constants.VISIBILITY_UNEXPLORED)
                            continue;

                        finished = false; //Set this to false. We still have places to check obviously!

                        //Loop over the Entity list in the current node and try to an entity that matches the criteria.
                        for(Entity entity : node.getEntityList()) {
                            if (entity.hasTags(tags)) { //If it has the tags required.
                                //If there is no callback or the callback passes, finish successfully.
                                if(control.callbacks == null || control.callbacks.criteria == null || control.callbacks.criteria.criteria(entity)) {
                                    //If we have a valid Entity, store it and finish with success.
                                    this.blackBoard.target = entity;
                                    this.done = true;
                                    this.failed = false;
                                    return;
                                }
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
