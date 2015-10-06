package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.Grid;
import com.mygdx.game.util.runnables.CallbackRunnable;

/**
 * Created by Paha on 2/17/2015.
 */
public class FindClosestUnexplored extends LeafTask{
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

        if(this.blackBoard.target == null)
            this.blackBoard.target = this.blackBoard.myManager.getEntityOwner();

        Grid.GridInstance grid = ColonyGame.instance.worldGrid;
        Grid.Node[][] gridMap = grid.getGrid();
        Functional.Callback findClosestUnexplored = () -> {
            int radius = 0;
            float closestDst = 999999999999999f;
            Grid.Node closestNode = null;
            boolean wholeMap = false;

            Grid.Node targetNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.target);
            Grid.Node myNode = this.blackBoard.colonyGrid.getNode(this.getBlackboard().myManager.getEntityOwner());

            while(closestNode == null) {
                //Some bounds stuff.
                int startX = targetNode.getX() - radius < 0 ? -1 : targetNode.getX() - radius;
                int endX = targetNode.getX() + radius >= gridMap.length ? -1 : targetNode.getX() + radius;
                int startY = targetNode.getY() - radius < 0 ? -1 : targetNode.getY() - radius;
                int endY = targetNode.getY() + radius >= gridMap[targetNode.getX()].length ? -1 : targetNode.getY() + radius;

                //So we don't keep checking the whole map or more than we need to.
                if(startX == -1 && endX == -1 && startY == -1 && endY == -1){
                    this.blackBoard.targetNode = null;
                    wholeMap = true;
                    break;
                }

                //Iterate over the radius
                for(int x = startX; x <= endX; x++){
                    for(int y = startY; y <= endY; y++){
                        //Check if we are still on the gridMap.
                        Grid.Node tmpNode = grid.getNode(x, y);
                        if(tmpNode == null) continue;

                        Grid.VisibilityTile visTile = grid.getVisibilityMap()[x][y];
                        Grid.TerrainTile terrainTile = tmpNode.getTerrainTile();
                        if(terrainTile == null || visTile == null) continue;

                        //Check terrain and visibility.
                        boolean avoid = terrainTile.tileRef.avoid;
                        int visibility = visTile.getVisibility();
                        if(visibility != Constants.VISIBILITY_UNEXPLORED || avoid)
                            continue;

                        //Get the distance from the current node to the tmpNode on the graph. If the closestNode is null or the dst is less than the closestDst, assign a new node!
                        float dst = Math.abs(targetNode.getX() - tmpNode.getX()) + Math.abs(targetNode.getY() - tmpNode.getY());
                        float dstToMe = Math.abs(myNode.getX() - tmpNode.getX()) + Math.abs(myNode.getY() - tmpNode.getY());
                        if(closestNode == null || dst + dstToMe < closestDst){
                            closestNode = tmpNode;
                            closestDst = dst + dstToMe;
                        }

                    }
                }

                radius++;
            }

            this.blackBoard.targetNode = closestNode;
            this.blackBoard.target = null;

            //If the 'wholeMap' flag is true, nothing could be found on the current available map and/or radius that we explored. Idle...
            if(wholeMap) this.blackBoard.myManager.changeTaskImmediate("idle");
            else
                if(this.blackBoard.targetNode == null) this.control.finishWithFailure();
                else this.control.finishWithSuccess();

        };

        ColonyGame.instance.threadPool.submit(new CallbackRunnable(findClosestUnexplored));
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
