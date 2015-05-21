package com.mygdx.game.behaviourtree.composite;

import com.mygdx.game.behaviourtree.ParentTask;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.util.BlackBoard;


/**
 * A ParentTask that runs its subtasks in parallel (not threaded, but calling update on each subtask every frame).
 */
public class Parallel extends ParentTask{
    public Parallel(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void start() {
        //When this parallel job starts, reset each task and start each task.
        this.control.getSubTasks().forEach(task -> {
            if(task.check()) {
                task.getControl().reset();
                task.getControl().safeStart();
            }
        });
    }

    @Override
    public void update(float delta) {
        //If we succeed, finish with success.
        if(control.callbacks.successCriteria != null && control.callbacks.successCriteria.test(this)){
            this.control.finishWithSuccess();
            return;
        }

        //If we fail, finish with failure.
        if(control.callbacks.failCriteria != null && control.callbacks.failCriteria.test(this)){
            this.control.finishWithFailure();
            return;
        }

        //For each task, if the task hasn't been started, start it! If a job is not finished, update it!
        for(Task task : this.control.getSubTasks()) {
            if(!task.getControl().hasStarted())
                task.start();

            //If the task is not finished, update it.
            if(!task.getControl().hasFinished())
                task.update(delta);
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

    @Override
    public String getName() {
        return this.name;
    }
}
