package com.mygdx.game.behaviourtree.decorator;

import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.behaviourtree.control.TaskController;
import com.mygdx.game.util.BlackBoard;

/**
 * Base class for the specific decorators.
 * Decorates all the task methods except
 * for the DoAction, for commodity.
 *
 * (Tough any method can be decorated in
 * the base classes with no problem,
 * they are decorated by default so the
 * programmer does not forget)
 *
 * @author Ying
 *
 */
public abstract class TaskDecorator extends Task
{
    protected TaskController control;
    protected Task task; //Reference to the task to decorate

    /**
     * Creates a new instance of the
     * Decorator class
     * @param bb Reference to
     * the AI Blackboard data
     * @param task Task to decorate
     */
    public TaskDecorator(String name, BlackBoard bb, Task task)
    {
        super(name, bb);
        this.task = task;
        this.control = new TaskController(task);
    }

    /**
     * Decorate the CheckConditions
     */
    @Override
    public boolean check()
    {
        return this.task.check();
    }

    /**
     * Decorate the start
     */
    @Override
    public void start()
    {
        this.task.start();
    }

    /**
     * Decorate the end
     */
    @Override
    public void end()
    {
        this.task.end();
    }

    @Override
    public void setBlackBoard(BlackBoard blackBoard) {
        this.blackBoard = blackBoard;
    }

    @Override
    public BlackBoard getBlackboard() {
        return this.blackBoard;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TaskController getControl() {
        return this.control;
    }
}


