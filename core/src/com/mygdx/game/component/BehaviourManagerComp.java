package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.behaviourtree.action.*;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.interfaces.Functional;
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

        Sequence sequence = new Sequence("Gathering Resource", this.blackBoard);

        //On the failing of finding a resource...
        Functional.Callback fail = () -> {
            //On the finishing of moving to a new spot.
            Functional.Callback onFinish = () -> {
              this.changeTask(this.gatherResource()); //Change this back to gathering.
            };

            this.changeTask(this.exploreUnexplored(onFinish));
        };

        FindClosestResource fr = new FindClosestResource("Finding Closest Resource", this.blackBoard, "woodlog", fail, null);
        FindPath findPath = new FindPath("Finding Path to Resource", this.blackBoard);
        MoveTo move = new MoveTo("Moving to Resource", this.blackBoard);
        Gather gather = new Gather("Gathering Resource", this.blackBoard);
        FindPath findPathToStorage = new FindPath("Finding Path to Storage", this.blackBoard);
        MoveTo moveToStorage = new MoveTo("Moving to Storage", this.blackBoard);
        TransferResource transferItems = new TransferResource("Transfering Resources", this.blackBoard);


        ((ParentTaskController) sequence.getControl()).addTask(fr);
        ((ParentTaskController)sequence.getControl()).addTask(findPath);
        ((ParentTaskController)sequence.getControl()).addTask(move);
        ((ParentTaskController)sequence.getControl()).addTask(gather);
        ((ParentTaskController)sequence.getControl()).addTask(findPathToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(moveToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(transferItems);

        return sequence;
    }

    private Task exploreUnexplored(Functional.Callback callbackOnCompletion){
        //Find an unexplored location.
        //Move to it!

        Sequence sequence = new Sequence("Exploring", this.blackBoard);

        FindClosestUnexplored findClosestUnexplored = new FindClosestUnexplored("Finding Closest Unexplored Location", this.blackBoard);
        FindPath findPathToUnexplored = new FindPath("Finding Path to Unexplored", this.blackBoard);
        MoveTo moveToLocation = new MoveTo("Moving to Explore", this.blackBoard, callbackOnCompletion);

        ((ParentTaskController) sequence.getControl()).addTask(findClosestUnexplored);
        ((ParentTaskController) sequence.getControl()).addTask(findPathToUnexplored);
        ((ParentTaskController) sequence.getControl()).addTask(moveToLocation);

        return sequence;
    }

    public Task idle(float baseIdleTime, float randomIdleTime, int radius){
        //Find random spot to walk to
        //Find path
        //Move there
        //Idle for some amount of time.

        Sequence sequence = new Sequence("Idling", this.blackBoard);

        FindRandomNearbyLocation findNearbyLocation = new FindRandomNearbyLocation("Finding Nearby Location", this.blackBoard, radius);
        FindPath findPath = new FindPath("Finding Path to Nearby Location", this.blackBoard);
        MoveTo moveTo = new MoveTo("Moving to Nearby Location", this.blackBoard);
        Idle idle = new Idle("Standing Still", this.blackBoard, baseIdleTime, randomIdleTime);

        ((ParentTaskController) sequence.getControl()).addTask(findNearbyLocation);
        ((ParentTaskController) sequence.getControl()).addTask(findPath);
        ((ParentTaskController) sequence.getControl()).addTask(moveTo);
        ((ParentTaskController) sequence.getControl()).addTask(idle);

        return sequence;
    }

    public void gather(){
        this.changeTask(this.gatherResource());
    }

    public void explore(){
        this.changeTask(this.exploreUnexplored(null));
    }

    private void changeTask(Task task){
        if(this.behaviourTree != null && !this.behaviourTree.getControl().hasFinished())
            this.behaviourTree.getControl().finishWithSuccess();

        this.behaviourTree = task;
        this.behaviourTree.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //If our behaviour is not null....
        if(behaviourTree != null) {
            //If it has finished successfully, start it over. (repeat)
            if(this.behaviourTree.getControl().hasFinished() && !this.behaviourTree.getControl().hasFailed()) {
                this.behaviourTree.getControl().reset(); //Reset it
                this.behaviourTree.start(); //Start again

            //If it finished but failed.
            }else if(this.behaviourTree.getControl().hasFinished()){
                this.behaviourTree = null; //Set it to null to get a new job. (default job)

            //Otherwise, update it.
            }else{
                this.behaviourTree.update(delta); //Update it.
            }
        }else{
            if(this.behaviourType.equals("colonist")) {
                this.behaviourTree = this.idle(2f, 2f, 1);
                this.behaviourTree.start();
            }else if(this.behaviourType.equals("animal")) {
                this.behaviourTree = this.idle(2f, 2f, 2);
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
        if(this.blackBoard.path != null && this.blackBoard.path.size() > 0){
            for(int i=0; i < this.blackBoard.path.size(); i++){
                Vector2 point = this.blackBoard.path.get(i);
                if(point == null) {
                    break;

                //Take a peak at the next point. If it's not null, draw a line from the Entity to the next square
                }else{
                    float nextX=0, nextY=0;
                    point = this.blackBoard.path.get(i);

                    float currX = point.x;
                    float currY = point.y;

                    //If the next point is within our boundaries and not null, draw from the current point to the next.
                    if((i+1) < this.blackBoard.path.size() && this.blackBoard.path.get(i + 1) != null){
                        nextX = this.blackBoard.path.get(i + 1).x;
                        nextY = this.blackBoard.path.get(i + 1).y;

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

    public String getCurrentTaskName(){
        if(this.behaviourTree != null)
            return this.behaviourTree.getName();

        return "Nothing";
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
