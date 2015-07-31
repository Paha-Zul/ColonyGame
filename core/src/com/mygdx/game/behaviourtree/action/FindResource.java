package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Resource;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.util.BlackBoard;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.Grid;
import com.mygdx.game.util.runnables.CallbackRunnable;

/**
 * Created by Paha on 7/31/2015.
 */
public class FindResource extends LeafTask {
    private volatile boolean failed = false, done = false;

    public FindResource(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && !blackBoard.resourceTypeTags.isEmpty();
    }

    @Override
    public void start() {
        super.start();

        Grid.GridInstance grid = ColonyGame.worldGrid;
        Grid.Node[][] gridMap = grid.getGrid();
        Functional.Callback getClosestResource = () -> {
            Grid.Node currNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.myManager.getEntityOwner()); //Get the nod we are standing on.
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
                            if(!entity.getTags().hasTag("resource")) continue; //If no resource tag, continue...
                            Resource resource = entity.getComponent(Resource.class); //Get the resource Component.

                            String[] blackTags = this.blackBoard.resourceTypeTags.getTagsAsString(); //Get the tags as a string array.
                            boolean notTaken = !resource.isTaken() || resource.getTaken() == this.blackBoard.myManager.getEntityOwner(); //Make sure it's not taken or we own it.
                            boolean hasTag = resource.resourceTypeTags.hasAnyTag(blackTags); //Check if the resource originally had any of the items we want.
                            boolean hasAvailableItem = resource.peekAvailableOnlyWanted(blackBoard.myInventory, blackTags); //Check if it has any still.

                            //If there is no callback or the callback passes, finish successfully.
                            if(notTaken && hasTag && hasAvailableItem) {
                                //If we have a valid Entity, store it and finish with success.
                                this.blackBoard.target = entity;
                                this.blackBoard.targetResource = resource;
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

        this.failed = false;
        this.done = false;
    }
}
