package com.mygdx.game.component;

import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.PrebuiltTasks;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.util.BlackBoard;
import com.mygdx.game.util.EventSystem;
import com.mygdx.game.util.StateSystem;
import com.mygdx.game.util.Tree;
import com.mygdx.game.util.timer.OneShotTimer;
import com.mygdx.game.util.timer.Timer;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

/**
 * A Component that manages the behaviour of Entities.
 */
public class BehaviourManagerComp extends Component{
    @JsonIgnore
    private BlackBoard blackBoard;
    @JsonIgnore
    private Task currentBehaviour;
    @JsonProperty
    private String nextBehaviour;
    @JsonIgnore
    private Stats stats;
    @JsonIgnore
    private ArrayList<Line> lineList = new ArrayList<>();
    @JsonIgnore
    private Timer feedTimer = new OneShotTimer(5f, null);
    @JsonIgnore
    private Tree taskTree = new Tree("taskTree", "root");
    @JsonIgnore
    private StateSystem<StateSystem.DefineTask> behaviourStates = new StateSystem<>();
    @JsonIgnore
    private static HashMap<String, BiFunction<BlackBoard, BehaviourManagerComp, Task>> taskMap = new HashMap<>();

    public BehaviourManagerComp() {

    }

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
    }

    @Override
    public void init() {
        super.init();
        initLoad();
    }

    @Override
    public void initLoad() {
        super.initLoad();

        this.blackBoard = new BlackBoard();
        this.blackBoard.colonyGrid = ColonyGame.worldGrid;
        this.blackBoard.myManager = this;
    }

    @Override
    public void load() {
        this.stats = this.owner.getComponent(Stats.class);
        this.blackBoard.myInventory = this.getComponent(Inventory.class);

        getBehaviourStates().addState("idle", true, new StateSystem.DefineTask("idle", "idle"));
        getBehaviourStates().setCurrState(getBehaviourStates().getDefaultState().stateName);
    }

    @Override
    public void save() {

    }

    @Override
    public void start() {
        super.start();
        load();
    }

    public void changeTaskImmediate(String taskName){
        StateSystem.State<StateSystem.DefineTask> lastState = behaviourStates.getCurrState();
        StateSystem.State<StateSystem.DefineTask> currState = behaviourStates.setCurrState(taskName);

        if(currState.getRepeatLastState()) currState.getUserData().lastState = lastState.stateName;

        //End the current task.
        if(this.currentBehaviour != null && !this.currentBehaviour.getControl().hasFinished()) {
            this.currentBehaviour.getControl().finishWithFailure();
            this.currentBehaviour.getControl().safeEnd();
        }

        Task task = taskMap.get(taskName).apply(this.blackBoard, this);
        this.currentBehaviour = task;
        this.currentBehaviour.getControl().reset();
        if(!this.currentBehaviour.check()) this.currentBehaviour.getControl().finishWithFailure();
        else this.currentBehaviour.getControl().safeStart();

        //Set the next behaviour.
        //this.nextBehaviour = task;

        EventSystem.notifyEntityEvent(this.getEntityOwner(), "task_started", task);
    }

    public void changeTaskQueued(String taskName){
        this.nextBehaviour = taskName;
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
                    //Get the default state as the next state for now, and get the current state.
                    StateSystem.State<StateSystem.DefineTask> nextState;
                    StateSystem.State<StateSystem.DefineTask> currState = behaviourStates.getCurrState();

                    //Repeat the last state if needed.
                    if(currState.getRepeatLastState()) nextState = behaviourStates.getState(currState.getUserData().lastState);
                    //Otherwise, if it didn't fail and needs to repeat, repeat it!.
                    else if(currState.getRepeat()) nextState = currState;
                    //If the job failed, try to set the current state to what is defined for "taskOnFailure".
                    else if(this.currentBehaviour.getControl().hasFailed()) nextState = behaviourStates.getState(currState.getUserData().taskOnFailure);
                    //Otherwise, if the task didn't fail and doesn't repeat, get what is defined for "taskOnSuccess".
                    else nextState = behaviourStates.getState(currState.getUserData().taskOnSuccess);
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
    public Tree getTaskTree(){
        return this.taskTree;
    }

    @JsonIgnore
    public StateSystem<StateSystem.DefineTask> getBehaviourStates(){
        return behaviourStates;
    }

    public static void addTaskToMap(String name, BiFunction<BlackBoard, BehaviourManagerComp, Task> taskFunction){
        taskMap.put(name, taskFunction);
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

    public class Line{
        public float startX, startY, width, height, rotation;
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
}
