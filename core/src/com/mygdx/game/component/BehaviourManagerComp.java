package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.PrebuiltTasks;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.util.StateSystem;
import com.mygdx.game.util.StateTree;
import com.mygdx.game.util.managers.EventSystem;
import com.mygdx.game.util.timer.OneShotTimer;
import com.mygdx.game.util.timer.Timer;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

/**
 * A Component that manages the behaviour of Entities.
 */
public class BehaviourManagerComp extends Component{
    @JsonIgnore
    private static HashMap<String, BiFunction<BlackBoard, BehaviourManagerComp, Task>> taskMap = new HashMap<>();

    static{
        BehaviourManagerComp.addTaskToMap("moveTo", PrebuiltTasks::moveTo);
        BehaviourManagerComp.addTaskToMap("gather", PrebuiltTasks::gatherResource);
        BehaviourManagerComp.addTaskToMap("hunt", PrebuiltTasks::searchAndHunt);
        BehaviourManagerComp.addTaskToMap("explore", PrebuiltTasks::exploreUnexplored);
        BehaviourManagerComp.addTaskToMap("consume", PrebuiltTasks::consumeTask);
        BehaviourManagerComp.addTaskToMap("attackTarget", PrebuiltTasks::attackTarget);
        BehaviourManagerComp.addTaskToMap("idle", PrebuiltTasks::idleTask);
        BehaviourManagerComp.addTaskToMap("fleeTarget", PrebuiltTasks::fleeTarget);
        BehaviourManagerComp.addTaskToMap("returnToBase", PrebuiltTasks::returnToBase);
        BehaviourManagerComp.addTaskToMap("returnTools", PrebuiltTasks::returnTools);
        BehaviourManagerComp.addTaskToMap("build", PrebuiltTasks::build);
        BehaviourManagerComp.addTaskToMap("returnItems", PrebuiltTasks::returnItems);
        BehaviourManagerComp.addTaskToMap("sleep", PrebuiltTasks::sleep);
        BehaviourManagerComp.addTaskToMap("craftItem", PrebuiltTasks::craftItem);
    }

    @JsonIgnore
    private BlackBoard blackBoard;
    @JsonIgnore
    private Task currentBehaviour;
    @JsonProperty
    private String nextBehaviour;
    @JsonProperty
    private boolean unchangeable = false;
    @JsonIgnore
    private Stats stats;
    @JsonIgnore
    private ArrayList<Line> lineList = new ArrayList<>();
    @JsonIgnore
    private Timer feedTimer = new OneShotTimer(5f, null);
    @JsonIgnore
    private StateTree<TaskInfo> taskTree = new StateTree<>("taskTree", "root");
    @JsonIgnore
    private StateSystem<StateSystem.DefineTask> behaviourStates = new StateSystem<>();

    public BehaviourManagerComp() {

    }

    public static void addTaskToMap(String name, BiFunction<BlackBoard, BehaviourManagerComp, Task> taskFunction){
        taskMap.put(name, taskFunction);
    }

    @Override
    public void added(Entity owner) {
        super.added(owner);
        this.addedLoad(null, null);
    }

    @Override
    public void addedLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.addedLoad(entityMap, compMap);
        this.blackBoard = new BlackBoard();
        this.blackBoard.colonyGrid = ColonyGame.instance.worldGrid;
        this.blackBoard.myManager = this;
    }

    @Override
    public void save() {

    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.initLoad(entityMap, compMap);

    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        this.stats = this.owner.getComponent(Stats.class);
        this.blackBoard.myInventory = this.getComponent(Inventory.class);

        getBehaviourStates().addState("idle", true, new StateSystem.DefineTask("idle", "idle"));
        getBehaviourStates().setCurrState(getBehaviourStates().getDefaultState().stateName);
    }

    @Override
    public void init() {
        super.init();
        initLoad(null, null);
    }

    @Override
    public void start() {
        super.start();
        load(null, null);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        feedTimer.update(delta);

        //If our current behaviour is not null.. execute it.
        if (this.currentBehaviour != null) {
            //If it has finished, try to start the next behaviour if there is one, repeat the last if
            if (this.currentBehaviour.getControl().hasFinished()) {
                this.currentBehaviour.getControl().safeEnd();

                //If our next behaviour is a task waiting to be executed, start it!
                if(this.nextBehaviour != null){
                    this.changeTaskImmediate(nextBehaviour);
                    this.nextBehaviour = null;

                //If the next behaviour is empty, do something based on the state.
                }else {
                    //Reset the unchangeable state.
                    this.unchangeable = false;

                    //Get the default state as the next state for now, and get the current state.
                    StateSystem.State<StateSystem.DefineTask> nextState;
                    StateSystem.State<StateSystem.DefineTask> currState = behaviourStates.getCurrState();

                    //Repeat the last state if needed.
                    if(currState.getRepeatLastState()) nextState = behaviourStates.getState(currState.getUserData().lastState);
                    //Otherwise, if it didn't fail and needs to repeat, repeat it!.
                    else if(currState.getRepeat()) nextState = currState;
                    //If the job failed, try to set the current state to what is defined for "taskOnFailure".
                    else if(this.currentBehaviour.getControl().hasFailed() && currState.getUserData() != null) nextState = behaviourStates.getState(currState.getUserData().taskOnFailure);
                    //Otherwise, if the task didn't fail and doesn't repeat, get what is defined for "taskOnSuccess".
                    else if(currState.getUserData() != null) nextState = behaviourStates.getState(currState.getUserData().taskOnSuccess);
                    //Otherwise, everything failed so get the default state.
                    else nextState = behaviourStates.getDefaultState();
                    //Change the task and state.
                    this.changeTaskImmediate(nextState.stateName);
                }

            //Update the job.
            }else
                this.currentBehaviour.update(delta); //Update it.

        //If our behaviour is null, set the next behaviour to the default behaviour.
        } else
            this.changeTaskImmediate(behaviourStates.getDefaultState().stateName);
    }

    public void changeTaskImmediate(String taskName){
        this.changeTaskImmediate(taskName, false);
    }

    public void changeTaskImmediate(String taskName, boolean unchangeable){
        if(this.unchangeable) return; //Don't do anything if the task is unchangeable!

        StateSystem.State<StateSystem.DefineTask> lastState = behaviourStates.getCurrState();
        StateSystem.State<StateSystem.DefineTask> currState = behaviourStates.setCurrState(taskName);

        //If the currState should repeat the last state, and the last state is able to be repeated, save it as the last state!
        if(currState.getRepeatLastState() && lastState.getCanBeSavedAsLast()) currState.getUserData().lastState = lastState.stateName;
        //Otherwise, set the default state as the last state.
        else if(currState.getRepeatLastState()) currState.getUserData().lastState = this.behaviourStates.getDefaultState().stateName;

        //End the current task.
        if(this.currentBehaviour != null && !this.currentBehaviour.getControl().hasFinished()) {
            this.currentBehaviour.getControl().finishWithFailure();
            this.currentBehaviour.getControl().safeEnd();
        }

        //Gets the new task from the task map, resets it (in case its being reused), checks and start.
        Task task = taskMap.get(taskName).apply(this.blackBoard, this);
        this.currentBehaviour = task;
        this.currentBehaviour.getControl().reset();
        if(!this.currentBehaviour.check()) this.currentBehaviour.getControl().finishWithFailure();
        else this.currentBehaviour.getControl().safeStart();

        this.unchangeable = unchangeable;

        //Set the next behaviour.
        //this.nextBehaviour = task;

        EventSystem.notifyEntityEvent(this.getEntityOwner(), "task_started", task);
    }

    @Override
    public void destroy(Entity destroyer) {
        if(this.currentBehaviour != null) {
            this.currentBehaviour.getControl().finishWithFailure();
            this.currentBehaviour.getControl().safeEnd();
            this.currentBehaviour = null;
        }
        super.destroy(destroyer);
    }

    @JsonIgnore
    public StateSystem<StateSystem.DefineTask> getBehaviourStates(){
        return behaviourStates;
    }

    public void changeTaskQueued(String taskName){
        this.nextBehaviour = taskName;
    }

    @JsonIgnore
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
                        nextX = this.getEntityOwner().getTransform().getPosition().x;
                        nextY = this.getEntityOwner().getTransform().getPosition().y;
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

    /**
     * @return The current task compName.
     */
    @JsonIgnore
    public String getCurrentTaskName(){
        if(this.currentBehaviour != null)
            return this.currentBehaviour.getName();

        return "Nothing";
    }

    @JsonIgnore
    public String getNextTaskName(){
        if(this.currentBehaviour != null)
            return this.currentBehaviour.getName();

        return "null";
    }

    /**
     * @return The blackboard of this Component.
     */
    @JsonIgnore
    public BlackBoard getBlackBoard(){
        return this.blackBoard;
    }

    /**
     * @return The task Tree of this Behaviour Component.
     */
    @JsonIgnore
    public StateTree<TaskInfo> getTaskTree(){
        return this.taskTree;
    }

    /**
     * A class used for the userData of the Tree class.
     */
    public static class TaskInfo {
        public String taskName = "";
        public Functional.Callback callback;
        public boolean active;
        public Object userData;

        public TaskInfo(String taskName){
            this.taskName = taskName;
        }

        public void doCallback(){
            if(callback!=null) callback.callback();
        }
    }

    public class Line{
        public float startX, startY, width, height, rotation;
    }
}
