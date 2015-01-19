package com.mygdx.game.helpers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;

/**
 * Created by Paha on 1/17/2015.
 */
public class Grid {
    private int numCols, numRows, squareSize;
    private Node[][] grid;

    public Grid(int width, int height, int squareSize){
        //Set some values.
        this.numCols = width/squareSize + 1;
        this.numRows = height/squareSize + 1;
        this.squareSize = squareSize;

        //Initialize the grid.
        this.grid = new Node[this.numCols][this.numRows];
        for(int col=0;col<grid.length; col++){
            for(int row=0; row<grid[col].length; row++){
                this.grid[col][row] = new Node(col, row);
            }
        }
    }

    public void perform(Functional.Perform perform){
        perform.perform(this.grid);
    }

    /**
     * Checks a Node to see if the Entity is still in the same Node as previously. If not, removes the Entity from the old Node and adds the Entity to the new Node.
     * @param currNode The current Node to check.
     * @param entity The Entity that should be in the Node.
     * @return The Node that the Entity is in. This could be the same as the currNode passed in, or a new Node.
     */
    public Node checkNode(Node currNode, Entity entity){
        Vector2 pos = entity.transform.getPosition();

        //If the currNode still matches our current position, return it.
        if(currNode != null && pos.x/this.squareSize == currNode.getCol() && pos.y/this.squareSize == currNode.getRow())
            return currNode;

        if(currNode != null) currNode.removeEntity(entity); //Remove from the old Node if it's not null.
        currNode = getNode(pos); //Get the new Node.
        if(currNode == null) return null; //If it's null, return null.

        currNode.addEntity(entity);
        return currNode;
    }

    /**
     * Gets the grid array of this Grid.
     * @return the Node[][] array of this Grid.
     */
    public Node[][] getGrid(){
        return this.grid;
    }

    /**
     * Gets the Node at the Entity's location.
     * @param entity The Entity to use for a location.
     * @return A Node at the Entity's location.
     */
    public Node getNode(Entity entity){
        return this.getNode((int)(entity.transform.getPosition().x/this.squareSize), (int)(entity.transform.getPosition().y/this.squareSize));
    }

    /**
     * Gets the Node at the Vector2 position.
     * @param pos The Vector2 position to get a Node at.
     * @return A Node at the Vector2 position.
     */
    public Node getNode(Vector2 pos){
        return this.getNode((int)(pos.x/this.squareSize), (int)(pos.y/this.squareSize));
    }

    /**
     * Gets a Node by a X and Y index.
     * @param x The X (col) index to get the Node at.
     * @param y The Y (row) index to get the Node at.
     * @return The Node if the index was valid, null otherwise.
     */
    public Node getNode(int x, int y){
        //If the index is not in bounds, return null.
        if(x < 0 || x >= this.grid.length || y < 0 || y >= this.grid[x].length)
            return null;

        return this.grid[x][y];
    }

    /**
     * Gets a Node by an index.
     * @param index An integer array containing X and Y index.
     * @return The Node if the index was valid, null otherwise.
     */
    public Node getNode(int[] index){
        if(index.length < 2)
            return null;

        return this.getNode(index[0], index[1]);
    }

    /**
     * Adds an Entity to the Grid.
     * @param entity The Entity to add.
     */
    public void addEntity(Entity entity){
        int xIndex = (int)(entity.transform.getPosition().x/this.squareSize);
        int yIndex = (int)(entity.transform.getPosition().y/this.squareSize);
        Node node = this.grid[xIndex][yIndex];

        node.addEntity(entity);
    }

    /**
     * Removes an Entity from the Grid.
     * @param entity The Entity to remove.
     */
    public void removeEntity(Entity entity){
        int xIndex = (int)(entity.transform.getPosition().x/this.squareSize);
        int yIndex = (int)(entity.transform.getPosition().y/this.squareSize);
        Node node = this.grid[xIndex][yIndex];

        node.removeEntity(entity);
    }

    public int getNumCols(){
        return this.numCols;
    }

    public int getNumRows(){
        return this.numRows;
    }

    public int[] getIndex(Vector2 position){
        return new int[]{(int)position.x/squareSize, (int)position.y/squareSize};
    }

    public void debugDraw(){
        Profiler.begin("Grid debugDraw");

        ShapeRenderer renderer = new ShapeRenderer();
        renderer.setProjectionMatrix(ColonyGame.camera.combined);

        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.GREEN);

        for(int col=0; col<grid.length; col++){
            for(int row=0; row<grid[col].length; row++){
                Node node = grid[col][row];
                renderer.rect(node.getCol()*squareSize, node.getRow()*squareSize, squareSize, squareSize);
            }
        }

        renderer.end();


        Profiler.end();
    }

    public class Node{
        private int col, row;
        private ArrayList<Entity> entList = new ArrayList<>();

        public Node(int col, int row){
            this.col = col;
            this.row = row;
        }

        public void addEntity(Entity entity){
            this.entList.add(entity);
        }

        public void removeEntity(Entity entity){
            this.entList.remove(entity);
        }

        public ArrayList<Entity> getEntityList(){
            return this.entList;
        }

        public Entity getEntity(Functional.GetEnt getEntFunc){
            return getEntFunc.getEnt(entList);
        }

        public int getCol(){
            return this.col;
        }

        public int getRow(){
            return this.row;
        }

    }
}
