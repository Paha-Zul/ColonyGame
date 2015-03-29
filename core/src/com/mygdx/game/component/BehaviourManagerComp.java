package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.behaviourtree.action.*;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.behaviourtree.decorator.RepeatUntilSuccess;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.timer.OneShotTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class BehaviourManagerComp extends Component{
    private BlackBoard blackBoard;
    private Task currentBehaviour, nextBehaviour, lastBehaviour;
    private String behaviourType = "";

    private Stats stats;

    private ArrayList<Line> lineList = new ArrayList<>();
    private Timer feedTimer = new OneShotTimer(10f, null);

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
//        this.currentBehaviour = this.gatherResource();
//        this.currentBehaviour.start();
        this.stats = this.owner.getComponent(Stats.class);
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
            Task task = this.exploreUnexplored();
            task.getControl().getCallbacks().finishCallback = () -> this.changeTask(this.gatherResource());
            this.changeTask(task);
        };

        Sequence sequence = new Sequence("Gathering Resource", this.blackBoard);
        FindClosestEntity fr = new FindClosestEntity("Finding Closest Resource", this.blackBoard, Constants.ENTITY_RESOURCE);
        FindPath findPath = new FindPath("Finding Path to Resource", this.blackBoard);
        MoveTo move = new MoveTo("Moving to Resource", this.blackBoard);
        Gather gather = new Gather("Gathering Resource", this.blackBoard);
        FindPath findPathToStorage = new FindPath("Finding Path to Storage", this.blackBoard);
        MoveTo moveToStorage = new MoveTo("Moving to Storage", this.blackBoard);
        TransferResource transferItems = new TransferResource("Transferring Resources", this.blackBoard);

        fr.getControl().callbacks.failureCallback = fail; //If we fail, call the fail callback.
        fr.getControl().callbacks.criteria = (e) -> ((Entity)e).hasTag(Constants.ENTITY_RESOURCE) && !((Entity)e).getComponent(Resource.class).isTaken(); //Check to make sure the resource isn't taken.

        fr.getControl().callbacks.successCallback = () ->  { //On success, take the resource.
            blackBoard.targetResource = blackBoard.target.getComponent(Resource.class);
            if(!blackBoard.targetResource.isTaken())
                blackBoard.targetResource.setTaken(this.blackBoard.getEntityOwner());
        };

        findPath.getControl().callbacks.checkCriteria = (task) -> (task).getBlackboard().targetResource.getTaken() == ((Task) task).getBlackboard().getEntityOwner();

        ((ParentTaskController)sequence.getControl()).addTask(fr);
        ((ParentTaskController)sequence.getControl()).addTask(findPath);
        ((ParentTaskController)sequence.getControl()).addTask(move);
        ((ParentTaskController)sequence.getControl()).addTask(gather);
        ((ParentTaskController)sequence.getControl()).addTask(findPathToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(moveToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(transferItems);

        //When we finish, set the target back to not taken IF it is still a valid target (if we ended early).
        sequence.getControl().callbacks.finishCallback = () -> {
            if (sequence.getBlackboard().target != null && !sequence.getBlackboard().target.isDestroyed() && sequence.getBlackboard().target.hasTag(Constants.ENTITY_RESOURCE)) {
                if(sequence.getBlackboard().targetResource.getTaken() == sequence.getBlackboard().getEntityOwner()) {
                    sequence.getBlackboard().targetResource.setTaken(null);
                    System.out.println("Set taken to null");
                }
            }

        };

        sequence.getControl().callbacks.startCallback = ()->{
            //Reset blackboard values...
            blackBoard.fromInventory = this.getComponent(Colonist.class).getInventory();
            blackBoard.toInventory = this.getComponent(Colonist.class).getColony().getInventory();
            blackBoard.transferAll = true;
            blackBoard.takeAmount = 0;
            blackBoard.itemNameToTake = null;
            blackBoard.targetResource = null;
            blackBoard.target = null;
            blackBoard.targetNode = null;
        };

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

    private Task consume(String effect){
        //Find a stockpile (easy for now)
        //Search for an itemRef to consume.
        //Pathfind to the stockpile
        //Move to the stockpile
        //Get the itemRef
        //Consume it.

        blackBoard.target = this.getComponent(Colonist.class).getColony().getEntityOwner();
        blackBoard.targetNode = null;
        blackBoard.fromInventory = this.getComponent(Colonist.class).getColony().getInventory();
        blackBoard.toInventory = this.getComponent(Inventory.class);
        blackBoard.transferAll = false;
        blackBoard.takeAmount = 1;
        blackBoard.itemNameToTake = null;

        Sequence sequence = new Sequence("Consuming Item", this.blackBoard);

        CheckInventoryHas check = new CheckInventoryHas("Checking Inventory", this.blackBoard, effect, 1);
        FindPath fp = new FindPath("Finding Path", this.blackBoard);
        MoveTo moveTo = new MoveTo("Moving to...", this.blackBoard);
        TransferResource tr = new TransferResource("Transferring Consumable", this.blackBoard);
        Consume consume = new Consume("Consuming Item", this.blackBoard, effect);

        check.getControl().getCallbacks().failureCallback = () -> this.changeTask(this.getLastBehaviour()); //Go back to last behaviour
        consume.getControl().getCallbacks().finishCallback = () -> this.changeTask(this.getLastBehaviour()); //Go back to last behaviour

        ((ParentTaskController) sequence.getControl()).addTask(check);
        ((ParentTaskController) sequence.getControl()).addTask(fp);
        ((ParentTaskController) sequence.getControl()).addTask(moveTo);
        ((ParentTaskController) sequence.getControl()).addTask(tr);
        ((ParentTaskController) sequence.getControl()).addTask(consume);

        sequence.getControl().callbacks.startCallback = ()->{
            //Reset blackboard values.
            blackBoard.target = this.getComponent(Colonist.class).getColony().getEntityOwner();
            blackBoard.targetNode = null;
            blackBoard.fromInventory = this.getComponent(Colonist.class).getColony().getInventory();
            blackBoard.toInventory = this.getComponent(Inventory.class);
            blackBoard.transferAll = false;
            blackBoard.takeAmount = 1;
            blackBoard.itemNameToTake = null;
        };

        return sequence;
    }

    private Task follow(){
        //Seq -> findclosest -> seq -> findpath/moveto...

        Sequence seq = new Sequence("Following", this.blackBoard);
        FindClosestEntity fc = new FindClosestEntity("Finding Closest Animal", this.blackBoard, Constants.ENTITY_ANIMAL);
        ((ParentTaskController) seq.getControl()).addTask(fc);

        Sequence seq2 = new Sequence("Moving Towards", this.blackBoard);
        RepeatUntilSuccess rp = new RepeatUntilSuccess("Following Target", this.blackBoard, seq2);

        FindPath fp = new FindPath("Finding Path To Target", this.blackBoard);
        MoveTo mt = new MoveTo("Moving to Target", this.blackBoard);

        ((ParentTaskController) seq.getControl()).addTask(rp); //Add the repeated task to the first sequence.
        ((ParentTaskController) seq2.getControl()).addTask(fp); //Add the find path to the second sequence.
        ((ParentTaskController) seq2.getControl()).addTask(mt); //Add the move to the second sequence.

        mt.getControl().callbacks.criteria = task -> {
            Task tsk = (Task)task;
            return tsk.getBlackboard().targetNode == ColonyGame.worldGrid.getNode(tsk.getBlackboard().target);
        };

        return seq;
    }

    public void gather(){
        this.changeTask(this.gatherResource());
    }

    public void explore(){
        this.changeTask(this.follow());
    }

    /**
     * Changes the next Task to the Task passed in. This essentially saves the current task to 'lastBehaviour' and sets the 'nextBehaviour' to the Task passed in.
     * @param task The Task to start immediately.
     */
    private void changeTask(Task task){
        //End the current task.
        if(this.currentBehaviour != null && !this.currentBehaviour.getControl().hasFinished()) {
            this.currentBehaviour.getControl().finishWithSuccess();
            this.currentBehaviour.getControl().safeEnd();
            this.lastBehaviour = this.currentBehaviour;
        }

        //Set the next behaviour.
        this.nextBehaviour = task;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        feedTimer.update(delta);

        if(this.nextBehaviour == null) {
            //If our behaviour is not null....
            if (currentBehaviour != null) {
                //If it has finished successfully, safely end it.
                if (this.currentBehaviour.getControl().hasFinished() && !this.currentBehaviour.getControl().hasFailed()) {
                    this.currentBehaviour = null;
                //If it finished but failed.
                } else if (this.currentBehaviour.getControl().hasFinished()) {
                    this.currentBehaviour = null;

                    //Otherwise, update it.
                } else {
                    this.currentBehaviour.update(delta); //Update it.
                }

            //If our behaviour is null, set the next behaviour to the default behaviour.
            } else {
                if (this.behaviourType.equals("colonist"))
                    this.nextBehaviour = this.idle(this.blackBoard.baseIdleTime, this.blackBoard.randomIdleTime, this.blackBoard.idleDistance);
                else if (this.behaviourType.equals("animal"))
                    this.nextBehaviour = this.idle(this.blackBoard.baseIdleTime, this.blackBoard.randomIdleTime, this.blackBoard.idleDistance);
            }

        //If our next behaviour is not null, we need to start it!
        }else{
            if(this.stats.getFood() <= 20 && feedTimer.isFinished() && this.behaviourType.equals("colonist")) {
                this.nextBehaviour = this.consume("feed");
                this.feedTimer.restart();
            }

            this.currentBehaviour = this.nextBehaviour;
            this.currentBehaviour.getControl().reset();
            this.currentBehaviour.getControl().safeStart();
            this.nextBehaviour = null;
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

    private Task getLastBehaviour(){
        return this.lastBehaviour;
    }

    public String getCurrentTaskName(){
        if(this.currentBehaviour != null)
            return this.currentBehaviour.getName();

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
