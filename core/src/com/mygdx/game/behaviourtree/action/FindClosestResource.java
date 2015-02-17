package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Resource;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.*;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
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

        if(this.blackBoard.target == null){
            this.control.finishWithFailure();
            Vector2 pos = this.blackBoard.getEntityOwner().transform.getPosition();
            new FloatingText("Couldn't find a nearby resource!", new Vector2(pos.x, pos.y+10), new Vector2(pos.x, pos.y+40), 1.5f, 0.8f);
            return;
        }

        this.blackBoard.targetNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.target);
        this.blackBoard.targetResource = this.blackBoard.target.getComponent(Resource.class);
        this.blackBoard.targetResource.setTaken(true);

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
                int startX = (currNode.getCol() - radius < 0) ? 0 : currNode.getCol() - radius;
                int endX = (currNode.getCol() + radius >= grid.length) ? grid.length - 1 : currNode.getCol() + radius;
                int startY = (currNode.getRow() - radius < 0) ? 0 : currNode.getRow() - radius;
                int endY = (currNode.getRow() + radius >= grid[currNode.getCol()].length) ? grid.length-1 : currNode.getRow() + radius;

                finished = true;

                //Loops over the nodes in the radius
                for (int col = startX; col <= endX; col++){
                    for(int row = startY; row <= endY; row++){

                        //If it's not on the edge, simply ignore it and continue.
                        if(!(col == startX || col == endX || row == startY || row == endY))
                            continue;

                        //If we try to get the node and it's null, continue.
                        Grid.Node node = this.blackBoard.colonyGrid.getNode(col, row);
                        if(node == null || WorldGen.getVisibilityMap()[col][row].getVisibility() == Constants.VISIBILITY_UNEXPLORED)
                            continue;

                        finished = false; //Set this to false. We still have places to check obviously!

                        //Loop over the Entity list in the current node and try to find a tree.
                        for(Entity entity : node.getEntityList()) {
                            if (entity.hasTag(Constants.ENTITY_RESOURCE)) {
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
