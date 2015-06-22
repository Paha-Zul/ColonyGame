package com.mygdx.game.behaviourtree.composite;

import com.mygdx.game.behaviourtree.ParentTask;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Paha on 6/21/2015.
 * A Composite task that will attempt each task until a task finishes with success.
 */
public class Selector extends ParentTask{
    public Selector(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }

    @Override
    public void childSucceeded() {
        this.control.finishWithSuccess();
    }

    @Override
    public void childFailed() {
        this.control.currIndex++; //Increment the index.
        //If the index is still less than the size, get the next task.
        if(this.control.currIndex < this.control.getSubTasks().size())
            this.control.currTask = this.control.getSubTasks().get(this.control.currIndex);

        //If we ran out of tasks, finish with success.
        else
            this.control.finishWithFailure();
    }
}
