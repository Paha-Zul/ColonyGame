package com.mygdx.game.helpers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.Functional;
import com.sun.istack.internal.NotNull;

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
        private VisibilityTile[][] visibilityMap;

        public GridInstance(int width, int height, int squareSize) {
            //Set some values.
            this.numCols = width / squareSize + 1;
            this.numRows = height / squareSize + 1;
            this.squareSize = squareSize;

            //Initialize the grid.
            this.grid = new Node[this.numCols][this.numRows];
            for (int col = 0; col < grid.length; col++) {
                for (int row = 0; row < grid[col].length; row++) {
                    this.grid[col][row] = new Grid.Node(col, row);
                }
            }

            this.visibilityMap = new VisibilityTile[numCols][numRows];
        }

        /**
         * Uses a functional interface(lambda/anonymous function) to perform on the grid.
         * @param perform The Functional interface/lambda/anonymous function to execute.
         */
        public void perform(Functional.Perform perform) {
            perform.perform(this.grid);
        }

        public <V extends Object> V performAndGet(Functional.PerformAndGet<V, Node[][]> performAndGet){
            return performAndGet.performAndGet(this.grid);
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

            int startX = node.getX() - 1;
            int endX = node.getX() + 1;
            int startY = node.getY() - 1;
            int endY = node.getY() + 1;

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

            int startX = node.getX() - 1;
            int endX = node.getX() + 1;
            int startY = node.getY() - 1;
            int endY = node.getY() + 1;

            for (int col = startX; col <= endX; col++) {
                for (int row = startY; row <= endY; row++) {
                    if (col == node.getX() && row == node.getY())
                        continue;
                    neighbors[counter] = getNode(col, row);
                    counter++;
                }
            }

            return neighbors;
        }

        public Node addToGrid(Entity entity, int exploreRadius){
            Node node = this.getNode(entity);
            node.addEntity(entity);

            int startX = node.getX()-exploreRadius;
            int endX = node.getX()+exploreRadius;
            int startY = node.getY()-exploreRadius;
            int endY = node.getY()+exploreRadius;
            VisibilityTile[][] visMap = getVisibilityMap();

            for(int x=startX;x<=endX;x++){
                for(int y=startY;y<=endY;y++){
                    Node tmpNode = this.getNode(x, y);
                    if(tmpNode == null) continue;

                    visMap[x][y].addViewer();
                }
            }

            return node;
        }

        /**
         * Checks a Node to see if the Entity is still in the same Node as previously. If not, removes the Entity from the old Node and adds the Entity to the new Node.
         *
         * @param currNode The current Node to check.
         * @param entity   The Entity that should be in the Node.
         * @return The Node that the Entity is in. This could be the same as the currNode passed in, or a new Node.
         */
        public Node checkNode(Node currNode, Entity entity, boolean changeVisibility, int radius) {
            Vector2 pos = entity.transform.getPosition();

            int[] index = this.getIndex(pos);
            //If the currNode still matches our current position, return it.
            if (currNode != null && (index[0] == currNode.getX() && index[1] == currNode.getY())) {
                return currNode;
            }

            if (currNode != null) currNode.removeEntity(entity); //Remove from the old Node if it's not null.
            Node newNode = getNode(pos); //Get the new Node.
            if (newNode == null) return null; //If it's null, return null.

            //Under work!
            if(changeVisibility){
                this.perform((grid)->{
                    int startX = newNode.getX()-radius < currNode.getX()-radius ? newNode.getX()-radius : currNode.getX()-radius; //Get the least
                    int endX = newNode.getX()+radius > currNode.getX()+radius ? newNode.getX()+radius : currNode.getX()+radius; //Get the greatest
                    int startY = newNode.getY()-radius < currNode.getY()-radius ? newNode.getY()-radius : currNode.getY()-radius; //get the least
                    int endY = newNode.getY()+radius > currNode.getY()+radius ? newNode.getY()+radius : currNode.getY()+radius; //get greatest

                    //Gdx.app.log("Startx/Endx/StartY/EndY: ",startX+"/"+endX+"/"+startY+"/"+endY);

                    VisibilityTile[][] visibilityMap = getVisibilityMap();

                    for(int x = startX; x <= endX ; x++){
                        for(int y = startY; y <= endY; y++){
                            Node n = this.getNode(x, y);
                            if(n == null) continue;

                            int nXRange = Math.abs(x - newNode.getX()); //Current node's range from x
                            int nYRange = Math.abs(y - newNode.getY()); //Current node's range from y
                            int lastXRange = Math.abs(x - currNode.getX()); //Last node's range from x
                            int lastYRange = Math.abs(y - currNode.getY()); //Last node's range from y
                            boolean currInRange = nXRange <= radius && nYRange <= radius;
                            boolean lastInRange = lastXRange <= radius && lastYRange <= radius;

                            //TODO FIX THIS!
                            if(currInRange && lastInRange)
                                continue;

                            //If we are on new territory, add a viewer.
                            else if(currInRange) {
                                //if((Math.abs(newNode.getX() - n.getX()) + Math.abs(newNode.getY() - n.getY()) <= radius*1.5f))
                                    visibilityMap[x][y].addViewer();
                            //If leaving old territory, remove a viewer
                            }else if(lastInRange){
                                //if((Math.abs(newNode.getX() - n.getX()) + Math.abs(newNode.getY() - n.getY()) <= radius*1.5f))
                                    visibilityMap[x][y].removeViewer();
                             }

                            if((Math.abs(x - newNode.getX()) + Math.abs(y - newNode.getY()) > radius*1.5f))
                                continue;

                            //WorldGen.getVisibilityMap()[x][y].addViewer();
                        }
                    }
                });
            }

            newNode.addEntity(entity);
            return newNode;
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
        public Node getNode(@NotNull Entity entity) {
            if(entity.transform == null)
                return null;

            return this.getNode((int) (entity.transform.getPosition().x / this.getSquareSize()), (int) (entity.transform.getPosition().y / this.getSquareSize()));
        }

        /**
         * Gets the Node at the Vector2 position.
         *
         * @param pos The Vector2 position to get a Node at.
         * @return A Node at the Vector2 position.
         */
        public Node getNode(Vector2 pos) {
            return this.getNode((int) (pos.x / this.getSquareSize()), (int) (pos.y / this.getSquareSize()));
        }

        /**
         * Gets a Node by a X and Y index.
         *
         * @param x The X (x) index to get the Node at.
         * @param y The Y (y) index to get the Node at.
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
                GH.writeErrorMessage("Trying to get a node via index but passed in an index of less than size 2.");

            return this.getNode(index[0], index[1]);
        }

        /**
         * Adds an Entity to the Grid.
         *
         * @param entity The Entity to add.
         */
        public void addEntity(Entity entity) {
            int xIndex = (int) (entity.transform.getPosition().x / this.getSquareSize());
            int yIndex = (int) (entity.transform.getPosition().y / this.getSquareSize());
            Node node = this.grid[xIndex][yIndex];

            node.addEntity(entity);
        }

        /**
         * Removes an Entity from the Grid.
         *
         * @param entity The Entity to remove.
         */
        public void removeEntity(Entity entity) {
            int xIndex = (int) (entity.transform.getPosition().x / this.getSquareSize());
            int yIndex = (int) (entity.transform.getPosition().y / this.getSquareSize());
            Node node = this.grid[xIndex][yIndex];

            node.removeEntity(entity);
        }

        /**
         * Returns the direction of n1 from n2. dir[0] == 1 means above, -1 means below. dir[1] == 1 means to the right, -1 means to the left.
         *
         * @param n1 The Node to get the direction of.
         * @param n2 The Node.
         * @return An integer array which holds the x(x) and y(y) direction.
         */
        public int[] getDirection(Node n1, Node n2) {
            int[] dir = new int[]{0, 0};

            if (n1.getX() > n2.getX())
                dir[0] = 1;
            else if (n1.getX() < n2.getX())
                dir[0] = -1;

            if (n1.getY() > n2.getY())
                dir[1] = 1;
            else if (n1.getY() < n2.getY())
                dir[1] = -1;

            return dir;
        }

        /**
         * @return The total width (num of X tiles across) of this Grid.
         */
        public int getWidth() {
            return this.numCols;
        }

        /**
         * @return Gets the height (total Y tiles) of this Grid.
         */
        public int getHeight() {
            return this.numRows;
        }

        /**
         * @return The square size of this Grid.
         */
        public float getSquareSize() {
            return GH.toMeters(this.squareSize);
        }

        /**
         * Gets the index from the Vector2 position passed in.
         * @param position The Vector2 position to get an index of.
         * @return An integer array containing 2 values, the X and Y index.
         */
        public int[] getIndex(Vector2 position) {
            return getIndex(position.x, position.y);
        }

        public int[] getIndex(float x, float y) {
            return new int[]{(int) (x / getSquareSize()), (int) (y / getSquareSize())};
        }

        public final VisibilityTile[][] getVisibilityMap(){
            return this.visibilityMap;
        }

        public void debugDraw() {
            ShapeRenderer renderer = new ShapeRenderer();
            renderer.setProjectionMatrix(ColonyGame.camera.combined);

            renderer.begin(ShapeRenderer.ShapeType.Line);
            renderer.setColor(Color.GREEN);

            for (int col = 0; col < grid.length; col++) {
                for (int row = 0; row < grid[col].length; row++) {
                    Node node = grid[col][row];
                    renderer.rect(node.getX() * getSquareSize(), node.getY() * getSquareSize(), getSquareSize(), getSquareSize());
                }
            }
            renderer.end();
        }

        public void drawText(SpriteBatch batch) {
            for (int col = 0; col < grid.length; col++) {
                for (int row = 0; row < grid[col].length; row++) {
                    Node node = grid[col][row];
                    GUI.Text("index: " + node.getX() + " " + node.getY(), batch, col * getSquareSize(), row * getSquareSize() + getSquareSize());
                }
            }
        }
    }

    /**
     * A Node for the GridInstance class. Holds information about each Node such as Entities in the Node, Terrain information... etc.
     */
    public static class Node{
        private int x=-1, y=-1;
        private ArrayList<Entity> entList = new ArrayList<>();
        private TerrainTile terrainTile = null;

        public Node(int x, int y){
            this.x = x;
            this.y = y;
        }

        public void addEntity(Entity entity){
            this.entList.add(entity);
        }

        public void removeEntity(@NotNull Entity entity){
            this.entList.remove(entity);
        }

        public ArrayList<Entity> getEntityList(){
            return this.entList;
        }

        public Entity getEntity(Functional.GetEnt getEntFunc){
            return getEntFunc.getEnt(entList);
        }

        public int getX(){
            return this.x;
        }

        public int getY(){
            return this.y;
        }

        public void setTerrainTile(TerrainTile terrainTile){
            this.terrainTile = terrainTile;
        }

        public TerrainTile getTerrainTile(){
            return this.terrainTile;
        }

        @Override
        public String toString() {
            return "["+ x +","+ y +"]";
        }
    }

    /**
     * Nodes that contain path information for pathfinding tasks.
     */
    public static class PathNode{
        public int x, y;
        public float G, H, B;
        public PathNode parentNode;
        public Node node;

        public PathNode(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public PathNode(Node node) {
            this(node.x, node.y);
            this.node = node;
        }

        public float getF(){
            return G+H+B;
        }

        public boolean hasEnts(){
            return node.getEntityList().size() > 0;
        }
    }

    /**
     * A class that contains terrain information and graphics for the tile it is attached to.
     */
    public static class TerrainTile {
        public String tileName, category;
        public Sprite terrainSprite;
        public double noiseValue;
        public int type;
        public boolean avoid = false;
        private int visibility = Constants.VISIBILITY_UNEXPLORED;

        public TerrainTile(){

        }

        public TerrainTile(Sprite sprite, double noiseValue, float rotation, int type, Vector2 position) {
            this.terrainSprite = new Sprite(sprite);
            this.terrainSprite.setColor(Constants.COLOR_UNEXPLORED);
            this.noiseValue = noiseValue;
            this.type = type;
            this.terrainSprite.setPosition(position.x, position.y);
            this.terrainSprite.setRotation(rotation);
        }

        public void changeVisibility(int visibility){
            if(this.visibility == visibility)
                return;

            this.visibility = visibility;
            if(visibility == Constants.VISIBILITY_UNEXPLORED) this.terrainSprite.setColor(Constants.COLOR_UNEXPLORED);
            if(visibility == Constants.VISIBILITY_EXPLORED) this.terrainSprite.setColor(Constants.COLOR_EXPLORED);
            if(visibility == Constants.VISIBILITY_VISIBLE) this.terrainSprite.setColor(Constants.COLOR_VISIBILE);
        }

        public void set(Sprite sprite, double noiseValue, float rotation, int type, Vector2 position){
            if(terrainSprite != null)
                this.terrainSprite.set(sprite);
            else this.terrainSprite = sprite;

            this.noiseValue = noiseValue;
            this.terrainSprite.setRotation(rotation);
            this.type = type;
            this.terrainSprite.setPosition(position.x, position.y);
            this.terrainSprite.setColor(Constants.COLOR_UNEXPLORED);
        }

        @Override
        public int hashCode() {
            int hash = (int)(terrainSprite.getX() + terrainSprite.getY() + terrainSprite.getWidth() + terrainSprite.getY());
            hash += hash*type;
            hash += noiseValue*10000d;
            hash += terrainSprite.hashCode();

            return hash;
        }
    }

    /**
     * A class that contains visibility information about the tile it is attached to.
     */
    public static class VisibilityTile{
        private int visibility = Constants.VISIBILITY_UNEXPLORED;
        private int currViewers = 0;

        public VisibilityTile(){
            this.visibility = Constants.VISIBILITY_UNEXPLORED;
        }

        /**
         * Adds a viewer to this Tile. This will immediately mark the Tile as visible.
         */
        public void addViewer(){
            this.currViewers++;
            this.changeVisibility(Constants.VISIBILITY_VISIBLE);
        }

        /**
         * Removes a viewer from this tile. If it reaches 0, the terrain is set to explored and not visibile.
         */
        public void removeViewer(){
            this.currViewers--;
            if(currViewers <= 0)
                this.changeVisibility(Constants.VISIBILITY_EXPLORED);
        }

        public int getVisibility() {
            return visibility;
        }

        public void changeVisibility(int visibility){
            this.visibility = visibility;
        }

        public int getCurrViewers() {
            return currViewers;
        }
    }
}
