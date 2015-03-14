package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.behaviourtree.action.*;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Callbacks;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class BehaviourManagerComp extends Component{
    private BlackBoard blackBoard;
    private Task behaviourTree;
    private String behaviourType = "";

    private ArrayList<Line> lineList = new ArrayList<>();

    private Texture blueSquare = ColonyGame.assetManager.get("blueSquare", Texture.class);

    public BehaviourManagerComp(String behaviourType) {
        this.behaviourType = behaviourType;
    }

    @Override
    public void init(Entity owner) {
        super.init(owner);

        this.blackBoard = this.owner.addComponent(new BlackBoard());
        this.blackBoard.colonyGrid = ColonyGame.worldGrid;
    }

    @Override
    public void start() {
        super.start();
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
        /*
         * Find the closest valid resource and valid storage closest to the resource.
         * Find a path to the resource.
         * Move to the resource.
         * Gather the resource.
         * Find a path back to the storage.
         * Store the resource.
         */

        //If we fail to find a resource, we need to explore until we find one...
        Functional.Callback fail = () -> {
            //When we finish moving to the newly explored area, try to gather a resource again.
            Functional.Callback onFinish = () ->  this.changeTask(this.gatherResource());
            Task task = this.exploreUnexplored();
            task.getControl().getCallbacks().finishCallback = onFinish;
            this.changeTask(task);
        };

        Sequence sequence = new Sequence("Gathering Resource", this.blackBoard);

        FindClosestEntity fr = new FindClosestEntity("Finding Closest Resource", this.blackBoard, "woodlog", Constants.ENTITY_RESOURCE);
        fr.getControl().callbacks.failureCallback = fail;
        fr.getControl().callbacks.criteria = (e) -> ((Entity)e).hasTag(Constants.ENTITY_RESOURCE) && !((Entity)e).getComponent(Resource.class).isTaken();
        fr.getControl().callbacks.successCallback = () -> {blackBoard.target.getComponent(Resource.class).setTaken(true); System.out.println("Set taken");};

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

    private Task exploreUnexplored(){
        /**
         * Find an unexplored location.
         * Move to it!
         */

        Sequence sequence = new Sequence("Exploring", this.blackBoard);

        FindClosestUnexplored findClosestUnexplored = new FindClosestUnexplored("Finding Closest Unexplored Location", this.blackBoard);
        FindPath findPathToUnexplored = new FindPath("Finding Path to Unexplored", this.blackBoard);
        MoveTo moveToLocation = new MoveTo("Moving to Explore", this.blackBoard);

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
        this.changeTask(this.exploreUnexplored());
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
                this.behaviourTree = this.idle(this.blackBoard.baseIdleTime, this.blackBoard.randomIdleTime, this.blackBoard.idleDistance);
                this.behaviourTree.start();
            }else if(this.behaviourType.equals("animal")) {
                this.behaviourTree = this.idle(this.blackBoard.baseIdleTime, this.blackBoard.randomIdleTime, this.blackBoard.idleDistance);
                this.behaviourTree.start();
            }
        }
    }

    public BlackBoard getBlackBoard(){
        return this.blackBoard;
    }

    public Line[] getLines() {
        lineList.clear(); //Clear the list.

        float lineWidth = 2;

        if(this.blackBoard.path != null && this.blackBoard.path.size() > 0){
            for(int i=0; i < this.blackBoard.path.size(); i++){
                Vector2 point = this.blackBoard.path.get(i);
                if(point == null) {
                    break;

                    //Take a peak at the next point. If it's not null, draw a line from the Entity to the next square
                }else{
                    float nextX=0, nextY=0; //Start at 0.
                    point = this.blackBoard.path.get(i); //Get the Vector2 start point of our next destination.

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

                    Line line = new Line();
                    line.startX = currX;
                    line.startY = currY;
                    line.width = dist;
                    line.height = lineWidth;
                    line.rotation = rotation;

                    lineList.add(line);

                    //Draw the line!
                    //batch.draw(blueSquare, currX, currY, 0, 0, dist, lineWidth, 1, 1, rotation, 0, 0, blueSquare.getWidth(), blueSquare.getHeight(), false, false);
                }
            }
        }

        return lineList.toArray(new Line[lineList.size()]);
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

    public class Line{
        public float startX, startY, width, height, rotation;
    }
}
