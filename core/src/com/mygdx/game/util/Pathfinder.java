package com.mygdx.game.util;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Created by Paha on 7/20/2015.
 */
public class Pathfinder {

    public static LinkedList<Vector2> findPath(Grid.Node startNode, Grid.Node targetNode){
        boolean found = false;
        Grid.PathNode target = null;

        //If our targetNode is still null somehow, fail and return.
        if(startNode == null) {
            return null;
        }

        LinkedList<Vector2> path = new LinkedList<>(); //Will hold the end path.
        HashMap<Grid.Node, Grid.Node> visitedMap = new HashMap<>();

        //Priority queue that sorts by the lowest F value of the PathNode.
        PriorityQueue<Grid.PathNode> openList = new PriorityQueue<>((n1, n2) -> (int)((n1.getF() - n2.getF())*100));

        //Set the first node and its G/H values
        Grid.PathNode currNode = new Grid.PathNode(startNode);
        currNode.parentNode = null;
        currNode.G = 0;
        currNode.H = Math.abs(startNode.getX() - currNode.x) + Math.abs(startNode.getY() - currNode.y)*10;
        visitedMap.put(currNode.node, currNode.node);

        while(currNode != null){
            //If we found the targetNode, end the search!
            if(currNode.node == targetNode){
                found = true;
                target = currNode;
                break;
            }

            //Add the currNode to the visited map and get its neighbors
            visitedMap.put(currNode.node, currNode.node);
            Grid.Node[] neighbors = ColonyGame.worldGrid.getNeighbors8(currNode.node);

            //Add all the (valid) neighbors to the openList.
            for (Grid.Node neighbor : neighbors) {
                if (neighbor == null || visitedMap.containsKey(neighbor) || neighbor.getTerrainTile() == null) continue;
                visitedMap.put(neighbor, neighbor);

                //Add the neighbor node to the visited map and open list.
                Grid.PathNode neighborNode = new Grid.PathNode(neighbor);

                //Set this neighbors values and add it to the openList.
                int[] dir = ColonyGame.worldGrid.getDirection(neighborNode.node, currNode.node);
                if (dir[0] == 0 || dir[1] == 0) //This is straight, not diagonal.
                    neighborNode.G = currNode.G + 10;
                else
                    neighborNode.G = currNode.G + 14; //Diagonal.

                //Get the TerrainTile and check if the tile is to be avoided.
                Grid.TerrainTile tile = ColonyGame.worldGrid.getNode(neighborNode.x, neighborNode.y).getTerrainTile();
                if (neighborNode.hasEnts() || (tile != null && tile.tileRef.avoid))
                    neighborNode.B = 500;

                //Set the H value, add it to the openList, and make its parent the current Node.
                neighborNode.H = Math.abs(targetNode.getX() - currNode.x) + Math.abs(targetNode.getY() - currNode.y) * 10;

                //Add the neighbor to the open list and set its parent node to the current node.
                openList.add(neighborNode);
                neighborNode.parentNode = currNode;
            }

            //Pop (poll) a node off of the list.
            currNode = openList.poll();
        }

        float squareSize = ColonyGame.worldGrid.getSquareSize();
        //If a path was found, record the path.
        if(found) {
            currNode = target;
            while (currNode.parentNode != null) {
                path.add(new Vector2(currNode.x*squareSize + squareSize*0.5f, currNode.y*squareSize + squareSize*0.5f));
                currNode = currNode.parentNode;
            }
        }

        return path;
    }
}
