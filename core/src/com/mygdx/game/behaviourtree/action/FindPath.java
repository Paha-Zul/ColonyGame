package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.helpers.Grid;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * A Task that finds a path from where the Entity owner of this task is standing to a target Entity or target node.
 */
public class FindPath extends LeafTask {

    public FindPath(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return control.callbacks.checkCriteria == null || control.callbacks.checkCriteria.criteria(this);
    }

    @Override
    public void start() {
        super.start();

        //If we have a target, get the target node here...
        if(this.blackBoard.target != null)
            this.blackBoard.targetNode = ColonyGame.worldGrid.getNode(this.blackBoard.target);

        this.blackBoard.path = getPath();
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

    public LinkedList<Vector2> getPath(){
        boolean found = false;
        Grid.PathNode target = null;
        Grid.Node targetNode;

        //If we have a target node in the blackboard, use it.
        if(this.blackBoard.targetNode != null)
            targetNode = this.blackBoard.targetNode;

        //Otherwise, if we have an Entity target, get the node from that.
        else if(this.blackBoard.target != null)
            targetNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.target);

        //Otherwise, end with failure.
        else{
            this.control.finishWithFailure();
            return new LinkedList<>();
        }

        //If our targetNode is still null somehow, fail and return.
        if(targetNode == null) {
            Gdx.app.log("[FindPath]:",this.blackBoard.getEntityOwner().name + "'s target is null");
            this.control.finishWithFailure();
            return null;
        }

        LinkedList<Vector2> path = new LinkedList<>(); //Will hold the end path.
        HashMap<Grid.Node, Grid.Node> visitedMap = new HashMap<>();

        //Priority queue that sorts by the lowest F value of the PathNode.
        PriorityQueue<Grid.PathNode> openList = new PriorityQueue<>((n1, n2) -> (int)((n1.getF() - n2.getF())*100));

        //Set the starting currNode and its G and H value.
        Grid.PathNode currNode = new Grid.PathNode(this.blackBoard.colonyGrid.getNode(this.blackBoard.getEntityOwner()));
        currNode.parentNode = null;
        currNode.G = 0;
        currNode.H = Math.abs(targetNode.getX() - currNode.x) + Math.abs(targetNode.getY() - currNode.y)*10;
        visitedMap.put(currNode.node, currNode.node);

        while(currNode != null){
            if(currNode.node == targetNode){
                found = true;
                target = currNode;
                currNode = null;
                break;
            }

            visitedMap.put(currNode.node, currNode.node);
            Grid.Node[] neighbors = this.blackBoard.colonyGrid.getNeighbors8(currNode.node);

            //Add all the (valid) neighbors to the openList.
            for (Grid.Node neighbor : neighbors) {
                if (neighbor == null || visitedMap.containsKey(neighbor)) continue;
                visitedMap.put(neighbor, neighbor);

                //Add the neighbor node to the visited map and open list.
                Grid.PathNode neighborNode = new Grid.PathNode(neighbor);

                //Set this neighbors values and add it to the openList.
                int[] dir = this.blackBoard.colonyGrid.getDirection(neighborNode.node, currNode.node);
                if (dir[0] == 0 || dir[1] == 0) //This is straight, not diagonal.
                    neighborNode.G = currNode.G + 10;
                else
                    neighborNode.G = currNode.G + 14; //Diagonal.

                //Get the TerrainTile and check if the tile is to be avoided.
                Grid.TerrainTile tile = ColonyGame.worldGrid.getNode(neighborNode.x, neighborNode.y).getTerrainTile();
                if (neighborNode.hasEnts() || (tile != null && tile.avoid))
                    neighborNode.B = 500;

                //Set the H value, add it to the openList, and make its parent the current Node.
                neighborNode.H = Math.abs(targetNode.getX() - currNode.x) + Math.abs(targetNode.getY() - currNode.y) * 10;

                openList.add(neighborNode);
                neighborNode.parentNode = currNode;
            }

            currNode = openList.poll();
        }

        float squareSize = this.blackBoard.colonyGrid.getSquareSize();
        //If a path was found, record the path.
        if(found) {
            currNode = target;
            while (currNode.parentNode != null) {
                path.add(new Vector2(currNode.x*squareSize + squareSize*0.5f, currNode.y*squareSize + squareSize*0.5f));
                currNode = currNode.parentNode;
            }
        }

        openList.clear();
        visitedMap.clear();
        return path;
    }
}
