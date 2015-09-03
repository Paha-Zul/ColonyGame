package com.mygdx.game.util;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.function.Consumer;

/**
 * Created by Paha on 7/20/2015.
 */
public class Pathfinder {
    private static Pathfinder instance = null;
    private LinkedList<FindPathInfo> queue = new LinkedList<>();
    private long lastTick = 0;
    private final int numPathsPerTick = 10;

    public Pathfinder(){

    }

    /**
     * Updates the Pathfinder.
     * @param tick The current tick of the game.
     */
    public void update(long tick){
        if(queue.size() > 0 && tick != this.lastTick){
            for(int i=0; i<this.numPathsPerTick && queue.size() > 0; i++) {
                FindPathInfo fp = this.queue.pop();
                if (fp.startNode != null && fp.endNode != null)
                    fp.consumer.accept(this.findPath(fp.startNode, fp.endNode));
                else if (fp.startPos != null && fp.endPos != null)
                    fp.consumer.accept(this.findPath(fp.startPos, fp.endPos));
                else {
                    Logger.log(Logger.ERROR, "The pathfinding request was null in both nodes and positions. What happened?", true);
                }
            }
        }

        this.lastTick = tick;
    }

    /**
     * Finds a path from the startNode to the targetNode.
     * @param startNode The start Node.
     * @param targetNode The target Node.
     * @param callback The callback to execute when the path is found. The parameter to this is the path that was found.
     */
    public void findPath(Grid.Node startNode, Grid.Node targetNode, Consumer<LinkedList<Vector2>> callback){
        this.queue.add(new FindPathInfo(null, null, startNode, targetNode, callback));
    }

    /**
     * Finds a path from a start position to an end position.
     * @param start The start position.
     * @param end The end position.
     * @param callback The callback to execute when the path is found. The parameter to this is the path that was found.
     */
    public void findPath(Vector2 start, Vector2 end, Consumer<LinkedList<Vector2>> callback){
        this.queue.add(new FindPathInfo(start, end, null, null, callback));
    }

    public LinkedList<Vector2> findPath(Grid.Node startNode, Grid.Node targetNode){
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

    public LinkedList<Vector2> findPath(Vector2 start, Vector2 end){
        boolean found = false;
        Grid.PathNode target = null;
        Grid.Node startNode = ColonyGame.worldGrid.getNode(start);
        Grid.Node targetNode = ColonyGame.worldGrid.getNode(end);

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
            boolean first = true;
            currNode = target; //We start at the target node and work backwards...
            while (currNode.parentNode != null) {
                if (first) {
                    path.add(new Vector2(end.x, end.y));
                    first = false;
                }
                else
                    path.add(new Vector2(currNode.x*squareSize + squareSize*0.5f, currNode.y*squareSize + squareSize*0.5f));

                currNode = currNode.parentNode;
            }
        }

        return path;
    }

    public static Pathfinder GetInstance(){
        if(Pathfinder.instance == null)
            Pathfinder.instance = new Pathfinder();

        return Pathfinder.instance;
    }

    private class FindPathInfo{
        Vector2 startPos, endPos;
        Grid.Node startNode;
        Grid.Node endNode;
        Consumer<LinkedList<Vector2>> consumer;

        public FindPathInfo(Vector2 startPos, Vector2 endPos, Grid.Node startNode, Grid.Node endNode, Consumer<LinkedList<Vector2>> consumer) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.startNode = startNode;
            this.endNode = endNode;
            this.consumer = consumer;
        }
    }
}
