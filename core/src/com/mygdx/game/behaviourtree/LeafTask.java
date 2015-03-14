package com.mygdx.game.behaviourtree;

import com.mygdx.game.behaviourtree.control.TaskController;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.helpers.Callbacks;
import com.mygdx.game.interfaces.Functional;

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
        return false;
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
    public String getName() {
        return this.name;
    }
}
