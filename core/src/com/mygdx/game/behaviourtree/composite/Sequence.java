package com.mygdx.game.behaviourtree.composite;

import com.mygdx.game.behaviourtree.ParentTask;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class Sequence extends ParentTask {

    public Sequence(String name, BlackBoard blackBoard){
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && control.getSubTasks().size() > 0;
    }

    @Override
    public void start() {
        this.control.currTask = this.control.getSubTasks().get(0);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void end() {

    }

    @Override
    public void childSucceeded() {
        this.control.currIndex++; //Increment the index.
        //If the index is still less than the size, get the next task.
        if(this.control.currIndex < this.control.getSubTasks().size())
            this.control.currTask = this.control.getSubTasks().get(this.control.currIndex);

        //If we ran out of tasks, finish with success.
        else
            this.control.finishWithSuccess();
    }

    @Override
    public void childFailed() {
        this.control.finishWithFailure();
        this.control.safeEnd();
    }
}
