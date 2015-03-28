package com.mygdx.game.behaviourtree;

import com.mygdx.game.behaviourtree.control.TaskController;
import com.mygdx.game.component.BlackBoard;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class LeafTask extends Task{
    protected TaskController control;

    public LeafTask(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

        this.control = new TaskController(this);
    }

    @Override
    public boolean check() {
        return this.control.callbacks.checkCriteria == null || this.control.callbacks.checkCriteria.criteria(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void end() {

    }

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
