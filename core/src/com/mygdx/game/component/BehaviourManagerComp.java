package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.behaviourtree.action.*;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.behaviourtree.decorator.RepeatUntilCondition;
import com.mygdx.game.behaviourtree.decorator.RepeatUntilSuccess;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.FloatingText;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.timer.OneShotTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;

/**
 * A Component that manages the behaviour of Entities.
 */
public class BehaviourManagerComp extends Component{
    private BlackBoard blackBoard;
    private Task currentBehaviour, nextBehaviour;
    private String behaviourType = "";

    private Stats stats;

    private ArrayList<Line> lineList = new ArrayList<>();
    private Timer feedTimer = new OneShotTimer(5f, null);

    private State currentState;

    private enum State {
        Idle, Gathering, Exploring, Attacking
    }

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
            Vector2 pos = this.blackBoard.getEntityOwner().transform.getPosition();
            new FloatingText("Couldn't find a nearby resource!", new Vector2(pos.x, pos.y + 1), new Vector2(pos.x, pos.y + 10), 1.5f, 0.8f);

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

        ((ParentTaskController)sequence.getControl()).addTask(fr);
        ((ParentTaskController)sequence.getControl()).addTask(findPath);
        ((ParentTaskController)sequence.getControl()).addTask(move);
        ((ParentTaskController)sequence.getControl()).addTask(gather);
        ((ParentTaskController)sequence.getControl()).addTask(findPathToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(moveToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(transferItems);

        //If we fail, call the fail callback.
        fr.getControl().callbacks.failureCallback = fail;

        //Check to make sure the resource isn't taken.
        fr.getControl().callbacks.successCriteria = (e) -> ((Entity)e).hasTag(Constants.ENTITY_RESOURCE) && !((Entity)e).getComponent(Resource.class).isTaken();

        //On success, set the resource as taken if not already taken.
        fr.getControl().callbacks.successCallback = () ->  {
            blackBoard.targetResource = blackBoard.target.getComponent(Resource.class);
            if(!blackBoard.targetResource.isTaken())
                blackBoard.targetResource.setTaken(this.blackBoard.getEntityOwner());
        };

        //When finding a path, if the resource is taken by someone other than me, fail it!
        findPath.getControl().callbacks.checkCriteria = (task) -> (task).getBlackboard().targetResource.getTaken() == task.getBlackboard().getEntityOwner();

        //When we finish, set the target back to not taken IF it is still a valid target (if we ended early).
        sequence.getControl().callbacks.finishCallback = () -> {
            if (sequence.getBlackboard().target != null && !sequence.getBlackboard().target.isValid() && sequence.getBlackboard().target.hasTag(Constants.ENTITY_RESOURCE)) {
                if(sequence.getBlackboard().targetResource.getTaken() == sequence.getBlackboard().getEntityOwner())
                    sequence.getBlackboard().targetResource.setTaken(null);
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
         * Find path.
         * Move to it!
         */

        //Reset these. Left over assignments from other jobs will cause the explore behaviour to simply move to the wrong area.
        this.blackBoard.target = null;
        this.blackBoard.targetNode = null;

        Sequence sequence = new Sequence("Exploring", this.blackBoard);

        FindClosestUnexplored findClosestUnexplored = new FindClosestUnexplored("Finding Closest Unexplored Location", this.blackBoard);
        FindPath findPathToUnexplored = new FindPath("Finding Path to Unexplored", this.blackBoard);
        MoveTo moveToLocation = new MoveTo("Moving to Explore", this.blackBoard);

        ((ParentTaskController) sequence.getControl()).addTask(findClosestUnexplored);
        ((ParentTaskController) sequence.getControl()).addTask(findPathToUnexplored);
        ((ParentTaskController) sequence.getControl()).addTask(moveToLocation);

        return sequence;
    }

    public Task idleTask(float baseIdleTime, float randomIdleTime, int radius){
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

    private Task consumeTask(String effect){
        //Find a stockpile (easy for now)
        //Search for an itemRef to consumeTask.
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
        FindPath fp = new FindPath("Finding Path to consume item", this.blackBoard);
        MoveTo moveTo = new MoveTo("Moving to consume item", this.blackBoard);
        TransferResource tr = new TransferResource("Transferring Consumable", this.blackBoard);
        Consume consume = new Consume("Consuming Item", this.blackBoard, effect);

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

    private Task searchAndDestroy(){
        /**
         * Find an Entity to follow
         * Repeat until success...
         *      Find path to Entity (If getting a path fails because the target is already gone, forcefully fail the main sequence/whole behaviour).
         *      MoveTo - If target moves ^ (fail and repeat), if target is reached, finish the job successfully.
         *
         * Find base
         * Find path to base
         * Move to base
         * Transfer resources to base.
         */

        this.blackBoard.fromInventory = this.owner.getComponent(Inventory.class);
        this.blackBoard.toInventory = this.owner.getComponent(Colonist.class).getColony().getInventory();
        this.blackBoard.transferAll = true;

        Sequence mainSeq = new Sequence("Following", this.blackBoard);
        FindClosestEntity fc = new FindClosestEntity("Finding Closest Animal", this.blackBoard, Constants.ENTITY_ANIMAL);
        ((ParentTaskController) mainSeq.getControl()).addTask(fc);

        Sequence repeatSeq = new Sequence("Moving Towards", this.blackBoard);                           //Sequence which will be repeated.
        RepeatUntilSuccess rp = new RepeatUntilSuccess("Following Target", this.blackBoard, repeatSeq); //Repeat decorator

        FindPath fp = new FindPath("Finding Path To Target", this.blackBoard);      //Find a path to the target (repeat)
        MoveTo mt = new MoveTo("Moving to Target", this.blackBoard);                //Move to the target (repeat)
        Attack at = new Attack("Attacking", this.blackBoard);                       //Attack the squirrel (repeat)

        FindPath fpToResource = new FindPath("Finding Path to Dead Animal", this.blackBoard);                               //Find a path to the newly killed animal which is now a resource.
        MoveTo mtTargetResource = new MoveTo("Moving to Dead Animal", this.blackBoard);                                     //Move to it.
        Gather gatherResource = new Gather("Gathering Dead Animal", this.blackBoard);                                       //Gather it.
        FindClosestEntity fcBase = new FindClosestEntity("Finding storage", this.blackBoard, Constants.ENTITY_BUILDING);    //Find the closest base/storage
        FindPath fpToBase = new FindPath("Finding path to base", this.blackBoard);                                          //Find the path to the base/storage
        MoveTo mtBase = new MoveTo("Moving to base", this.blackBoard);                                                      //Move to the base/storage
        TransferResource trToBase = new TransferResource("Transfering items to base", this.blackBoard);                     //Transfer the resource from me/colonist to the base.

        ((ParentTaskController) mainSeq.getControl()).addTask(rp); //Add the repeated task to the first sequence.

        ((ParentTaskController) repeatSeq.getControl()).addTask(fp); //Add the find path to the second sequence.
        ((ParentTaskController) repeatSeq.getControl()).addTask(mt); //Add the move to the second sequence.
        ((ParentTaskController) repeatSeq.getControl()).addTask(at); //Add the searchAndAttack sequence

        ((ParentTaskController) mainSeq.getControl()).addTask(fpToResource);
        ((ParentTaskController) mainSeq.getControl()).addTask(mtTargetResource);
        ((ParentTaskController) mainSeq.getControl()).addTask(gatherResource);
        ((ParentTaskController) mainSeq.getControl()).addTask(fcBase);
        ((ParentTaskController) mainSeq.getControl()).addTask(fpToBase);
        ((ParentTaskController) mainSeq.getControl()).addTask(mtBase);
        ((ParentTaskController) mainSeq.getControl()).addTask(trToBase);

        //Creates a floating text object when trying to find an animal fails.
        fc.getControl().callbacks.failureCallback = () -> {
            Vector2 pos = this.blackBoard.getEntityOwner().transform.getPosition();
            new FloatingText("Couldn't find a nearby animal to hunt!", new Vector2(pos.x, pos.y + 1), new Vector2(pos.x, pos.y + 10), 1.5f, 0.8f);
        };

        //Since this FindPath behaviour is under a RepeatUntilSuccess, it will get stuck getting a path to nothing (failing).
        //We need to forcefully end the whole behaviour if this happens.
        fp.getControl().callbacks.failureCallback = () -> repeatSeq.getControl().finishWithFailure();

        //On each movement, we need to check if the target has moved nodes. The successCriteria will fail if the two nodes don't equal each other.
        //The behaviour will fail and a new path will be calculated.
        mt.getControl().callbacks.failCriteria = task -> {
            Task tsk = (Task)task;
            return tsk.getBlackboard().targetNode != ColonyGame.worldGrid.getNode(tsk.getBlackboard().target);
        };

        mt.getControl().callbacks.successCriteria = task -> {
            Task tsk = (Task)task;
            return this.blackBoard.target != null && (this.owner.transform.getPosition().dst(tsk.getBlackboard().target.transform.getPosition()) <= GH.toMeters(tsk.getBlackboard().attackRange));
        };

        //On success, kill the animal and get items from it.
        rp.getControl().callbacks.successCallback = () -> {
            this.blackBoard.targetNode = null;
        };

        return mainSeq;
    }

    private Task fish(){
        /**
         * Find a fishing spot.
         * Path to it.
         * Move to it.
         * Fish.
         * Find base.
         * Path to it.
         * Move to it.
         * Transfer all resources.
         */

        this.blackBoard.transferAll = true;
        this.blackBoard.itemNameToTake = null;
        this.blackBoard.fromInventory = this.blackBoard.owner.getComponent(Inventory.class);

        Sequence seq = new Sequence("Fishing", this.blackBoard);

        FindClosestTile fct = new FindClosestTile("Finding fishing spot", this.blackBoard);
        FindPath fp = new FindPath("Finding path to fishing spot", this.blackBoard);
        MoveTo mt = new MoveTo("Moving to fishing spot", this.blackBoard);
        Fish fish = new Fish("Fishing", this.blackBoard);
        FindClosestEntity fc = new FindClosestEntity("Finding base", this.blackBoard, Constants.ENTITY_BUILDING);
        FindPath fpBase = new FindPath("Finding path to base", this.blackBoard);
        MoveTo mtBase = new MoveTo("Moving to base", this.blackBoard);
        TransferResource tr = new TransferResource("Transfering resources", this.blackBoard);

        //We need to tell this fct what can pass as a valid tile.
        fct.getControl().callbacks.successCriteria = nd -> {
            Grid.Node node = (Grid.Node)nd;
            Grid.TerrainTile tile = blackBoard.colonyGrid.getNode(node.getX(), node.getY()).getTerrainTile();
            int visibility = blackBoard.colonyGrid.getVisibilityMap()[node.getX()][node.getY()].getVisibility();

            return tile.category.equals("LightWater") && visibility != Constants.VISIBILITY_UNEXPLORED;
        };

        //We want to remove the last step in our destination (first in the list) since it will be on the shore line.
        fp.getControl().callbacks.successCallback = () -> this.blackBoard.path.removeFirst();

        fc.getControl().callbacks.successCallback = () -> this.blackBoard.toInventory = this.blackBoard.target.getComponent(Inventory.class);

        ((ParentTaskController)seq.getControl()).addTask(fct);
        ((ParentTaskController)seq.getControl()).addTask(fp);
        ((ParentTaskController)seq.getControl()).addTask(mt);
        ((ParentTaskController)seq.getControl()).addTask(fish);
        ((ParentTaskController)seq.getControl()).addTask(fc);
        ((ParentTaskController)seq.getControl()).addTask(fpBase);
        ((ParentTaskController)seq.getControl()).addTask(mtBase);
        ((ParentTaskController)seq.getControl()).addTask(tr);

        return seq;
    }

    private Task attackTarget(){

        Sequence seq = new Sequence("Attacking", this.getBlackBoard());
        RepeatUntilCondition repeat = new RepeatUntilCondition("Repeating", this.getBlackBoard(), seq);

        FindPath fp = new FindPath("Finding path", this.getBlackBoard());
        MoveTo mt = new MoveTo("Moving", this.getBlackBoard());
        Attack attack = new Attack("Attacking Target", this.getBlackBoard());

        ((ParentTaskController)seq.getControl()).addTask(fp);
        ((ParentTaskController)seq.getControl()).addTask(mt);
        ((ParentTaskController)seq.getControl()).addTask(attack);

        repeat.getControl().callbacks.checkCriteria = task -> task.getBlackboard().target != null;

        mt.getControl().callbacks.failCriteria = task -> {
            Entity target = ((Task)task).getBlackboard().target;
            return target == null || !target.isValid() || !target.hasTag(Constants.ENTITY_ALIVE);
        };

        mt.getControl().callbacks.successCriteria = tsk -> {
            Task task = (Task)tsk;
            float dis = task.getBlackboard().target.transform.getPosition().dst(this.owner.transform.getPosition());
            return dis <= GH.toMeters(task.getBlackboard().attackRange);
        };

        repeat.getControl().callbacks.successCriteria = tsk -> {
            Task task = (Task)tsk;
            return task.getBlackboard().target == null || !task.getBlackboard().target.isValid() || !task.getBlackboard().target.hasTag(Constants.ENTITY_ALIVE);
        };

        return repeat;
    }

    public void idle(){
        this.changeTask(this.idleTask(this.blackBoard.baseIdleTime, this.blackBoard.randomIdleTime, this.blackBoard.idleDistance));
        this.currentState = State.Idle;
    }

    public void gather(){
        this.changeTask(this.gatherResource());
        this.currentState = State.Gathering;
    }

    public void explore(){
        this.changeTask(this.exploreUnexplored());
        this.currentState = State.Exploring;
    }

    public void searchAndAttack(){
        this.changeTask(this.searchAndDestroy());
        this.currentState = State.Idle;
    }

    public void attack(){
        if(this.currentState != State.Attacking) {
            this.changeTask(this.attackTarget());
            this.currentState = State.Attacking;
        }
    }

    /**
     * Changes the next Task to the Task passed in. This essentially saves the current task to 'lastBehaviour' and sets the 'nextBehaviour' to the Task passed in.
     * @param task The Task to start immediately.
     */
    public void changeTask(Task task){
        //End the current task.
        if(this.currentBehaviour != null && !this.currentBehaviour.getControl().hasFinished()) {
            this.currentBehaviour.getControl().finishWithSuccess();
            this.currentBehaviour.getControl().safeEnd();
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
                //If it has finished
                if (this.currentBehaviour.getControl().hasFinished()) {
                    if (this.currentState == State.Gathering) gather();
                    else if (this.currentState == State.Exploring) explore();
                    else idle();
                    //If it finished but failed.
                }else
                    this.currentBehaviour.update(delta); //Update it.

            //If our behaviour is null, set the next behaviour to the default behaviour.
            } else {
                if (this.behaviourType.equals("colonist"))
                    this.nextBehaviour = this.idleTask(this.blackBoard.baseIdleTime, this.blackBoard.randomIdleTime, this.blackBoard.idleDistance);
                else if (this.behaviourType.equals("animal"))
                    this.nextBehaviour = this.idleTask(this.blackBoard.baseIdleTime, this.blackBoard.randomIdleTime, this.blackBoard.idleDistance);
            }

        //If our next behaviour is not null, we need to start it!
        }else{
            //If we're hungry, eat!
            if(this.behaviourType.equals("colonist") && this.stats.getStat("food").getCurrVal() <= 20 && feedTimer.isFinished()) {
                this.changeTask(this.consumeTask("feed"));
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
                    float nextX, nextY; //Start at 0.
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
        if(this.currentBehaviour != null)
            return this.currentBehaviour.getName();

        return "Nothing";
    }

    public State getCurrentState(){
        return this.currentState;
    }

    @Override
    public void destroy() {
        this.currentBehaviour.getControl().finishWithFailure();
        this.currentBehaviour.getControl().safeEnd();
        this.currentBehaviour = null;
        super.destroy();
    }

    public class Line{
        public float startX, startY, width, height, rotation;
    }
}
