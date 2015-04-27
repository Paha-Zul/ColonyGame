package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.PrebuiltTasks;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.StateSystem;
import com.mygdx.game.helpers.Tree;
import com.mygdx.game.helpers.timer.OneShotTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;
import java.util.HashMap;

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

    private StateSystem behaviourStates = new StateSystem();
    private Tree taskTree = new Tree("taskTree", "root");

    private HashMap<String, Task> taskMap = new HashMap<>();

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
        this.stats = this.owner.getComponent(Stats.class);
        this.blackBoard = this.getComponent(BlackBoard.class);

        behaviourStates.addState("idle", true, PrebuiltTasks.idleTask(this.blackBoard, this));
        taskMap.put("idle", PrebuiltTasks.idleTask(this.blackBoard, this));
    }

    public void idle(){
        this.changeTaskImmediate(PrebuiltTasks.idleTask(this.blackBoard, this));
    }

    public void gather(){
        this.changeTaskImmediate(PrebuiltTasks.gatherResource(this.blackBoard, this));
    }

    public void explore(){
        this.changeTaskImmediate(PrebuiltTasks.exploreUnexplored(this.blackBoard, this));
    }

    public void searchAndAttack(){
        this.changeTaskImmediate(PrebuiltTasks.searchAndHunt(this.blackBoard, this));
    }

    public void attack(){
        this.changeTaskImmediate(PrebuiltTasks.attackTarget(this.blackBoard, this));
    }

    /**
     * Changes the next Task to the Task passed in. This essentially saves the current task to 'lastBehaviour' and sets the 'nextBehaviour' to the Task passed in.
     * @param task The Task to start immediately.
     */
    public void changeTaskImmediate(Task task){
        //End the current task.
        if(this.currentBehaviour != null && !this.currentBehaviour.getControl().hasFinished()) {
            this.currentBehaviour.getControl().finishWithSuccess();
            this.currentBehaviour.getControl().safeEnd();
        }

        this.currentBehaviour = task;
        this.currentBehaviour.getControl().reset();
        this.currentBehaviour.getControl().safeStart();

        //Set the next behaviour.
        //this.nextBehaviour = task;
    }

    /**
     * Queues the task to be the next task to be executed.
     * @param task The next task.
     */
    public void changeTaskQueued(Task task){
        this.nextBehaviour = task;
    }

    public void changeTaskImmediate(String taskName){
        this.behaviourStates.setCurrState(taskName);
        changeTaskImmediate(taskMap.get(taskName));
    }

    public void changeTaskQueued(String taskName){
        changeTaskQueued(taskMap.get(taskName));
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        feedTimer.update(delta);

        //If our current behaviour is not null.. execute it.
        if (this.currentBehaviour != null) {
            //If it has finished, try to start the next behaviour if there is one, repeat the last if
            if (this.currentBehaviour.getControl().hasFinished()) {

                //If our next behaviour is a task waiting to be executed, start it!
                if(this.nextBehaviour != null){
                    this.changeTaskImmediate(nextBehaviour);
                    this.nextBehaviour = null;

                //If the next behaviour is empty, do something based on the state.
                }else {
                    Object data = behaviourStates.getCurrState().userData;
                    if(data != null) this.changeTaskImmediate((Task)data);
                    else this.changeTaskImmediate((Task)behaviourStates.getDefaultState().userData);
                }

            //Update the job.
            }else
                this.currentBehaviour.update(delta); //Update it.

        //If our behaviour is null, set the next behaviour to the default behaviour.
        } else
            this.idle();
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

    /**
     * @return The current task name.
     */
    public String getCurrentTaskName(){
        if(this.currentBehaviour != null)
            return this.currentBehaviour.getName();

        return "Nothing";
    }

    /**
     * @return The blackboard of this Component.
     */
    public BlackBoard getBlackBoard(){
        return this.blackBoard;
    }

    /**
     * @return The task Tree of this Behaviour Component.
     */
    public Tree getTaskTree(){
        return this.taskTree;
    }

    public StateSystem getBehaviourStates(){
        return this.behaviourStates;
    }

    public void addTaskToMap(String name, Task task){
        this.taskMap.put(name, task);
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
