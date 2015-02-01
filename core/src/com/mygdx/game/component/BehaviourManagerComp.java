package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.behaviourtree.action.*;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.helpers.BehaviourManager;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class BehaviourManagerComp extends Component implements IDisplayable{
    private BlackBoard blackBoard;
    private Task behaviourTree;
    private String behaviourType = "";

    private Texture blueSquare = new Texture("img/blueSquare.png");

    public BehaviourManagerComp(String behaviourType) {
        this.behaviourType = behaviourType;
    }

    @Override
    public void start() {
        super.start();

        this.blackBoard = this.owner.addComponent(new BlackBoard());
        this.blackBoard.colonyGrid = ColonyGame.worldGrid;

//        this.behaviourTree = this.gatherResource();
//        this.behaviourTree.start();
    }

    private Task moveTo(){
        //Get the target node/Entity.
        //Find the path.
        //Move to the target.

        Sequence sequence = new Sequence("MoveTo", this.blackBoard);

        FindPath findPath = new FindPath("FindPath",  this.blackBoard);
        MoveTo followPath = new MoveTo("FollowPath",  this.blackBoard);

        ((ParentTaskController)(sequence.getControl())).addTask(findPath);
        ((ParentTaskController)(sequence.getControl())).addTask(followPath);

        return sequence;
    }

    private Task gatherResource(){
        //Find the closest valid resource and valid storage closest to the resource.
        //Find a path to the resource.
        //Move to the resource.
        //Gather the resource.
        //Find a path back to the storage.
        //Store the resource.

        Sequence sequence = new Sequence("FindResource", this.blackBoard);

        FindClosestResource fr = new FindClosestResource("FindClosestResource", this.blackBoard, "Wood Log");
        FindPath findPath = new FindPath("FindPathToResource", this.blackBoard);
        MoveTo move = new MoveTo("MoveToResource", this.blackBoard);
        Gather gather = new Gather("GatherResource", this.blackBoard);
        FindPath findPathToStorage = new FindPath("FindPathToStorage", this.blackBoard);
        MoveTo moveToStorage = new MoveTo("MoveToStorage", this.blackBoard);
        TransferResource transferItems = new TransferResource("Transfer", this.blackBoard);


        ((ParentTaskController) sequence.getControl()).addTask(fr);
        ((ParentTaskController)sequence.getControl()).addTask(findPath);
        ((ParentTaskController)sequence.getControl()).addTask(move);
        ((ParentTaskController)sequence.getControl()).addTask(gather);
        ((ParentTaskController)sequence.getControl()).addTask(findPathToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(moveToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(transferItems);

        return sequence;
    }

    public void move(Vector2 position){
        //this.blackBoard.targetNode = this.blackBoard.colonyGrid.getNode(position);
        this.behaviourTree = this.gatherResource();
        this.behaviourTree.start();

//        behaviourTree = moveTo();
//        behaviourTree.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if(behaviourTree != null) {
            this.behaviourTree.update(delta);
            if(this.behaviourTree.getControl().hasFinished())
                this.behaviourTree = null;
        }else{
            if(this.behaviourType.equals("colonist")) {
                this.behaviourTree = this.gatherResource();
                this.behaviourTree.start();
            }
        }
    }

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name) {
        Matrix4 projection = batch.getProjectionMatrix();
        batch.setProjectionMatrix(ColonyGame.camera.combined);

        float lineWidth = 2;

        int squareSize = this.blackBoard.colonyGrid.getSquareSize();
        if(this.blackBoard.path != null && this.blackBoard.path.length > 0){
            for(int i=0; i < this.blackBoard.path.length; i++){
                Grid.Node node = this.blackBoard.path[i];
                if(node == null) {
                    break;

                //Take a peak at the next node. If it's not null, draw a line from the Entity to the next square
                }else{
                    float nextX=0, nextY=0;
                    node = this.blackBoard.path[i];

                    float currX = node.getCol()*squareSize + squareSize*0.5f;
                    float currY = node.getRow()*squareSize + squareSize*0.5f;

                    //If the next node is within our boundaries and not null, draw from the current node to the next.
                    if((i+1) < this.blackBoard.path.length && this.blackBoard.path[i+1] != null){
                        nextX = this.blackBoard.path[i+1].getCol()*squareSize + squareSize*0.5f;
                        nextY = this.blackBoard.path[i+1].getRow()*squareSize + squareSize*0.5f;

                    //Otherwise, draw to the Entity.
                    }else{
                        nextX = this.getEntityOwner().transform.getPosition().x;
                        nextY = this.getEntityOwner().transform.getPosition().y;
                    }

                    //Get the rotation from our entity to the next location.
                    float rotation = (float)Math.atan2(nextY - currY, nextX - currX )* MathUtils.radDeg;
                    //Get the distance to the next location
                    float dist = Vector2.dst(currX, currY, nextX, nextY);
                    //Draw the line!
                    batch.draw(blueSquare, currX, currY, 0, 0, dist, lineWidth, 1, 1, rotation, 0, 0, blueSquare.getWidth(), blueSquare.getHeight(), false, false);
                }
            }
        }

        batch.setProjectionMatrix(projection);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
