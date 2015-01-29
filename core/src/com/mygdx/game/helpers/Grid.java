package com.mygdx.game.helpers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paha on 1/17/2015.
 */
public class Grid {
    private static HashMap<String, GridInstance> gridMap = new HashMap<>();

    public static GridInstance newGridInstance(String name, int width, int height, int squareSize){
        GridInstance instance = new GridInstance(width, height, squareSize);
        gridMap.put(name, instance);
        return instance;
    }

    public static GridInstance getGridInstanceByName(String name){
        return gridMap.get(name);
    }

    public static class GridInstance {
        private int numCols, numRows, squareSize;
        private Node[][] grid;

        public GridInstance(int width, int height, int squareSize) {
            //Set some values.
            this.numCols = width / squareSize + 1;
            this.numRows = height / squareSize + 1;
            this.squareSize = squareSize;

            //Initialize the grid.
            this.grid = new Node[this.numCols][this.numRows];
            for (int col = 0; col < grid.length; col++) {
                for (int row = 0; row < grid[col].length; row++) {
                    this.grid[col][row] = new Grid.PathNode(col, row);
                }
            }
        }

        public void perform(Functional.Perform perform) {
            perform.perform(this.grid);
        }

        /**
         * Gets the Node's neighbors in 4 directions (left, down, right, up).
         *
         * @param node The Node to get the neighbors of.
         * @return A Node[] array with 4 neighbors. Values could be null to indicate an invalid neighbor.
         */
        public Node[] getNeighbors4(Node node) {
            int counter = 0;
            Node[] neighbors = new Node[4];

            int startX = node.getCol() - 1;
            int endX = node.getCol() + 1;
            int startY = node.getRow() - 1;
            int endY = node.getRow() + 1;

            for (int col = startX; col <= endX; col++) {
                for (int row = startY; row <= endY; row++) {
                    //If we are on the corners, continue without doing anything;
                    if ((col == startX && row == startY) || (col == startX && row == endY) || (col == endX && row == startY) || (col == endX && row == endY))
                        continue;

                    neighbors[counter] = getNode(col, row);
                    counter++;
                }
            }

            return neighbors;
        }

        /**
         * Gets the Node's neighbor in 8 directions around the Node.
         *
         * @param node The Node to get the neighbors of.
         * @return A Node array of 8 neighbors. Values could be null to indicate an invalid neighbor.
         */
        public Node[] getNeighbors8(Node node) {
            int counter = 0;
            Node[] neighbors = new Node[8];

            int startX = node.getCol() - 1;
            int endX = node.getCol() + 1;
            int startY = node.getRow() - 1;
            int endY = node.getRow() + 1;

            for (int col = startX; col <= endX; col++) {
                for (int row = startY; row <= endY; row++) {
                    if (col == node.getCol() && row == node.getRow())
                        continue;
                    neighbors[counter] = getNode(col, row);
                    counter++;
                }
            }

            return neighbors;
        }

        /**
         * Checks a Node to see if the Entity is still in the same Node as previously. If not, removes the Entity from the old Node and adds the Entity to the new Node.
         *
         * @param currNode The current Node to check.
         * @param entity   The Entity that should be in the Node.
         * @return The Node that the Entity is in. This could be the same as the currNode passed in, or a new Node.
         */
        public Node checkNode(Node currNode, Entity entity) {
            Vector2 pos = entity.transform.getPosition();

            //If the currNode still matches our current position, return it.
            if (currNode != null && pos.x / this.squareSize == currNode.getCol() && pos.y / this.squareSize == currNode.getRow())
                return currNode;

            if (currNode != null) currNode.removeEntity(entity); //Remove from the old Node if it's not null.
            currNode = getNode(pos); //Get the new Node.
            if (currNode == null) return null; //If it's null, return null.

            currNode.addEntity(entity);
            return currNode;
        }

        /**
         * Gets the grid array of this Grid.
         *
         * @return the Node[][] array of this Grid.
         */
        public Node[][] getGrid() {
            return this.grid;
        }

        /**
         * Gets the Node at the Entity's location.
         *
         * @param entity The Entity to use for a location.
         * @return A Node at the Entity's location.
         */
        public Node getNode(Entity entity) {
            return this.getNode((int) (entity.transform.getPosition().x / this.squareSize), (int) (entity.transform.getPosition().y / this.squareSize));
        }

        /**
         * Gets the Node at the Vector2 position.
         *
         * @param pos The Vector2 position to get a Node at.
         * @return A Node at the Vector2 position.
         */
        public Node getNode(Vector2 pos) {
            return this.getNode((int) (pos.x / this.squareSize), (int) (pos.y / this.squareSize));
        }

        /**
         * Gets a Node by a X and Y index.
         *
         * @param x The X (col) index to get the Node at.
         * @param y The Y (row) index to get the Node at.
         * @return The Node if the index was valid, null otherwise.
         */
        public Node getNode(int x, int y) {
            //If the index is not in bounds, return null.
            if (x < 0 || x >= this.grid.length || y < 0 || y >= this.grid[x].length)
                return null;

            return this.grid[x][y];
        }

        /**
         * Gets a Node by an index.
         *
         * @param index An integer array containing X and Y index.
         * @return The Node if the index was valid, null otherwise.
         */
        public Node getNode(int[] index) {
            if (index.length < 2)
                return null;

            return this.getNode(index[0], index[1]);
        }

        /**
         * Adds an Entity to the Grid.
         *
         * @param entity The Entity to add.
         */
        public void addEntity(Entity entity) {
            int xIndex = (int) (entity.transform.getPosition().x / this.squareSize);
            int yIndex = (int) (entity.transform.getPosition().y / this.squareSize);
            Node node = this.grid[xIndex][yIndex];

            node.addEntity(entity);
        }

        /**
         * Removes an Entity from the Grid.
         *
         * @param entity The Entity to remove.
         */
        public void removeEntity(Entity entity) {
            int xIndex = (int) (entity.transform.getPosition().x / this.squareSize);
            int yIndex = (int) (entity.transform.getPosition().y / this.squareSize);
            Node node = this.grid[xIndex][yIndex];

            node.removeEntity(entity);
        }

        /**
         * Returns the direction of n1 from n2. dir[0] == 1 means above, -1 means below. dir[1] == 1 means to the right, -1 means to the left.
         *
         * @param n1 The Node to get the direction of.
         * @param n2 The Node.
         * @return An integer array which holds the col(x) and row(y) direction.
         */
        public int[] getDirection(Node n1, Node n2) {
            int[] dir = new int[]{0, 0};

            if (n1.getCol() > n2.getCol())
                dir[0] = 1;
            else if (n1.getCol() < n2.getCol())
                dir[0] = -1;

            if (n1.getRow() > n2.getRow())
                dir[1] = 1;
            else if (n1.getRow() < n2.getRow())
                dir[1] = -1;

            return dir;
        }

        public int getNumCols() {
            return this.numCols;
        }

        public int getNumRows() {
            return this.numRows;
        }

        public int getSquareSize() {
            return this.squareSize;
        }

        public int[] getIndex(Vector2 position) {
            return new int[]{(int) position.x / squareSize, (int) position.y / squareSize};
        }

        public void debugDraw() {
            Profiler.begin("Grid debugDraw");

            ShapeRenderer renderer = new ShapeRenderer();
            renderer.setProjectionMatrix(ColonyGame.camera.combined);

            renderer.begin(ShapeRenderer.ShapeType.Line);
            renderer.setColor(Color.GREEN);

            for (int col = 0; col < grid.length; col++) {
                for (int row = 0; row < grid[col].length; row++) {
                    Node node = grid[col][row];
                    renderer.rect(node.getCol() * squareSize, node.getRow() * squareSize, squareSize, squareSize);
                }
            }
            renderer.end();
            Profiler.end();
        }

        public void drawText(SpriteBatch batch) {
            Profiler.begin("Grid drawText");

            for (int col = 0; col < grid.length; col++) {
                for (int row = 0; row < grid[col].length; row++) {
                    Node node = grid[col][row];
                    GUI.Text("index: " + node.getCol() + " " + node.getRow(), batch, col * squareSize, row * squareSize + squareSize);
                    GUI.Text("G: " + ((PathNode) node).G, batch, col * squareSize, row * squareSize + squareSize - 20);
                    GUI.Text("H: " + ((PathNode) node).H, batch, col * squareSize, row * squareSize + squareSize - 40);
                    GUI.Text("B: " + ((PathNode) node).B, batch, col * squareSize, row * squareSize + squareSize - 60);
                    GUI.Text("F: " + ((PathNode) node).getF(), batch, col * squareSize, row * squareSize + squareSize - 80);
                }
            }
            Profiler.end();
        }
    }

    public static class Node{
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

    public static class PathNode extends Node{
        public float G, H, B;
        public boolean visited = false, open = false, closed = false;
        public PathNode parentNode;

        public PathNode(int col, int row) {
            super(col, row);
        }

        public float getF(){
            return G+H+B;
        }
    }
}
