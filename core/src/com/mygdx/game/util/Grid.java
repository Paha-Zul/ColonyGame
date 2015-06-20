package com.mygdx.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IRecyclable;
import com.mygdx.game.util.gui.GUI;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
        public int padding = 3;
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
                    this.grid[col][row] = new Grid.Node(col, row, this);
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

        /**
         * Preforms a function on each Entity in each Node within the radius from the start index.
         * @param entityConsumer The Consumer function to execute on the Entity in the Node.
         * @param nodePredicate The Node predicate that the Node must pass in order to be used. For instance, we don't want to destroy water sources in water near our base, so ignore water tiles.
         * @param radius The radius to extends out to.
         * @param startIndex The start index of this perform task.
         */
        public void performOnEntityInRadius(Consumer<Entity> entityConsumer, Predicate<Node> nodePredicate, int radius, int[] startIndex){
            int[] ranges = GH.fixRanges(startIndex[0] - radius, startIndex[0] + radius, startIndex[1] - radius, startIndex[1] + radius, getWidth(), getHeight());

            for(int x = ranges[0]; x <= ranges[1]; x++){
                for(int y = ranges[2]; y <= ranges[3]; y++){
                    Node node = getNode(x, y);
                    if(node == null) continue; //If null, continue
                    if(nodePredicate != null && !nodePredicate.test(node)) continue; //If we test the predicate and it's false, continue.

                    node.getEntityList().forEach(entityConsumer::accept);
                }
            }
        }

        /**
         * Performs a Predicate function on Nodes within the radius from the start index. If any Predicate test returns true, the function returns.
         * @param radius The radius of the search.
         * @param startIndex The start index.
         * @param nodePredicate The Predicate function to test and perform on the Node. If this return true, the function returns and does not process any further.
         */
        public boolean performOnNodeInRadius(int radius, int[] startIndex, Predicate<Node> nodePredicate){
            int[] ranges = GH.fixRanges(startIndex[0] - radius, startIndex[0] + radius, startIndex[1] - radius, startIndex[1] + radius, getWidth(), getHeight());

            for(int x = ranges[0]; x <= ranges[1]; x++){
                for(int y = ranges[2]; y <= ranges[3]; y++){
                    Node node = getNode(x, y);
                    if(node == null) continue; //If null, continue
                    if(nodePredicate.test(node))
                        return true;
                }
            }

            return false;
        }

        public boolean performOnNodeInArea(int[] area, Predicate<Node> nodePredicate){
            int startX = area[0] < 0 ? 0 : area[0];
            int endX = area[1] >= this.getWidth() ? this.getWidth()-1 : area[1];
            int startY = area[2] < 0 ? 0 : area[2];
            int endY = area[3] >= this.getHeight() ? this.getHeight()-1 : area[3];

            System.out.println("Area: "+area[0]+" "+area[1]+" "+area[2]+" "+area[3]);
            System.out.println("start/end: "+startX+" "+endX+" "+startY+" "+endY);

            for(int x = startX; x <= endX; x++){
                for(int y = startY; y <= endY; y++){
                    Node node = getNode(x, y);
                    if(node == null) continue; //If null, continue
                    if(nodePredicate.test(node))
                        return true;
                }
            }

            return false;
        }

        public <V> V performAndGet(Functional.PerformAndGet<V, Node[][]> performAndGet){
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

            neighbors[0] = this.getNode(node.getX() - 1, node.getY()); // Left
            neighbors[1] = this.getNode(node.getX() + 1, node.getY()); // Right
            neighbors[2] = this.getNode(node.getX(), node.getY() + 1); // Up
            neighbors[3] = this.getNode(node.getX(), node.getY() - 1); // Down

            return neighbors;
        }

        /**
         * Gets the Node's neighbor in 8 directions around the Node.
         *
         * @param node The Node to get the neighbors of.
         * @return A Node array of 8 neighbors. Values can/will be null to indicate an invalid neighbor.
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

        /**
         * Adds an Entity to the grid, using it's position to find the Node.
         * @param entity The Entity to add.
         * @return The Node that the Entity was added to.
         */
        public Node addToGrid(Entity entity){
            Node node = this.getNode(entity);
            node.addEntity(entity);
            return node;
        }

        /**
         * Adds an Entity to the grid. Uses the position of the Entity for the node to return, but may add this Entity to multiple nodes
         * using the bounds parameter.
         * @param entity The Entity to add/
         * @param multi True if this should be added to multiple nodes. False otherwise.
         * @param bounds The bounds to use for adding to multiple nodes.
         * @return The node that the Entity's position is on.
         */
        public Node addToGrid(Entity entity, boolean multi, Rectangle bounds){
            if(multi){
                System.out.println();
                int[] ranges = GH.fixRanges(this.getSquareSize(), bounds.x, bounds.y, bounds.x+bounds.width, bounds.y+bounds.height, this.getWidth(), this.getHeight());
                performOnNodeInArea(ranges, node -> {
                    if (node.getX() == (int) (entity.getTransform().getPosition().x / this.getSquareSize()) && node.getY() == (int) (entity.getTransform().getPosition().y / this.getSquareSize()))
                        return false;
                    node.addEntity(entity);
                    return false;
                });
            }
            Node node = this.addToGrid(entity);
            return node;
        }

        /**
         * Adds a viewer to the VisibilityTiles within the Node's position plus radius.
         * @param startingNode The starting Node to begin at.
         * @param exploreRadius The radius to add viewers.
         */
        public void addViewer(Node startingNode, int exploreRadius){
            int startX = startingNode.getX()-exploreRadius;
            int endX = startingNode.getX()+exploreRadius;
            int startY = startingNode.getY()-exploreRadius;
            int endY = startingNode.getY()+exploreRadius;
            VisibilityTile[][] visMap = getVisibilityMap();

            for(int x=startX;x<=endX;x++){
                for(int y=startY;y<=endY;y++){
                    Node tmpNode = this.getNode(x, y);
                    if(tmpNode == null) continue;

                    visMap[x][y].addViewer();
                }
            }
        }

        public void removeViewer(GridComponent gridComp){
            this.removeViewer(gridComp.getCurrNode(), gridComp.exploreRadius);
        }

        /**
         * Removes a viewer to the VisibilityTiles within the Node's position plus radius.
         * @param startingNode The Node to start at.
         * @param exploreRadius The radius to remove viewers.
         */
        public void removeViewer(Node startingNode, int exploreRadius){
            int startX = startingNode.getX()-exploreRadius;
            int endX = startingNode.getX()+exploreRadius;
            int startY = startingNode.getY()-exploreRadius;
            int endY = startingNode.getY()+exploreRadius;
            VisibilityTile[][] visMap = getVisibilityMap();

            for(int x=startX;x<=endX;x++){
                for(int y=startY;y<=endY;y++){
                    Node tmpNode = this.getNode(x, y);
                    if(tmpNode == null) continue;

                    visMap[x][y].removeViewer();
                }
            }
        }

        /**
         * Checks a Node to see if the Entity is still in the same Node as previously. If not, removes the Entity from the old Node and adds the Entity to the new Node.
         * @param currNode The current Node to check.
         * @param entity   The Entity that should be in the Node.
         * @return The Node that the Entity is in. This could be the same as the currNode passed in, or a new Node.
         */
        public Node checkNode(Node currNode, Entity entity, boolean changeVisibility, int radius) {
            Vector2 pos = entity.getTransform().getPosition();

            //Get the index of the node we need.
            int[] index = this.getIndex(pos);

            //If the currNode isn't null and the node we are getting matches the position of our old one, simply return with the old node.
            if (currNode != null && (index[0] == currNode.getX() && index[1] == currNode.getY())) {
                return currNode;

            //If the currNode is null, return null.
            }else if(currNode == null)
                return null;

            //Remove us from the currentNode and get a new node.
            currNode.removeEntity(entity);
            Node newNode = getNode(pos);
            if (newNode == null) return null;

            //Under work!
            if(changeVisibility){
                this.perform((grid)->{
                    //Get the range.
                    int startX = newNode.getX()-radius < currNode.getX()-radius ? newNode.getX()-radius : currNode.getX()-radius; //Get the least
                    int endX = newNode.getX()+radius > currNode.getX()+radius ? newNode.getX()+radius : currNode.getX()+radius; //Get the greatest
                    int startY = newNode.getY()-radius < currNode.getY()-radius ? newNode.getY()-radius : currNode.getY()-radius; //get the least
                    int endY = newNode.getY()+radius > currNode.getY()+radius ? newNode.getY()+radius : currNode.getY()+radius; //get greatest

                    VisibilityTile[][] visibilityMap = getVisibilityMap();

                    //Loop over the range adding viewers.
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

                            //If we're in an area that shouldn't be touched, continue past it.
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

//                            if((Math.abs(x - newNode.getX()) + Math.abs(y - newNode.getY()) > radius*1.5f))
//                                continue;

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
         * @return the Node[][] array of this Grid.
         */
        public Node[][] getGrid() {
            return this.grid;
        }

        /**
         * Gets the Node at the Entity's location.
         * @param entity The Entity to use for a location.
         * @return A Node at the Entity's location.
         */
        public Node getNode(@NotNull Entity entity) {
            if(entity.getTransform() == null)
                return null;

            return this.getNode((int) (entity.getTransform().getPosition().x / this.getSquareSize()), (int) (entity.getTransform().getPosition().y / this.getSquareSize()));
        }

        /**
         * Gets the Node at the Vector2 position.
         * @param pos The Vector2 position to get a Node at.
         * @return A Node at the Vector2 position.
         */
        public Node getNode(Vector2 pos) {
            return this.getNode((int) (pos.x / this.getSquareSize()), (int) (pos.y / this.getSquareSize()));
        }

        /**
         * Gets a Node by a X and Y index.
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
         * @param entity The Entity to add.
         */
        public void addEntity(Entity entity) {
            int xIndex = (int) (entity.getTransform().getPosition().x / this.getSquareSize());
            int yIndex = (int) (entity.getTransform().getPosition().y / this.getSquareSize());
            Node node = this.grid[xIndex][yIndex];

            node.addEntity(entity);
        }

        /**
         * Removes an Entity from the Grid.
         *
         * @param entity The Entity to remove.
         */
        public void removeEntity(Entity entity) {
            int xIndex = (int) (entity.getTransform().getPosition().x / this.getSquareSize());
            int yIndex = (int) (entity.getTransform().getPosition().y / this.getSquareSize());
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

        public int getOriginalSquareSize(){
            return this.squareSize;
        }

        /**
         * @return The square size of this Grid.
         */
        public float getSquareSize() {
            return GH.toMeters(this.squareSize);
        }

        /**
         * Gets the index for the location of the Entity on the GridInstance.
         * @param entity The Entity to use for position.
         * @return An integer array containing the X and Y values of the index.
         */
        public int[] getIndex(Entity entity) {
            return getIndex(entity.getTransform().getPosition());
        }

        /**
         * Gets the index from the Vector2 position passed in.
         * @param position The Vector2 position to get an index of.
         * @return An integer array containing 2 values, the X and Y index.
         */
        public int[] getIndex(Vector2 position) {
            return getIndex(position.x, position.y);
        }

        /**
         * Gets the index of a float X and Y value.
         * @param x The X position.
         * @param y The Y position.
         * @return The index of the position.
         */
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
        private GridInstance gridOwner;

        public Node(int x, int y, GridInstance gridOwner){
            this.x = x;
            this.y = y;
            this.gridOwner = gridOwner;
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

        /**
         * Gets the X index of this Node. Not the real location.
         * @return An integer which is the X's index on the map.
         */
        public int getX(){
            return this.x;
        }

        /**
         * Gets the Y index of this Node. Not the real location.
         * @return An integer which is the Y's index on the map.
         */
        public int getY(){
            return this.y;
        }

        public void setTerrainTile(TerrainTile terrainTile){
            this.terrainTile = terrainTile;
        }

        public TerrainTile getTerrainTile(){
            return this.terrainTile;
        }

        public int getNumNodesAway(Node otherNode){
            return Math.abs(this.getX() - otherNode.getX()) + Math.abs(this.getY() - otherNode.getY());
        }

        public float getXCenter(){
            return this.x * gridOwner.getSquareSize();
        }

        public float getYCenter(){
            return this.y * gridOwner.getSquareSize();
        }

        @Override
        public String toString() {
            return "["+ x +","+ y +"]";
        }
    }

    /**
     * Nodes that contain path information for pathfinding tasks.
     */
    public static class PathNode implements IRecyclable{
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

        @Override
        public void recycle() {
            ObjectPool.recycleObject(this, PathNode.class);
        }

        @Override
        public void clear() {
            node = null;
            parentNode = null;
            G=H=B=0;
            x=y=0;
        }
    }

    /**
     * A class that contains terrain information and graphics for the tile it is attached to.
     */
    public static class TerrainTile {
        public DataBuilder.JsonTile tileRef;
        public Sprite terrainSprite;
        public String tileTextureName, tileAtlasName;
        public double noiseValue;
        private int visibility = Constants.VISIBILITY_UNEXPLORED;

        public TerrainTile(){

        }

        public TerrainTile(Sprite sprite, double noiseValue, float rotation, Vector2 position, DataBuilder.JsonTile tileRef) {
            this.terrainSprite = new Sprite(sprite);
            this.terrainSprite.setColor(Constants.COLOR_UNEXPLORED);
            this.noiseValue = noiseValue;
            this.tileRef = tileRef;
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

        public void set(Sprite sprite, double noiseValue, float rotation, Vector2 position, DataBuilder.JsonTile tileRef, String tileTextureName, String tileAtlasName){
            if(terrainSprite != null)
                this.terrainSprite.set(sprite);
            else this.terrainSprite = sprite;

            this.noiseValue = noiseValue;
            this.terrainSprite.setRotation(rotation);
            this.tileRef = tileRef;
            this.terrainSprite.setPosition(position.x, position.y);
            this.terrainSprite.setColor(Constants.COLOR_UNEXPLORED);
            this.tileTextureName = tileTextureName;
            this.tileAtlasName = tileAtlasName;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
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
