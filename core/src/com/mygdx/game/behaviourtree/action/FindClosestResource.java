package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Resource;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.ItemManager;
import com.mygdx.game.helpers.Profiler;
import com.mygdx.game.interfaces.Functional;

/**
 * Created by Paha on 1/29/2015.
 */
public class FindClosestResource extends LeafTask{
    private String itemName;

    public FindClosestResource(String name, BlackBoard blackBoard, String itemName) {
        super(name, blackBoard);

        this.itemName = itemName;
    }

    @Override
    public boolean check() {
        return ItemManager.doesItemExist(this.itemName);
    }

    @Override
    public void start() {
        super.start();

        Profiler.begin("FindClosestResource");
        this.getClosestResource();
        Profiler.end();

//        if(this.blackBoard.target == null)
//            System.out.println("[FindClosestResource]Null resource");
//        else if(this.blackBoard.target.transform == null)
//            System.out.println("[FindClosestResource]Null resource transform on "+this.blackBoard.target.name);


        this.control.finishWithSuccess();
    }

    private void getClosestResource(){
        Functional.PerformAndGet<Entity, Grid.Node[][]> getClosestResource = grid -> {
            Entity closest = null;
            Grid.Node currNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.getEntityOwner());
            boolean finished = false;
            int radius = 0;

            if(currNode == null){
                this.control.finishWithFailure();
                return null;
            }

            while(!finished) {
                int startX = currNode.getCol() - radius;
                int endX = currNode.getCol() + radius;
                int startY = currNode.getRow() - radius;
                int endY = currNode.getRow() + radius;

                finished = true;

                //Loops over the nodes in the radius
                for (int col = startX; col <= endX; col++){
                    for(int row = startY; row <= endY; row++){

                        //If it's not on the edge, simply ignore it and continue.
                        if(!(col == startX || col == endX || row == startY || row == endY))
                            continue;

                        //If we try to get the node and it's null, continue.
                        Grid.Node node = this.blackBoard.colonyGrid.getNode(col, row);
                        if(node == null)
                            continue;

                        finished = false; //Set this to false. We still have places to check obviously!

                        //Loop over the Entity list in the current node and try to find a tree.
                        for(Entity entity : node.getEntityList()) {
                            if (entity.entityType == Constants.ENTITY_RESOURCE) {
                                if(!entity.getComponent(Resource.class).isTaken())
                                    return entity;
                            }
                        }
                    }
                }

                radius++; //Increase the radius
            }

            return closest;
        };

        this.blackBoard.target = this.blackBoard.colonyGrid.performAndGet(getClosestResource);
        if(this.blackBoard.target == null){
            this.control.finishWithFailure();
            return;
        }
        this.blackBoard.targetNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.target);
        this.blackBoard.targetResource = this.blackBoard.target.getComponent(Resource.class);
        this.blackBoard.targetResource.setTaken(true);
    }

    private void getClosestStockpile(){
        Functional.PerformAndGet<Entity, Grid.Node[][]> pg = grid -> {
            Entity closest = null;
            Grid.Node currNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.getEntityOwner());
            boolean finished = false;
            int radius = 1;

            while(!finished) {
                int startX = currNode.getCol() - radius;
                int endX = currNode.getCol() + radius;
                int startY = currNode.getRow() - radius;
                int endY = currNode.getRow() + radius;

                finished = true;

                for (int col = startX; col <= endX; col++){
                    for(int row = startY; row <= endY; row++){

                        if((col != startX && col != endX) || (row != startY && row != endY))
                            continue;

                        Grid.Node node = this.blackBoard.colonyGrid.getNode(col, row);
                        if(node == null)
                            continue;

                        finished = false;

                        for(Entity entity : node.getEntityList()) {
                            if (entity.entityType == Constants.ENTITY_RESOURCE) {
                                return entity;
                            }
                        }
                    }
                }

                radius++;
            }

            return closest;
        };
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
