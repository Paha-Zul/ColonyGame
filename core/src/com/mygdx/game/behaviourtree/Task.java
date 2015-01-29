package com.mygdx.game.behaviourtree;

import com.mygdx.game.behaviourtree.control.TaskController;
import com.mygdx.game.component.BlackBoard;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public abstract class Task {
    protected BlackBoard blackBoard;

    public Task(String name, BlackBoard blackBoard){
        this.blackBoard = blackBoard;
    }

    /**
     * The prerequisite check for if the behaviour is able to run (ie: something is null that shouldn't be).
     * @return True if able to run, false otherwise.
     */
    public abstract boolean check();

    /**
     * The start of the Behaviour. This is called immediately before the first update tick.
     */
    public abstract void start();

    /**
     * The end of the Task. Override for speicifc needs.
     */
    public abstract void end();

    /**
     * Called every frame for each behaviour.
     * @param delta The time between the current and previous frame.
     */
    public abstract void update(float delta);

    /**
     * Gets the Controller of this Task.
     * @return The Controller of this Task.
     */
    public abstract TaskController getControl();

    public abstract void setBlackBoard(BlackBoard blackBoard);


}
