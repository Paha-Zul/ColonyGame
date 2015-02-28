package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.Profiler;
import com.mygdx.game.helpers.worldgeneration.WorldGen;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Paha on 1/21/2015.
 */
public class FindPath extends LeafTask {
    private LinkedList<Vector2> path;

    public FindPath(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void start() {
        super.start();

        Profiler.begin("FindPath");
        this.path = getPath();
        Profiler.end();
        this.blackBoard.path = this.path;
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
        Grid.PathNode targetNode;

        //If we have a target node in the blackboard, use it.
        if(this.blackBoard.targetNode != null)
            targetNode = (Grid.PathNode)this.blackBoard.targetNode;

        //Otherwise, if we have an Entity target, get the node from that.
        else if(this.blackBoard.target != null)
            targetNode = (Grid.PathNode)this.blackBoard.colonyGrid.getNode(this.blackBoard.target);

        //Otherwise, end with failure.
        else{
            this.control.finishWithFailure();
            return new LinkedList<>();
        }

        //If our targetNode is still null somehow, fail and return.
        if(targetNode == null) {
            System.out.println(this.blackBoard.getEntityOwner().name + " target is null");
            this.control.finishWithFailure();
            return null;
        }

        LinkedList<Vector2> path = new LinkedList<>(); //Will hold the end path.
        ArrayDeque<Grid.PathNode> closedList = new ArrayDeque<>(20); //Holds already visited nodes.

        //Priority queue that sorts by the lowest F value of the PathNode.
        PriorityQueue<Grid.PathNode> openList = new PriorityQueue<>(new Comparator<Grid.PathNode>() {
            @Override
            public int compare(Grid.PathNode n1, Grid.PathNode n2) {
                return (int)((n1.getF() - n2.getF())*100);
            }
        });

        //Set the starting currNode and its G and H value.
        Grid.PathNode currNode = (Grid.PathNode)this.blackBoard.colonyGrid.getNode(this.blackBoard.getEntityOwner());
        currNode.parentNode = null;
        currNode.G = 0;
        currNode.H = Math.abs(targetNode.getCol() - currNode.getCol()) + Math.abs(targetNode.getRow() - currNode.getRow())*10;
        currNode.visited = true;

        while(currNode != null && !found){
            if(currNode == targetNode){
                found = true;
                target = currNode;
                break;
            }

            Grid.Node[] neighbors = this.blackBoard.colonyGrid.getNeighbors8(currNode);

            //Add all the (valid) neighbors to the openList.
            for(int i=0;i<neighbors.length;i++){
                Grid.PathNode node = (Grid.PathNode)neighbors[i];
                if(node == null || node.visited)
                    continue;

                //Set this neighbors values and add it to the openList.
                int[] dir = this.blackBoard.colonyGrid.getDirection(node, currNode);
                if(dir[0] == 0 || dir[1] == 0) //This is straight, not diagonal.
                    node.G  = currNode.G + 10;
                else
                    node.G = currNode.G + 20; //Diagonal.

                WorldGen.TerrainTile tile = WorldGen.getInstance().getNode(node.getCol(), node.getRow());
                if(node.getEntityList().size() > 0 || (tile != null && tile.avoid))
                    node.B = 500;

                //Set the H value, add it to the openList, and make its parent the current Node.
                node.H = Math.abs(targetNode.getCol() - currNode.getCol()) + Math.abs(targetNode.getRow() - currNode.getRow())*10;
                node.visited = true;
                openList.add(node);
                node.parentNode = currNode;
            }

            //Add currNode to the path and closedList.
            closedList.add(currNode);

            //Get a new Node.
            if(openList.size() > 0)
                currNode = openList.remove();
            else
                currNode = null;
        }

        //Set visited to false for all in the closed list and clear it.
        for(Grid.PathNode node : closedList) {
            node.visited = false;
            node.B = 0;
            node.G = 0;
            node.H = 0;
        }

        //Set visited to false for all in the closed list and clear it.
        for(Grid.PathNode node : openList) {
            node.visited = false;
            node.B = 0;
            node.G = 0;
            node.H = 0;
        }

        int squareSize = this.blackBoard.colonyGrid.getSquareSize();
        //If a path was found, record the path.
        if(found) {
            currNode = target;
            while (currNode != null) {
                path.add(new Vector2(currNode.getCol()*squareSize + squareSize*0.5f, currNode.getRow()*squareSize + squareSize*0.5f));
                currNode.visited = false;
                currNode = currNode.parentNode;
            }
        }

        closedList.clear();
        openList.clear();
        return path;
    }

    private LinkedList<Vector2> smoothPath(LinkedList<Vector2> path){
        ListIterator<Vector2> iter = path.listIterator();
        Vector2 nextPoint = null;
        Vector2 currPoint = null;

        if(path.size() <= 2)
            return path;

        System.out.println("path length: "+path.size());

        //Loop over.
        while(iter.hasNext()){
            currPoint = iter.next(); //Get the current point
            nextPoint = iter.hasNext() ? iter.next() : currPoint; //Try and get the next point.

            //If both X and Y doesn't match, we have a turn...
            if(currPoint.x != nextPoint.x && currPoint.y != nextPoint.y){
                Vector2 middle = getCenterPoint(currPoint, nextPoint);
                iter.previous(); //Go back to the currPoint index.
                iter.add(middle);
                iter.next(); //Get to the point we just added. This will make the nextPoint our currPoint on the next iter.next call.
                System.out.println("Added new point "+middle+" curr: "+currPoint+" next: "+nextPoint);
            }
        }

        System.out.println("path length after: "+path.size());


        return path;
    }

    private ArrayList<Vector2> getCenterPoint(Vector2 first, Vector2 second, int counter, int maxInterp, ArrayList<Vector2> list){
        if(counter >= maxInterp)
            return list;

        list.add(new Vector2((first.x + second.x)/2, (first.y + second.y)/2));

        list = (getCenterPoint(first, list.get(0), counter+1, maxInterp, list));
        list = (getCenterPoint(second, list.get(list.size()-1), counter+1, maxInterp, list));
        //get left
        //get right

        return list;
    }

    private Vector2 getCenterPoint(Vector2 first, Vector2 second){
        return new Vector2((first.x + second.x)/2, (first.y + second.y)/2);
    }


}
