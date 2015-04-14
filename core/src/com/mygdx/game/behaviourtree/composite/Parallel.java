package com.mygdx.game.behaviourtree.composite;

import com.mygdx.game.behaviourtree.ParentTask;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.component.BlackBoard;


/**
 * A ParentTask that runs its subtasks in parallel (not threaded, but calling update on each subtask every frame).
 */
public class Parallel extends ParentTask{
    public Parallel(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return false;
    }

    @Override
    public void start() {
        this.control.getSubTasks().forEach(Task::start);
    }

    @Override
    public void update(float delta) {
        for(Task task : this.control.getSubTasks()) {
            if(!task.getControl().hasStarted())
                task.start();

            task.update(delta);
        }

        if(control.callbacks.successCriteria != null && control.callbacks.successCriteria.criteria(this)){
            this.control.finishWithSuccess();
            return;
        }

        if(control.callbacks.failCriteria != null && control.callbacks.failCriteria.criteria(this)){
            this.control.finishWithFailure();
            return;
        }

    }

    @Override
    public void end() {

    }

    @Override
    public void childSucceeded() {

    }

    @Override
    public void childFailed() {

    }
}
