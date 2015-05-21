package com.mygdx.game.behaviourtree;

import com.mygdx.game.behaviourtree.control.TaskController;
import com.mygdx.game.util.BlackBoard;

/**
 * A LeafTask which extends the Task class. This task is designed for tasks that do not operate on any children
 * tasks or lists of children tasks.
 */
public class LeafTask extends Task{
    protected TaskController control;

    public LeafTask(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

        this.control = new TaskController(this);
    }

    @Override
    public boolean check() {
        return this.control.callbacks.checkCriteria == null || this.control.callbacks.checkCriteria.test(this);
    }

    @Override
    public void start() {}

    @Override
    public void update(float delta) {}

    @Override
    public void end() {}

    @Override
    public TaskController getControl() {
        return this.control;
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
}
